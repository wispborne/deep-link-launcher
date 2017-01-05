package com.thunderclouddev.deeplink.ui.home

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.app.Activity
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.databinding.DataBindingUtil
import android.net.Uri
import android.support.v4.content.res.ResourcesCompat
import android.support.v7.app.AlertDialog
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.InputType
import android.view.*
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.Toast
import com.thunderclouddev.deeplink.*
import com.thunderclouddev.deeplink.database.DeepLinkDatabase
import com.thunderclouddev.deeplink.databinding.ActivityHomeBinding
import com.thunderclouddev.deeplink.events.DeepLinkFireEvent
import com.thunderclouddev.deeplink.features.DeepLinkHistoryFeature
import com.thunderclouddev.deeplink.models.DeepLinkInfo
import com.thunderclouddev.deeplink.models.ResultType
import com.thunderclouddev.deeplink.ui.BaseController
import com.thunderclouddev.deeplink.ui.edit.EditLinkDialog
import com.thunderclouddev.deeplink.ui.utils.ItemClickSupport
import com.thunderclouddev.deeplink.ui.utils.tint
import com.thunderclouddev.deeplink.utils.TextChangedListener
import com.thunderclouddev.deeplink.utils.Utilities
import com.thunderclouddev.deeplink.viewModels.DeepLinkViewModel
import hotchemi.android.rate.AppRate
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.util.*


class HomeController : BaseController() {
    private var adapter: DeepLinkListAdapter? = null
    // TODO Don't store this in the activity, rotation will killlll it
    private var deepLinkViewModels: List<DeepLinkViewModel> = emptyList()

    private val listComparator by lazy { createListComparator() }

    private val menuItemListener by lazy { createMenuItemListener() }

    private lateinit var binding: ActivityHomeBinding

    init {
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.activity_home, container, false)
//        val view = inflater.inflate(R.layout.activity_home, container, false)
        getActionBar().setTitle(R.string.title_activity_deep_link_history)

        // Alphabetical sorting for now
        adapter = DeepLinkListAdapter(activity!!, listComparator, menuItemListener)
        configureListView()
        configureInputs()
        binding.deepLinkBtnGo.setOnClickListener { extractAndFireLink() }
        binding.deepLinkPaste.setOnClickListener { pasteFromClipboard() }

        if (Utilities.isAppTutorialSeen(activity!!)) {
            AppRate.showRateDialogIfMeetsConditions(activity!!)
        } else {
            launchTutorial()
            Utilities.setAppTutorialSeen(true, activity!!)
        }

        return binding.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_main, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val itemId = item.itemId

