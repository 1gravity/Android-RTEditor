/*
 * <!--
 *   Copyright (C) 2015-2016 Emanuel Moecklin
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *   -->
 */

package com.onegravity.rteditor.spans;

import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;

import com.onegravity.rteditor.RTEditText;
import com.onegravity.rteditor.api.media.RTGif;

import java.io.IOException;

/**
 * An ImageSpan representing an embedded gif.
 */
public class GifSpan extends MediaSpan {

    private RTEditText mEditor;

    public GifSpan(final RTEditText editor, final RTGif image, boolean isSaved) throws IOException {
        super(image, isSaved);
        mEditor = editor;
        image.getDrawable().setCallback(new Drawable.Callback() {
            @Override
            public void invalidateDrawable(@NonNull Drawable who) {
                mEditor.invalidate();
            }

            @Override
            public void scheduleDrawable(@NonNull Drawable who, @NonNull Runnable what, long when) {
                mEditor.postDelayed(what, when);
            }

            @Override
            public void unscheduleDrawable(@NonNull Drawable who, @NonNull Runnable what) {
                mEditor.removeCallbacks(what);
            }
        });
    }

    public RTGif getGif() {
        return (RTGif) mMedia;
    }
}