/*
 * Copyright (C) 2016 Emanuel Moecklin
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

package com.onegravity.rteditor.barcode;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.onegravity.rteditor.R;
import com.onegravity.rteditor.api.media.RTImage;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.EnumMap;
import java.util.Map;
import java.util.UUID;

import static android.graphics.Color.BLACK;
import static android.graphics.Color.WHITE;

/**
 * A DialogFragment to add, modify or remove QR Codes from Spanned text.
 */

public class BarcodeFragment extends DialogFragment {

    private static final String BARCODE_DATA = "barcode_data";
    private static final String BARCODE_WIDTH = "barcode_width";

    public static BarcodeFragment newInstance(String encodeText, int width) {
        BarcodeFragment fragment = new BarcodeFragment();
        Bundle args = new Bundle();
        args.putString(BARCODE_DATA, encodeText);
        args.putInt(BARCODE_WIDTH, width);
        fragment.setArguments(args);
        return fragment;
    }

    public BarcodeFragment() {}

    @NonNull
    @SuppressLint("InflateParams")
    @Override
    public final Dialog onCreateDialog(Bundle savedInstanceState) {
        Context context = getActivity();
        LayoutInflater li = LayoutInflater.from(context);
        View view = li.inflate(R.layout.rte_barcode, null);

        Bundle args = getArguments();

        // barcode data
        final String data = args.getString(BARCODE_DATA);

        final EditText dataView = ((EditText) view.findViewById(R.id.barcodeData));
        if (data != null) {
            dataView.setText(data);
        }

        // barcode width
        final int width = args.getInt(BARCODE_WIDTH) != 0 ? args.getInt(BARCODE_WIDTH) : 200;
        final EditText widthView = ((EditText) view.findViewById(R.id.barcodeWidth));
        if (width > 1500) {
            widthView.setText(String.valueOf(width));
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(context)
                .setTitle(R.string.rte_create_barcode)
                .setView(view)
                .setCancelable(false)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        try {
                            validate(dataView, widthView);
                        } catch (WriterException | IOException e) {
                            e.printStackTrace();
                        }
                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        EventBus.getDefault().post(new BarcodeEvent(BarcodeFragment.this, new Barcode(null, null, null, 0), true));
                    }
                });

        if (data != null) {
            builder.setNeutralButton(R.string.rte_remove_action, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // Remove button
                    EventBus.getDefault().post(new BarcodeEvent(BarcodeFragment.this, null, false));
                }
            });
        }

        return builder.create();
    }

    private void validate(EditText dataView, EditText widthView) throws WriterException, IOException {
        final String data = dataView.getText().toString().trim();

        int width = 200;    // default size of the QR code
        String widthString = widthView.getText().toString();
        try {
            width = Integer.parseInt(widthString);
        }
        catch (NumberFormatException ignore) {}

        String errorMessage = null;

        if (!data.isEmpty()) {
            if (width < 1500) {
                final Bitmap bitmap = encodeAsBitmap(data, width);

                FileSaveAsyncTask task = new FileSaveAsyncTask(getActivity());
                task.execute(new Barcode(bitmap, data, width));
            } else {
                errorMessage = getString(R.string.rte_barcode_big_image);
            }
        } else {
            errorMessage = getString(R.string.rte_barcode_no_data);
        }

        if (errorMessage != null) dataView.setText(errorMessage);
    }


    @Override
    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);
        EventBus.getDefault().post(new BarcodeEvent(BarcodeFragment.this, null, true));
    }

    public static Bitmap encodeAsBitmap(String data, int width) throws WriterException, IllegalArgumentException {
        if (data == null) {
            return null;
        }

        BarcodeFormat format = BarcodeFormat.QR_CODE;
        String encoding = "UTF-8";

        Map<EncodeHintType, Object> hints = new EnumMap<>(EncodeHintType.class);
        hints.put(EncodeHintType.CHARACTER_SET, encoding);

        MultiFormatWriter writer = new MultiFormatWriter();
        BitMatrix result;

        try {
            result = writer.encode(data, format, width, width, hints);
        } catch (IllegalArgumentException iae) {
            return null;
        }

        int imageWidth = result.getWidth();
        int imageHeight = result.getHeight();
        int[] pixels = new int[imageWidth * imageHeight];

        for (int y = 0; y < imageHeight; y++) {
            int offset = y * imageWidth;
            for (int x = 0; x < imageWidth; x++) {
                pixels[offset + x] = result.get(x, y) ? BLACK : WHITE;
            }
        }

        Bitmap bitmap = Bitmap.createBitmap(imageWidth, imageHeight, Bitmap.Config.ARGB_8888);
        bitmap.setPixels(pixels, 0, imageWidth, 0, 0, imageWidth, imageHeight);
        return bitmap;
    }

    private class FileSaveAsyncTask extends AsyncTask<Barcode, Void, RTImage> {
        Activity activity;
        Bitmap bitmap;
        String data;
        int width;

        FileSaveAsyncTask(Activity activity) {
            this.activity = activity;
        }

        @Override
        protected RTImage doInBackground(Barcode... params) {
            String uniqueFileName = UUID.randomUUID().toString().replaceAll("-", "");
            String directory = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Android/data/" + activity.getPackageName() + "/files/images";

            Barcode param = params[0];

            bitmap = param.getBitmap();
            data = param.getEncodeText();
            width = param.getWidth();

            File dir = new File(directory);
            if (!dir.exists()) dir.mkdirs();

            final String pathImage = directory + "/BARCODE-" + uniqueFileName + ".bmp";

            FileOutputStream outImage = null;

            try {
                outImage = new FileOutputStream(pathImage);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

            RTImage barcode = null;
            if (bitmap != null) {
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, outImage);
                try {
                    outImage.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                barcode = new BarcodeImage(pathImage);
            }

            return barcode;
        }

        @Override
        protected void onPostExecute(RTImage image) {
            BarcodeEvent event = new BarcodeEvent(BarcodeFragment.this, new Barcode(image, data, width), false);
            EventBus.getDefault().post(event);
        }
    }

}
