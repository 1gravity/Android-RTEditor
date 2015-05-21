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
import android.text.Spanned;
import android.text.style.BackgroundColorSpan;

import com.onegravity.rteditor.RTEditText;
import com.onegravity.rteditor.utils.Selection;

/**
 * Background color
 */
public class BackgroundColorEffect extends Effect<Integer> {

	@Override
	public List<Integer> valuesInSelection(RTEditText editor, int spanType) {
		Selection expandedSelection = getExpandedSelection(editor, spanType);

		List<Integer> result = new ArrayList<Integer>();
		for (BackgroundColorSpan span : getSpans(editor.getText(), expandedSelection)) {
			result.add(span.getBackgroundColor());
		}
		return result;
	}

	/**
	 * @param color If the color is Null then the background color will be removed
	 */
	public void applyToSelection(RTEditText editor, Integer color) {
		Selection selection = new Selection(editor);
		Spannable str = editor.getText();

		for (BackgroundColorSpan span : getSpans(str, selection)) {
			int spanStart = str.getSpanStart(span);
			if (spanStart < selection.start()) {
				str.setSpan(new BackgroundColorSpan(span.getBackgroundColor()), spanStart, selection.start(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
			}
			int spanEnd = str.getSpanEnd(span);
			if (spanEnd > selection.end()) {
				str.setSpan(new BackgroundColorSpan(span.getBackgroundColor()), selection.end()+1, spanEnd, Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
			}
			str.removeSpan(span);
		}

		if (color != null) {
			// if the style is enabled add it to the selection (add the leading and trailing spans too if there are any)
			str.setSpan(new BackgroundColorSpan(color), selection.start(), selection.end(),
						selection.start()==selection.end() ? Spanned.SPAN_INCLUSIVE_INCLUSIVE : Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
		}
	}
	
	@Override
	protected BackgroundColorSpan[] getSpans(Spannable str, Selection selection) {
		return str.getSpans(selection.start(), selection.end(), BackgroundColorSpan.class);
	}
	
}