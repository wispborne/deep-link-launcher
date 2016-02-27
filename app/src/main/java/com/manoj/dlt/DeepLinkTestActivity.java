package com.manoj.dlt;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;


public class DeepLinkTestActivity extends AppCompatActivity
{
    private EditText _deepLinkInput;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_deep_link_test);
        initView();
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
            if (canResolve(intent))
            {
                startActivity(intent);
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
        _deepLinkInput = (EditText) findViewById(R.id.deep_link_input);
    }

    private boolean canResolve(Intent intent)
    {
        PackageManager pm = getPackageManager();
        if (pm.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY) != null)
        {
            return true;
        } else
        {
            return false;
        }
    }

    private void raiseError(String errorText)
    {
        Toast.makeText(this,errorText,Toast.LENGTH_LONG).show();
    }
}
