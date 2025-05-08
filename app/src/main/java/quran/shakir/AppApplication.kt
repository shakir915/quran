package quran.shakir

import android.app.Application
import android.content.Context
import android.content.SharedPreferences

lateinit var pref: SharedPreferences
lateinit var appInstance: Application


class AppApplication : Application() {


    override fun onCreate() {
        appInstance=this
        super.onCreate()
        pref = getSharedPreferences("my_prefs_v1", Context.MODE_PRIVATE)
    }


}