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

package com.onegravity.rteditor.toolbar;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.ImageButton;

/**
 * An ImageButton for the toolbar.
 * It adds a checked state.
 */
public class RTToolbarImageButton extends ImageButton {
    private static final int[] CHECKED_STATE_SET = {R.attr.state_checked};

    private boolean mChecked;

    public RTToolbarImageButton(Context context) {
        this(context, null);
    }

    public RTToolbarImageButton(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.rte_ToolbarButton);
    }

    public RTToolbarImageButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ToolbarButton, defStyle, 0);
        mChecked = a.getBoolean(R.styleable.ToolbarButton_checked, false);
        a.recycle();
    }

    public boolean isChecked() {
        return mChecked;
    }

    public void setChecked(boolean checked) {
        if (mChecked != checked) {
            mChecked = checked;
            refreshDrawableState();
        }
    }

    @Override
    public int[] onCreateDrawableState(int extraSpace) {
        final int[] drawableState = super.onCreateDrawableState(extraSpace + CHECKED_STATE_SET.length);
        if (mChecked) mergeDrawableStates(drawableState, CHECKED_STATE_SET);
        return drawableState;
    }
}