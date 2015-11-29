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

package com.onegravity.rteditor.spans;

import android.os.Parcel;
import android.text.Layout;

/**
 * Implementation for an alignment span (left, center, right alignment)
 */
public class AlignmentSpan extends android.text.style.AlignmentSpan.Standard implements RTSpan<Layout.Alignment> {

    public AlignmentSpan(Layout.Alignment align) {
        super(align);
    }

    public AlignmentSpan(Parcel src) {
        super(src);
    }

    @Override
    public Layout.Alignment getValue() {
       return super.getAlignment();
    }

}
