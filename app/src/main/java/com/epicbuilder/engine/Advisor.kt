package com.epicbuilder.engine

import com.epicbuilder.data.model.Hero
import com.epicbuilder.data.model.HeroDatabase
import com.epicbuilder.data.model.Role
import kotlin.math.min

enum class BattleMode(val teamSize: Int, val labelVi: String) {
    ARENA(4, "Đấu Trường"),
    GUILD_WAR(3, "Guild War")
}

data class Reason(val textVi: String, val delta: Double)

data class Suggestion(val hero: Hero, val score: Double, val reasons: List<Reason>)

/** Bốn "trụ cột" một team defense tốt cần phủ. */
private val DEFENSE_PILLARS: List<Pair<String, Set<Role>>> = listOf(
    "sát thương uy hiếp" to setOf(Role.AOE_DPS, Role.SINGLE_DPS, Role.INJURY),
    "trụ / hồi phục" to setOf(Role.HEALER, Role.TANK, Role.BARRIER, Role.REVIVER, Role.AGGRO),
    "khống chế / phá lối chơi" to setOf(
        Role.CC, Role.STRIPPER, Role.CR_CONTROL, Role.HEAL_BLOCK, Role.UNBUFFABLE, Role.SOUL_BLOCK
    ),
    "chống cleave" to setOf(
        Role.ANTI_CLEAVE, Role.SPEED_CAP, Role.COUNTER, Role.IMMUNITY, Role.AGGRO, Role.EVASION
    )
)

private val OFFENSE_DPS_ROLES = setOf(Role.AOE_DPS, Role.SINGLE_DPS, Role.PENETRATE, Role.INJURY)
private val OFFENSE_SUPPORT_ROLES = setOf(Role.HEALER, Role.TANK, Role.CLEANSER, Role.IMMUNITY, Role.BARRIER)

/**
 * Gợi ý hero cho team DEFENSE (Arena / Guild War).
 * Điểm = sức mạnh phòng thủ nền + synergy trực tiếp + lấp trụ cột còn thiếu - phạt trùng lặp.
 */
class DefenseAdvisor(private val db: HeroDatabase) {

    fun suggest(team: List<Hero>, mode: BattleMode): List<Suggestion> {
        val teamIds = team.map { it.id }.toSet()
        return db.heroes
            .asSequence()
            .filter { it.id !in teamIds }
            .map { candidate -> score(candidate, team, mode) }
            .sortedByDescending { it.score }
            .toList()
    }

    private fun score(candidate: Hero, team: List<Hero>, mode: BattleMode): Suggestion {
        val reasons = mutableListOf<Reason>()
        var score = if (mode == BattleMode.ARENA) candidate.arenaDef else candidate.gwDef
        reasons += Reason("Độ mạnh phòng thủ trong meta (${mode.labelVi})", score)

        // Synergy trực tiếp với từng thành viên đã chọn
        for (member in team) {
            for (pair in db.synergies) {
                if (pair.matches(candidate.id, member.id)) {
                    score += pair.weight
                    reasons += Reason("Synergy với ${member.name}: ${pair.reasonVi}", pair.weight)
                }
            }
        }

        // Lấp trụ cột team còn thiếu
        for ((label, pillarRoles) in DEFENSE_PILLARS) {
            val teamCovers = team.any { m -> m.roles.any { it in pillarRoles } }
            val candidateCovers = candidate.roles.any { it in pillarRoles }
            if (!teamCovers && candidateCovers && team.isNotEmpty()) {
                score += 1.2
                reasons += Reason("Bổ sung vai trò team đang thiếu: $label", 1.2)
            }
        }

        // Phạt trùng lặp
        val sameClass = team.count { it.heroClass == candidate.heroClass }
        if (sameClass >= 2) {
            score -= 1.0
            reasons += Reason("Đã có $sameClass hero cùng class ${candidate.heroClass.labelVi}", -1.0)
        }
        val sameElement = team.count { it.element == candidate.element }
        if (sameElement >= 2) {
            score -= 0.5
            reasons += Reason("Đã có $sameElement hero cùng hệ ${candidate.element.labelVi}", -0.5)
        }
        if (candidate.roles.contains(Role.HEALER) && team.count { it.roles.contains(Role.HEALER) } >= 1) {
            score -= 0.8
            reasons += Reason("Team đã có healer, dễ thiếu sát thương", -0.8)
        }

        return Suggestion(candidate, score, reasons)
    }
}

