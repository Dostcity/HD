package com.koyuncu.takip

import android.content.Context

/** Son acilis cokmesinin yigin izini (stack trace) saklar. */
object CrashStore {
    private const val PREF = "crash_store"
    private const val KEY = "last_trace"

    fun save(ctx: Context, text: String) {
        ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE).edit().putString(KEY, text).apply()
    }

    fun get(ctx: Context): String? =
        ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE).getString(KEY, null)

    fun hasTrace(ctx: Context): Boolean = !get(ctx).isNullOrBlank()

    fun clear(ctx: Context) {
        ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE).edit().remove(KEY).apply()
    }
}
