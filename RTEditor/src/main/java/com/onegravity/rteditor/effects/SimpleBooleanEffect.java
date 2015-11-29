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

import android.util.Log;

import com.onegravity.rteditor.spans.RTSpan;

/**
 * Base class for all binary Effect classes (on/off) like BoldEffect or
 * ItalicEffect (text is either italic or not)
 */
public class SimpleBooleanEffect extends Effect<Boolean> {
    private Class<? extends RTSpan> mSpanClazz;

    public SimpleBooleanEffect(Class<? extends RTSpan> spanClazz) {
        mSpanClazz = spanClazz;
    }

    @Override
    final protected Class<? extends RTSpan> getSpanClazz() {
        return mSpanClazz;
    }

    /**
     * @return If the value is Null or False then return Null -> remove all spans.
     */
    @Override
    final protected RTSpan<Boolean> newSpan(Boolean value) {
        try {
            return value ? mSpanClazz.newInstance() : null;
        } catch (IllegalAccessException e) {
            Log.e(getClass().getSimpleName(), "Exception instantiating " + getClass().getSimpleName(), e);
        } catch (InstantiationException e) {
            Log.e(getClass().getSimpleName(), "Exception instantiating " + getClass().getSimpleName(), e);
        }

        return null;
    }

}