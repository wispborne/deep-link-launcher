package com.manoj.dlt;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import java.sql.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class DeepLinkTestActivity extends AppCompatActivity
{
    private AutoCompleteTextView _deepLinkInput;

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
        _deepLinkInput = (AutoCompleteTextView) findViewById(R.id.deep_link_input);
        List<String> array = Arrays.asList("zophop","zopnow","asdafasfda");
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_dropdown_item_1line,array);
        _deepLinkInput.setAdapter(new AutoCompleteAdapter(this,android.R.layout.simple_dropdown_item_1line,array));
        _deepLinkInput.setThreshold(0);
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
        Toast.makeText(this, errorText, Toast.LENGTH_LONG).show();
    }

    private class AutoCompleteAdapter extends ArrayAdapter<String>
    {
        public AutoCompleteAdapter(Context context, int i, List<String> stringList)
        {
            super(context,i, stringList);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent)
        {
            TextView textView = new TextView(getContext());
            String input = _deepLinkInput.getText().toString();
            SpannableString inputHint = new SpannableString(input);
            inputHint.setSpan(new ForegroundColorSpan(R.color.Blue), 0, inputHint.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            textView.setText(inputHint);
            SpannableString remtext = new SpannableString(getItem(position).replace(input,""));
            remtext.setSpan(new ForegroundColorSpan(R.color.Black), 0, remtext.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            textView.append(remtext);
            return textView;
        }
    }
}
