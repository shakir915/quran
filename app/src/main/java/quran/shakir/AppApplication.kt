package quran.shakir

import android.app.Application
import android.content.Context
import android.content.SharedPreferences

lateinit var pref: SharedPreferences


class AppApplication : Application() {


    override fun onCreate() {
        super.onCreate()
        pref = getSharedPreferences("my_prefs_v1", Context.MODE_PRIVATE)
    }


}