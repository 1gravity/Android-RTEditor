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

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.support.annotation.NonNull;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TabHost;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TabHost.TabContentFactory;
import android.widget.TabHost.TabSpec;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;

import de.greenrobot.event.EventBus;

public class ColorPickerDialog implements OnColorChangedListener, OnTabChangeListener {

    private static final String WHEEL_TAG = "wheel";
    private static final String EXACT_TAG = "exact";

    private static int sCount;

    final private int mId;

    final private Context mContext;

    final private int mInitialColor;

    private int mNewColor;

    final private boolean mUseOpacityBar;

    private Dialog mDialog;

    private TabHost mTabHost;
    private String mCurrentTab;

    private ColorWheelView mColorPicker;

    private EditText mExactViewA;
    private EditText mExactViewR;
    private EditText mExactViewG;
    private EditText mExactViewB;
    private ColorWheelView mExactColorPicker;

    private OnColorChangedListener mListener;

    private int mOrientation;

    public ColorPickerDialog(Context context, int initialColor, boolean useOpacityBar) {
        mId = sCount++;
        mContext = context;
        mInitialColor = initialColor;
        mNewColor = initialColor;
        mUseOpacityBar = useOpacityBar;
    }

    public int getId() {
        return mId;
    }

    @SuppressLint("InflateParams")
    public ColorPickerDialog show() {
        mOrientation = Util.getScreenOrientation(mContext);

        View view = LayoutInflater.from(mContext).inflate(R.layout.dialog_color_picker, null);

   		/*
         * Create tabs
         */
        mTabHost = (TabHost) view.findViewById(android.R.id.tabhost);
        mTabHost.setup();
        createTabs();

        /*
         * Create Dialog
         */
        String ok = mContext.getString(android.R.string.ok);
        String cancel = mContext.getString(android.R.string.cancel);

        mDialog = new MaterialDialog.Builder(mContext)
                .customView(view, false)
                .cancelable(true)
                .title(null)
                .positiveText(ok)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        finalizeChanges(mNewColor);
                    }
                })
                .negativeText(cancel)
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        finalizeChanges(mInitialColor);
                    }
                })
                .cancelListener(new OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        finalizeChanges(mInitialColor);
                    }
                })
                .build();

        mDialog.setCanceledOnTouchOutside(false);

        mDialog.show();

        // otherwise the keyboard won't show
        mDialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);

        EventBus.getDefault().register(this);

        return this;
    }

    private void finalizeChanges(int color) {
        if (mListener != null) {
            mListener.onColorChanged(color);
        }
        EventBus.getDefault().unregister(this);
    }

    private void createTabs() {
        mTabHost.clearAllTabs();
        mTabHost.setOnTabChangedListener(null);        // or we would get NPEs in onTabChanged() when calling addTab()
        TabSpec tabSpec1 = mTabHost.newTabSpec(WHEEL_TAG)
                .setIndicator(mContext.getString(R.string.color_picker_wheel))
                .setContent(mFactory);
        TabSpec tabSpec2 = mTabHost.newTabSpec(EXACT_TAG)
                .setIndicator(mContext.getString(R.string.color_picker_exact))
                .setContent(mFactory);
        mTabHost.addTab(tabSpec1);
        mTabHost.addTab(tabSpec2);
        mTabHost.setOnTabChangedListener(this);
        String tag = mCurrentTab != null ? mCurrentTab : WHEEL_TAG;
        mTabHost.setCurrentTabByTag(tag);
    }

    private TabContentFactory mFactory = new TabContentFactory() {
        @Override
        public View createTabContent(String tag) {
            return tag.equals(WHEEL_TAG) ? createWheel() :
                    tag.equals(EXACT_TAG) ? createExact() : null;
        }
    };

    @SuppressLint("InflateParams")
    private View createWheel() {
        View view = LayoutInflater.from(mContext).inflate(R.layout.dialog_color_wheel, null);

        mColorPicker = (ColorWheelView) view.findViewById(R.id.picker);

        ValueBar valueBar = (ValueBar) view.findViewById(R.id.valuebar);
        if (valueBar != null) {
            mColorPicker.addValueBar(valueBar);
        }

        SaturationBar saturationBar = (SaturationBar) view.findViewById(R.id.saturationbar);
        if (saturationBar != null) {
            mColorPicker.addSaturationBar(saturationBar);
        }

        OpacityBar opacityBar = (OpacityBar) view.findViewById(R.id.opacitybar);
        if (opacityBar != null) {
            if (mUseOpacityBar) {
                mColorPicker.addOpacityBar(opacityBar);
            }
            opacityBar.setVisibility(mUseOpacityBar ? View.VISIBLE : View.GONE);
        }

        mColorPicker.setOldCenterColor(mInitialColor);
        mColorPicker.setColor(mNewColor);
        mColorPicker.setOnColorChangedListener(this);

        return view;
    }

    @SuppressLint("InflateParams")
    private View createExact() {
        View view = LayoutInflater.from(mContext).inflate(R.layout.dialog_color_exact, null);

        mExactViewA = (EditText) view.findViewById(R.id.exactA);
        mExactViewR = (EditText) view.findViewById(R.id.exactR);
        mExactViewG = (EditText) view.findViewById(R.id.exactG);
        mExactViewB = (EditText) view.findViewById(R.id.exactB);

        InputFilter[] filters = new InputFilter[]{new InputFilter.LengthFilter(2)};
        mExactViewA.setFilters(filters);
        mExactViewR.setFilters(filters);
        mExactViewG.setFilters(filters);
        mExactViewB.setFilters(filters);

        mExactViewA.setVisibility(mUseOpacityBar ? View.VISIBLE : View.GONE);

        setExactColor(mInitialColor);

        mExactColorPicker = (ColorWheelView) view.findViewById(R.id.picker_exact);
        mExactColorPicker.setOldCenterColor(mInitialColor);
        mExactColorPicker.setNewCenterColor(mNewColor);

        return view;
    }

    private void setExactColor(int color) {
        String[] colorComponents = Util.convertToARGB(color);
        mExactViewA.removeTextChangedListener(mExactTextWatcher);
        mExactViewR.removeTextChangedListener(mExactTextWatcher);
        mExactViewG.removeTextChangedListener(mExactTextWatcher);
        mExactViewB.removeTextChangedListener(mExactTextWatcher);

        mExactViewA.setText(colorComponents[0]);
        mExactViewR.setText(colorComponents[1]);
        mExactViewG.setText(colorComponents[2]);
        mExactViewB.setText(colorComponents[3]);

        mExactViewA.addTextChangedListener(mExactTextWatcher);
        mExactViewR.addTextChangedListener(mExactTextWatcher);
        mExactViewG.addTextChangedListener(mExactTextWatcher);
        mExactViewB.addTextChangedListener(mExactTextWatcher);
    }

    private TextWatcher mExactTextWatcher = new TextWatcher() {
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        public void afterTextChanged(Editable s) {
            try {
                int color = Util.convertToColorInt(mExactViewA.getText().toString(), mExactViewR.getText().toString(),
                        mExactViewG.getText().toString(), mExactViewB.getText().toString(), mUseOpacityBar);
                mExactColorPicker.setNewCenterColor(color);
                onColorChanged(color);
            } catch (NumberFormatException ignore) {
            }
        }
    };

    public void dismiss() {
        try {
            mDialog.dismiss();
        } catch (Exception ignore) {
        }
    }

    /**
     * Set a OnColorChangedListener to get notified when the color
     * selected by the user has changed.
     */
    public void onEventMainThread(SetColorChangedListenerEvent event) {
        if (event.getId() == mId) {
            int screenOrientation = Util.getScreenOrientation(mContext);
            if (mOrientation != screenOrientation) {
                mOrientation = screenOrientation;
                createTabs();
            }

            mListener = event.getOnColorChangedListener();
        }
    }

    @Override
    public void onColorChanged(int color) {
        mNewColor = color;
        if (mListener != null) {
            mListener.onColorChanged(mNewColor);
        }
    }

    @Override
    public void onTabChanged(String tabId) {
        mCurrentTab = tabId;
        if (tabId.equals(WHEEL_TAG) && mColorPicker != null) {
            mColorPicker.setColor(mNewColor);
            InputMethodManager imm = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(mExactViewA.getWindowToken(), 0);
        } else if (tabId.equals(EXACT_TAG) && mExactViewA != null) {
            setExactColor(mNewColor);
            mExactColorPicker.setOldCenterColor(mInitialColor);
            mExactColorPicker.setNewCenterColor(mNewColor);
            InputMethodManager imm = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(mExactViewR, 0);
        }
    }

    public int getColor() {
        return mColorPicker.getColor();
    }

}