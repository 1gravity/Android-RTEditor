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

import com.onegravity.rteditor.RTEditText;
import com.onegravity.rteditor.spans.RTSpan;
import com.onegravity.rteditor.utils.Selection;

/**
 * ParagraphEffect are always applied to whole paragraphs, like bullet points or alignment.
 *
 * If we apply ParagraphEffects we need to call the cleanupParagraphs() afterwards!
 */
abstract class ParagraphEffect<V, C extends RTSpan<V>> extends Effect<V, C> {

    @Override
    final protected SpanCollector<V> newSpanCollector(Class<? extends RTSpan<V>> spanClazz) {
        return new ParagraphSpanCollector<V>(spanClazz);
    }

    /**
     * @return the start and end of the paragraph(s) encompassing the current selection because
     *         ParagraphEffects always operate on whole paragraphs.
     */
    @Override
    final protected Selection getSelection(RTEditText editor) {
        return editor.getParagraphsInSelection();
    }

    @Override
    public final void applyToSelection(RTEditText editor, V value) {
        Selection selection = new Selection(editor);
        applyToSelection(editor, selection, value);
        Effects.cleanupParagraphs(editor);
    }

    public abstract void applyToSelection(RTEditText editor, Selection selectedParagraphs, V alignment);

}
