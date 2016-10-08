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

package com.onegravity.rteditor.api.media;

import android.graphics.drawable.Drawable;

import java.io.IOException;

/**
 * This is a basic implementation of the RTImage interface.
 */
public class RTGifImpl extends RTMediaImpl implements RTGif {
    private static final long serialVersionUID = -5252562131414951720L;

    Drawable mDrawable;

    public RTGifImpl(String filePath, Drawable drawable) {
        super(filePath);
        this.mDrawable = drawable;

    }

    @Override
    public Drawable getDrawable() throws IOException {
        return mDrawable;
    }

    @Override
    public void setDrawable(Drawable drawable) {
        this.mDrawable = drawable;
    }
}