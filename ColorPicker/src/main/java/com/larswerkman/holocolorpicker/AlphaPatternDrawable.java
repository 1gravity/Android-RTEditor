/*
 * Copyright (C) 2015-2016 Emanuel Moecklin
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
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;

/**
 * This drawable that draws a simple white and gray chessboard pattern.
 * It's pattern you will often see as a background behind a
 * partly transparent image in many applications.
 */
public class AlphaPatternDrawable extends Drawable {

    private int mRectangleSize = 10;

    private Paint mPaint = new Paint();
    private Paint mPaintWhite = new Paint();
    private Paint mPaintGray = new Paint();

    private int numRectanglesHorizontal;
    private int numRectanglesVertical;

    /**
     * Bitmap in which the pattern will be cached.
     */
    private Bitmap mBitmap;

    public AlphaPatternDrawable(Context context) {
        float density = Util.getDisplayDensity(context);
        mRectangleSize = (int) (5 * density);
        mPaintWhite.setColor(0xffffffff);
        mPaintGray.setColor(0xffcbcbcb);
    }

    @Override
    public void draw(Canvas canvas) {
        if (mBitmap != null) {
            canvas.drawBitmap(mBitmap, null, getBounds(), mPaint);
        }
    }

    @Override
    public int getOpacity() {
        return PixelFormat.OPAQUE;
    }

    @Override
    public void setAlpha(int arg0) { /* not supported */ }

    @Override
    public void setColorFilter(ColorFilter arg0) { /* not supported */ }

    @Override
    protected void onBoundsChange(Rect bounds) {
        super.onBoundsChange(bounds);

        int height = bounds.height();
        int width = bounds.width();

        numRectanglesHorizontal = (int) Math.ceil((width / mRectangleSize));
        numRectanglesVertical = (int) Math.ceil(height / mRectangleSize);

        generatePatternBitmap();
    }

    /**
     * This will generate a bitmap with the pattern
     * as big as the rectangle we were allow to draw on.
     * We do this to cache the bitmap so we don't need to
     * recreate it each time draw() is called since it
     * takes a few milliseconds.
     */
    private void generatePatternBitmap() {

        if (getBounds().width() <= 0 || getBounds().height() <= 0) return;

        mBitmap = Util.allocateBitmap(getBounds().width(), getBounds().height());
        if (mBitmap == null) return;

        Canvas canvas = new Canvas(mBitmap);
        Rect r = new Rect();
        boolean verticalStartWhite = true;
        for (int i = 0; i <= numRectanglesVertical; i++) {
            boolean isWhite = verticalStartWhite;
            for (int j = 0; j <= numRectanglesHorizontal; j++) {
                r.top = i * mRectangleSize;
                r.left = j * mRectangleSize;
                r.bottom = r.top + mRectangleSize;
                r.right = r.left + mRectangleSize;
                canvas.drawRect(r, isWhite ? mPaintWhite : mPaintGray);
                isWhite = !isWhite;
            }
            verticalStartWhite = !verticalStartWhite;
        }

    }

    /**
     * This will generate a bitmap with the pattern
     * as big as the rectangle we are allowed to draw on.
     * We do this to cache the bitmap so we don't need to
     * recreate it each time draw() is called since it
     * takes a few milliseconds.
     */
    public Bitmap generatePatternBitmap(int w, int h) {
        if (w <= 0 || h <= 0) return null;

        Bitmap bitmap = Util.allocateBitmap(w, h);
        if (bitmap == null) return null;

        Canvas canvas = new Canvas(bitmap);

        int numRectanglesHorizontal = (int) Math.ceil((w / mRectangleSize));
        int numRectanglesVertical = (int) Math.ceil(h / mRectangleSize);

        Rect r = new Rect();
        boolean verticalStartWhite = true;
        for (int i = 0; i <= numRectanglesVertical; i++) {

            boolean isWhite = verticalStartWhite;
            for (int j = 0; j <= numRectanglesHorizontal; j++) {

                r.top = i * mRectangleSize;
                r.left = j * mRectangleSize;
                r.bottom = r.top + mRectangleSize;
                r.right = r.left + mRectangleSize;

                canvas.drawRect(r, isWhite ? mPaintWhite : mPaintGray);

                isWhite = !isWhite;
            }

            verticalStartWhite = !verticalStartWhite;

        }

        return bitmap;
    }
}