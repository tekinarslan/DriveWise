package com.drivewise.core

import com.drivewise.app.db.AppDatabase

class OnboardingStore(private val db: AppDatabase) {
    private val q = db.keyValueQueries

    private val KEY_LANG = "lang"
    private val KEY_DONE = "onboarding_done"

    fun getLanguage(): Language {
        val code = q.selectByKey(KEY_LANG).executeAsOneOrNull()
            ?: Language.DE.code
        return Language.entries.firstOrNull { it.code == code } ?: Language.DE
    }

    fun setLanguage(lang: Language) {
        q.insert(KEY_LANG, lang.code)
        q.update(lang.code, KEY_LANG)
    }

    fun isDone(): Boolean =
        (q.selectByKey(KEY_DONE).executeAsOneOrNull() ?: "false").toBoolean()

    fun setDone(done: Boolean) {
        val value = done.toString()
        q.insert(KEY_DONE, value)
        q.update(value, KEY_DONE)
    }
}