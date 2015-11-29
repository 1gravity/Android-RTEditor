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

import com.onegravity.rteditor.spans.AbsoluteSizeSpan;
import com.onegravity.rteditor.spans.RTSpan;

/**
 * Text Size
 */
public class AbsoluteSizeEffect extends Effect<Integer> {

    @Override
    protected Class<? extends RTSpan> getSpanClazz() {
        return AbsoluteSizeSpan.class;
    }

    /**
     * @return If the value is Null, 0 or negative then return Null -> remove all AbsoluteSizeSpan.
     */
    @Override
    protected RTSpan<Integer> newSpan(Integer value) {
        return value == null || value <= 0 ? null : new AbsoluteSizeSpan(value);
    }

}
