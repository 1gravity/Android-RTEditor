package com.onegravity.rteditor.demo;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.onegravity.rteditor.RTEditText;
import com.onegravity.rteditor.RTManager;
import com.onegravity.rteditor.RTToolbar;
import com.onegravity.rteditor.api.RTApi;
import com.onegravity.rteditor.api.RTMediaFactoryImpl;
import com.onegravity.rteditor.api.RTProxyImpl;
import com.onegravity.rteditor.api.format.RTFormat;

public class RTEditorFragment extends Fragment {

    private RTManager mRTManager;
    private EditText mSubjectField;
    private RTEditText mRTMessageField;
    private RTEditText mRTSignatureField;

    public static RTEditorFragment getInstance(String subject, String message, String signature, boolean splitToolbar) {
        RTEditorFragment fragment = new RTEditorFragment();

        Bundle args = new Bundle();
        args.putString("subject", subject);
        args.putString("message", message);
        args.putString("signature", signature);
        args.putBoolean("splitToolbar", splitToolbar);
        fragment.setArguments(args);

        return fragment;
    }

    public RTEditorFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Bundle args = getArguments();
        String subject = args.getString("subject");
        String message = args.getString("message");
        String signature = args.getString("signature");
        boolean splitToolbar = args.getBoolean("splitToolbar");

        View view = inflater.inflate(splitToolbar ? R.layout.rte_demo_2 : R.layout.rte_demo_1, null);

        // create RTManager
        RTApi rtApi = new RTApi(getActivity(), new RTProxyImpl(getActivity()), new RTMediaFactoryImpl(getActivity(), true));
        mRTManager = new RTManager(rtApi, savedInstanceState);

        ViewGroup toolbarContainer = (ViewGroup) view.findViewById(R.id.rte_toolbar_container);

        // register toolbar 0 (if it exists)
        RTToolbar rtToolbar0 = (RTToolbar) view.findViewById(R.id.rte_toolbar);
        if (rtToolbar0 != null) {
            mRTManager.registerToolbar(toolbarContainer, rtToolbar0);
        }

        // register toolbar 1 (if it exists)
        RTToolbar rtToolbar1 = (RTToolbar) view.findViewById(R.id.rte_toolbar_character);
        if (rtToolbar1 != null) {
            mRTManager.registerToolbar(toolbarContainer, rtToolbar1);
        }

        // register toolbar 2 (if it exists)
        RTToolbar rtToolbar2 = (RTToolbar) view.findViewById(R.id.rte_toolbar_paragraph);
        if (rtToolbar2 != null) {
            mRTManager.registerToolbar(toolbarContainer, rtToolbar2);
        }

        // set subject
        mSubjectField = (EditText) view.findViewById(R.id.subject);
        mSubjectField.setText(subject);

        // register message editor
        mRTMessageField = (RTEditText) view.findViewById(R.id.rtEditText_1);
        mRTManager.registerEditor(mRTMessageField, true);
        if (message != null) {
            mRTMessageField.setRichTextEditing(true, message);
        }

        // register signature editor
        mRTSignatureField = (RTEditText) view.findViewById(R.id.rtEditText_2);
        mRTManager.registerEditor(mRTSignatureField, true);
        if (signature != null) {
            mRTSignatureField.setRichTextEditing(true, signature);
        }

        return view;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        mRTManager.onSaveInstanceState(outState);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mRTManager != null) {
            mRTManager.onDestroy(true);
        }
    }

    public String getSubject() {
        return mSubjectField.getText().toString();
    }

    public void setSubject(String value) {
        mSubjectField.setText(value);
    }

    public String getMessage() {
        return mRTMessageField.getText(RTFormat.HTML);
    }

    public void setMessage(String value) {
        mRTMessageField.setRichTextEditing(true, value);
    }

    public String getSignature() {
        return mRTSignatureField.getText(RTFormat.HTML);
    }

    public void setSignature(String value) {
        mRTSignatureField.setRichTextEditing(true, value);
    }

}
