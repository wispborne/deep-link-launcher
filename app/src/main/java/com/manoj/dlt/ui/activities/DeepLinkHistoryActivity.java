package com.manoj.dlt.ui.activities;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import com.manoj.dlt.R;
import com.manoj.dlt.features.DeepLinkHistory;
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

    public void initView()
    {
        _deepLinkInput = (EditText) findViewById(R.id.deep_link_input);
        _listView = (ListView) findViewById(R.id.deep_link_list_view);
        _history = new DeepLinkHistory(this);
        _adapter = new DeepLinkListAdapter(_history.getAllLinksSearchedInfo(),this);
        _listView.setAdapter(_adapter);
        _deepLinkInput.addTextChangedListener(new TextChangedListener()
        {
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2)
            {
                _adapter.updateResults(charSequence);
            }
        });
        findViewById(R.id.deep_link_fire).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                testDeepLink();
            }
        });
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
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_deep_link_history, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings)
        {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
