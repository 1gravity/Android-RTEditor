/*
 * Copyright 2014 Emanuel Moecklin
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
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
import com.onegravity.rteditor.utils.Selection;

import java.util.ArrayList;
import java.util.List;

/*
 * Base class for all effects.
 * An "effect" is a particular type of styling to apply to the selected text in
 * a rich text editor. Most of them are wrappers around the corresponding
 * CharacterStyle classes (e.g. BulletSpan).
 * The generic type T is the sort of configuration information that the effect
 * needs -- many will be Effect<Boolean>, meaning the effect is a toggle (on or
 * off), such as boldface.
 */
abstract public class Effect<T> {

    protected static class Range {
        int start;
        int end;

        Range(int _start, int _end) {
            start = _start;
            end = _end;
        }

        @Override
        public String toString() {
            return "[" + start + ", " + end + "]";
        }
    }

    /**
     * Returns the value of this styles in the current selection.
     * Never null.
     */
    public abstract List<T> valuesInSelection(RTEditText editor, int spanType);

    /**
     * Apply this style to the selection
     */
    public abstract void applyToSelection(RTEditText editor, T value);

    /**
     * Return the Spans for this specific Effect within a a certain Spannable for a given Selection.
     * The method must be implemented by all sub classes.
     *
     * @return The returned array of spans must NEVER be null.
     */
    protected abstract Object[] getSpans(Spannable str, Selection selection);

    /**
     * @param spanType correspondends to the four flags
     *                 http://developer.android.com/reference/android/text/Spanned.html#SPAN_EXCLUSIVE_EXCLUSIVE
     *                 http://developer.android.com/reference/android/text/Spanned.html#SPAN_EXCLUSIVE_INCLUSIVE
     *                 http://developer.android.com/reference/android/text/Spanned.html#SPAN_INCLUSIVE_INCLUSIVE
     *                 http://developer.android.com/reference/android/text/Spanned.html#SPAN_INCLUSIVE_EXCLUSIVE
     */
    public boolean existsInSelection(RTEditText editor, int spanType) {
        Selection expandedSelection = getExpandedSelection(editor, spanType);
        Object[] spans = getSpans(editor.getText(), expandedSelection);
        return spans != null && spans.length > 0;
    }

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

    final protected Selection getExpandedSelection(RTEditText editor, int spanType) {
        Selection selection = new Selection(editor);
        int offsetLeft = spanType == Spanned.SPAN_INCLUSIVE_EXCLUSIVE || spanType == Spanned.SPAN_INCLUSIVE_INCLUSIVE ? 1 : 0;
        int offsetRight = spanType == Spanned.SPAN_EXCLUSIVE_INCLUSIVE || spanType == Spanned.SPAN_INCLUSIVE_INCLUSIVE ? 1 : 0;
        return selection.expandSelection(offsetLeft, offsetRight);
    }

    /**
     * Given the current selection and an "extended" search area that is potentially a superset
     * of the current selection, this method finds the range of characters for the style this Effect stands for.
     * It does also remove all spans it finds. The caller is responsible to apply the ones still needed.
     *
     * @return Range The [start, end] interval of the styles within the "extended" search area
     */
    final protected Range getProAndEpilogue(Spannable str, Selection currentSelection, Selection selection2Search) {
        int prologueStart = Integer.MAX_VALUE;
        int epilogueEnd = -1;
        for (Object span : getSpans(str, selection2Search)) {
            int spanStart = str.getSpanStart(span);
            if (spanStart < currentSelection.start()) {
                prologueStart = Math.min(prologueStart, spanStart);
            }

            int spanEnd = str.getSpanEnd(span);
            if (spanEnd > currentSelection.end()) {
                epilogueEnd = Math.max(epilogueEnd, spanEnd);
            }

            str.removeSpan(span);
        }
        return new Range(prologueStart, epilogueEnd);
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
    protected Object[] getCleanSpans(Spannable str, Selection sel) {
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