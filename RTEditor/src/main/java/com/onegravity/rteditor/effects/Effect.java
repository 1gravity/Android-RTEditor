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

import com.onegravity.rteditor.RTEditText;
import com.onegravity.rteditor.spans.RTSpan;
import com.onegravity.rteditor.utils.Selection;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

/**
 * Base class for all effects.
 * An "effect" is a particular type of styling to apply to the selected text in
 * a rich text editor. Most of them are wrappers around the corresponding
 * CharacterStyle classes (e.g. BulletSpan).
 *
 * @param <V> is the sort of configuration information that the effect needs.
 *           Many will be Effect<Boolean>, meaning the effect is a toggle (on or off),
 *           such as boldface.
 */
abstract public class Effect<V extends Object> {

    /**
     * Subclasses return the class of the span the effect supports.
     * This method is used to retrieve the spans in getSpans(Spannable, Selection)
     *
     * @return the class of the span this effect supports, @NonNull.
     */
    abstract protected Class<? extends RTSpan> getSpanClazz();

    /**
     * Create an RTSpan for this effect.
     * This method is used in applyToSelection(RTEditText, V).
     * If the RTSpan can't be created only with V as value parameter then applyToSelection needs
     * to be implemented by the sub class.
     *
     * @return the class of the span this effect supports. Can be Null but then the subclass has to
     * override applyToSelection(RTEditText, V)
     */
    abstract protected RTSpan<V> newSpan(V value);

    /**
     * Return the Spans for this specific Effect within a a certain Spannable for a given Selection.
     *
     * @return The returned array of spans, @NonNull.
     */
    final public RTSpan<V>[] getSpans(Spannable str, Selection selection) {
        Class<? extends RTSpan> spanClazz = getSpanClazz();
        RTSpan<V>[] result = str.getSpans(selection.start(), selection.end(), spanClazz);
        return result != null ? result : (RTSpan<V>[]) Array.newInstance(spanClazz);
    }

    /**
     * Check whether the effect exists in the currently selected text of the active RTEditText.
     *
     * @param editor The RTEditText we want the check.
     * @param spanType corresponds to the four flags
     *                 http://developer.android.com/reference/android/text/Spanned.html#SPAN_EXCLUSIVE_EXCLUSIVE
     *                 http://developer.android.com/reference/android/text/Spanned.html#SPAN_EXCLUSIVE_INCLUSIVE
     *                 http://developer.android.com/reference/android/text/Spanned.html#SPAN_INCLUSIVE_INCLUSIVE
     *                 http://developer.android.com/reference/android/text/Spanned.html#SPAN_INCLUSIVE_EXCLUSIVE
     *
     * @return True if the effect exists in the current selection, False otherwise.
     */
    final public boolean existsInSelection(RTEditText editor, int spanType) {
        Selection expandedSelection = getExpandedSelection(editor, spanType);
        if (expandedSelection != null) {
            RTSpan<V>[] spans = getSpans(editor.getText(), expandedSelection);
            return spans.length > 0;
        }

        return false;
    }

    /**
     * Returns the value of this effect in the current selection.
     *
     * @return The returned list, must NEVER be null.
     */
    public List<V> valuesInSelection(RTEditText editor, int spanType) {
        List<V> result = new ArrayList<V>();

        Selection expandedSelection = getExpandedSelection(editor, spanType);
        if (expandedSelection != null) {
            for (RTSpan<V> span : getSpans(editor.getText(), expandedSelection)) {
                result.add(span.getValue());
            }
        }

        return result;
    }

