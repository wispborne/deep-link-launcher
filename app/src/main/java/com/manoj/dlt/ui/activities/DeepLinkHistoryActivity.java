package com.manoj.dlt.ui.activities;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.*;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.manoj.dlt.DbConstants;
import com.manoj.dlt.R;
import com.manoj.dlt.events.DeepLinkFireEvent;
import com.manoj.dlt.features.DeepLinkHistoryFeature;
import com.manoj.dlt.features.ProfileFeature;
import com.manoj.dlt.models.DeepLinkInfo;
import com.manoj.dlt.models.ResultType;
import com.manoj.dlt.ui.adapters.DeepLinkListAdapter;
import com.manoj.dlt.utils.TextChangedListener;
import com.manoj.dlt.utils.Utilities;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import hotchemi.android.rate.AppRate;

public class DeepLinkHistoryActivity extends AppCompatActivity
{
    private ListView _listView;
    private EditText _deepLinkInput;
    private DeepLinkListAdapter _adapter;
    private String _previousClipboardText;
    private ValueEventListener _historyUpdateListener;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_deep_link_history);
        initView();
        _historyUpdateListener = getFirebaseHistoryListener();
    }

    private void initView()
    {
        _deepLinkInput = (EditText) findViewById(R.id.deep_link_input);
        _listView = (ListView) findViewById(R.id.deep_link_list_view);
        _adapter = new DeepLinkListAdapter(DeepLinkHistoryFeature.getInstance(this).getAllLinksSearchedInfo(), this);
        configureListView();
        configureDeepLinkInput();
        findViewById(R.id.deep_link_fire).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                extractAndFireLink();
            }
        });
        setAppropriateLayout();
    }

    private void configureListView()
    {
        _listView.setAdapter(_adapter);
        _listView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l)
            {
                DeepLinkInfo info = (DeepLinkInfo) _adapter.getItem(position);
                setDeepLinkInputText(info.getDeepLink());
            }
        });
    }

    private void configureDeepLinkInput()
    {
        _deepLinkInput.requestFocus();
        _deepLinkInput.setOnEditorActionListener(new TextView.OnEditorActionListener()
        {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent)
            {
                if (shouldFireDeepLink(actionId))
                {
                    extractAndFireLink();
                    return true;
                } else
                {
                    return false;
                }
            }
        });
        _deepLinkInput.addTextChangedListener(new TextChangedListener()
        {
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2)
            {
                _adapter.updateResults(charSequence);
            }
        });
    }

    private void pasteFromClipboard()
    {
        ClipboardManager clipboardManager = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        if (!Utilities.isProperUri(_deepLinkInput.getText().toString()) && clipboardManager.hasPrimaryClip())
        {
            ClipData.Item clipItem = clipboardManager.getPrimaryClip().getItemAt(0);
            if (clipItem != null)
            {
                if (clipItem.getText() != null)
                {
                    String clipBoardText = clipItem.getText().toString();
                    if (Utilities.isProperUri(clipBoardText) && !clipBoardText.equals(_previousClipboardText))
                    {
                        setDeepLinkInputText(clipBoardText);
                        _previousClipboardText = clipBoardText;
                    }
                } else if (clipItem.getUri() != null)
                {
                    String clipBoardText = clipItem.getUri().toString();
                    if (Utilities.isProperUri(clipBoardText) && !clipBoardText.equals(_previousClipboardText))
                    {
                        setDeepLinkInputText(clipBoardText);
                        _previousClipboardText = clipBoardText;
                    }
                }
            }
        }
    }

    private void setAppropriateLayout()
    {
        if (Utilities.isAppTutorialSeen(this))
        {
            AppRate.showRateDialogIfMeetsConditions(this);
            showDeepLinkRootView();
        } else
        {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            View tutorialView = findViewById(R.id.tutorial_layer);
            tutorialView.setVisibility(View.VISIBLE);
            tutorialView.setClickable(true);
            tutorialView.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    Utilities.setAppTutorialSeen(DeepLinkHistoryActivity.this);
                    showDeepLinkRootView();
                }
            });
        }
    }

    private void showDeepLinkRootView()
    {
        findViewById(R.id.tutorial_layer).setVisibility(View.GONE);
        findViewById(R.id.deep_link_history_root).setVisibility(View.VISIBLE);
        _deepLinkInput.requestFocus();
        Utilities.showKeyboard(this);
    }

    public void extractAndFireLink()
    {
        String deepLinkUri = _deepLinkInput.getText().toString();
        Utilities.checkAndFireDeepLink(deepLinkUri, this);
    }

    @Override
    protected void onStart()
    {
        super.onStart();
        EventBus.getDefault().register(this);
        attachFirebaseListener();
        //_adapter.updateBaseData(DeepLinkHistoryFeature.getInstance(this).getAllLinksSearchedInfo());
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        pasteFromClipboard();
    }

    @Override
    protected void onStop()
    {
        EventBus.getDefault().unregister(this);
        removeFirebaseListener();
        super.onStop();
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onEvent(DeepLinkFireEvent deepLinkFireEvent)
    {
        String deepLinkString = deepLinkFireEvent.getDeepLinkInfo().getDeepLink();
        setDeepLinkInputText(deepLinkString);
        if(deepLinkFireEvent.getResultType().equals(ResultType.SUCCESS))
        {
            _adapter.updateBaseData(DeepLinkHistoryFeature.getInstance(this).getAllLinksSearchedInfo());
        } else
        {
            if(DeepLinkFireEvent.FAILURE_REASON.NO_ACTIVITY_FOUND.equals(deepLinkFireEvent.getFailureReason()))
            {
                Utilities.raiseError(getString(R.string.error_no_activity_resolved).concat(": ").concat(deepLinkString), this);
            } else if(DeepLinkFireEvent.FAILURE_REASON.IMPROPER_URI.equals(deepLinkFireEvent.getFailureReason()))
            {
                Utilities.raiseError(getString(R.string.error_improper_uri).concat(": ").concat(deepLinkString), this);
            }
        }
        EventBus.getDefault().removeStickyEvent(deepLinkFireEvent);
    }

    private void attachFirebaseListener()
    {
        DatabaseReference baseUserReference = ProfileFeature.getInstance(this).getCurrentUserFirebaseBaseRef();
        DatabaseReference linkReference = baseUserReference.child(DbConstants.USER_HISTORY);
        linkReference.addValueEventListener(_historyUpdateListener);
    }

    private void removeFirebaseListener()
    {
        DatabaseReference baseUserReference = ProfileFeature.getInstance(this).getCurrentUserFirebaseBaseRef();
        DatabaseReference linkReference = baseUserReference.child(DbConstants.USER_HISTORY);
        linkReference.removeEventListener(_historyUpdateListener);
    }

    private ValueEventListener getFirebaseHistoryListener()
    {
        return new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                List<DeepLinkInfo> deepLinkInfos = new ArrayList<DeepLinkInfo>();
                for(DataSnapshot child: dataSnapshot.getChildren())
                {
                    DeepLinkInfo info = Utilities.getLinkInfo(child);
                    deepLinkInfos.add(info);
                }
                _adapter.updateBaseData(deepLinkInfos);
            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {

            }
        };
    }
    private boolean shouldFireDeepLink(int actionId)
    {
        if (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_GO || actionId == EditorInfo.IME_ACTION_NEXT)
        {
            return true;
        }
        return false;
    }

    private void setDeepLinkInputText(String text)
    {
        _deepLinkInput.setText(text);
        _deepLinkInput.setSelection(text.length());
    }

}
