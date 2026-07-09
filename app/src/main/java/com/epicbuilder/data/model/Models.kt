package com.epicbuilder.data.model

import kotlinx.serialization.Serializable

@Serializable
enum class Element(val labelVi: String) {
    FIRE("Hỏa"),
    ICE("Băng"),
    EARTH("Thổ"),
    LIGHT("Quang"),
    DARK("Ám");

    /** Bảng khắc hệ Epic Seven: Băng > Hỏa > Thổ > Băng, Quang <-> Ám. */
    fun hasAdvantageOver(other: Element): Boolean = when (this) {
        FIRE -> other == EARTH
        EARTH -> other == ICE
        ICE -> other == FIRE
        LIGHT -> other == DARK
        DARK -> other == LIGHT
    }
}

@Serializable
enum class HeroClass(val labelVi: String) {
    KNIGHT("Hiệp sĩ"),
    WARRIOR("Chiến binh"),
    THIEF("Đạo tặc"),
    RANGER("Xạ thủ"),
    MAGE("Pháp sư"),
    SOUL_WEAVER("Linh sư")
}

@Serializable
enum class SpeedTier(val labelVi: String) {
    OPENER("Rất nhanh"),
    FAST("Nhanh"),
    MID("Trung bình"),
    SLOW("Chậm")
}

@Serializable
enum class Role(val labelVi: String) {
    TANK("Đỡ đòn"),
    HEALER("Hồi máu"),
    AOE_DPS("Sát thương AoE"),
    SINGLE_DPS("Sát thương đơn"),
    OPENER("Mở lượt"),
    STRIPPER("Xóa buff"),
    DEF_BREAK("Phá giáp"),
    CC("Khống chế"),
    CR_CONTROL("Kéo CR địch"),
    CR_PUSH("Đẩy CR đội"),
    IMMUNITY("Miễn nhiễm/Buff"),
    REVIVER("Hồi sinh"),
    SELF_REVIVE("Tự hồi sinh"),
    EXTINCTION("Extinction"),
    REVIVE_BLOCK("Chặn hồi sinh"),
    INJURY("Injury"),
    COUNTER("Phản đòn"),
    EVASION("Né đòn"),
    PENETRATE("Xuyên giáp/ST cố định"),
    UNBUFFABLE("Cấm buff"),
    SPEED_CAP("Khóa tốc độ"),
    ANTI_CLEAVE("Chống cleave"),
    SOUL_BLOCK("Khóa soul"),
    CLEANSER("Giải debuff"),
    BARRIER("Khiên"),
    AGGRO("Kéo đòn đơn"),
    HEAL_BLOCK("Chặn hồi máu")
}

@Serializable
data class Hero(
    val id: String,
    val name: String,
    val element: Element,
    val heroClass: HeroClass,
    val speedTier: SpeedTier,
    val roles: List<Role>,
    val arenaDef: Double,
    val gwDef: Double,
    val offense: Double,
    val noteVi: String = ""
) {
    fun hasAny(vararg wanted: Role): Boolean = roles.any { it in wanted }
}

/** Cặp hero có synergy khi đứng chung team (không phân biệt thứ tự). */
@Serializable
data class SynergyPair(
    val heroes: List<String>,
    val weight: Double,
    val reasonVi: String
) {
    fun matches(a: String, b: String): Boolean =
        heroes.size == 2 && ((heroes[0] == a && heroes[1] == b) || (heroes[0] == b && heroes[1] == a))
}

/** [attacker] khắc chế trực tiếp [defender]. */
@Serializable
data class CounterEntry(
    val attacker: String,
    val defender: String,
    val weight: Double,
    val reasonVi: String
)

/** Luật khắc chế theo vai trò: hero có [counterRole] được cộng điểm khi địch có [enemyRole]. */
@Serializable
data class TagCounter(
    val enemyRole: Role,
    val counterRole: Role,
    val weight: Double,
    val reasonVi: String
)

@Serializable
data class HeroDatabase(
    val heroes: List<Hero>,
    val synergies: List<SynergyPair> = emptyList(),
    val counters: List<CounterEntry> = emptyList(),
    val tagCounters: List<TagCounter> = emptyList()
)
