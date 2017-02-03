package com.thunderclouddev.deeplink.ui

/**
 * Defines a ViewModel's lifecycle methods.
 *
 * Created by David Whitman on 02 Feb, 2017.
 */
interface ViewModel {
    fun onCreate()
    fun onPause()
    fun onResume()
    fun onDestroy()
}