        when (itemId) {
            R.id.menu_share -> Utilities.shareApp(activity!!)
            R.id.menu_rate -> {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(Constants.GOOGLE_PLAY_URI)))
                // Do not show app rate dialog anymore
                AppRate.with(activity!!).setAgreeShowDialog(false)
            }
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onActivityStarted(activity: Activity) {
        super.onActivityStarted(activity)
        initListViewData()
        EventBus.getDefault().register(this)
    }

    override fun onActivityStopped(activity: Activity) {
        super.onActivityStopped(activity)
        EventBus.getDefault().unregister(this)
        removeFirebaseListener()
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    fun onEvent(deepLinkFireEvent: DeepLinkFireEvent) {
        val deepLinkString = deepLinkFireEvent.info.deepLink.toString()
        setAndSelectInput(deepLinkString)

        if (deepLinkFireEvent.resultType == ResultType.SUCCESS) {
            adapter!!.stringToHighlight = deepLinkString
            adapter!!.edit()
                    .replaceAll(deepLinkViewModels.filter { it.deepLinkInfo.deepLink.toString().contains(deepLinkString) })
                    .commit()
        } else {
            if (DeepLinkFireEvent.FAILURE_REASON.NO_ACTIVITY_FOUND == deepLinkFireEvent.failureReason) {
                Utilities.raiseError(
                        activity!!.getString(R.string.error_no_activity_resolved) + ": " + deepLinkString,
                        activity!!)
            } else if (DeepLinkFireEvent.FAILURE_REASON.IMPROPER_URI == deepLinkFireEvent.failureReason) {
                Utilities.raiseError(
                        activity!!.getString(R.string.error_improper_uri) + ": " + deepLinkString, activity!!)
            }
        }
        EventBus.getDefault().removeStickyEvent(deepLinkFireEvent)
    }

    private fun extractAndFireLink() {
        val deepLinkUri = binding.deepLinkEditTextInput.text.toString()
        Utilities.checkAndFireDeepLink(deepLinkUri, activity!!)
    }

    private fun initListViewData() {
        //Attach callback to init adapter from data
        attachDatabaseListener()
        val deepLinkString = binding.deepLinkEditTextInput.text.toString()
        adapter!!.stringToHighlight = deepLinkString
        adapter!!.edit()
                .replaceAll(deepLinkViewModels.filter { it.deepLinkInfo.deepLink.toString().contains(deepLinkString) })
                .commit()
    }

    private fun configureListView() {
        binding.deepLinkList.layoutManager = LinearLayoutManager(activity!!)
        binding.deepLinkList.adapter = adapter
        binding.deepLinkList.itemAnimator
        val clickSupport = ItemClickSupport.addTo(binding.deepLinkList)
        clickSupport.setOnItemClickListener(object : ItemClickSupport.OnItemClickListener {
            override fun onItemClicked(recyclerView: RecyclerView, position: Int, v: View) {
                val info = adapter!!.getItem(position)
                Utilities.resolveAndFire(info.deepLinkInfo.deepLink, activity!!)
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
        val builder = AlertDialog.Builder(activity!!)
        val input = EditText(activity!!)
        input.inputType = InputType.TYPE_CLASS_TEXT

        if (info.activityLabel.isNotEmpty()) {
            input.setText(info.activityLabel)
            input.setSelection(info.activityLabel.length)
        }

        builder.setView(input)
        builder.setMessage(activity!!.getString(R.string.placeShortcut_title))
        builder.setNegativeButton(R.string.placeShortcut_cancel, null)
        builder.setPositiveButton(R.string.placeShortcut_ok) { dialog, buttonId ->
            val shortcutAdded = Utilities.addShortcut(info.deepLink, activity!!, input.text.toString())

            if (shortcutAdded) {
                Toast.makeText(activity, R.string.placeShortcut_success, Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(activity, R.string.placeShortcut_failure, Toast.LENGTH_LONG).show()
            }
            dialog?.dismiss()
        }

        builder.show()

        // Set editText margin
        val margin = activity!!.resources.getDimensionPixelOffset(R.dimen.spacingMedium)
        (input.layoutParams as ViewGroup.MarginLayoutParams).setMargins(margin, 0, margin, 0)
    }

    private fun configureInputs() {
        val accentColor = ResourcesCompat.getColor(activity!!.resources, R.color.accent, activity!!.theme)
        val disabledColor = ResourcesCompat.getColor(activity!!.resources, R.color.grayDark, activity!!.theme)

        binding.deepLinkEditTextInput.requestFocus()
        binding.deepLinkEditTextInput.setOnEditorActionListener { textView, actionId, keyEvent ->
            if (isDoneKey(actionId)) {
                extractAndFireLink()
                true
            } else {
                false
            }
        }

        binding.deepLinkBtnClearInput.setOnClickListener { binding.deepLinkEditTextInput.text.clear() }

        binding.deepLinkEditTextInput.addTextChangedListener(object : TextChangedListener() {
            var oldText: String = String.empty

            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                oldText = charSequence.toString()
            }

            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                val deepLinkString = charSequence.toString()
                adapter!!.stringToHighlight = deepLinkString
                adapter!!.edit()
                        .replaceAll(deepLinkViewModels
                                .filter { it.deepLinkInfo.deepLink.toString().contains(deepLinkString) })
                        .commit()

                val isOldStringValidUriWithHandlingActivity = isValidUriWithHandlingActivity(oldText)
                val isNewStringValidUriWithHandlingActivity = isValidUriWithHandlingActivity(deepLinkString)
                val didValidityChange = isOldStringValidUriWithHandlingActivity xor isNewStringValidUriWithHandlingActivity

                if (didValidityChange) {
                    // animation!
                    if (isNewStringValidUriWithHandlingActivity) {
                        binding.deepLinkBtnGoForAnims.visibility = View.VISIBLE
                        val fadeAnim = ObjectAnimator.ofFloat(binding.deepLinkBtnGoForAnims, "alpha", 1f, 0f)
                        val scaleXAnim = ObjectAnimator.ofFloat(binding.deepLinkBtnGoForAnims, "scaleX", 1f, 2.5f)
                        val scaleYAnim = ObjectAnimator.ofFloat(binding.deepLinkBtnGoForAnims, "scaleY", 1f, 2.5f)
                        val animSet = AnimatorSet()
                        animSet.playTogether(fadeAnim, scaleXAnim, scaleYAnim)
                        animSet.duration = 160
                        animSet.start()
                        animSet.addListener(object : Animator.AnimatorListener {
                            override fun onAnimationEnd(p0: Animator?) {
                                animSet.removeListener(this)
                                binding.deepLinkBtnGoForAnims.visibility = View.GONE
                                binding.deepLinkBtnGoForAnims.clearAnimation()
                            }

                            override fun onAnimationCancel(p0: Animator?) {

                            }

                            override fun onAnimationStart(p0: Animator?) {
                            }

                            override fun onAnimationRepeat(p0: Animator?) {

                            }

                        })
                    }

                    binding.deepLinkBtnGo.drawable.tint(if (isNewStringValidUriWithHandlingActivity)
                        accentColor
                    else
                        disabledColor)
                }

                binding.deepLinkBtnClearInput.visibility = if (charSequence.isEmpty()) View.INVISIBLE else View.VISIBLE
            }
        })

        // Set disabled by default
        binding.deepLinkBtnGo.drawable.tint(disabledColor)
    }

    private fun isValidUriWithHandlingActivity(deepLinkText: String) = deepLinkText.isUri()
            && Utilities.createDeepLinkIntent(Uri.parse(deepLinkText)).hasHandlingActivity(activity!!.packageManager)

    private fun pasteFromClipboard() {
        val clipboardManager = activity!!.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

        if (!binding.deepLinkEditTextInput.text.toString().isUri() && clipboardManager.hasPrimaryClip()) {
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
//        val deepLinkInfo = DeepLinkInfo("deeplinktester://example", "Deep Link Tester", packageName,
//                Date().time)

//        val demoHeaderView = adapter!!.(0,
//                layoutInflater.inflate(R.layout.deep_link_info_layout, null, false), deepLinkInfo)
//        demoHeaderView.setBackgroundColor(
//                ResourcesCompat.getColor(resources, R.color.white, theme))
//        binding.deepLinkList.addHeaderView(demoHeaderView)
//
//        TapTargetSequence(activity!!).targets(
//                TapTarget.forView(deepLink_card_view,
//                        getString(R.string.onboarding_input_title))
//                        .dimColor(android.R.color.black)
//                        .outerCircleColor(R.color.primary)
//                        .targetCircleColor(R.color.accent)
//                        .tintTarget(false),
//
//                TapTarget.forView(deepLinkBtnGo,
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
//                deepLinkList.removeHeaderView(demoHeaderView)
//            }
//
//            override fun onSequenceCanceled(lastTarget: TapTarget) {
//                deepLinkList.removeHeaderView(demoHeaderView)
//            }
//        }).start()
    }

    private var databaseListenerId: Int = 0

    private fun attachDatabaseListener() {
        binding.progressWheel.visibility = View.VISIBLE
        databaseListenerId = BaseApplication.database.addListener(firebaseHistoryListener)
    }

    private fun removeFirebaseListener() {
        BaseApplication.database.removeListener(databaseListenerId)
    }

    private val firebaseHistoryListener: DeepLinkDatabase.Listener
        get() = object : DeepLinkDatabase.Listener {
            override fun onDataChanged(dataSnapshot: List<DeepLinkInfo>) {
                binding.progressWheel.visibility = View.GONE
                deepLinkViewModels = dataSnapshot.map(::DeepLinkViewModel)
                adapter!!.edit().replaceAll(deepLinkViewModels).commit()

//                if (deepLinkEditTextInput != null && deepLinkEditTextInput.text.isNotEmpty()) {
//                    adapter!!.updateResults(deepLinkEditTextInput.text.toString())
//                }

                if (dataSnapshot.isNotEmpty()) {
                    showShortcutBannerIfNeeded()
                }
            }
        }

    private fun showShortcutBannerIfNeeded() {
        if (!Utilities.isShortcutHintSeen(activity!!)) {
            binding.shortcutHintBanner.visibility = View.VISIBLE
            binding.shortcutHintBannerCancel.setOnClickListener {
                Utilities.setShortcutBannerSeen(activity!!)
                binding.shortcutHintBanner.visibility = View.GONE
            }
        }
    }

    private fun isDoneKey(actionId: Int): Boolean {
        return actionId == EditorInfo.IME_ACTION_DONE
                || actionId == EditorInfo.IME_ACTION_GO
                || actionId == EditorInfo.IME_ACTION_NEXT
    }

    private fun setAndSelectInput(text: String) {
        binding.deepLinkEditTextInput.setText(text)
        binding.deepLinkEditTextInput.setSelection(text.length)
    }

    private fun createListComparator(): Comparator<DeepLinkViewModel> {
        return Comparator { t1, t2 ->
            val packageComparison = t1.deepLinkInfo.packageName.compareTo(t2.deepLinkInfo.packageName, true)
            if (packageComparison == 0)
                packageComparison
            else
                t1.deepLinkInfo.deepLink.compareTo(t2.deepLinkInfo.deepLink)
        }
    }

    private fun createMenuItemListener(): DeepLinkListAdapter.MenuItemListener {
        return object : DeepLinkListAdapter.MenuItemListener {
            override fun onMenuItemClick(menuItem: MenuItem, deepLinkViewModel: DeepLinkViewModel): Boolean {
                val deepLinkInfo = deepLinkViewModel.deepLinkInfo

                return when (menuItem.itemId) {
                    R.id.menu_list_item_edit -> {
                        EditLinkDialog.newInstance(deepLinkInfo)
                                .show(activity!!.fragmentManager, "EditDialogTag")
                        true
                    }
                    R.id.menu_list_item_delete -> {
                        DeepLinkHistoryFeature.getInstance(activity!!).removeLinkFromHistory(deepLinkInfo.id)
                        adapter!!.edit().remove(deepLinkViewModel).commit()
                        true
                    }
                    R.id.menu_list_item_createShortcut -> {
                        showConfirmShortcutDialog(deepLinkInfo)
                        true
                    }
                    else -> false
                }
            }
        }
    }

    companion object {
        val TAG_DIALOG = "dialog"
    }
}