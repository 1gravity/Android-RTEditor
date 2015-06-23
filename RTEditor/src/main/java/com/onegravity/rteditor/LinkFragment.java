/*
 * Copyright (C) 2015 Emanuel Moecklin
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

package com.onegravity.rteditor;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;

import org.apache.commons.httpclient.URIException;
import org.apache.commons.httpclient.util.URIUtil;
import org.apache.commons.validator.routines.UrlValidator;

import java.lang.ref.SoftReference;

import de.greenrobot.event.EventBus;

/**
 * A DialogFragment to add, modify or remove links from Spanned text.
 */
public class LinkFragment extends DialogFragment {

    /**
     * The Link class describes a link (link text and an URL).
     */
    static class Link {
        final private String mLinkText;
        final private String mUrl;

        private Link(String linkText, String url) {
            mLinkText = linkText;
            mUrl = url;
        }

        public String getLinkText() {
            return mLinkText;
        }

        public String getUrl() {
            return mUrl;
        }

        public boolean isValid() {
            return mUrl != null && mUrl.length() > 0 && mLinkText != null && mLinkText.length() > 0;
        }
    }

    /**
     * This event is broadcast via EventBus when the dialog closes.
     * It's received by the RTManager to update the active editor.
     */
    static class LinkEvent {
        private final String mFragmentTag;
        private final Link mLink;
        private final boolean mWasCancelled;

        public LinkEvent(Fragment fragment, Link link, boolean wasCancelled) {
            mFragmentTag = fragment.getTag();
            mLink = link;
            mWasCancelled = wasCancelled;
        }

        public String getFragmentTag() {
            return mFragmentTag;
        }

        public Link getLink() {
            return mLink;
        }

        public boolean wasCancelled() {
            return mWasCancelled;
        }
    }

    private static final UrlValidator sValidator = new UrlValidator(UrlValidator.ALLOW_2_SLASHES + UrlValidator.ALLOW_ALL_SCHEMES);

    private SoftReference<Activity> mActivity;

    public static LinkFragment newInstance(String linkText, String url) {
        LinkFragment fragment = new LinkFragment();
        Bundle args = new Bundle();
        args.putString("linkText", linkText);
        args.putString("url", url);
        fragment.setArguments(args);
        return fragment;
    }

    public LinkFragment() {
    }

    @Override
    public final void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = new SoftReference<Activity>(activity);
    }

    @Override
    public final void onDetach() {
        super.onDetach();
        mActivity = null;
    }

    @SuppressLint("InflateParams")
    @Override
    public final Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater li = LayoutInflater.from(mActivity.get());
        View view = li.inflate(R.layout.rte_link, null);

        // set field values
        Bundle args = getArguments();
        final String urlArg = args.getString("url");

        String tmp = "http://";
        if (urlArg != null && ! urlArg.isEmpty()) {
            try {
                tmp = URIUtil.decode(urlArg);
            } catch (URIException ignore) {}
        }
        final String url = tmp;

        final TextView urlView = ((TextView) view.findViewById(R.id.linkURL));
        if (url != null) {
            urlView.setText(url);
        }
        String linkText = args.getString("linkText");
        final TextView linkTextView = ((TextView) view.findViewById(R.id.linkText));
        if (linkText != null) {
            linkTextView.setText(linkText);
        }

        MaterialDialog.Builder builder = new MaterialDialog.Builder(mActivity.get())
                .title(R.string.rte_create_a_link)
                .customView(view)
                .cancelable(false)
                .autoDismiss(false)
                .positiveText(android.R.string.ok)
                .negativeText(android.R.string.cancel)
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        // OK button
                        String newUrl = urlView.getText().toString().trim();
                        try {
                            newUrl = URIUtil.encodeQuery(newUrl);
                        } catch (URIException ignore) {}
                        if (!requiredFieldValid(urlView) || !sValidator.isValid(newUrl)) {
                            String errorMessage = getString(R.string.rte_invalid_link, newUrl);
                            urlView.setError(errorMessage);
                        } else {
                            String linkText = linkTextView.getText().toString();
                            if (linkText.length() == 0) {
                                linkText = newUrl;
                            }
                            EventBus.getDefault().post(new LinkEvent(LinkFragment.this, new Link(linkText, newUrl), false));
                            try {
                                dialog.dismiss();
                            } catch (Exception ignore) {
                            }
                        }
                    }

                    @Override
                    public void onNegative(MaterialDialog dialog) {
                        // Cancel button
                        EventBus.getDefault().post(new LinkEvent(LinkFragment.this, new Link(null, url), true));
                    }

                    @Override
                    public void onNeutral(MaterialDialog dialog) {
                        // Remove button
                        EventBus.getDefault().post(new LinkEvent(LinkFragment.this, null, false));
                    }

                });

        // Remove button
        if (urlArg != null) {
            builder.neutralText(R.string.rte_remove_action);
        }

        return builder.build();
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);
        EventBus.getDefault().post(new LinkEvent(LinkFragment.this, null, true));
    }

    private boolean requiredFieldValid(TextView view) {
        return view.getText() != null && view.getText().length() > 0;
    }
}