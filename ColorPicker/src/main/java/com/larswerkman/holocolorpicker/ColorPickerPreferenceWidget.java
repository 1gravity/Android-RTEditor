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
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

/**
 * The widget that shows the selected color for a Preference.
 * It's optimized to update in "real-time" when the user picks a color from the color wheel.
 */
public class ColorPickerPreferenceWidget extends ImageView {

    private static final String IMAGE_VIEW_TAG = "#IMAGE_VIEW_TAG#";

    private int mDefaultSize;
    private int mCurrentSize;

    private Bitmap mAlphaPattern;

    private int mColor;
    private Paint mColorPaint;

    private int mBorderColor;
    private Paint mBorderColorPaint;

    public ColorPickerPreferenceWidget(Context context) {
        super(context);
        init(context, null);
    }

    public ColorPickerPreferenceWidget(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public ColorPickerPreferenceWidget(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        setTag(IMAGE_VIEW_TAG);
        setBackgroundColor(Color.TRANSPARENT);

        // create alpha pattern and set as image
        mDefaultSize = (int) (Util.getDisplayDensity(context) * 31); // 30dip
        mCurrentSize = mDefaultSize;
        setAlphaPattern(context, mDefaultSize);

        int wrap = ViewGroup.LayoutParams.WRAP_CONTENT;
        setLayoutParams(new ViewGroup.LayoutParams(wrap, wrap));
    }

    private void setAlphaPattern(Context context, int size) {
        AlphaPatternDrawable apd = new AlphaPatternDrawable(context);
        mAlphaPattern = apd.generatePatternBitmap(size, size);
        setImageBitmap(mAlphaPattern);
    }

    public void setColor(int color, int borderColor) {
        mColor = color;
        mColorPaint = new Paint();
        mColorPaint.setColor(mColor);

        mBorderColor = borderColor;
        mBorderColorPaint = new Paint();
        mBorderColorPaint.setColor(mBorderColor);
        mBorderColorPaint.setStrokeWidth(2f);

        invalidate();
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (changed) {
            resize(getWidth(), getHeight());
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (w != oldw || h != oldh) {
            resize(getWidth(), getHeight());
        }
    }

    private void resize(int width, int height) {
        int size = Math.min(Math.min(mDefaultSize, width), height);
        if (size != mCurrentSize) {
            mCurrentSize = size;
            setAlphaPattern(getContext(), mCurrentSize);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int x = 0;
        int y = 0;

        // draw color
        canvas.drawRect(x, y, mCurrentSize, mCurrentSize, mColorPaint);

        // draw border
        canvas.drawLine(x, y, x + mCurrentSize, y, mBorderColorPaint);
        canvas.drawLine(x, y, x, y + mCurrentSize, mBorderColorPaint);
        canvas.drawLine(x + mCurrentSize, y, x + mCurrentSize, y + mCurrentSize, mBorderColorPaint);
        canvas.drawLine(x, y + mCurrentSize, x + mCurrentSize, y + mCurrentSize, mBorderColorPaint);
    }

    static void setPreviewColor(View preferenceView, int color, boolean isEnabled) {
        if (preferenceView != null && preferenceView instanceof ViewGroup) {
            Context context = preferenceView.getContext();
            ViewGroup widgetFrameView = ((ViewGroup) preferenceView.findViewById(android.R.id.widget_frame));

            if (widgetFrameView != null) {

                ColorPickerPreferenceWidget widgetView = null;

                // find already created preview image
                int count = widgetFrameView.getChildCount();
                for (int i = 0; i < count; i++) {
                    View view = widgetFrameView.getChildAt(i);
                    if (view instanceof ColorPickerPreferenceWidget && IMAGE_VIEW_TAG.equals(view.getTag())) {
                        widgetView = (ColorPickerPreferenceWidget) view;
                        break;
                    }
                }
                if (widgetView == null) {
                    // remove already created preview image and create new one
                    if (count > 0) widgetFrameView.removeViews(0, count);
                    widgetView = new ColorPickerPreferenceWidget(context);
                    widgetView.setTag(IMAGE_VIEW_TAG);
                    widgetFrameView.setVisibility(View.VISIBLE);
                    widgetFrameView.setPadding(
                            widgetFrameView.getPaddingLeft(),
                            widgetFrameView.getPaddingTop(),
                            (int) (Util.getDisplayDensity(context) * 8),
                            widgetFrameView.getPaddingBottom()
                    );
                    widgetFrameView.addView(widgetView);
                }

                // determine and set colors
                int borderColor = Color.WHITE;
                if (!isEnabled) {
                    color = reduceBrightness(color, 1);
                    borderColor = reduceBrightness(borderColor, 1);
                }
                widgetView.setColor(color, borderColor);
            }
        }
    }

    private static int reduceBrightness(int color, int factor) {
        return (color & 0xff000000) +
                reduceBrightness(color, 0xff0000, factor) +
                reduceBrightness(color, 0x00ff00, factor) +
                reduceBrightness(color, 0x0000ff, factor);
    }

    private static int reduceBrightness(int color, int mask, int factor) {
        return ((color & mask) >>> factor) & mask;
    }
}