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

import com.onegravity.rteditor.utils.Selection;

public class CharacterSpanCollector<V> extends SpanCollector<V> {

    public CharacterSpanCollector(Class spanClazz) {
        super(spanClazz);
    }

    @Override
    protected boolean isAttached(Spannable str, Selection sel, Object span, SpanCollectMode mode) {
        int spanStart = str.getSpanStart(span);
        int spanEnd = str.getSpanEnd(span);
        int selStart = sel.start();
        int selEnd = sel.end();

        // [start, end] define the intersection of span and selection
        int start = Math.max(spanStart, selStart);
        int end = Math.min(spanEnd, selEnd);

        if (start > end) {
            // 1) no character in common and not adjacent
            // [span]...|selection| or |selection|...[span]
            return false;
        }
        else if (start < end) {
            // 2) at least one character in common:
            // [span]
            //  [     span    ]
            //    |selection|
            //      [span]
            //            [span]
            return true;
        }
        else if ((spanStart > selStart && spanEnd < selEnd) ||   // point span within selection
                (selStart > spanStart && selEnd < spanEnd)) {    // point selection within span
            // 3) point span within selection or point selection within span (within, not adjacent)
            //    |selection|
            //        []
            //    [span     ]
            //        ||
            return true;
        }
        else if (mode == SpanCollectMode.EXACT) {
            // 4) adjacent MatchMode.EXACT
            return false;
        }
        else {
            // 5) adjacent MatchMode.SPAN_FLAGS
            int flags = str.getSpanFlags(span) & Spanned.SPAN_POINT_MARK_MASK;
            if (spanEnd == selStart) {
                // 5.1) [span][selection] -> span must include at the end
                return isOneFlagSet(flags, Spanned.SPAN_EXCLUSIVE_INCLUSIVE, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
            }
            else {
                // 5.2) [selection][span] -> span must include at the start
                return isOneFlagSet(flags, Spanned.SPAN_INCLUSIVE_EXCLUSIVE, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
            }
        }

    }

}
