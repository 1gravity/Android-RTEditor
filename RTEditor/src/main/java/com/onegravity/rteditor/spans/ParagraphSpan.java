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

package com.onegravity.rteditor.spans;

import android.text.Spannable;
import android.text.Spanned;

import com.onegravity.rteditor.utils.Paragraph;

/**
 * This is a temporary container for paragraph spans and their meta information
 * that will be processed later (added to or removed from a Spannable)
 */
public class ParagraphSpan {
    final private Object mWhat;
    final private Paragraph mParagraph;
    final private boolean mRemove;

    public ParagraphSpan(Object what, Paragraph paragraph, boolean remove) {
        mWhat = what;
        mParagraph = paragraph;
        mRemove = remove;
    }

    public void process(Spannable str) {
        if (mRemove) {
            str.removeSpan(mWhat);
        } else {
            int start = mParagraph.start();
            int end = mParagraph.end();
            int flags = mParagraph.isEmpty() || mParagraph.isLast() ? Spanned.SPAN_INCLUSIVE_INCLUSIVE : Spanned.SPAN_INCLUSIVE_EXCLUSIVE;
            str.setSpan(mWhat, start, end, flags);
        }
    }
}