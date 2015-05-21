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

import java.util.ArrayList;
import java.util.List;

import android.text.Spannable;

import com.onegravity.rteditor.RTEditText;
import com.onegravity.rteditor.spans.ParagraphSpan;
import com.onegravity.rteditor.utils.Helper;
import com.onegravity.rteditor.utils.Paragraph;
import com.onegravity.rteditor.utils.Selection;

/**
 * Text indentation.
 * 
 * LeadingMarginSpans are always applied to whole paragraphs and each paragraphs gets its "own" LeadingMarginSpan (1:1).
 * Editing might violate this rule (deleting a line feed merges two paragraphs).
 * Each call to applyToSelection will again make sure that each paragraph has again its own LeadingMarginSpan
 * (call applyToSelection(RTEditText, null, null) and all will be good again).
 * 
 * The Boolean parameter is used to increment, decrement the indentation   
 */
public abstract class LeadingMarginEffect extends Effect<Boolean> implements ParagraphEffect {

	private static final int LEADING_MARGIN_INCREMENT = 28;
	private static int sLeadingMargingIncrement = -1;
	
	public static int getLeadingMargingIncrement() {
		if (sLeadingMargingIncrement == -1) {
			float density = Helper.getDisplayDensity();
			sLeadingMargingIncrement = Math.round( LEADING_MARGIN_INCREMENT * density ); 
		}
		return sLeadingMargingIncrement;
	}

	@Override
	public List<Boolean> valuesInSelection(RTEditText editor, int spanType) {
		List<Boolean> result = new ArrayList<Boolean>();

		Selection expandedSelection = editor.getParagraphsInSelection();
		if (expandedSelection != null) {
			for (@SuppressWarnings("unused") Object span : getSpans(editor.getText(), expandedSelection)) {
				result.add(true);
			}
		}

		return result;
	}

	@Override
	public void applyToSelection(RTEditText editor, Boolean value) {
		Selection selection = new Selection(editor);
		applyToSelection(editor, selection, value);
	}

	public abstract void applyToSelection(RTEditText editor, Selection selectedParagraphs, Boolean value);

	protected void findSpans2Remove(Spannable str, Paragraph paragraph, List<ParagraphSpan> spans2Remove) {
		Object[] spans = getSpans(str, paragraph);
		if (spans != null) {
			for (Object span : spans) {
				spans2Remove.add( new ParagraphSpan(span, paragraph, true) );
			}
		}
	}
}