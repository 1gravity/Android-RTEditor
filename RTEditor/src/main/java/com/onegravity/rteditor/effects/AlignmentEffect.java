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

import android.text.Layout;
import android.text.Layout.Alignment;
import android.text.Spannable;
import android.text.style.AlignmentSpan;

import com.onegravity.rteditor.RTEditText;
import com.onegravity.rteditor.spans.ParagraphSpan;
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
public class AlignmentEffect extends Effect<Layout.Alignment> implements ParagraphEffect {

    @Override
    public List<Alignment> valuesInSelection(RTEditText editor, int spanType) {
        List<Alignment> result = new ArrayList<Alignment>();

        Selection expandedSelection = editor.getParagraphsInSelection();
        if (expandedSelection != null) {
            for (AlignmentSpan.Standard span : getSpans(editor.getText(), expandedSelection)) {
                result.add(span.getAlignment());
            }
        }

        return result;
    }

    @Override
    public void applyToSelection(RTEditText editor, Layout.Alignment alignment) {
        Selection selection = new Selection(editor);
        applyToSelection(editor, selection, alignment);
    }

    public void applyToSelection(RTEditText editor, Selection selectedParagraphs, Layout.Alignment alignment) {
        final Spannable str = editor.getText();

        List<ParagraphSpan> spans2Process = new ArrayList<ParagraphSpan>();

        for (Paragraph paragraph : editor.getParagraphs()) {
            // find existing alignment spans for this paragraph
            Object[] existingSpans = getCleanSpans(str, paragraph);
            boolean hasExistingSpans = existingSpans != null && existingSpans.length > 0;
            if (hasExistingSpans)
                for (Object span : existingSpans) {
                    spans2Process.add(new ParagraphSpan(span, paragraph, true));
                }

            // if the paragraph is selected then we sure have an alignment
            Alignment newAlignment = paragraph.isSelected(selectedParagraphs) ? alignment :
                    hasExistingSpans ? ((AlignmentSpan.Standard) existingSpans[0]).getAlignment() : null;

            if (newAlignment != null) {
                spans2Process.add(new ParagraphSpan(new AlignmentSpan.Standard(newAlignment), paragraph, false));
            }
        }

        // add or remove spans
        for (final ParagraphSpan spanDef : spans2Process) {
            spanDef.process(str);
        }
    }

    @Override
    protected AlignmentSpan.Standard[] getSpans(Spannable str, Selection selection) {
        return str.getSpans(selection.start(), selection.end(), AlignmentSpan.Standard.class);
    }

}