/*
 * Copyright (C) 2015-2018 Emanuel Moecklin
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
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.widget.Toast;

import com.onegravity.rteditor.utils.Helper;
import com.onegravity.rteditor.utils.io.IOUtils;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;

public class FileHelper {

    private static final String[][] PICK_DIRECTORY_INTENTS = {
            {"org.openintents.action.PICK_DIRECTORY", "file://"},        // OI File Manager (maybe others)
            {"com.estrongs.action.PICK_DIRECTORY", "file://"},            // ES File Explorer
            {Intent.ACTION_PICK, "folder://"},                            // Blackmoon File Browser (maybe others)
            {"com.androidworkz.action.PICK_DIRECTORY", "file://"},
    };

    /**
     * Tries to open a known file browsers to pick a directory.
     *
     * @return True if a filebrowser has been found (the result will be in the onActivityResult), False otherwise
     */
    public static boolean pickDirectory(Activity activity, File startPath, int requestCode) {
        PackageManager packageMgr = activity.getPackageManager();
        for (String[] intent : PICK_DIRECTORY_INTENTS) {
            String intentAction = intent[0];
            String uriPrefix = intent[1];
            Intent startIntent = new Intent(intentAction)
                    .putExtra("org.openintents.extra.TITLE", activity.getString(R.string.save_as))
                    .setData(Uri.parse(uriPrefix + startPath.getPath()));

            try {
                if (startIntent.resolveActivity(packageMgr) != null) {
                    activity.startActivityForResult(startIntent, requestCode);
                    return true;
                }
            } catch (ActivityNotFoundException e) {
                showNoFilePickerError(activity, e);
            }
        }

        return false;
    }

    public static boolean pickFile(Activity activity, int requestCode) {
        try {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("file/*");
            activity.startActivityForResult(intent, requestCode);
            return true;
        }
        catch (ActivityNotFoundException e) {
            showNoFilePickerError(activity, e);
        }
        return false;
    }

    private static void showNoFilePickerError(Context context, Exception e) {
        String msg = context.getString(R.string.no_file_picker, e.getMessage());
        Toast.makeText(context, msg , Toast.LENGTH_LONG).show();
    }

    public static String save(Context context, File outFile, String html) {
        Reader in = null;
        Writer out = null;

        try {
            in = new StringReader(html);
            out = new FileWriter(outFile);

            IOUtils.copy(in, out);

            return outFile.getAbsolutePath();
        } catch (IOException ioe) {
            String toastMsg = context.getString(R.string.save_as_failure, ioe.getMessage());
            Toast.makeText(context, toastMsg, Toast.LENGTH_LONG).show();
        } finally {
            Helper.closeQuietly(in);
            Helper.closeQuietly(out);
        }

        return null;
    }

    public static String load(Context context, String filePath) {
        File inFile = new File(filePath);
        Reader in = null;
        Writer out = null;

        try {
            in = new FileReader(inFile);
            out = new StringWriter();

            IOUtils.copy(in, out);

            return out.toString();
        } catch (IOException ioe) {
            String toastMsg = context.getString(R.string.load_failure_2, ioe.getMessage());
            Toast.makeText(context, toastMsg, Toast.LENGTH_LONG).show();
        } finally {
            Helper.closeQuietly(in);
            Helper.closeQuietly(out);
        }

        return null;
    }

}