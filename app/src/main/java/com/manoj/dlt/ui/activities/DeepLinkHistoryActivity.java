package com.manoj.dlt.ui.activities;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import com.manoj.dlt.R;
import com.manoj.dlt.features.DeepLinkHistory;
import com.manoj.dlt.models.DeepLinkInfo;
import com.manoj.dlt.ui.adapters.DeepLinkListAdapter;
import com.manoj.dlt.utils.TextChangedListener;
import com.manoj.dlt.utils.Utilities;

public class DeepLinkHistoryActivity extends AppCompatActivity
{
    private ListView _listView;
    private EditText _deepLinkInput;
    private DeepLinkHistory _history;
    private DeepLinkListAdapter _adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_deep_link_history);
        initView();
    }

    private void initView()
    {
        _deepLinkInput = (EditText) findViewById(R.id.deep_link_input);
        _listView = (ListView) findViewById(R.id.deep_link_list_view);
        _history = new DeepLinkHistory(this);
        _adapter = new DeepLinkListAdapter(_history.getAllLinksSearchedInfo(), this);
        configureListView();
        configureDeepLinkInput();
        findViewById(R.id.deep_link_fire).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                testDeepLink();
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
                _deepLinkInput.setText(info.getDeepLink());
                _deepLinkInput.setSelection(info.getDeepLink().length());
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
                if (shouldFireDeepLink(actionId, keyEvent))
                {
                    testDeepLink();
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

    private void setAppropriateLayout()
    {
        if (Utilities.isAppTutorialSeen(this))
        {
            showDeepLinkRootView();
        } else
        {
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

    public void testDeepLink()
    {
        String deepLinkUri = _deepLinkInput.getText().toString();
        if (deepLinkUri.contains(":"))
        {
            Uri uri = Uri.parse(deepLinkUri);
            Intent intent = new Intent();
            intent.setData(uri);
            intent.setAction(Intent.ACTION_VIEW);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            PackageManager pm = getPackageManager();
            ResolveInfo resolveInfo = pm.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY);
            if (resolveInfo != null)
            {
                startActivity(intent);
                Utilities.addResolvedInfoToHistory(deepLinkUri, resolveInfo, this);
            } else
            {
                Utilities.raiseError(getString(R.string.error_no_activity_resolved), this);
            }
        } else
        {
            Utilities.raiseError(getString(R.string.error_improper_uri), this);
        }
    }

    @Override
    protected void onStart()
    {
        super.onStart();
        _adapter.updateBaseData(_history.getAllLinksSearchedInfo());
    }

    private boolean shouldFireDeepLink(int actionId, KeyEvent keyEvent)
    {
        if (actionId == EditorInfo.IME_ACTION_GO)
        {
            return true;
        } else
        {
            return false;
        }
    }

}