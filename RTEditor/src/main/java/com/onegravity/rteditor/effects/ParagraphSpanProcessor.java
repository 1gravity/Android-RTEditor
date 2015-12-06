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

package com.onegravity.rteditor.effects;

import android.text.Spannable;
import android.text.Spanned;

import com.onegravity.rteditor.spans.RTSpan;
import com.onegravity.rteditor.utils.Paragraph;

import java.util.ArrayList;

/**
 * This is a temporary container for paragraph spans and their meta information
 * that will be processed later (added to or removed from a Spannable)
 */
class ParagraphSpanProcessor<V extends Object> {

    private static class ParagraphSpan<V extends Object> {
        final Object mSpan;
        final Paragraph mParagraph;
        final boolean mRemove;

        ParagraphSpan(RTSpan<V> span, Paragraph paragraph, boolean remove) {
            mSpan = span;
            mParagraph = paragraph;
            mRemove = remove;
        }
    }

    final private ArrayList<ParagraphSpan<V>> mParagraphSpans = new ArrayList<>();

    void clear() {
        mParagraphSpans.clear();
    }

    void addParagraphSpan(RTSpan<V> span, Paragraph paragraph, boolean remove) {
        mParagraphSpans.add( new ParagraphSpan<V>(span, paragraph, remove));
    }

    // todo this needs some more work to make it work properly with ParagraphEffects
    void process(Spannable str) {
        for (int i = 0, len = mParagraphSpans.size(); i < len; i++) {
            ParagraphSpan paragraphSpan = mParagraphSpans.get(i);
            Object span = paragraphSpan.mSpan;
            if (paragraphSpan.mRemove) {
                str.removeSpan(span);
            } else {
                Paragraph paragraph = paragraphSpan.mParagraph;
                int start = paragraph.start();
                int end = paragraph.end();
                int flags = paragraph.isLast() && paragraph.isEmpty() ? Spanned.SPAN_INCLUSIVE_INCLUSIVE :
                        Spanned.SPAN_INCLUSIVE_INCLUSIVE;
                str.setSpan(span, start, end, flags);
            }
        }
    }
}
