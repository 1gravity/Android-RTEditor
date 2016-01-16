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

package com.onegravity.rteditor.toolbar.spinner;

import android.graphics.Color;
import android.widget.TextView;

/**
 * The spinner item for the background color.
 */
public class BGColorSpinnerItem extends ColorSpinnerItem {
    private static final double rY = 0.212655;
    private static final double gY = 0.715158;
    private static final double bY = 0.072187;

    /**
     * @param color    This item's color
     * @param title    This item's title
     * @param isEmpty  True if we have the empty color entry (to remove any color setting)
     * @param isCustom True if we have the custom color entry opening the color wheel
     */
    public BGColorSpinnerItem(int color, String title, boolean isEmpty, boolean isCustom) {
        super(color, title, isEmpty, isCustom);
    }

    @Override
    void formatNameView(TextView view) {
        super.formatNameView(view);

        if (isEmpty()) {
            view.setBackgroundColor(0x00000000);
        } else {
            view.setBackgroundColor(mColor);

            int r = (mColor) & 0xFF;
            int g = (mColor >> 8) & 0xFF;
            int b = (mColor >> 16) & 0xFF;
            double Y = rY * r + gY * g + bY * b;
            view.setTextColor(Y > 0x88 ? Color.BLACK : Color.WHITE);
        }
    }

}