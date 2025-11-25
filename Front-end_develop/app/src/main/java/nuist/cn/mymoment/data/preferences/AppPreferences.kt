package nuist.cn.mymoment.data.preferences

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate

class AppPreferences(context: Context) {

    private val prefs = context.getSharedPreferences(PREF_FILE, Context.MODE_PRIVATE)

    fun isLoggedIn(): Boolean = prefs.getBoolean(KEY_LOGGED_IN, false)

    fun setLoggedIn(loggedIn: Boolean) {
        prefs.edit().putBoolean(KEY_LOGGED_IN, loggedIn).apply()
    }

    fun isNightMode(): Boolean = prefs.getBoolean(KEY_NIGHT_MODE, false)

    fun setNightMode(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_NIGHT_MODE, enabled).apply()
        AppCompatDelegate.setDefaultNightMode(
            if (enabled) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
        )
    }

    fun isLargeFont(): Boolean = prefs.getBoolean(KEY_LARGE_FONT, false)

    fun setLargeFont(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_LARGE_FONT, enabled).apply()
    }

    companion object {
        private const val PREF_FILE = "mymoment_prefs"
        private const val KEY_LOGGED_IN = "key_logged_in"
        private const val KEY_NIGHT_MODE = "key_night_mode"
        private const val KEY_LARGE_FONT = "key_large_font"
    }
}

