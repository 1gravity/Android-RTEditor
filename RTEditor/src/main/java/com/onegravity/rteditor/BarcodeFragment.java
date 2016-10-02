package com.onegravity.rteditor;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
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
import com.onegravity.rteditor.api.format.RTFormat;
import com.onegravity.rteditor.api.media.RTImage;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.EnumMap;
import java.util.IllegalFormatException;
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


    static class Barcode {
        final private RTImage mBarcode;
        final private Bitmap mBitmap;
        final private String mEncodeText;
        final private int mWidth;

        private Barcode(RTImage barcode, String encodeText, int width) {
            mBarcode = barcode;
            mBitmap = null;
            mEncodeText = encodeText;
            mWidth = width;
        }

        private Barcode(Bitmap bitmap, String encodeText, int width) {
            mBarcode = null;
            mBitmap = bitmap;
            mEncodeText = encodeText;
            mWidth = width;
        }

        public Barcode(RTImage mBarcode, Bitmap mBitmap, String mEncodeText, int mWidth) {
            this.mBarcode = mBarcode;
            this.mBitmap = mBitmap;
            this.mEncodeText = mEncodeText;
            this.mWidth = mWidth;
        }


        public RTImage getImage() {
            return mBarcode;
        }

        public Bitmap getBitmap() {
            return mBitmap;
        }

        public String getEncodeText() {
            return mEncodeText;
        }

        public int getWidth() {
            return mWidth;
        }

        public boolean isValid() {
            return !mEncodeText.isEmpty() && mWidth != 0;
        }
    }

    /**
     * This event is broadcast via EventBus when the dialog closes.
     * It's received by the RTManager to update the active editor.
     */
    static class BarcodeEvent {
        private final String mFragmentTag;
        private final Barcode mBarcode;
        private final boolean mWasCancelled;

        BarcodeEvent(Fragment fragment, Barcode barcode, boolean wasCancelled) {
            mFragmentTag = fragment.getTag();
            mBarcode = barcode;
            mWasCancelled = wasCancelled;
        }


        public String getFragmentTag() {
            return mFragmentTag;
        }

        public Barcode getBarcode() {
            return mBarcode;
        }

        public boolean wasCancelled() {
            return mWasCancelled;
        }
    }

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
        catch (IllegalFormatException ignore) {}

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
                barcode = new RTImage() {
                    @Override
                    public String getFilePath(RTFormat format) {
                        return pathImage;
                    }

                    @Override
                    public String getFileName() {
                        return pathImage.substring(pathImage.lastIndexOf("/"));
                    }

                    @Override
                    public String getFileExtension() {
                        return pathImage.substring(pathImage.lastIndexOf("."));
                    }

                    @Override
                    public boolean exists() {
                        return new File(pathImage).exists();
                    }

                    @Override
                    public void remove() {
                        new File(pathImage).delete();
                    }

                    @Override
                    public int getWidth() {
                        return width;
                    }

                    @Override
                    public int getHeight() {
                        return width;
                    }

                    @Override
                    public long getSize() {
                        return bitmap.getByteCount();
                    }
                };
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