    /**
     * Apply this effect to the selection.
     * If value is Null then the effect will be removed from the current selection.
     *
     * @param editor The editor to apply the effect to (current selection)
     * @param value The value to apply (depends on the Effect)
     */
    public void applyToSelection(RTEditText editor, V value) {
        Selection selection = new Selection(editor);
        Spannable str = editor.getText();

        // we expand the selection to "catch" identical leading and trailing styles
        Selection expandedSelection = selection.expand(1, 1);
        for (RTSpan<V> span : getSpans(str, expandedSelection)) {
            boolean equalSpan = span.getValue() == value;
            int spanStart = str.getSpanStart(span);
            if (spanStart < selection.start()) {
                if (equalSpan) {
                    selection.offset(selection.start() - spanStart, 0);
                }
                else {
                    str.setSpan(newSpan(span.getValue()), spanStart, selection.start(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
            }
            int spanEnd = str.getSpanEnd(span);
            if (spanEnd > selection.end()) {
                if (equalSpan) {
                    selection.offset(0, spanEnd - selection.end());
                }
                else {
                    str.setSpan(newSpan(span.getValue()), selection.end(), spanEnd, Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
                }
            }
            str.removeSpan(span);
        }

        if (value != null) {
            RTSpan<V> newSpan = newSpan(value);
            if (newSpan != null) {
                int flags = selection.isEmpty() ? Spanned.SPAN_INCLUSIVE_INCLUSIVE : Spanned.SPAN_EXCLUSIVE_INCLUSIVE;
                str.setSpan(newSpan, selection.start(), selection.end(), flags);
            }
        }
    }

    /**
     * Remove all effects of this type from the current selection.
     */
    final public void clearFormattingInSelection(RTEditText editor) {
        Spannable text = editor.getText();
        Selection selection = new Selection(editor);
        if (selection.isEmpty()) {
            selection = new Selection(0, text.length());
        }
        for (Object span : getSpans(text, selection)) {
            editor.getText().removeSpan(span);
        }
    }

    /**
     * Expand the selection to the left and/or right depending on the spanType:
     *   http://developer.android.com/reference/android/text/Spanned.html#SPAN_EXCLUSIVE_EXCLUSIVE
     *   http://developer.android.com/reference/android/text/Spanned.html#SPAN_EXCLUSIVE_INCLUSIVE
     *   http://developer.android.com/reference/android/text/Spanned.html#SPAN_INCLUSIVE_INCLUSIVE
     *   http://developer.android.com/reference/android/text/Spanned.html#SPAN_INCLUSIVE_EXCLUSIVE
     */
    protected Selection getExpandedSelection(RTEditText editor, int spanType) {
        Selection selection = new Selection(editor);
        int offsetLeft = spanType == Spanned.SPAN_INCLUSIVE_EXCLUSIVE || spanType == Spanned.SPAN_INCLUSIVE_INCLUSIVE ? 1 : 0;
        int offsetRight = spanType == Spanned.SPAN_EXCLUSIVE_INCLUSIVE || spanType == Spanned.SPAN_INCLUSIVE_INCLUSIVE ? 1 : 0;
        return selection.expand(offsetLeft, offsetRight);
    }

    /**
     * Spanned() unfortunately doesn't respects the mark/point flags (SPAN_EXCLUSIVE_EXCLUSIVE etc.).
     * If a selection starts at the end or ends at the start of a span it will be returned by getSpans()
     * regardless whether the span would really be applied to that selection if we were to enter text.
     * E.g. [abc] with SPAN_EXCLUSIVE_EXCLUSIVE would not expand to a character entered at the end: [abc]d,
     * nevertheless getSpans(3, 3, type) would still return the span.
     * <p>
     * This method returns just the spans that will affect the selection if text is entered.
     */
    final protected Object[] getCleanSpans(Spannable str, Selection sel) {
        List<Object> spans = new ArrayList<Object>();
        if (spans != null) {
            for (Object span : getSpans(str, sel)) {
                if (isCleanSpan(str, sel, span)) {
                    spans.add(span);
                }
            }
        }
        return spans.toArray();
    }

    private boolean isCleanSpan(Spannable str, Selection sel, Object span) {
        int spanStart = str.getSpanStart(span);
        int spanEnd = str.getSpanEnd(span);
        int start = Math.max(spanStart, sel.start());
        int end = Math.min(spanEnd, sel.end());

        if (start < end) {
            // 1) at least one character in common:
            // span...
            // [  [xx]    ]
            //    selection
            return true;
        } else if (start > end) {
            // 2) no character in common and not adjunctive
            // [span]...[selection] or [selection]...[span]
            return false;
        } else {
            // 3) adjunctive
            int flags = str.getSpanFlags(span) & Spanned.SPAN_POINT_MARK_MASK;
            if (spanEnd == sel.start()) {
                // [span][selection] -> span must include at the end
                return ((flags & Spanned.SPAN_EXCLUSIVE_INCLUSIVE) == Spanned.SPAN_EXCLUSIVE_INCLUSIVE) ||
                        ((flags & Spanned.SPAN_INCLUSIVE_INCLUSIVE) == Spanned.SPAN_INCLUSIVE_INCLUSIVE);
            } else {
                // [selection][span] -> span must include at the start
                return ((flags & Spanned.SPAN_INCLUSIVE_EXCLUSIVE) == Spanned.SPAN_INCLUSIVE_EXCLUSIVE) ||
                        ((flags & Spanned.SPAN_INCLUSIVE_INCLUSIVE) == Spanned.SPAN_INCLUSIVE_INCLUSIVE);
            }

        }

    }

}
