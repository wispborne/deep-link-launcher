package com.thunderclouddev.deeplink.ui

import android.os.Bundle
import android.support.v7.app.ActionBar
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.ViewGroup
import com.bluelinelabs.conductor.Conductor
import com.bluelinelabs.conductor.Router
import com.bluelinelabs.conductor.RouterTransaction
import com.thunderclouddev.deeplink.R
import com.thunderclouddev.deeplink.ui.home.HomeController


/**
 * Created by David Whitman on 02 Dec, 2016.
 */
class MainActivity : AppCompatActivity(), ActionBarProvider {
    override val actionBar: ActionBar
        get() = supportActionBar!!

    private var router: Router? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)
        setSupportActionBar(findViewById(R.id.toolbar) as Toolbar)

        router = Conductor.attachRouter(this, findViewById(R.id.controller_container) as ViewGroup, savedInstanceState)
        if (!router!!.hasRootController()) {
            router!!.setRoot(RouterTransaction.with(HomeController()))
        }
    }

    override fun onBackPressed() {
        if (!router!!.handleBack()) {
            super.onBackPressed()
        }
    }
}