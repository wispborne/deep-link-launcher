package com.manoj.dlt.ui.activities;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
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
import com.manoj.dlt.Constants;
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
import java.util.Collections;
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
        _adapter = new DeepLinkListAdapter(new ArrayList<DeepLinkInfo>(), this);
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
        findViewById(R.id.fab_web).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                if(Constants.isFirebaseAvailable(DeepLinkHistoryActivity.this))
                {
                    String userId = ProfileFeature.getInstance(DeepLinkHistoryActivity.this).getUserId();
                    Utilities.showAlert("Fire from your PC", "go to https://swelteringfire-2158.firebaseapp.com/" + userId, DeepLinkHistoryActivity.this);
                } else
                {
                    Utilities.raiseError(getString(R.string.play_services_error), DeepLinkHistoryActivity.this);
                }
            }
        });
        findViewById(R.id.fab_share).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.share_text));
                sendIntent.setType("text/plain");
                startActivity(Intent.createChooser(sendIntent, getString(R.string.share_chooser_title)));
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
        initListViewData();
        EventBus.getDefault().register(this);
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
            _adapter.updateResults(deepLinkString);
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

    private void initListViewData()
    {
        if(Constants.isFirebaseAvailable(this))
        {
            //Attach callback to init adapter from data in firebase
            attachFirebaseListener();
        } else
        {
            _adapter.updateBaseData(DeepLinkHistoryFeature.getInstance(this).getLinkHistoryFromFileSystem());
            findViewById(R.id.progress_wheel).setVisibility(View.GONE);
        }
        _adapter.updateResults(_deepLinkInput.getText().toString());
    }

    private void attachFirebaseListener()
    {
        if(Constants.isFirebaseAvailable(this))
        {
            DatabaseReference baseUserReference = ProfileFeature.getInstance(this).getCurrentUserFirebaseBaseRef();
            DatabaseReference linkReference = baseUserReference.child(DbConstants.USER_HISTORY);
            linkReference.addValueEventListener(_historyUpdateListener);
        }
    }

    private void removeFirebaseListener()
    {
        if(Constants.isFirebaseAvailable(this))
        {
            DatabaseReference baseUserReference = ProfileFeature.getInstance(this).getCurrentUserFirebaseBaseRef();
            DatabaseReference linkReference = baseUserReference.child(DbConstants.USER_HISTORY);
            linkReference.removeEventListener(_historyUpdateListener);
        }
    }

    private ValueEventListener getFirebaseHistoryListener()
    {
        return new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                findViewById(R.id.progress_wheel).setVisibility(View.GONE);
                List<DeepLinkInfo> deepLinkInfos = new ArrayList<DeepLinkInfo>();
                for(DataSnapshot child: dataSnapshot.getChildren())
                {
                    DeepLinkInfo info = Utilities.getLinkInfo(child);
                    deepLinkInfos.add(info);
                }
                Collections.sort(deepLinkInfos);
                _adapter.updateBaseData(deepLinkInfos);
                if(_deepLinkInput != null && _deepLinkInput.getText().length() > 0)
                {
                    _adapter.updateResults(_deepLinkInput.getText().toString());
                }
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
