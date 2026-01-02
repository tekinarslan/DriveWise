package com.drivewise.core

import com.drivewise.app.db.AppDatabase
import io.github.aakira.napier.Napier

private const val KEY_LANG = "lang"
private const val KEY_DONE = "onboarding_done"

class OnboardingStore(private val db: AppDatabase) {
    private val keyValueQueries = db.keyValueQueries

    fun getLanguage(): Language {
        Napier.i("OnboardingStore getLanguage")
        val code = keyValueQueries.selectByKey(KEY_LANG).executeAsOneOrNull()
            ?: Language.DE.code
        return Language.entries.firstOrNull { it.code == code } ?: Language.DE
    }

    fun setLanguage(lang: Language) {
        keyValueQueries.insert(KEY_LANG, lang.code)
        keyValueQueries.update(lang.code, KEY_LANG)
    }

    fun isDone(): Boolean =
        (keyValueQueries.selectByKey(KEY_DONE).executeAsOneOrNull() ?: "false").toBoolean()

    fun setDone(done: Boolean) {
        val value = done.toString()
        keyValueQueries.insert(KEY_DONE, value)
        keyValueQueries.update(value, KEY_DONE)
    }
}