package com.thunderclouddev.deeplink.ui.home

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.databinding.DataBindingUtil
import android.net.Uri
import android.support.v4.content.res.ResourcesCompat
import android.support.v7.app.AlertDialog
import android.support.v7.widget.LinearLayoutManager
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.view.*
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.Toast
import com.bluelinelabs.conductor.RouterTransaction
import com.thunderclouddev.deeplink.BaseApp
import com.thunderclouddev.deeplink.R
import com.thunderclouddev.deeplink.data.DeepLinkDatabase
import com.thunderclouddev.deeplink.data.DeepLinkHistory
import com.thunderclouddev.deeplink.data.DeepLinkInfo
import com.thunderclouddev.deeplink.databinding.HomeViewBinding
import com.thunderclouddev.deeplink.logging.timberkt.TimberKt
import com.thunderclouddev.deeplink.ui.BaseController
import com.thunderclouddev.deeplink.ui.BaseRecyclerViewAdapter
import com.thunderclouddev.deeplink.ui.DeepLinkLauncher
import com.thunderclouddev.deeplink.ui.JsonSerializer
import com.thunderclouddev.deeplink.ui.about.AboutController
import com.thunderclouddev.deeplink.ui.edit.EditLinkDialog
import com.thunderclouddev.deeplink.ui.qrcode.ViewQrCodeController
import com.thunderclouddev.deeplink.ui.scanner.QrScannerController
import com.thunderclouddev.deeplink.utils.*
import javax.inject.Inject


class HomeController : BaseController() {
    @Inject lateinit var deepLinkHistory: DeepLinkHistory
    @Inject lateinit var deepLinkLauncher: DeepLinkLauncher
    @Inject lateinit var jsonSerializer: JsonSerializer

    private var adapter: DeepLinkListAdapter? = null
    // TODO Don't store this in the activity, rotation will killlll it
    private var deepLinkViewModels: List<DeepLinkViewModel> = emptyList()

    private val listComparator = DeepLinkViewModel.DefaultComparator()

    private lateinit var binding: HomeViewBinding

    private var databaseListenerId: Int = 0

    init {
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup): View {
        super.onCreateView(inflater, container)
        BaseApp.component.inject(this)

        binding = DataBindingUtil.inflate(inflater, R.layout.home_view, container, false)
        getActionBar().setTitle(R.string.title_activity_deep_link_history)

        // Alphabetical sorting for now
        adapter = DeepLinkListAdapter(activity!!, listComparator, createMenuItemListener(), createListItemListener())
        configureListView()
        configureInputs()
        binding.deepLinkBtnGo.setOnClickListener { launchDeepLink() }
        binding.deepLinkPaste.setOnClickListener { pasteFromClipboard() }
        binding.homeFab.setOnClickListener {
            EditLinkDialog.Creator().newInstance().show(activity!!.fragmentManager, "EditDialogTag")
        }

        return binding.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.main, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_scan -> {
                router.pushController(RouterTransaction.with(QrScannerController()))
                true
            }
            R.id.menu_about -> {
                router.pushController(RouterTransaction.with(AboutController()))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onActivityStarted(activity: Activity) {
        super.onActivityStarted(activity)
        attachDatabaseListener()
    }

    override fun onActivityStopped(activity: Activity) {
        super.onActivityStopped(activity)
        removeDatabaseListener()
    }

    override fun handleBack() =
            if (binding.deepLinkEditTextInput.text.isNullOrBlank()) {
                super.handleBack()
            } else {
                binding.deepLinkEditTextInput.setText(String.empty)
                true
            }

    private fun launchDeepLink() {
        val deepLinkUri = binding.deepLinkEditTextInput.text.toString().trim().getOrNullIfBlank()

        if (deepLinkUri.isUri()) {
            val uri = Uri.parse(deepLinkUri)

            if (!deepLinkLauncher.resolveAndFire(uri.toString(), activity!!)) {
                Utilities.raiseError("${activity!!.getString(R.string.error_no_activity_resolved)}: $uri", activity!!)
            }
        } else {
            Utilities.raiseError("${activity!!.getString(R.string.error_improper_uri)}: $deepLinkUri", activity!!)
        }
    }

    private fun configureListView() {
        binding.deepLinkList.layoutManager = LinearLayoutManager(activity!!)
        binding.deepLinkList.adapter = adapter
        binding.deepLinkList.itemAnimator
    }

    private fun showConfirmShortcutDialog(info: DeepLinkInfo) {
        val builder = AlertDialog.Builder(activity!!)
        val input = EditText(activity!!)
        input.inputType = InputType.TYPE_CLASS_TEXT

        if (info.label.isNotNullOrBlank()) {
            input.setText(info.label)
            input.setSelection(info.label?.length ?: 0)
        }

        builder.setView(input)
        builder.setMessage(activity!!.getString(R.string.placeShortcut_title))
        builder.setNegativeButton(R.string.placeShortcut_cancel, null)
        builder.setPositiveButton(R.string.placeShortcut_ok) { dialog, buttonId ->
            val shortcutAdded = Utilities.addShortcut(info, activity!!, input.text.toString())

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
                launchDeepLink()
                true
            } else false
        }

        binding.deepLinkBtnClearInput.setOnClickListener { binding.deepLinkEditTextInput.text.clear() }

        binding.deepLinkEditTextInput.addTextChangedListener(object : TextWatcher {
            var oldText: String = String.empty

            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                oldText = charSequence.toString().trim()
            }

            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                val deepLinkString = charSequence.toString()
                updateListFilter(deepLinkString)

                val activityVal = activity ?: return
                val isOldStringValidUriWithHandlingActivity = isValidUriWithHandlingActivity(oldText, activityVal.packageManager)
                val isNewStringValidUriWithHandlingActivity = isValidUriWithHandlingActivity(deepLinkString.trim(), activityVal.packageManager)
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

            override fun afterTextChanged(s: Editable?) {}
        })

        // Set disabled by default
        binding.deepLinkBtnGo.drawable.tint(disabledColor)
    }

