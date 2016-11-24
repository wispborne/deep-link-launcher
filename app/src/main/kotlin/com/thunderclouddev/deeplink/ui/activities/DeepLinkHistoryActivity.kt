package com.thunderclouddev.deeplink.ui.activities

import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v4.content.res.ResourcesCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.EditorInfo
import com.thunderclouddev.deeplink.*
import com.thunderclouddev.deeplink.database.DeepLinkDatabase
import com.thunderclouddev.deeplink.deepLinkListing.DeepLinkViewModel
import com.thunderclouddev.deeplink.events.DeepLinkFireEvent
import com.thunderclouddev.deeplink.models.DeepLinkInfo
import com.thunderclouddev.deeplink.models.ResultType
import com.thunderclouddev.deeplink.ui.ConfirmShortcutDialog
import com.thunderclouddev.deeplink.ui.adapters.DeepLinkListAdapter
import com.thunderclouddev.deeplink.ui.utils.ItemClickSupport
import com.thunderclouddev.deeplink.ui.utils.tint
import com.thunderclouddev.deeplink.utils.TextChangedListener
import com.thunderclouddev.deeplink.utils.Utilities
import hotchemi.android.rate.AppRate
import kotlinx.android.synthetic.main.activity_deep_link_history.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.util.*

class DeepLinkHistoryActivity : AppCompatActivity() {
    private var adapter: DeepLinkListAdapter? = null
    // TODO Don't store this in the activity, rotation will killlll it
    private var deepLinkViewModels: List<DeepLinkViewModel> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_deep_link_history)

        supportActionBar!!.setTitle(R.string.title_activity_deep_link_history)
        // Alphabetical sorting for now
        adapter = DeepLinkListAdapter(this, Comparator { t1, t2 ->
            val packageComparison = t1.deepLinkInfo.packageName.compareTo(t2.deepLinkInfo.packageName, true)
            if (packageComparison == 0)
                packageComparison
            else
                t1.deepLinkInfo.deepLink.compareTo(t2.deepLinkInfo.deepLink, true)
        })
        configureListView()
        configureInputs()
        deepLink_btn_go.setOnClickListener { extractAndFireLink() }
        deepLink_paste.setOnClickListener { pasteFromClipboard() }

        if (Utilities.isAppTutorialSeen(this)) {
            AppRate.showRateDialogIfMeetsConditions(this)
        } else {
            launchTutorial()
            Utilities.setAppTutorialSeen(true, this@DeepLinkHistoryActivity)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val itemId = item.itemId

        when (itemId) {
            R.id.menu_share -> Utilities.shareApp(this@DeepLinkHistoryActivity)
            R.id.menu_rate -> {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(Constants.GOOGLE_PLAY_URI)))
                // Do not show app rate dialog anymore
                AppRate.with(this@DeepLinkHistoryActivity).setAgreeShowDialog(false)
            }
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onStart() {
        super.onStart()
        initListViewData()
        EventBus.getDefault().register(this)
    }

    override fun onStop() {
        EventBus.getDefault().unregister(this)
        removeFirebaseListener()
        super.onStop()
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    fun onEvent(deepLinkFireEvent: DeepLinkFireEvent) {
        val deepLinkString = deepLinkFireEvent.info.deepLink
        setAndSelectInput(deepLinkString)

        if (deepLinkFireEvent.resultType == ResultType.SUCCESS) {
            adapter!!.stringToHighlight = deepLinkString
            adapter!!.edit()
                    .replaceAll(deepLinkViewModels.filter { it.deepLinkInfo.deepLink.contains(deepLinkString) })
                    .commit()
        } else {
            if (DeepLinkFireEvent.FAILURE_REASON.NO_ACTIVITY_FOUND == deepLinkFireEvent.failureReason) {
                Utilities.raiseError(
                        getString(R.string.error_no_activity_resolved) + ": " + deepLinkString,
                        this)
            } else if (DeepLinkFireEvent.FAILURE_REASON.IMPROPER_URI == deepLinkFireEvent.failureReason) {
                Utilities.raiseError(
                        getString(R.string.error_improper_uri) + ": " + deepLinkString, this)
            }
        }
        EventBus.getDefault().removeStickyEvent(deepLinkFireEvent)
    }

    private fun extractAndFireLink() {
        val deepLinkUri = deepLink_editText_input.text.toString()
        Utilities.checkAndFireDeepLink(deepLinkUri, this)
    }

    private fun initListViewData() {
        //Attach callback to init adapter from data
        attachDatabaseListener()
        val deepLinkString = deepLink_editText_input.text.toString()
        adapter!!.stringToHighlight = deepLinkString
        adapter!!.edit()
                .replaceAll(deepLinkViewModels.filter { it.deepLinkInfo.deepLink.contains(deepLinkString) })
                .commit()
    }

    private fun configureListView() {
        deepLink_list.layoutManager = LinearLayoutManager(this)
        deepLink_list.adapter = adapter
        val clickSupport = ItemClickSupport.addTo(deepLink_list)
        clickSupport.setOnItemClickListener(object : ItemClickSupport.OnItemClickListener {
            override fun onItemClicked(recyclerView: RecyclerView, position: Int, v: View) {
                val info = adapter!!.getItem(position)
                setAndSelectInput(info.deepLinkInfo.deepLink)
            }
        })
        clickSupport.setOnItemLongClickListener(object : ItemClickSupport.OnItemLongClickListener {
            override fun onItemLongClicked(recyclerView: RecyclerView, position: Int, v: View): Boolean {
                showConfirmShortcutDialog(adapter!!.getItem(position).deepLinkInfo)
                return true
            }
        })
    }

    private fun showConfirmShortcutDialog(info: DeepLinkInfo) {
        val ft = supportFragmentManager.beginTransaction()
        val prev = supportFragmentManager.findFragmentByTag("dialog")
        if (prev != null) {
            ft.remove(prev)
        }
        ft.addToBackStack(null)

        // Create and show the dialog.
        ConfirmShortcutDialog.newInstance(info.deepLink, info.activityLabel)
                .show(ft, TAG_DIALOG)
    }

    private fun configureInputs() {
        val accentColor = ResourcesCompat.getColor(resources, R.color.accent, theme)
        val disabledColor = ResourcesCompat.getColor(resources, R.color.grayDark, theme)

        deepLink_editText_input.requestFocus()
        deepLink_editText_input.setOnEditorActionListener { textView, actionId, keyEvent ->
            if (isDoneKey(actionId)) {
                extractAndFireLink()
                true
            } else {
                false
            }
        }

        deepLink_btn_clearInput.setOnClickListener { deepLink_editText_input.text.clear() }

        deepLink_editText_input.addTextChangedListener(object : TextChangedListener() {
            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                val deepLinkString = charSequence.toString()
                adapter!!.stringToHighlight = deepLinkString
                adapter!!.edit()
                        .replaceAll(deepLinkViewModels.filter { it.deepLinkInfo.deepLink.contains(deepLinkString) })
                        .commit()

                deepLink_btn_go.drawable.tint(if (isValidUriWithHandlingActivity(deepLink_editText_input.text.toString()))
                    accentColor
                else
                    disabledColor)

                deepLink_btn_clearInput.visibility = if (charSequence.isEmpty()) View.INVISIBLE else View.VISIBLE
            }
        })

        // Trigger text changed listener
        deepLink_editText_input.setText("")
    }

    private fun isValidUriWithHandlingActivity(deepLinkText: String) = deepLinkText.isUri()
            && Utilities.createDeepLinkIntent(deepLinkText).hasHandlingActivity(packageManager)

    private fun pasteFromClipboard() {
        val clipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

        if (!deepLink_editText_input.text.toString().isUri() && clipboardManager.hasPrimaryClip()) {
            val clipItem = clipboardManager.primaryClip.getItemAt(0)

            if (clipItem != null) {
                if (clipItem.text != null) {
                    val clipBoardText = clipItem.text.toString()
                    setAndSelectInput(clipBoardText)
                } else if (clipItem.uri != null) {
                    val clipBoardText = clipItem.uri.toString()
                    setAndSelectInput(clipBoardText)
                }
            }
        }
    }

    private fun launchTutorial() {
        val deepLinkInfo = DeepLinkInfo("deeplinktester://example", "Deep Link Tester", packageName,
                Date().time)

//        val demoHeaderView = adapter!!.(0,
//                layoutInflater.inflate(R.layout.deep_link_info_layout, null, false), deepLinkInfo)
//        demoHeaderView.setBackgroundColor(
//                ResourcesCompat.getColor(resources, R.color.white, theme))
//        deepLink_list.addHeaderView(demoHeaderView)
//
//        TapTargetSequence(this).targets(
//                TapTarget.forView(deepLink_card_view,
//                        getString(R.string.onboarding_input_title))
//                        .dimColor(android.R.color.black)
//                        .outerCircleColor(R.color.primary)
//                        .targetCircleColor(R.color.accent)
//                        .tintTarget(false),
//
//                TapTarget.forView(deepLink_btn_go,
//                        getString(R.string.onboarding_launch_title))
//                        .dimColor(android.R.color.black)
//                        .outerCircleColor(R.color.primary)
//                        .targetCircleColor(R.color.accent)
//                        .tintTarget(true),
//
//                TapTarget.forView(demoHeaderView, getString(R.string.onboarding_history_title))
//                        .dimColor(android.R.color.black)
//                        .outerCircleColor(R.color.primary)
//                        .targetCircleColor(R.color.accent)
//                        .tintTarget(false)).listener(object : TapTargetSequence.Listener {
//            override fun onSequenceFinish() {
//                deepLink_list.removeHeaderView(demoHeaderView)
//            }
//
//            override fun onSequenceCanceled(lastTarget: TapTarget) {
//                deepLink_list.removeHeaderView(demoHeaderView)
//            }
//        }).start()
    }

    private var databaseListenerId: Int = 0

    private fun attachDatabaseListener() {
        databaseListenerId = BaseApplication.database.addListener(firebaseHistoryListener)
    }

    private fun removeFirebaseListener() {
        BaseApplication.database.removeListener(databaseListenerId)
    }

    private val firebaseHistoryListener: DeepLinkDatabase.Listener
        get() = object : DeepLinkDatabase.Listener {
            override fun onDataChanged(dataSnapshot: List<DeepLinkInfo>) {
                progress_wheel.visibility = View.GONE
                deepLinkViewModels = dataSnapshot.map(::DeepLinkViewModel)
                adapter!!.edit().replaceAll(deepLinkViewModels).commit()

//                if (deepLink_editText_input != null && deepLink_editText_input.text.isNotEmpty()) {
//                    adapter!!.updateResults(deepLink_editText_input.text.toString())
//                }

                if (dataSnapshot.isNotEmpty()) {
                    showShortcutBannerIfNeeded()
                }
            }
        }

    private fun showShortcutBannerIfNeeded() {
        if (!Utilities.isShortcutHintSeen(this)) {
            findViewById(R.id.shortcut_hint_banner).visibility = View.VISIBLE
            findViewById(R.id.shortcut_hint_banner_cancel).setOnClickListener {
                Utilities.setShortcutBannerSeen(this@DeepLinkHistoryActivity)
                shortcut_hint_banner.visibility = View.GONE
            }
        }
    }

    private fun isDoneKey(actionId: Int): Boolean {
        return actionId == EditorInfo.IME_ACTION_DONE
                || actionId == EditorInfo.IME_ACTION_GO
                || actionId == EditorInfo.IME_ACTION_NEXT
    }

    private fun setAndSelectInput(text: String) {
        deepLink_editText_input.setText(text)
        deepLink_editText_input.setSelection(text.length)
    }

    companion object {
        val TAG_DIALOG = "dialog"
    }
}