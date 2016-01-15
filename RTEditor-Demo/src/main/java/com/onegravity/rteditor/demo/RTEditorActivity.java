/*
 * Copyright (C) 2015 Emanuel Moecklin
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.onegravity.rteditor.demo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.onegravity.rteditor.RTEditText;
import com.onegravity.rteditor.RTManager;
import com.onegravity.rteditor.RTToolbar;
import com.onegravity.rteditor.api.RTApi;
import com.onegravity.rteditor.api.RTMediaFactoryImpl;
import com.onegravity.rteditor.api.RTProxyImpl;
import com.onegravity.rteditor.api.format.RTFormat;
import com.onegravity.rteditor.media.MediaUtils;

import java.io.File;

public class RTEditorActivity extends RTEditorBaseActivity {

    private static final int REQUEST_LOAD_FILE = 1;
    private static final int REQUEST_SAVE_FILE = 2;

    private RTManager mRTManager;
    private EditText mSubjectField;
    private RTEditText mRTMessageField;
    private RTEditText mRTSignatureField;

    private boolean mUseDarkTheme;
    private boolean mSplitToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // read parameters
        String subject = null;
        String message = null;
        String signature = null;
        if (savedInstanceState == null) {
            Intent intent = getIntent();
            subject = getStringExtra(intent, PARAM_SUBJECT);
            message = getStringExtra(intent, PARAM_MESSAGE);
            signature = getStringExtra(intent, PARAM_SIGNATURE);
            mUseDarkTheme = intent.getBooleanExtra(PARAM_DARK_THEME, false);
            mSplitToolbar = intent.getBooleanExtra(PARAM_SPLIT_TOOLBAR, false);
        } else {
            subject = savedInstanceState.getString(PARAM_SUBJECT, "");
            mUseDarkTheme = savedInstanceState.getBoolean(PARAM_DARK_THEME, false);
            mSplitToolbar = savedInstanceState.getBoolean(PARAM_SPLIT_TOOLBAR, false);
        }

        // set theme
        setTheme(mUseDarkTheme ? R.style.ThemeDark : R.style.ThemeLight);

        super.onCreate(savedInstanceState);

        // set layout
        setContentView(mSplitToolbar ? R.layout.rte_demo_2 : R.layout.rte_demo_1);

        setTitle(R.string.title_activity);

        // initialize rich text manager
        RTApi rtApi = new RTApi(this, new RTProxyImpl(this), new RTMediaFactoryImpl(this, true));
        mRTManager = new RTManager(rtApi, savedInstanceState);

        ViewGroup toolbarContainer = (ViewGroup) findViewById(R.id.rte_toolbar_container);

        // register toolbar 0 (if it exists)
        RTToolbar rtToolbar0 = (RTToolbar) findViewById(R.id.rte_toolbar);
        if (rtToolbar0 != null) {
            mRTManager.registerToolbar(toolbarContainer, rtToolbar0);
        }

        // register toolbar 1 (if it exists)
        RTToolbar rtToolbar1 = (RTToolbar) findViewById(R.id.rte_toolbar_character);
        if (rtToolbar1 != null) {
            mRTManager.registerToolbar(toolbarContainer, rtToolbar1);
        }

        // register toolbar 2 (if it exists)
        RTToolbar rtToolbar2 = (RTToolbar) findViewById(R.id.rte_toolbar_paragraph);
        if (rtToolbar2 != null) {
            mRTManager.registerToolbar(toolbarContainer, rtToolbar2);
        }

        // set subject
        mSubjectField = (EditText) findViewById(R.id.subject);
        mSubjectField.setText(subject);

        // register message editor
        mRTMessageField = (RTEditText) findViewById(R.id.rtEditText_1);
        mRTManager.registerEditor(mRTMessageField, true);
        if (message != null) {
            mRTMessageField.setRichTextEditing(true, message);
        }

        // register signature editor
        mRTSignatureField = (RTEditText) findViewById(R.id.rtEditText_2);
        mRTManager.registerEditor(mRTSignatureField, true);
        if (signature != null) {
            mRTSignatureField.setRichTextEditing(true, signature);
        }

        mRTMessageField.requestFocus();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mRTManager != null) {
            mRTManager.onDestroy(true);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        mRTManager.onSaveInstanceState(outState);

        String subject = mSubjectField.getText().toString();
        if (subject != null) outState.putString(PARAM_SUBJECT, subject);

        outState.putBoolean(PARAM_DARK_THEME, mUseDarkTheme);
        outState.putBoolean(PARAM_SPLIT_TOOLBAR, mSplitToolbar);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (mRTManager != null && mRTManager.onActivityResult(requestCode, resultCode, data)) {
            // The call to RTManager.onActivityResult is needed because images are picked in a
            // separate Activity and a reference is sent back in the Intent data to be inserted into
            // the rich text.
            return;
        }
        if (resultCode == Activity.RESULT_OK && data != null && data.getData() != null && data.getData().getPath() != null) {
            String filePath = data.getData().getPath();

            if (requestCode == REQUEST_SAVE_FILE) {
                /*
            	 * Save file.
            	 * 
            	 * Of course this is a hack but since this is just a demo
            	 * to show how to integrate the rich text editor this is ok ;-)
            	 */

                // write subject
                File targetFile = MediaUtils.createUniqueFile(new File(filePath), "subject.html", true);
                String fileName = FileHelper.save(this, targetFile, mSubjectField.getText().toString());

                // write message
                targetFile = new File(targetFile.getAbsolutePath().replace("subject_", "message_"));
                FileHelper.save(this, targetFile, mRTMessageField.getText(RTFormat.HTML));

                // write signature
                targetFile = new File(targetFile.getAbsolutePath().replace("message_", "signature_"));
                FileHelper.save(this, targetFile, mRTSignatureField.getText(RTFormat.HTML));

                if (fileName != null) {
                    String toastMsg = getString(R.string.save_as_success, fileName);
                    Toast.makeText(this, toastMsg, Toast.LENGTH_LONG).show();
                }
            } else if (requestCode == REQUEST_LOAD_FILE) {
        		/*
        		 * Load File
        		 * 
            	 * A hack, I know ...
        		 */

                if (filePath.contains("message_")) {
                    filePath = filePath.replace("message_", "subject_");
                } else if (filePath.contains("signature_")) {
                    filePath = filePath.replace("signature_", "subject_");
                }

                if (filePath.contains("subject_")) {
                    // load subject
                    String s = FileHelper.load(this, filePath);
                    mSubjectField.setText(s);

                    // load message
                    filePath = filePath.replace("subject_", "message_");
                    s = FileHelper.load(this, filePath);
                    mRTMessageField.setRichTextEditing(true, s);

                    // load signature
                    filePath = filePath.replace("message_", "signature_");
                    s = FileHelper.load(this, filePath);
                    mRTSignatureField.setRichTextEditing(true, s);
                } else {
                    Toast.makeText(this, R.string.load_failure_1, Toast.LENGTH_LONG).show();
                }
            }

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // configure theme item
        MenuItem item = menu.findItem(R.id.theme);
        item.setTitle(mUseDarkTheme ? R.string.menu_light_theme : R.string.menu_dark_theme);

        // configure split toolbar item
        item = menu.findItem(R.id.split_toolbar);
        item.setTitle(mSplitToolbar ? R.string.menu_single_toolbar : R.string.menu_split_toolbar);

        item = menu.findItem(R.id.editor_activity);
        if (item != null) item.setVisible(false);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.load) {
            FileHelper.pickFile(this, REQUEST_LOAD_FILE);
            return true;
        } else if (itemId == R.id.save) {
			/*
			 * Note that you need a third party file explorer that
			 * supports a pick directory Intent (like ES File Explorer or any
			 * Open Intent file explorer).
			 */
            File targetDir = getExternalFilesDir(null);
            FileHelper.pickDirectory(this, targetDir, REQUEST_SAVE_FILE);
            return true;
        } else if (itemId == R.id.theme) {
            mUseDarkTheme = !mUseDarkTheme;
            startAndFinish(getClass());
            return true;
        } else if (itemId == R.id.split_toolbar) {
            mSplitToolbar = !mSplitToolbar;
            startAndFinish(getClass());
            return true;
        } else if (itemId == R.id.editor_fragment) {
            startAndFinish(RTEditorActivityWithFragment.class);
            return true;
        }
        return false;
    }

    private void startAndFinish(Class<? extends Activity> clazz) {
        String subject = mSubjectField.getText().toString();
        String message = mRTMessageField.getText(RTFormat.HTML);
        String signature = mRTSignatureField.getText(RTFormat.HTML);
        Intent intent = new Intent(this, clazz)
                .putExtra(PARAM_DARK_THEME, mUseDarkTheme)
                .putExtra(PARAM_SPLIT_TOOLBAR, mSplitToolbar)
                .putExtra(PARAM_SUBJECT, subject)
                .putExtra(PARAM_MESSAGE, message)
                .putExtra(PARAM_SIGNATURE, signature);
        startActivity(intent);
        finish();
    }

}