    private fun updateListFilter(newDeepLinkString: String) {
        adapter!!.stringToHighlight = newDeepLinkString
        adapter!!.edit()
                .replaceAll(deepLinkViewModels
                        .filter { it.deepLinkInfo.deepLink.contains(newDeepLinkString, ignoreCase = true) })
                .commit()
        binding.deepLinkList.scrollToPosition(0)
    }

    private fun isValidUriWithHandlingActivity(deepLinkText: String, packageManager: PackageManager) = deepLinkText.isUri()
            && Utilities.createDeepLinkIntent(Uri.parse(deepLinkText)).hasAnyHandlingActivity(packageManager)

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

    private fun attachDatabaseListener() {
        binding.progressWheel.visibility = View.VISIBLE
        databaseListenerId = deepLinkHistory.addListener(databaseListener)
    }

    private fun removeDatabaseListener() {
        deepLinkHistory.removeListener(databaseListenerId)
    }

    private val databaseListener: DeepLinkDatabase.Listener
        get() = object : DeepLinkDatabase.Listener {
            override fun onDataChanged(dataSnapshot: List<DeepLinkInfo>) {
                binding.progressWheel.visibility = View.GONE
                deepLinkViewModels = dataSnapshot.map(::DeepLinkViewModel)
                updateListFilter(binding.deepLinkEditTextInput.text.toString())
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

    private fun createMenuItemListener(): DeepLinkListAdapter.MenuItemListener {
        return object : DeepLinkListAdapter.MenuItemListener {
            override fun onMenuItemClick(menuItem: MenuItem, deepLinkViewModel: DeepLinkViewModel): Boolean {
                val deepLinkInfo = deepLinkViewModel.deepLinkInfo

                when (menuItem.itemId) {
                    R.id.menu_list_item_edit -> EditLinkDialog.Creator().newInstance(deepLinkInfo)
                            .show(activity!!.fragmentManager, "EditDialogTag")

                    R.id.menu_list_item_qr -> router.pushController(
                            RouterTransaction.with(ViewQrCodeController(deepLinkInfo)))

                    R.id.menu_list_item_delete -> {
                        deepLinkHistory.removeLink(deepLinkInfo.id)
                        adapter!!.edit().remove(deepLinkViewModel).commit()
                    }

                    R.id.menu_list_item_createShortcut -> showConfirmShortcutDialog(deepLinkInfo)

                    R.id.menulist_item_copyToClipboard -> {
                        try {
                            val clipboard = activity!!.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.primaryClip = ClipData.newPlainText(deepLinkInfo.deepLink.toString(), deepLinkInfo.deepLink.toString())
                            Toast.makeText(activity!!, activity!!.getString(R.string.copiedToClipboard), Toast.LENGTH_SHORT).show()
                        } catch (ignored: Exception) {
                            TimberKt.e(ignored, { "Failed to copy text ${deepLinkInfo.deepLink} to clipboard." })
                        }
                    }

                    R.id.menu_list_item_share -> {
                        startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).apply {
                            this.putExtra(Intent.EXTRA_TEXT, deepLinkInfo.deepLink)
                            this.type = "text/plain"
                        }, activity!!.getString(R.string.list_item_share_chooserTitle)))
                    }
                }

                return true
            }
        }
    }

    private fun createListItemListener(): BaseRecyclerViewAdapter.OnClickListener<DeepLinkViewModel> {
        return object : BaseRecyclerViewAdapter.OnClickListener<DeepLinkViewModel> {
            override fun onItemClick(item: DeepLinkViewModel) {
                deepLinkLauncher.resolveAndFire(item.deepLinkInfo.deepLink, activity!!)
            }
        }
    }
}