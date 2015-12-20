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
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.onegravity.rteditor.utils.Helper;

import java.lang.ref.SoftReference;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.greenrobot.event.EventBus;

/**
 * A DialogFragment to add, modify or remove links from Spanned text.
 */
public class LinkFragment extends DialogFragment {

    private static final String LINK_ADDRESS = "link_address";
    private static final String LINK_TEXT = "link_text";

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

    /**
     * Use this to find the URI scheme
     */
    Pattern SCHEME = Pattern.compile("^([a-zA-Z][a-zA-Z\\+\\.\\-]*):.*$");

    /**
     *  Check the most important schemes according to:
     *  http://www.iana.org/assignments/uri-schemes/uri-schemes.xhtml
     */
    private static Set<String> sSchemes = new HashSet<String>();
    static {
        sSchemes.add("attachment");
        sSchemes.add("bitcoin");
        sSchemes.add("callto");
        sSchemes.add("cid");
        sSchemes.add("data");
        sSchemes.add("dns");
        sSchemes.add("file");
        sSchemes.add("ftp");
        sSchemes.add("git");
        sSchemes.add("http");
        sSchemes.add("https");
        sSchemes.add("imap");
        sSchemes.add("ldap");
        sSchemes.add("ldaps");
        sSchemes.add("mailto");
        sSchemes.add("market");
        sSchemes.add("news");
        sSchemes.add("nfs");
        sSchemes.add("nntp");
        sSchemes.add("pop");
        sSchemes.add("proxy");
        sSchemes.add("sftp");
        sSchemes.add("skype");
        sSchemes.add("smb");
        sSchemes.add("sms");
        sSchemes.add("smtp");
        sSchemes.add("snmp");
        sSchemes.add("ssh");
        sSchemes.add("svn");
        sSchemes.add("tel");
        sSchemes.add("telnet");
    }

    private SoftReference<Activity> mActivity;

    public static LinkFragment newInstance(String linkText, String url) {
        LinkFragment fragment = new LinkFragment();
        Bundle args = new Bundle();
        args.putString(LINK_TEXT, linkText);
        args.putString(LINK_ADDRESS, url);
        fragment.setArguments(args);
        return fragment;
    }

    public LinkFragment() {}

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

        Bundle args = getArguments();

        // link address
        final String address = args.getString(LINK_ADDRESS);
        String tmp = "";
        if (address != null && ! address.isEmpty()) {
            try {
                tmp = Uri.parse( Helper.decodeQuery(address) ).toString();
            } catch (Exception ignore) {}
        }
        final String url = tmp;
        final TextView addressView = ((TextView) view.findViewById(R.id.linkURL));
        if (url != null) {
            addressView.setText(url);
        }

        // link text
        String linkText = args.getString(LINK_TEXT);
        final TextView textView = ((TextView) view.findViewById(R.id.linkText));
        if (linkText != null) {
            textView.setText(linkText);
        }

        MaterialDialog.Builder builder = new MaterialDialog.Builder(mActivity.get())
                .title(R.string.rte_create_a_link)
                .customView(view, true)
                .cancelable(false)
                .autoDismiss(false)
                .positiveText(android.R.string.ok)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        // OK button
                        validate(dialog, addressView, textView);
                    }
                })
                .negativeText(android.R.string.cancel)
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        // Cancel button
                        EventBus.getDefault().post(new LinkEvent(LinkFragment.this, new Link(null, url), true));
                    }
                });

        if (address != null) {
            builder.neutralText(R.string.rte_remove_action)
            .onNeutral(new MaterialDialog.SingleButtonCallback() {
                @Override
                public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction) {
                    // Remove button
                    EventBus.getDefault().post(new LinkEvent(LinkFragment.this, null, false));
                }
            });
        }

        return builder.build();
    }

    private void validate(Dialog dialog, TextView addressView, TextView textView) {
        // retrieve link address and do some cleanup
        final String address = addressView.getText().toString().trim();

        //boolean isEmail = sEmailValidator.isValid(address);
        //boolean isUrl = sUrlValidator.isValid(address);
        if (requiredFieldValid(addressView) && schemeIsValid(address)) {
            // valid url or email address

            // encode address
            String newAddress = Helper.encodeQuery(address);

            // use the original address text as link text if the user didn't enter anything
            String linkText = textView.getText().toString();
            if (linkText.length() == 0) {
                linkText = address;
            }

            EventBus.getDefault().post(new LinkEvent(LinkFragment.this, new Link(linkText, newAddress), false));
            try { dialog.dismiss(); } catch (Exception ignore) {}
        } else {
            // invalid Uri
            String errorMessage = getString(R.string.rte_invalid_link, address);
            addressView.setError(errorMessage);
        }
    }

    private boolean schemeIsValid(String address) {
        String scheme = getScheme(address);
        return scheme == null || sSchemes.contains(scheme);
    }

    private String getScheme(String address) {
        String lcAddress = address.toLowerCase(Locale.getDefault());
        Matcher m = SCHEME.matcher(lcAddress);
        if (m.find()) {
            return m.group(1);
        }
        return null;
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