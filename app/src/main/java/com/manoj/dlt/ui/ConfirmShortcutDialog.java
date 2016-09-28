package com.manoj.dlt.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.manoj.dlt.R;
import com.manoj.dlt.utils.Utilities;

public class ConfirmShortcutDialog extends DialogFragment
{
    private static final String KEY_DEEP_LINK = "key_deep_link";
    private static final String KEY_LABEL = "key_label";

    private String _deepLink;
    private String _defaultLabel;

    public static ConfirmShortcutDialog newInstance(String deepLinkUri, String defaultLabel)
    {
        ConfirmShortcutDialog dialog = new ConfirmShortcutDialog();

        Bundle args = new Bundle();
        args.putString(KEY_DEEP_LINK, deepLinkUri);
        args.putString(KEY_LABEL, defaultLabel);
        dialog.setArguments(args);

        return dialog;
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState)
    {
        setStyle(STYLE_NO_TITLE, 0);
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        extractData();
        View view = inflater.inflate(R.layout.confirm_shortcut_dialog, container, false);
        initView(view);
        return view;
    }

    private void extractData()
    {
        _deepLink = getArguments().getString(KEY_DEEP_LINK);
        _defaultLabel = getArguments().getString(KEY_LABEL, "");
    }

    private void initView(View view)
    {
        final EditText labelEditText = (EditText) view.findViewById(R.id.shortcut_label);
        if(!TextUtils.isEmpty(_defaultLabel))
        {
            labelEditText.setText(_defaultLabel);
            labelEditText.setSelection(_defaultLabel.length());
        }
        view.findViewById(R.id.confirm_shortcut_negative).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                dismiss();
            }
        });
        view.findViewById(R.id.confirm_shortcut_positive).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                boolean shortcutAdded = Utilities.addShortcut(_deepLink,getActivity(), labelEditText.getText().toString());
                if(shortcutAdded)
                {
                    Toast.makeText(getActivity(), "shortcut added", Toast.LENGTH_LONG).show();
                } else
                {
                    Toast.makeText(getActivity(), "could not add shortcut", Toast.LENGTH_LONG).show();
                }
                dismiss();
            }
        });
    }
}
