/*
 * Copyright (C) 2015-2017 Emanuel Moecklin
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

package com.onegravity.rteditor;

import android.text.Selection;
import android.text.Spannable;
import android.text.method.ArrowKeyMovementMethod;
import android.text.method.MovementMethod;
import android.text.style.ClickableSpan;
import android.view.MotionEvent;
import android.widget.TextView;

/**
 * ArrowKeyMovementMethod does support selection of text but not the clicking of links.
 * LinkMovementMethod does support clicking of links but not the selection of text.
 * This class adds the link clicking to the ArrowKeyMovementMethod.
 * We basically take the LinkMovementMethod onTouchEvent code and remove the line
 * Selection.removeSelection(buffer);
 * which de-selects all text when no link was found.
 */
public class RTEditorMovementMethod extends ArrowKeyMovementMethod {

    private static RTEditorMovementMethod sInstance;

    public static synchronized MovementMethod getInstance() {
        if (sInstance == null) {
            sInstance = new RTEditorMovementMethod();
        }
        return sInstance;
    }

    @Override
    public boolean onTouchEvent(TextView widget, Spannable buffer, MotionEvent event) {
        int action = event.getAction();

        if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_DOWN) {

            int index = widget.getOffsetForPosition(event.getX(), event.getY());
            if (index != -1) {
                ClickableSpan[] link = buffer.getSpans(index, index, ClickableSpan.class);
                if (link.length != 0) {
                    if (action == MotionEvent.ACTION_UP) {
                        link[0].onClick(widget);
                    } else {
                        Selection.setSelection(buffer, buffer.getSpanStart(link[0]), buffer.getSpanEnd(link[0]));
                    }
                    return true;
                }
            }
        }

        return super.onTouchEvent(widget, buffer, event);
    }
}