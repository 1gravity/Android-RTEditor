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
import android.util.Log;

import com.onegravity.rteditor.RTEditText;
import com.onegravity.rteditor.utils.Selection;

import java.util.ArrayList;
import java.util.List;

/**
 * Base class for all binary Effect classes (on/off) like BoldEffect or
 * ItalicEffect (text is either italic or not)
 */
public class SimpleBooleanEffect<T> extends Effect<Boolean> {
    protected Class<T> mClazz;

    public SimpleBooleanEffect(Class<T> clazz) {
        mClazz = clazz;
    }

    @Override
    public List<Boolean> valuesInSelection(RTEditText editor, int spanType) {
        Selection expandedSelection = getExpandedSelection(editor, spanType);

        List<Boolean> result = new ArrayList<Boolean>();
        Object[] spans = getSpans(editor.getText(), expandedSelection);
        for (int i = 0; i < spans.length; i++) {
            result.add(true);
        }
        return result;
    }

    @Override
    public void applyToSelection(RTEditText editor, Boolean enable) {
        Selection selection = new Selection(editor);
        Spannable str = editor.getText();

        // we expand the selection to "catch" identical leading and trailing styles
        Selection expandedSelection = selection.expandSelection(1, 1);

        // remove all identical spans we find within the expandedSelection
        // and find the start resp. the end if the leading and trailing spans
        Range proAndEpi = getProAndEpilogue(str, selection, expandedSelection);

        try {
            if (enable) {
                // if the style is enabled add it to the selection (add the leading and trailing spans too if there are any)
                int start = Math.min(proAndEpi.start, selection.start());
                int end = Math.max(proAndEpi.end, selection.end());
                str.setSpan(mClazz.newInstance(), start, end,
                        start == end ? Spanned.SPAN_INCLUSIVE_INCLUSIVE : Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
            } else {
                if (proAndEpi.start < selection.start()) {
                    // if there was a leading span we have to set it again as it was removed above
                    str.setSpan(mClazz.newInstance(), proAndEpi.start, selection.start(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                }

                if (proAndEpi.end > selection.end()) {
                    // if there was a trailing span we have to set it again as it was removed above
                    str.setSpan(mClazz.newInstance(), selection.end(), proAndEpi.end, Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
                }
            }
        } catch (IllegalAccessException e) {
            Log.e(getClass().getSimpleName(), "Exception instantiating " + getClass().getSimpleName(), e);
        } catch (InstantiationException e) {
            Log.e(getClass().getSimpleName(), "Exception instantiating " + getClass().getSimpleName(), e);
        }
    }

    @Override
    protected Object[] getSpans(Spannable str, Selection selection) {
        return str.getSpans(selection.start(), selection.end(), mClazz);
    }

}