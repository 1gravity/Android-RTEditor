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

import android.graphics.Bitmap;

import com.onegravity.rteditor.api.media.RTImage;

/**
 * Represents a Barcode.
 */
public class Barcode {
    final private RTImage mBarcode;
    final private Bitmap mBitmap;
    final private String mEncodeText;
    final private int mWidth;

    Barcode(RTImage barcode, String encodeText, int width) {
        mBarcode = barcode;
        mBitmap = null;
        mEncodeText = encodeText;
        mWidth = width;
    }

    Barcode(Bitmap bitmap, String encodeText, int width) {
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
