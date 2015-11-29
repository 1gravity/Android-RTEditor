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
import com.onegravity.rteditor.spans.LinkSpan;
import com.onegravity.rteditor.spans.RTSpan;
import com.onegravity.rteditor.utils.Selection;

/**
 * Links.
 */
public class LinkEffect extends Effect<String> {

    @Override
    protected Class<? extends RTSpan> getSpanClazz() {
        return LinkSpan.class;
    }

    @Override
    protected RTSpan<String> newSpan(String value) {
        return null;
    }

    @Override
    public void applyToSelection(RTEditText editor, String url) {
        Selection selection = new Selection(editor);
        Spannable str = editor.getText();

        for (RTSpan<String> span : getSpans(str, selection)) {
            str.removeSpan(span);
        }

        if (url != null) {
            // if url is Null then the link won't be set meaning existing links will be removed
            str.setSpan(new LinkSpan(url), selection.start(), selection.end(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
    }

}