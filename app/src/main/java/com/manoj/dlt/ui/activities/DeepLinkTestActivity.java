package com.manoj.dlt.ui.activities;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;
import com.manoj.dlt.R;
import com.manoj.dlt.features.DeepLinkHistory;
import com.manoj.dlt.models.DeepLinkInfo;
import com.manoj.dlt.ui.adapters.AutoCompleteMatchArrayAdapter;

import java.util.List;


public class DeepLinkTestActivity extends AppCompatActivity
{
    private AutoCompleteTextView _deepLinkInput;
    private DeepLinkHistory _deepLinkHistory;
    private AutoCompleteMatchArrayAdapter _adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_deep_link_test);
        _deepLinkHistory = new DeepLinkHistory(this);
        initView();
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        refreshAutoCompleteDeepLinks();
    }

    private void refreshAutoCompleteDeepLinks()
    {
        List<String> stringList = _deepLinkHistory.getAllLinksSearched();
        _adapter.updateData(stringList);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_deep_link_test, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        int id = item.getItemId();
        if (id == R.id.action_settings)
        {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void testDeepLink(View view)
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
                addResolvedInfoToHistory(deepLinkUri, resolveInfo);
            } else
            {
                raiseError("No Activity found to resolve deep link");
            }
        } else
        {
            raiseError("Uri is improperly formed");
        }
    }

    private void initView()
    {
        List<String> stringList = _deepLinkHistory.getAllLinksSearched();
        _adapter = new AutoCompleteMatchArrayAdapter(this, R.layout.autocomplete_textview, stringList);
        _deepLinkInput = (AutoCompleteTextView) findViewById(R.id.deep_link_input);
        _deepLinkInput.setAdapter(_adapter);
        _deepLinkInput.setThreshold(0);
        _deepLinkInput.addTextChangedListener(getAutoCompleteTextChangedListener());
    }

    private TextWatcher getAutoCompleteTextChangedListener()
    {
        return new TextWatcher()
        {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2)
            {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2)
            {
                _adapter.setSearchString(charSequence.toString());
            }

            @Override
            public void afterTextChanged(Editable editable)
            {

            }
        };
    }

    private void addResolvedInfoToHistory(String deepLink, ResolveInfo resolveInfo)
    {
        String packageName = resolveInfo.activityInfo.packageName;
        String activityName = resolveInfo.activityInfo.targetActivity;
        String activityLabel = resolveInfo.loadLabel(getPackageManager()).toString();
        int iconRes = resolveInfo.getIconResource();
        DeepLinkInfo deepLinkInfo = new DeepLinkInfo(deepLink, activityName, activityLabel, packageName, iconRes);
        _deepLinkHistory.addLinkToHistory(deepLinkInfo);
    }

    private void raiseError(String errorText)
    {
        Toast.makeText(this, errorText, Toast.LENGTH_LONG).show();
    }
}
