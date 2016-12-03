package com.thunderclouddev.deeplink.ui

import android.support.annotation.StringRes
import android.support.v7.app.ActionBar
import android.view.View
import com.bluelinelabs.conductor.Controller


/**
 * Created by David Whitman on 02 Dec, 2016.
 */
abstract class BaseController : Controller() {
    // Note: This is just a quick demo of how an ActionBar *can* be accessed, not necessarily how it *should*
    // be accessed. In a production app, this would use Dagger instead.
    protected fun getActionBar(): ActionBar {
        val actionBarProvider = activity as ActionBarProvider
        return actionBarProvider.actionBar
    }

    override fun onAttach(view: View) {
        setTitle()
        super.onAttach(view)
    }

    protected fun setTitle() {
        var parentController = parentController
        while (parentController != null) {
            if (parentController is BaseController && parentController.getTitle() != null) {
                return
            }

            parentController = parentController.parentController
        }

        val title = getTitle()
        val actionBar = getActionBar()
        if (title != null) {
            actionBar.setTitle(title)
        }
    }

    protected fun getTitle(): String? {
        return null
    }

    protected fun getString(@StringRes stringRes: Int) = activity!!.getString(stringRes)
}