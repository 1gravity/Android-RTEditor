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

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Base class for all effects.
 * An "effect" is a particular type of styling to apply to the selected text in
 * a rich text editor. Most of them are wrappers around the corresponding
 * CharacterStyle or ParagraphStyle classes (e.g. BulletSpan).
 *
 * @param <V> is the sort of configuration information that the effect needs.
 *           Many will be Effect<Boolean>, meaning the effect is a toggle (on or off),
 *           such as boldface.
 *
 * @param <C> is the span class extending RTSpan<V>
 */
abstract public class Effect<V extends Object, C extends RTSpan<V>> {

    /*
     * Spanned.getSpans(int, int, Class) unfortunately doesn't respect the mark/point flags.
     * The SpanCollector allows us to implement and use different getSpan methods depending on the
     * span flags, the position of a span, the selection (last line, empty lines...) and the type of
     * the Effect.
     */
    private SpanCollector<V> mSpanCollector;

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
     * Equivalent to the Spanned.getSpans(int, int, Class<T>) method.
     * Return the markup objects (spans) attached to the specified slice of this Spannable.
     * The type of the spans is defined in the SpanCollector.
     *
     * @param str The Spannable to search for spans.
     * @param selection The selection within the Spannable to search for spans.
     * @param mode
     *
     * @return the list of spans in this Spannable/Selection, never Null
     */
    final public List<RTSpan<V>> getSpans(Spannable str, Selection selection, SpanCollectMode mode) {
        return getSpanCollector().getSpans(str, selection, mode);
    }

    /**
     * Lazy initialize the SpanCollector
     */
    @SuppressWarnings("unchecked")
    private SpanCollector<V> getSpanCollector() {
        if (mSpanCollector == null) {
            Type[] types = ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments();
            Class<? extends RTSpan<V>> spanClazz = (Class<? extends RTSpan<V>>) types[types.length - 1];

            if (this instanceof ParagraphEffect) {
                mSpanCollector = new ParagraphSpanCollector<V>(spanClazz);
            } else {
                mSpanCollector = new CharacterSpanCollector<V>(spanClazz);
            }
        }

        return mSpanCollector;
    }

    /**
     * Check whether the effect exists in the currently selected text of the active RTEditText.
     *
     * @param editor The RTEditText we want the check.
     *
     * @return True if the effect exists in the current selection, False otherwise.
     */
    final public boolean existsInSelection(RTEditText editor) {
        Selection selection = getSelection(editor);
        List<RTSpan<V>> spans = getSpans(editor.getText(), selection, SpanCollectMode.SPAN_FLAGS);
        return ! spans.isEmpty();
    }

    /**
     * Returns the value of this effect in the current selection.
     *
     * @return The returned list, must NEVER be null.
     */
    final public List<V> valuesInSelection(RTEditText editor) {
        List<V> result = new ArrayList<V>();

        Selection selection = getSelection(editor);
        for (RTSpan<V> span : getSpans(editor.getText(), selection, SpanCollectMode.SPAN_FLAGS)) {
            result.add( span.getValue() );
        }

        return result;
    }

    /**
     * Remove all effects of this type from the current selection.
     * If the selection is empty (cursor) the formatting for the whole text is removed.
     */
    final public void clearFormattingInSelection(RTEditText editor) {
        Spannable text = editor.getText();
        Selection selection = new Selection(editor);

        // if no selection --> select the whole text
        // otherwise use the getSelection method which might be overridden by sub classes
        selection = selection.isEmpty() ? new Selection(0, text.length()) : getSelection(editor);

        for (Object span : getSpans(text, selection, SpanCollectMode.EXACT)) {
            editor.getText().removeSpan(span);
        }
    }

    /**
     * Apply this effect to the selection.
     * If value is Null then the effect will be removed from the current selection.
     *
     * @param editor The editor to apply the effect to (current selection)
     * @param value The value to apply (depends on the Effect)
     */
    public void applyToSelection(RTEditText editor, V value) {
        Selection selection = getSelection(editor);
        Spannable str = editor.getText();

        // we expand the selection to "catch" identical leading and trailing styles
        Selection expandedSelection = selection.expand(1, 1);
        for (RTSpan<V> span : getSpans(str, expandedSelection, SpanCollectMode.EXACT)) {
            boolean sameSpan = span.getValue().equals( value );
            int spanStart = str.getSpanStart(span);
            if (spanStart < selection.start()) {
                // process preceding spans
                if (sameSpan) {
                    selection.offset(selection.start() - spanStart, 0);
                }
                else {
                    str.setSpan(newSpan(span.getValue()), spanStart, selection.start(), Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
                }
            }
            int spanEnd = str.getSpanEnd(span);
            if (spanEnd > selection.end()) {
                // process succeeding spans
                if (sameSpan) {
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
     * @return the Selection for the specified RTEditText.
     * For ParagraphEffect return the start and end of the paragraph(s) encompassing the current
     * selection because ParagraphEffects always operate on whole paragraphs.
     */
    final protected Selection getSelection(RTEditText editor) {
        if (this instanceof ParagraphEffect) {
            return editor.getParagraphsInSelection();
        } else {
            return new Selection(editor);
        }
    }
}