/**
 * Gợi ý hero cho team OFFENSE để counter một defense cụ thể.
 * Điểm = sức tấn công nền + counter trực tiếp + counter theo vai trò + khắc hệ
 *        + synergy với attacker đã chọn + cân bằng vai trò.
 */
class OffenseAdvisor(private val db: HeroDatabase) {

    fun suggest(enemies: List<Hero>, allies: List<Hero>, mode: BattleMode): List<Suggestion> {
        val allyIds = allies.map { it.id }.toSet()
        return db.heroes
            .asSequence()
            .filter { it.id !in allyIds }
            .map { candidate -> score(candidate, enemies, allies, mode) }
            .sortedByDescending { it.score }
            .toList()
    }

    private fun score(
        candidate: Hero,
        enemies: List<Hero>,
        allies: List<Hero>,
        mode: BattleMode
    ): Suggestion {
        val reasons = mutableListOf<Reason>()
        var score = candidate.offense
        reasons += Reason("Độ mạnh tấn công trong meta", score)

        // Counter trực tiếp từng hero địch (giới hạn tổng để tránh lạm phát điểm)
        var directTotal = 0.0
        for (enemy in enemies) {
            for (entry in db.counters) {
                if (entry.attacker == candidate.id && entry.defender == enemy.id) {
                    val add = min(entry.weight, DIRECT_COUNTER_CAP - directTotal)
                    if (add > 0) {
                        directTotal += add
                        score += add
                        reasons += Reason("Khắc chế ${enemy.name}: ${entry.reasonVi}", add)
                    }
                }
            }
        }

        // Counter theo vai trò (mỗi cặp luật/địch chỉ tính 1 lần, có giới hạn tổng)
        var tagTotal = 0.0
        outer@ for (rule in db.tagCounters) {
            if (!candidate.roles.contains(rule.counterRole)) continue
            for (enemy in enemies) {
                if (!enemy.roles.contains(rule.enemyRole)) continue
                val add = min(rule.weight, TAG_COUNTER_CAP - tagTotal)
                if (add <= 0) break@outer
                tagTotal += add
                score += add
                reasons += Reason("${rule.reasonVi} (vs ${enemy.name})", add)
            }
        }

        // Khắc hệ
        for (enemy in enemies) {
            if (candidate.element.hasAdvantageOver(enemy.element)) {
                score += 0.5
                reasons += Reason("Khắc hệ ${enemy.name} (${enemy.element.labelVi})", 0.5)
            } else if (enemy.element.hasAdvantageOver(candidate.element)) {
                score -= 0.4
                reasons += Reason("Bị ${enemy.name} khắc hệ", -0.4)
            }
        }

        // Synergy với các attacker đã chọn
        for (ally in allies) {
            for (pair in db.synergies) {
                if (pair.matches(candidate.id, ally.id)) {
                    score += pair.weight
                    reasons += Reason("Synergy với ${ally.name}: ${pair.reasonVi}", pair.weight)
                }
            }
        }

        // Cân bằng vai trò của team tấn công
        if (allies.isNotEmpty()) {
            val teamHasDps = allies.any { a -> a.roles.any { it in OFFENSE_DPS_ROLES } }
            if (!teamHasDps && candidate.roles.any { it in OFFENSE_DPS_ROLES }) {
                score += 1.0
                reasons += Reason("Team đang thiếu nguồn sát thương chính", 1.0)
            }
            val lastSlot = allies.size == mode.teamSize - 1
            val teamHasSupport = allies.any { a -> a.roles.any { it in OFFENSE_SUPPORT_ROLES } }
            if (lastSlot && !teamHasSupport && candidate.roles.any { it in OFFENSE_SUPPORT_ROLES }) {
                score += 0.8
                reasons += Reason("Slot cuối nên có hỗ trợ/sống sót", 0.8)
            }
            val sameClass = allies.count { it.heroClass == candidate.heroClass }
            if (sameClass >= 2) {
                score -= 0.8
                reasons += Reason("Đã có $sameClass hero cùng class ${candidate.heroClass.labelVi}", -0.8)
            }
        }

        return Suggestion(candidate, score, reasons)
    }

    private companion object {
        const val DIRECT_COUNTER_CAP = 5.0
        const val TAG_COUNTER_CAP = 3.5
    }
}
