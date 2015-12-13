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

import android.text.Layout;
import android.text.Layout.Alignment;
import android.text.Spannable;

import com.onegravity.rteditor.RTEditText;
import com.onegravity.rteditor.spans.AlignmentSpan;
import com.onegravity.rteditor.spans.RTSpan;
import com.onegravity.rteditor.utils.Helper;
import com.onegravity.rteditor.utils.Paragraph;
import com.onegravity.rteditor.utils.Selection;

import java.util.ArrayList;
import java.util.List;

/**
 * Left, Center, Right alignment.
 * <p>
 * AlignmentSpans are always applied to whole paragraphs and each paragraphs gets its "own" AlignmentSpan (1:1).
 * Editing might violate this rule (deleting a line feed merges two paragraphs).
 * Each call to applyToSelection will again make sure that each paragraph has again its own AlignmentSpan
 * (call applyToSelection(RTEditText, null, null) and all will be good again).
 */
public class AlignmentEffect extends Effect<Layout.Alignment, AlignmentSpan> implements ParagraphEffect {

    private ParagraphSpanProcessor<Layout.Alignment> mSpans2Process = new ParagraphSpanProcessor();

    @Override
    public RTSpan<Alignment> newSpan(Alignment value) {
        return null;
    }

    @Override
    public void applyToSelection(RTEditText editor, Layout.Alignment alignment) {
        Selection selection = new Selection(editor);
        applyToSelection(editor, selection, alignment);
    }

    public void applyToSelection(RTEditText editor, Selection selectedParagraphs, Layout.Alignment alignment) {
        final Spannable str = editor.getText();

        List<ParagraphSpanProcessor> spans2Process = new ArrayList<ParagraphSpanProcessor>();

        for (Paragraph paragraph : editor.getParagraphs()) {
            // find existing alignment spans for this paragraph
            List<RTSpan<Layout.Alignment>> existingSpans = getSpans(str, paragraph, SpanCollectMode.SPAN_FLAGS);
            boolean hasExistingSpans = !existingSpans.isEmpty();
            if (hasExistingSpans)
                for (RTSpan<Layout.Alignment> span : existingSpans) {
                    mSpans2Process.addParagraphSpan(span, paragraph, true);
                }

            // if the paragraph is selected then we sure have an alignment
            Alignment newAlignment = paragraph.isSelected(selectedParagraphs) ? alignment :
                    hasExistingSpans ? existingSpans.get(0).getValue() : null;

            if (newAlignment != null) {
                boolean isRTL = Helper.isRTL(str, paragraph.start(), paragraph.end());
                mSpans2Process.addParagraphSpan(new AlignmentSpan(newAlignment, isRTL), paragraph, false);
            }
        }

        // add or remove spans
        mSpans2Process.process(str);
    }

}
