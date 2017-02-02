package com.thunderclouddev.deeplink

import android.app.Application
import com.thunderclouddev.deeplink.dagger.AppComponent
import com.thunderclouddev.deeplink.dagger.AppModule
import com.thunderclouddev.deeplink.dagger.DaggerAppComponent


open class BaseApplication : Application() {
    companion object {
        lateinit var component: AppComponent
//        private lateinit var databaseComponent: DatabaseComponent
    }

    override fun onCreate() {
        super.onCreate()

        component = DaggerAppComponent.builder()
                .appModule(AppModule(this))
                .build()

//        databaseComponent = createDatabaseComponent()


//        Colorful.defaults()
//                .primaryColor(Colorful.ThemeColor.valueOf())
//                .accentColor(Colorful.ThemeColor.BLUE)
//                .translucent(false)
//                .dark(true);
//        Colorful.init(this);
    }

//    protected open fun createDatabaseComponent() = DaggerDatabaseComponent.builder()
//            .debugDatabaseModule(DebugDatabaseModule(this))
//            .build()
}
