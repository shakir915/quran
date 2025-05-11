package quran.shakir

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.ProcessLifecycleOwner

import android.util.Log
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner



lateinit var pref: SharedPreferences
lateinit var appInstance: Application


class AppApplication : Application ()  {


    override fun onCreate() {
        appInstance=this
        super.onCreate()
        pref = getSharedPreferences("my_prefs_v1", Context.MODE_PRIVATE)
        ProcessLifecycleOwner.get().lifecycle.addObserver(AppLifecycleObserver())
    }




}




class AppLifecycleObserver : DefaultLifecycleObserver {
    override fun onResume(owner: LifecycleOwner) {
    }

    override fun onPause(owner: LifecycleOwner) {
       MyDb.closeDatabase()
    }
}



