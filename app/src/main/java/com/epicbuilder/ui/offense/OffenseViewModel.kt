package com.epicbuilder.ui.offense

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import com.epicbuilder.data.HeroRepository
import com.epicbuilder.data.model.Hero
import com.epicbuilder.engine.BattleMode
import com.epicbuilder.engine.OffenseAdvisor
import com.epicbuilder.engine.Suggestion

class OffenseViewModel(app: Application) : AndroidViewModel(app) {

    private val repository = HeroRepository.get(app)
    private val advisor = OffenseAdvisor(repository.db)

    val allHeroes: List<Hero> = repository.heroes

    var mode by mutableStateOf(BattleMode.ARENA)
        private set

    /** Team defense của địch cần đánh. */
    val enemies = mutableStateListOf<Hero>()

    /** Team attacker của mình, chọn lần lượt. */
    val allies = mutableStateListOf<Hero>()

    /** true khi đang chọn team địch, false khi đang chọn attacker. */
    var pickingEnemies by mutableStateOf(true)

    var suggestions by mutableStateOf<List<Suggestion>>(emptyList())
        private set

    fun changeMode(newMode: BattleMode) {
        if (mode == newMode) return
        mode = newMode
        while (enemies.size > newMode.teamSize) enemies.removeAt(enemies.size - 1)
        while (allies.size > newMode.teamSize) allies.removeAt(allies.size - 1)
        recompute()
    }

    fun addEnemy(hero: Hero) {
        if (enemies.size >= mode.teamSize) return
        if (enemies.any { it.id == hero.id }) return
        enemies.add(hero)
        recompute()
    }

    fun removeEnemy(hero: Hero) {
        enemies.removeAll { it.id == hero.id }
        if (enemies.isEmpty()) pickingEnemies = true
        recompute()
    }

    fun startPickingAllies() {
        if (enemies.isNotEmpty()) {
            pickingEnemies = false
            recompute()
        }
    }

    fun backToEnemies() {
        pickingEnemies = true
    }

    fun addAlly(hero: Hero) {
        if (allies.size >= mode.teamSize) return
        if (allies.any { it.id == hero.id }) return
        allies.add(hero)
        recompute()
    }

    fun removeAlly(hero: Hero) {
        allies.removeAll { it.id == hero.id }
        recompute()
    }

    fun reset() {
        enemies.clear()
        allies.clear()
        pickingEnemies = true
        recompute()
    }

    private fun recompute() {
        suggestions = if (enemies.isEmpty()) {
            emptyList()
        } else {
            advisor.suggest(enemies.toList(), allies.toList(), mode)
        }
    }
}
