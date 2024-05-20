package dev.mainhq.bus2go.utils

import android.content.Context
import androidx.preference.PreferenceManager
import androidx.appcompat.app.AppCompatDelegate

class SettingsData(applicationContext : Context) {
    private var language : Language
    private var darkMode : Boolean
    private var realTimeData : Boolean
    private var autoUpdates : Boolean = false
    private var updateNotification : Boolean = false

    init{
        PreferenceManager.getDefaultSharedPreferences(applicationContext).also {
            language = when (it.getString("language", "system").toString().lowercase()) {
                "english" -> Language.ENGLISH
                "français" -> Language.FRENCH
                "español" -> Language.SPANISH
                else -> Language.SYSTEM
            }
            darkMode = it.getBoolean("dark-mode", true)
            realTimeData = it.getBoolean("real-time-data", false)
            autoUpdates = it.getBoolean("auto-updates", false)
            updateNotification = it.getBoolean("update-notifications", false)
        }
    }

    fun setTheme() {
        TODO("Not working properly")
        if (darkMode) AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        else AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
    }

    fun setLang(applicationContext: Context){
        applicationContext.apply {
            when(this@SettingsData.language){
                Language.SYSTEM -> {
                    resources.configuration.locales
                }
                Language.ENGLISH -> TODO()
                Language.FRENCH -> TODO()
                Language.SPANISH -> TODO()
            }
        }
    }

    fun isRealTime() : Boolean{
        return realTimeData
    }

    enum class Language{
        SYSTEM, FRENCH, ENGLISH, SPANISH,
    }
    enum class Theme{
        SYSTEM, LIGHT, DARK
    }
}