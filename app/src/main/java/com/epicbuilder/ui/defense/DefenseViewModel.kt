package com.epicbuilder.ui.defense

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import com.epicbuilder.data.HeroRepository
import com.epicbuilder.data.model.Hero
import com.epicbuilder.engine.BattleMode
import com.epicbuilder.engine.DefenseAdvisor
import com.epicbuilder.engine.Suggestion

class DefenseViewModel(app: Application) : AndroidViewModel(app) {

    private val repository = HeroRepository.get(app)
    private val advisor = DefenseAdvisor(repository.db)

    var mode by mutableStateOf(BattleMode.ARENA)
        private set

    val team = mutableStateListOf<Hero>()

    var suggestions by mutableStateOf<List<Suggestion>>(emptyList())
        private set

    init {
        recompute()
    }

    fun changeMode(newMode: BattleMode) {
        if (mode == newMode) return
        mode = newMode
        while (team.size > newMode.teamSize) {
            team.removeAt(team.size - 1)
        }
        recompute()
    }

    fun addHero(hero: Hero) {
        if (team.size >= mode.teamSize) return
        if (team.any { it.id == hero.id }) return
        team.add(hero)
        recompute()
    }

    fun removeHero(hero: Hero) {
        team.removeAll { it.id == hero.id }
        recompute()
    }

    fun reset() {
        team.clear()
        recompute()
    }

    private fun recompute() {
        suggestions = advisor.suggest(team.toList(), mode)
    }
}
