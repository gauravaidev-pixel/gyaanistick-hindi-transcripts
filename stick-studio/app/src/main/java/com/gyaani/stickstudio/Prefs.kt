package com.gyaani.stickstudio

import android.content.Context
import java.security.SecureRandom

/** Small settings store: port, token, whether the server listens on the LAN. */
object Prefs {
    private const val NAME = "stickstudio"
    private const val KEY_TOKEN = "token"
    private const val KEY_PORT = "port"
    private const val KEY_LAN = "lan"

    private var ctx: Context? = null

    fun init(c: Context) { ctx = c.applicationContext }

    private fun sp() = ctx!!.getSharedPreferences(NAME, Context.MODE_PRIVATE)

    var token: String
        get() {
            val cur = sp().getString(KEY_TOKEN, null)
            if (cur != null) return cur
            val fresh = randomToken()
            sp().edit().putString(KEY_TOKEN, fresh).apply()
            return fresh
        }
        set(v) = sp().edit().putString(KEY_TOKEN, v).apply()

    var port: Int
        get() = sp().getInt(KEY_PORT, 8777)
        set(v) = sp().edit().putInt(KEY_PORT, v).apply()

    var lan: Boolean
        get() = sp().getBoolean(KEY_LAN, false)
        set(v) = sp().edit().putBoolean(KEY_LAN, v).apply()

    fun newToken(): String {
        val t = randomToken()
        token = t
        return t
    }

    private fun randomToken(): String {
        val chars = "abcdefghijklmnopqrstuvwxyz0123456789"
        val r = SecureRandom()
        return (1..24).map { chars[r.nextInt(chars.length)] }.joinToString("")
    }
}
