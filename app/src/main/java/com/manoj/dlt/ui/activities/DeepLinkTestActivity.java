package com.manoj.dlt.ui.activities;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;
import com.manoj.dlt.R;
import com.manoj.dlt.features.DeepLinkHistory;
import com.manoj.dlt.models.DeepLinkInfo;

import java.util.List;


public class DeepLinkTestActivity extends AppCompatActivity
{
    private AutoCompleteTextView _deepLinkInput;
    private DeepLinkHistory _deepLinkHistory;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_deep_link_test);
        initView();
        _deepLinkHistory = new DeepLinkHistory(this);
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        List<String> array = _deepLinkHistory.getAllLinksSearched();
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_dropdown_item_1line,array);
        _deepLinkInput.setAdapter(adapter);
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

    public void testDeepLink(View view)
    {
        String deepLinkUri = _deepLinkInput.getText().toString();
        if (deepLinkUri.contains(":"))
        {
            Uri uri = Uri.parse(deepLinkUri);
            Intent intent = new Intent();
            intent.setData(uri);
            intent.setAction(Intent.ACTION_VIEW);
            PackageManager pm = getPackageManager();
            ResolveInfo resolveInfo = pm.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY);
            if (resolveInfo != null)
            {
                startActivity(intent);
                addResolvedInfoToHistory(deepLinkUri,resolveInfo);
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
        _deepLinkInput = (AutoCompleteTextView) findViewById(R.id.deep_link_input);
        _deepLinkInput.setThreshold(0);
    }

    private void addResolvedInfoToHistory(String deepLink,ResolveInfo resolveInfo)
    {
        String packageName = resolveInfo.activityInfo.packageName;
        String activityName = resolveInfo.activityInfo.targetActivity;
        String activityLabel = resolveInfo.loadLabel(getPackageManager()).toString();
        int iconRes = resolveInfo.getIconResource();
        DeepLinkInfo deepLinkInfo = new DeepLinkInfo(activityName,activityLabel,packageName,iconRes,deepLink);
        _deepLinkHistory.addLinkToHistory(deepLinkInfo);
    }

    private void raiseError(String errorText)
    {
        Toast.makeText(this, errorText, Toast.LENGTH_LONG).show();
    }
}
