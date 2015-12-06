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

import com.onegravity.rteditor.RTEditText;
import com.onegravity.rteditor.spans.IndentationSpan;
import com.onegravity.rteditor.spans.RTSpan;
import com.onegravity.rteditor.utils.Paragraph;
import com.onegravity.rteditor.utils.Selection;

import java.util.ArrayList;
import java.util.List;

/**
 * Text indentation.
 * <p>
 * LeadingMarginSpans are always applied to whole paragraphs and each paragraphs gets its "own" LeadingMarginSpan (1:1).
 * Editing might violate this rule (deleting a line feed merges two paragraphs).
 * Each call to applyToSelection will make sure that each paragraph has again its own LeadingMarginSpan
 * (call applyToSelection(RTEditText, null, null) and all will be good again).
 * <p>
 * The Boolean parameter is used to increment, decrement the indentation
 */
public class IndentationEffect extends Effect<Integer, IndentationSpan> implements ParagraphEffect {

    private ParagraphSpanProcessor<Integer> mSpans2Process = new ParagraphSpanProcessor();

    @Override
    protected RTSpan<Integer> newSpan(Integer value) {
        return null;
    }

    @Override
    public void applyToSelection(RTEditText editor, Integer value) {
        Selection selection = new Selection(editor);
        applyToSelection(editor, selection, value);
    }

    public void applyToSelection(RTEditText editor, Selection selectedParagraphs, Integer increment) {
        final Spannable str = editor.getText();

        List<ParagraphSpanProcessor> spans2Process = new ArrayList<ParagraphSpanProcessor>();

        for (Paragraph paragraph : editor.getParagraphs()) {
            int indentation = 0;

            // find existing indentations/spans for this paragraph
            List<RTSpan<Integer>> existingSpans = getSpans(str, paragraph, SpanCollectMode.SPAN_FLAGS);
            boolean hasExistingSpans = ! existingSpans.isEmpty();
            if (hasExistingSpans) {
                for (RTSpan<Integer> span : existingSpans) {
                    mSpans2Process.addParagraphSpan(span, paragraph, true);
                    indentation += span.getValue();
                }
            }

            // if the paragraph is selected inc/dec the existing indentation
            int incIndentation = increment == null ? 0 : increment;
            indentation += paragraph.isSelected(selectedParagraphs) ? incIndentation : 0;

            // if indentation>0 then apply a new span
            if (indentation > 0) {
                IndentationSpan leadingMarginSpan = new IndentationSpan(indentation, paragraph.isEmpty(), paragraph.isFirst(), paragraph.isLast());
                mSpans2Process.addParagraphSpan(leadingMarginSpan, paragraph, false);
            }
        }

        // add or remove spans
        mSpans2Process.process(str);
    }

}
