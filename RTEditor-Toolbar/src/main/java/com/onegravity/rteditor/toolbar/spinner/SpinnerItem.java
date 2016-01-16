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

import android.view.View;
import android.widget.TextView;

/**
 * An abstract spinner item.
 * The base class for the color and font spinners.
 */
public abstract class SpinnerItem {
    final protected String mTitle;

    protected OnChangedListener mOnChangedListener;
    protected Object mListenerTag;

    public interface OnChangedListener {
        void onSpinnerItemChanged(Object tag);
    }

    public SpinnerItem(String title) {
        mTitle = title;
    }

    public String getName() {
        return mTitle;
    }

    void formatNameView(TextView view) {
        if (view != null) {
            view.setText(getName());
            view.setHorizontallyScrolling(true);
        }
    }

    void formatColorView(View view) {
    }

    void setOnChangedListener(OnChangedListener listener, Object tag) {
        mOnChangedListener = listener;
        mListenerTag = tag;
    }

}