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
import android.graphics.Color;
import android.os.Bundle;
import android.os.Parcelable;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import de.greenrobot.event.EventBus;

/**
 * A preference type that allows a user to choose a color
 */
public class ColorPickerPreference extends Preference implements
        PreferenceManager.OnActivityDestroyListener,
        Preference.OnPreferenceClickListener,
        OnColorChangedListener {

    private static final String androidns = "http://schemas.android.com/apk/res/android";

    private View mPreferenceView;
    private int mDefaultValue = Color.BLACK;
    private int mCurrentValue = Color.BLACK;
    private boolean mAlphaSliderEnabled = false;

    private int mPickerId = -1;
    private ColorPickerDialog mPicker;

    public ColorPickerPreference(Context context) {
        super(context);
        init(context, null);
    }

    public ColorPickerPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public ColorPickerPreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        setOnPreferenceClickListener(this);
        if (attrs != null) {
            mAlphaSliderEnabled = attrs.getAttributeBooleanValue(null, "alphaSlider", false);
            String defaultValue = attrs.getAttributeValue(androidns, "defaultValue");
            if (defaultValue != null && defaultValue.startsWith("#")) {
                try {
                    mDefaultValue = Util.convertToColorInt(defaultValue, mAlphaSliderEnabled);
                } catch (NumberFormatException e) {
                    mDefaultValue = Util.convertToColorInt("#FF000000", mAlphaSliderEnabled);
                }
            } else {
                int resourceId = attrs.getAttributeResourceValue(androidns, "defaultValue", 0);
                if (resourceId != 0) {
                    mDefaultValue = context.getResources().getInteger(resourceId);
                }
            }
        }
        mCurrentValue = mDefaultValue;
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        Bundle state = new Bundle();
        state.putInt("mDefaultValue", mDefaultValue);
        state.putInt("mCurrentValue", mCurrentValue);
        state.putBoolean("mAlphaSliderEnabled", mAlphaSliderEnabled);
        state.putInt("mPickerId", mPickerId);
        state.putParcelable("instanceState", super.onSaveInstanceState());
        return state;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (state instanceof Bundle) {
            Bundle bundle = (Bundle) state;
            mDefaultValue = bundle.getInt("mDefaultValue");
            mCurrentValue = bundle.getInt("mCurrentValue");
            mAlphaSliderEnabled = bundle.getBoolean("mAlphaSliderEnabled");
            mPickerId = bundle.getInt("mPickerId");
            state = bundle.getParcelable("instanceState");
        }
        super.onRestoreInstanceState(state);
    }

    @Override
    protected View onCreateView(ViewGroup parent) {
        mPreferenceView = super.onCreateView(parent);
        ColorPickerPreferenceWidget.setPreviewColor(mPreferenceView, getValue(), isEnabled());
        // we need this to have a listener after a screen rotation
        EventBus.getDefault().post(new SetColorChangedListenerEvent(mPickerId, this));
        return mPreferenceView;
    }

    @Override
    public void onActivityDestroy() {
        try {
            if (mPicker != null) mPicker.dismiss();
        } catch (Exception e) {
        }
    }

    @Override
    public void onDependencyChanged(Preference dependency, boolean disableDependent) {
        super.onDependencyChanged(dependency, disableDependent);
        ColorPickerPreferenceWidget.setPreviewColor(mPreferenceView, getValue(), isEnabled());
    }

    public int getValue() {
        try {
            if (isPersistent()) {
                mCurrentValue = getPersistedInt(mDefaultValue);
            }
        } catch (ClassCastException e) {
            mCurrentValue = mDefaultValue;
        }

        return mCurrentValue;
    }

    @Override
    public void onColorChanged(int color) {
        if (isPersistent()) {
            persistInt(color);
        }
        mCurrentValue = color;
        ColorPickerPreferenceWidget.setPreviewColor(mPreferenceView, getValue(), isEnabled());
        OnPreferenceChangeListener listener = getOnPreferenceChangeListener();
        if (listener != null) {
            listener.onPreferenceChange(this, color);
        }
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        mPicker = new ColorPickerDialog(getContext(), getValue(), mAlphaSliderEnabled).show();
        mPickerId = mPicker.getId();
        EventBus.getDefault().post(new SetColorChangedListenerEvent(mPickerId, this));
        return false;
    }

}