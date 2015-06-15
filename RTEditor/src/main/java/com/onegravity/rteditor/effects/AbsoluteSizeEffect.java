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
import android.text.style.AbsoluteSizeSpan;

import com.onegravity.rteditor.RTEditText;
import com.onegravity.rteditor.utils.Selection;

import java.util.ArrayList;
import java.util.List;

/**
 * Text Size
 */
public class AbsoluteSizeEffect extends Effect<Integer> {

    public static boolean equalsStyle(AbsoluteSizeSpan s1, AbsoluteSizeSpan s2) {
        if (s1 != null && s2 != null) {
            return s1.getSize() == s2.getSize();
        }
        return false;
    }

    @Override
    public List<Integer> valuesInSelection(RTEditText editor, int spanType) {
        Selection expandedSelection = getExpandedSelection(editor, spanType);

        List<Integer> result = new ArrayList<Integer>();
        for (AbsoluteSizeSpan span : getSpans(editor.getText(), expandedSelection)) {
            result.add(span.getSize());
        }
        return result;
    }

    /**
     * @param size If the size is Null then all AbsoluteSizeSpan will be removed
     */
    @Override
    public void applyToSelection(RTEditText editor, Integer size) {
        Selection selection = new Selection(editor);
        Spannable str = editor.getText();

        for (AbsoluteSizeSpan span : getSpans(str, selection)) {
            int spanStart = str.getSpanStart(span);
            if (spanStart < selection.start()) {
                str.setSpan(new AbsoluteSizeSpan(span.getSize()), spanStart, selection.start(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
            int spanEnd = str.getSpanEnd(span);
            if (spanEnd > selection.end()) {
                str.setSpan(new AbsoluteSizeSpan(span.getSize()), selection.end() + 1, spanEnd, Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
            }
            str.removeSpan(span);
        }

        if (size != null) {
            str.setSpan(new AbsoluteSizeSpan(size), selection.start(), selection.end(),
                    selection.start() == selection.end() ? Spanned.SPAN_INCLUSIVE_INCLUSIVE : Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
        }
    }

    @Override
    protected AbsoluteSizeSpan[] getSpans(Spannable str, Selection selection) {
        return str.getSpans(selection.start(), selection.end(), AbsoluteSizeSpan.class);
    }

}