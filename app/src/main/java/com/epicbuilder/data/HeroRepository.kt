package com.epicbuilder.data

import android.content.Context
import com.epicbuilder.data.model.Hero
import com.epicbuilder.data.model.HeroDatabase
import kotlinx.serialization.json.Json

class HeroRepository private constructor(val db: HeroDatabase) {

    val heroes: List<Hero> = db.heroes.sortedBy { it.name }

    private val byId: Map<String, Hero> = db.heroes.associateBy { it.id }

    fun heroById(id: String): Hero? = byId[id]

    companion object {
        private val json = Json { ignoreUnknownKeys = true }

        @Volatile
        private var instance: HeroRepository? = null

        fun get(context: Context): HeroRepository =
            instance ?: synchronized(this) {
                instance ?: run {
                    val text = context.applicationContext.assets
                        .open("heroes.json")
                        .bufferedReader(Charsets.UTF_8)
                        .use { it.readText() }
                    HeroRepository(json.decodeFromString<HeroDatabase>(text))
                        .also { instance = it }
                }
            }
    }
}
