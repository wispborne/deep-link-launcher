package com.manoj.dlt.ui.activities

import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v4.content.res.ResourcesCompat
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.AdapterView
import com.getkeepsafe.taptargetview.TapTarget
import com.getkeepsafe.taptargetview.TapTargetSequence
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.manoj.dlt.Constants
import com.manoj.dlt.DbConstants
import com.manoj.dlt.R
import com.manoj.dlt.events.DeepLinkFireEvent
import com.manoj.dlt.features.DeepLinkHistoryFeature
import com.manoj.dlt.features.ProfileFeature
import com.manoj.dlt.models.DeepLinkInfo
import com.manoj.dlt.models.ResultType
import com.manoj.dlt.ui.ConfirmShortcutDialog
import com.manoj.dlt.ui.adapters.DeepLinkListAdapter
import com.manoj.dlt.utils.TextChangedListener
import com.manoj.dlt.utils.Utilities
import hotchemi.android.rate.AppRate
import kotlinx.android.synthetic.main.activity_deep_link_history.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.util.*

class DeepLinkHistoryActivity : AppCompatActivity() {
    private var adapter: DeepLinkListAdapter? = null
    private var historyUpdateListener: ValueEventListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_deep_link_history)
        initView()
        historyUpdateListener = firebaseHistoryListener
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val itemId = item.itemId

        when (itemId) {
            R.id.menu_web -> if (Constants.isFirebaseAvailable(this@DeepLinkHistoryActivity)) {
                val userId = ProfileFeature.getInstance(this@DeepLinkHistoryActivity).userId
                Utilities.showAlert("Fire from your PC",
                        "go to https://swelteringfire-2158.firebaseapp.com/" + userId,
                        this@DeepLinkHistoryActivity)
            } else {
                Utilities.raiseError(getString(R.string.play_services_error),
                        this@DeepLinkHistoryActivity)
            }
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
        setdeep_link_inputText(deepLinkString)
        if (deepLinkFireEvent.resultType == ResultType.SUCCESS) {
            adapter!!.updateResults(deepLinkString)
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

    private fun showDeepLinkRootView() {
        findViewById(R.id.deep_link_history_root).visibility = View.VISIBLE
        deep_link_input.requestFocus()
        Utilities.showKeyboard(this)
    }

    private fun extractAndFireLink() {
        val deepLinkUri = deep_link_input.text.toString()
        Utilities.checkAndFireDeepLink(deepLinkUri, this)
    }

    private fun initListViewData() {
        if (Constants.isFirebaseAvailable(this)) {
            //Attach callback to init adapter from data in firebase
            attachFirebaseListener()
        } else {
            val deepLinkInfoList = DeepLinkHistoryFeature.getInstance(this).linkHistoryFromFileSystem
            if (deepLinkInfoList.isNotEmpty()) {
                showShortcutBannerIfNeeded()
            }
            adapter!!.updateBaseData(deepLinkInfoList)
            findViewById(R.id.progress_wheel).visibility = View.GONE
        }
        adapter!!.updateResults(deep_link_input.text.toString())
    }

    private fun initView() {
        supportActionBar!!.setTitle(R.string.title_activity_deep_link_history)
        adapter = DeepLinkListAdapter(ArrayList<DeepLinkInfo>(), this)
        configureListView()
        configureInput()
        findViewById(R.id.deep_link_fire).setOnClickListener { extractAndFireLink() }
        findViewById(R.id.deepLink_paste).setOnClickListener { pasteFromClipboard() }
        setAppropriateLayout()
    }

    private fun configureListView() {
        deep_link_list_view.adapter = adapter
        deep_link_list_view.onItemClickListener = AdapterView.OnItemClickListener { adapterView, view, position, l ->
            val info = adapter!!.getItem(position) as DeepLinkInfo
            setdeep_link_inputText(info.deepLink)
        }
        deep_link_list_view.onItemLongClickListener = AdapterView.OnItemLongClickListener { parent, view, position, id ->
            showConfirmShortcutDialog(adapter!!.getItem(position) as DeepLinkInfo)
            true
        }
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

    private fun configureInput() {
        deep_link_input.requestFocus()
        deep_link_input.setOnEditorActionListener { textView, actionId, keyEvent ->
            if (isDoneKey(actionId)) {
                extractAndFireLink()
                true
            } else {
                false
            }
        }
        deep_link_input.addTextChangedListener(object : TextChangedListener() {
            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                adapter!!.updateResults(charSequence)
            }
        })
    }

    private fun pasteFromClipboard() {
        val clipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

        if (!Utilities.isProperUri(deep_link_input.text.toString()) && clipboardManager.hasPrimaryClip()) {
            val clipItem = clipboardManager.primaryClip.getItemAt(0)

            if (clipItem != null) {
                if (clipItem.text != null) {
                    val clipBoardText = clipItem.text.toString()
                    setdeep_link_inputText(clipBoardText)
                } else if (clipItem.uri != null) {
                    val clipBoardText = clipItem.uri.toString()
                    setdeep_link_inputText(clipBoardText)
                }
            }
        }
    }

    private fun setAppropriateLayout() {
        showDeepLinkRootView()

        if (Utilities.isAppTutorialSeen(this)) {
            AppRate.showRateDialogIfMeetsConditions(this)
        } else {
            launchTutorial()
            Utilities.setAppTutorialSeen(this@DeepLinkHistoryActivity)
        }
    }

    private fun launchTutorial() {
        val deepLinkInfo = DeepLinkInfo("deeplinktester://example", "Deep Link Tester", packageName,
                Date().time)

        val demoHeaderView = adapter!!.createView(0,
                layoutInflater.inflate(R.layout.deep_link_info_layout, null, false), deepLinkInfo)
        demoHeaderView.setBackgroundColor(
                ResourcesCompat.getColor(resources, R.color.white, theme))
        deep_link_list_view.addHeaderView(demoHeaderView)

        TapTargetSequence(this).targets(TapTarget.forView(findViewById(R.id.deep_link_input),
                getString(R.string.onboarding_input_title))
                .dimColor(android.R.color.black)
                .outerCircleColor(R.color.primary)
                .targetCircleColor(R.color.accent)
                .tintTarget(false), TapTarget.forView(findViewById(R.id.deep_link_fire),
                getString(R.string.onboarding_launch_title))
                .dimColor(android.R.color.black)
                .outerCircleColor(R.color.primary)
                .targetCircleColor(R.color.accent)
                .tintTarget(false),
                TapTarget.forView(demoHeaderView, getString(R.string.onboarding_history_title))
                        .dimColor(android.R.color.black)
                        .outerCircleColor(R.color.primary)
                        .targetCircleColor(R.color.accent)
                        .tintTarget(false)).listener(object : TapTargetSequence.Listener {
            override fun onSequenceFinish() {
                deep_link_list_view.removeHeaderView(demoHeaderView)
            }

            override fun onSequenceCanceled(lastTarget: TapTarget) {
                deep_link_list_view.removeHeaderView(demoHeaderView)
            }
        }).start()
    }

    private fun attachFirebaseListener() {
        if (Constants.isFirebaseAvailable(this)) {
            val baseUserReference = ProfileFeature.getInstance(this).currentUserFirebaseBaseRef
            val linkReference = baseUserReference.child(DbConstants.USER_HISTORY)
            linkReference.addValueEventListener(historyUpdateListener)
        }
    }

    private fun removeFirebaseListener() {
        if (Constants.isFirebaseAvailable(this)) {
            val baseUserReference = ProfileFeature.getInstance(this).currentUserFirebaseBaseRef
            val linkReference = baseUserReference.child(DbConstants.USER_HISTORY)
            linkReference.removeEventListener(historyUpdateListener!!)
        }
    }

    private val firebaseHistoryListener: ValueEventListener
        get() = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                findViewById(R.id.progress_wheel).visibility = View.GONE
                val deepLinkInfos = dataSnapshot.children.map { Utilities.getLinkInfo(it) }.sorted()
                adapter!!.updateBaseData(deepLinkInfos)
                if (deep_link_input != null && deep_link_input.text.isNotEmpty()) {
                    adapter!!.updateResults(deep_link_input.text.toString())
                }
                if (deepLinkInfos.isNotEmpty()) {
                    showShortcutBannerIfNeeded()
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {

            }
        }

    private fun showShortcutBannerIfNeeded() {
        if (!Utilities.isShortcutHintSeen(this)) {
            findViewById(R.id.shortcut_hint_banner).visibility = View.VISIBLE
            findViewById(R.id.shortcut_hint_banner_cancel).setOnClickListener {
                Utilities.setShortcutBannerSeen(this@DeepLinkHistoryActivity)
                findViewById(R.id.shortcut_hint_banner).visibility = View.GONE
            }
        }
    }

    private fun isDoneKey(actionId: Int): Boolean {
        return actionId == EditorInfo.IME_ACTION_DONE
                || actionId == EditorInfo.IME_ACTION_GO
                || actionId == EditorInfo.IME_ACTION_NEXT
    }

    private fun setdeep_link_inputText(text: String) {
        deep_link_input.setText(text)
        deep_link_input.setSelection(text.length)
    }

    companion object {
        val TAG_DIALOG = "dialog"
    }
}