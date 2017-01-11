package com.thunderclouddev.deeplink.ui

import android.os.Bundle
import android.support.v7.app.ActionBar
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import com.bluelinelabs.conductor.Controller


/**
 * Created by David Whitman on 02 Dec, 2016.
 */
abstract class BaseController(bundle: Bundle? = null) : Controller(bundle) {
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

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup): View {
        val isHomeScreen = router.backstackSize == 1
        getActionBar().setDisplayHomeAsUpEnabled(!isHomeScreen)
        setHasOptionsMenu(true)
        setOptionsMenuHidden(false)

        return View(activity)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (item.itemId == android.R.id.home) {
            router.handleBack()
            true
        } else {
            super.onOptionsItemSelected(item)
        }
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

        if (title != null) {
            getActionBar().title = title
        }
    }

    protected open fun getTitle(): String? {
        return null
    }
}