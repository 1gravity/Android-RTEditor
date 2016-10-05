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
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.onegravity.rteditor.R;
import com.onegravity.rteditor.api.media.RTImage;

import org.greenrobot.eventbus.EventBus;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.EnumMap;
import java.util.Map;
import java.util.UUID;

import static android.graphics.Color.BLACK;
import static android.graphics.Color.WHITE;

/**
 * A DialogFragment to add, modify or remove QR Codes from Spanned text.
 */

public class BarcodeFragment extends DialogFragment {

    private static final String BARCODE_PATH = "barcode_path";
    private static final String BARCODE_DATA = "barcode_data";
    private static final String BARCODE_WIDTH = "barcode_width";

    EditText dataView, widthView;
    ProgressBar progressBar;

    public static BarcodeFragment newInstance(String filePath, String encodeText, int width) {
        BarcodeFragment fragment = new BarcodeFragment();
        Bundle args = new Bundle();
        if (encodeText != null) {
            args.putString(BARCODE_PATH, filePath);
            args.putString(BARCODE_DATA, encodeText);
            args.putInt(BARCODE_WIDTH, width);
        }
        fragment.setArguments(args);
        return fragment;
    }

    public BarcodeFragment() {
    }

    @NonNull
    @SuppressLint("InflateParams")
    @Override
    public final Dialog onCreateDialog(Bundle savedInstanceState) {
        Context context = getActivity();
        LayoutInflater li = LayoutInflater.from(context);
        View bodyView = li.inflate(R.layout.rte_barcode_body, null);
        View headerView = li.inflate(R.layout.rte_barcode_header, null);

        progressBar = (ProgressBar) headerView.findViewById(R.id.progressBar);


        Bundle args = getArguments();

        // barcode data
        final String data = args.getString(BARCODE_DATA);
        dataView = ((EditText) bodyView.findViewById(R.id.barcodeData));
        if (data != null) {
            dataView.setText(data);
        }

        // barcode width
        int width = args.getInt(BARCODE_WIDTH);
        widthView = ((EditText) bodyView.findViewById(R.id.barcodeWidth));
        if (width > 10) {
            widthView.setText(String.valueOf(width));
        }

        final TextView errorView = (TextView) headerView.findViewById(R.id.barcodeError);
        final String filePath = args.getString(BARCODE_PATH);

        AlertDialog.Builder builder = new AlertDialog.Builder(context)
                .setCustomTitle(headerView)
                .setView(bodyView)
                .setCancelable(false)
                .setPositiveButton(android.R.string.ok, null)//We override the onClick method bellow
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        EventBus.getDefault().post(new BarcodeEvent(BarcodeFragment.this, new Barcode(null, null, null, 0), true));
                    }
                });

        if (data != null && filePath != null) {
            builder.setNeutralButton("Remove", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    removeBarcode(filePath);
                }
            });
        }

        final AlertDialog dialog = builder.create();

        //Override the onClick method of the Positive Button, so as to not close the dialog when Click.
        //The dialog is closed when the image is saved onPostExecute.
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface d) {
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        try {
                            if (data == null && filePath == null) {
                                validate(dataView, widthView, errorView);
                            } else {
                                removeBarcode(filePath);
                                validate(dataView, widthView, errorView);
                            }
                        } catch (WriterException | IOException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        });

        return dialog;
    }

    public void removeBarcode(String filePath) {
        Barcode barcode = new Barcode(new BarcodeImage(filePath), null, 0);
        barcode.setRemoveRequest(true);
        EventBus.getDefault().post(new BarcodeEvent(BarcodeFragment.this, barcode, false));
    }


    private void validate(EditText dataView, EditText widthView, TextView errorView) throws WriterException, IOException {
        final String data = dataView.getText().toString().trim();
        int width = 400;    // default size of the QR code
        String widthString = widthView.getText().toString();
        try {
            width = Integer.parseInt(widthString);
        } catch (NumberFormatException ignore) {
        }

        String errorMessage = null;

        if (!data.isEmpty()) {
            if (width <= 1500) {
                FileSaveAsyncTask task = new FileSaveAsyncTask(getActivity());
                task.execute(data, String.valueOf(width));
            } else {
                errorMessage = getString(R.string.rte_barcode_big_image);
            }
        } else {
            errorMessage = getString(R.string.rte_barcode_no_data);
        }

        errorView.setText(errorMessage != null ? errorMessage : "");
    }


    @Override
    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);
        EventBus.getDefault().post(new BarcodeEvent(BarcodeFragment.this, null, true));
    }

    private class FileSaveAsyncTask extends AsyncTask<String, Integer, RTImage> {
        Activity activity;
        String data;
        int width;

        FileSaveAsyncTask(Activity activity) {
            this.activity = activity;
        }

        /**
         * Disable the edit text fields and show the progress bar
         */
        @Override
        protected void onPreExecute() {
            progressBar.setVisibility(View.VISIBLE);
            dataView.setFocusable(false);
            dataView.setBackgroundResource(R.drawable.shape_disabled);
            widthView.setFocusable(false);
        }

        @Override
        protected RTImage doInBackground(String... params) {
            String uniqueFileName = UUID.randomUUID().toString().replaceAll("-", "");
            String directory = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Android/data/" + activity.getPackageName() + "/files/images";

            data = params[0];
            width = Integer.parseInt(params[1]);

            File dir = new File(directory);
            if (!dir.exists()) dir.mkdirs();
            final String pathImage = directory + "/BARCODE-" + uniqueFileName + ".bmp";

            try {
                Bitmap bitmap = encodeAsBitmap(data, width);//create the qr image

                ByteArrayOutputStream byteOutputStream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteOutputStream);
                byte[] mBitmapData = byteOutputStream.toByteArray();
                ByteArrayInputStream inputStream = new ByteArrayInputStream(mBitmapData);

                OutputStream outputStream = new FileOutputStream(pathImage);
                byteOutputStream.writeTo(outputStream);

                byte[] buffer = new byte[1024];
                int lengthOfFile = mBitmapData.length;
                int totalWritten = 0;
                int bufferedBytes = 0;

                while ((bufferedBytes = inputStream.read(buffer)) > 0) {
                    totalWritten += bufferedBytes;
                    publishProgress((int) ((totalWritten * 100) / lengthOfFile));
                    outputStream.write(buffer, 0, bufferedBytes);
                }
            } catch (Exception ignore) {
            }

            return new BarcodeImage(pathImage);
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            progressBar.setProgress(values[0]);
        }

        @Override
        protected void onPostExecute(RTImage image) {
            BarcodeEvent event = new BarcodeEvent(BarcodeFragment.this, new Barcode(image, data, width), false);
            EventBus.getDefault().post(event);
            dismiss();//we override the onClick of the positive button so the dialog doesn't dismiss on click
        }

        Bitmap encodeAsBitmap(String data, int width) throws WriterException, IllegalArgumentException {
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

    }

}
