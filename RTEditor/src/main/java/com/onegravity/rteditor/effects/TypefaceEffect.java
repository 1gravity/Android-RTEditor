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
import android.text.style.TypefaceSpan;

import com.onegravity.rteditor.RTEditText;
import com.onegravity.rteditor.utils.Selection;

import java.util.ArrayList;
import java.util.List;

/**
 * Currently not used
 */
public class TypefaceEffect extends Effect<String> {

    @Override
    public List<String> valuesInSelection(RTEditText editor, int spanType) {
        Selection expandedSelection = getExpandedSelection(editor, spanType);

        List<String> result = new ArrayList<String>();
        for (TypefaceSpan span : getSpans(editor.getText(), expandedSelection)) {
            result.add(span.getFamily());
        }
        return result;
    }

    @Override
    public void applyToSelection(RTEditText editor, String family) {
        applyToSpannable(editor.getText(), new Selection(editor), family);
    }

    void applyToSpannable(Spannable str, Selection selection, String family) {
        int prologueStart = Integer.MAX_VALUE;
        int epilogueEnd = -1;
        String oldFamily = null;

        for (TypefaceSpan span : getSpans(str, selection)) {
            int spanStart = str.getSpanStart(span);

            if (spanStart < selection.start()) {
                prologueStart = Math.min(prologueStart, spanStart);
                oldFamily = span.getFamily();
            }

            int spanEnd = str.getSpanEnd(span);

            if (spanEnd > selection.end()) {
                epilogueEnd = Math.max(epilogueEnd, spanEnd);
                oldFamily = span.getFamily();
            }

            str.removeSpan(span);
        }

        if (family != null) {
            str.setSpan(new TypefaceSpan(family), selection.start(), selection.end(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        if (prologueStart < Integer.MAX_VALUE) {
            str.setSpan(new TypefaceSpan(oldFamily), prologueStart, selection.start(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        if (epilogueEnd > -1) {
            str.setSpan(new TypefaceSpan(oldFamily), selection.end(), epilogueEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
    }

    @Override
    protected TypefaceSpan[] getSpans(Spannable str, Selection selection) {
        return str.getSpans(selection.start(), selection.end(), TypefaceSpan.class);
    }
}