/*
 * Copyright 2015 Emanuel Moecklin
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.onegravity.rteditor.demo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.onegravity.rteditor.media.MediaUtils;

import java.io.File;

public class RTEditorActivityWithFragment extends RTEditorBaseActivity {

    private static final int REQUEST_LOAD_FILE = 1;
    private static final int REQUEST_SAVE_FILE = 2;

    private boolean mUseDarkTheme;
    private boolean mSplitToolbar;

    private RTEditorFragment mFragment;

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
            message = savedInstanceState.getString(PARAM_MESSAGE, "");
            signature = savedInstanceState.getString(PARAM_SIGNATURE, "");
            mUseDarkTheme = savedInstanceState.getBoolean(PARAM_DARK_THEME, false);
            mSplitToolbar = savedInstanceState.getBoolean(PARAM_SPLIT_TOOLBAR, false);
        }

        // set theme
        setTheme(mUseDarkTheme ? R.style.ThemeDark : R.style.ThemeLight);

        super.onCreate(savedInstanceState);

        // set layout
        setContentView(R.layout.rte_demo_with_fragment);

        mFragment = RTEditorFragment.getInstance(subject, message, signature, mSplitToolbar);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentContainer, mFragment)
                .commitAllowingStateLoss();

        setTitle(R.string.title_fragment);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putString(PARAM_SUBJECT, getSubject());
        outState.putString(PARAM_MESSAGE, getMessage());
        outState.putString(PARAM_SIGNATURE, getSignature());
        outState.putBoolean(PARAM_DARK_THEME, mUseDarkTheme);
        outState.putBoolean(PARAM_SPLIT_TOOLBAR, mSplitToolbar);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
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
                String fileName = FileHelper.save(this, targetFile, getSubject());

                // write message
                targetFile = new File(targetFile.getAbsolutePath().replace("subject_", "message_"));
                FileHelper.save(this, targetFile, getMessage());

                // write signature
                targetFile = new File(targetFile.getAbsolutePath().replace("message_", "signature_"));
                FileHelper.save(this, targetFile, getSignature());

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
                    if (mFragment != null) mFragment.setSignature(s);

                    // load message
                    filePath = filePath.replace("subject_", "message_");
                    s = FileHelper.load(this, filePath);
                    if (mFragment != null) mFragment.setMessage(s);

                    // load signature
                    filePath = filePath.replace("message_", "signature_");
                    s = FileHelper.load(this, filePath);
                    if (mFragment != null) mFragment.setSignature(s);
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

        item = menu.findItem(R.id.editor_fragment);
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
        } else if (itemId == R.id.editor_activity) {
            startAndFinish(RTEditorActivity.class);
            return true;
        } else if (itemId == R.id.editor_preview) {
            Intent i = new Intent(this, RTEditorPreview.class);
            i.putExtra("text", mFragment.getMessage() + "\n" +
                    mFragment.getSignature());
            startActivity(i);
        }
        return false;
    }

    private void startAndFinish(Class<? extends Activity> clazz) {
        Intent intent = new Intent(this, clazz)
                .putExtra(PARAM_DARK_THEME, mUseDarkTheme)
                .putExtra(PARAM_SPLIT_TOOLBAR, mSplitToolbar)
                .putExtra(PARAM_SUBJECT, getSubject())
                .putExtra(PARAM_MESSAGE, getMessage())
                .putExtra(PARAM_SIGNATURE, getSignature());
        startActivity(intent);
        finish();
    }

    private String getSubject() {
        return mFragment != null ? mFragment.getSubject() : "";
    }

    private String getMessage() {
        return mFragment != null ? mFragment.getMessage() : "";
    }

    private String getSignature() {
        return mFragment != null ? mFragment.getSignature() : "";
    }

}
