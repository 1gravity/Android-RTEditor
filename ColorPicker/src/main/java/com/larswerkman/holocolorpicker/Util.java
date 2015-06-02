/*
 * Copyright 2012 Lars Werkman
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

package com.larswerkman.holocolorpicker;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.WindowManager;

import java.util.Locale;

public class Util {

    private static float sDensity = Float.MAX_VALUE;

    public static float getDisplayDensity(Context context) {
        if (sDensity == Float.MAX_VALUE) {
            Display display = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
            DisplayMetrics metrics = new DisplayMetrics();
            display.getMetrics(metrics);
            sDensity = metrics.density;
        }
        return sDensity;
    }

    public static int getScreenOrientation(Context context) {
        Configuration conf = context.getResources().getConfiguration();
        return conf.orientation;
    }

    public static Bitmap allocateBitmap(int width, int height) {
        Bitmap bitmap = null;
        try {
            bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        } catch (Throwable e1) {
            System.gc();
            try {
                bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            } catch (Throwable e2) {
                bitmap = null;
            }
        }
        return bitmap;
    }

    public static String[] convertToARGB(int color) {
        String alpha = Integer.toHexString(Color.alpha(color)).toUpperCase(Locale.getDefault());
        String red = Integer.toHexString(Color.red(color)).toUpperCase(Locale.getDefault());
        String green = Integer.toHexString(Color.green(color)).toUpperCase(Locale.getDefault());
        String blue = Integer.toHexString(Color.blue(color)).toUpperCase(Locale.getDefault());

        // we want the strings with a leading 0 if needed
        return new String[]{("00" + alpha).substring(alpha.length()),
                ("00" + red).substring(red.length()),
                ("00" + green).substring(green.length()),
                ("00" + blue).substring(blue.length())};
    }

    /**
     * Concerts a String color (#ff882465) to an int color
     *
     * @throws NumberFormatException
     */
    public static int convertToColorInt(String a, String r, String g, String b, boolean useAlpha) throws NumberFormatException {
        int alpha = useAlpha ? Integer.parseInt(a, 16) : 0xff;
        int red = Integer.parseInt(r, 16);
        int green = Integer.parseInt(g, 16);
        int blue = Integer.parseInt(b, 16);

        return Color.argb(useAlpha ? alpha : -1, red, green, blue);
    }

    /**
     * Concerts a String color (#ff882465) to an int color
     *
     * @throws NumberFormatException
     */
    public static int convertToColorInt(String argb, boolean useAlpha) throws NumberFormatException {

        if (argb.startsWith("#")) {
            argb = argb.replace("#", "");
        }

        int alpha = -1, red = -1, green = -1, blue = -1;

        if (argb.length() == 8) {
            alpha = Integer.parseInt(argb.substring(0, 2), 16);
            red = Integer.parseInt(argb.substring(2, 4), 16);
            green = Integer.parseInt(argb.substring(4, 6), 16);
            blue = Integer.parseInt(argb.substring(6, 8), 16);
        } else if (argb.length() == 6) {
            alpha = 255;
            red = Integer.parseInt(argb.substring(0, 2), 16);
            green = Integer.parseInt(argb.substring(2, 4), 16);
            blue = Integer.parseInt(argb.substring(4, 6), 16);
        }

        return Color.argb(useAlpha ? alpha : -1, red, green, blue);
    }

}