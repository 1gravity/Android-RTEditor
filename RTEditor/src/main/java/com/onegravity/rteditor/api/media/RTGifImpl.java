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

import com.onegravity.rteditor.api.format.RTFormat;

import java.io.IOException;

import pl.droidsonroids.gif.GifDrawable;

/**
 * This is a basic implementation of the RTImage interface.
 */
public class RTGifImpl extends RTMediaImpl implements RTGif {
    private static final long serialVersionUID = -5252562131414951720L;

    public RTGifImpl(String filePath) {
        super(filePath);
    }

    @Override
    public Drawable getDrawable() throws IOException {
        GifDrawable gd = new GifDrawable(getFilePath(RTFormat.SPANNED));
        gd.setBounds(0, 0, gd.getIntrinsicWidth(), gd.getIntrinsicHeight());
        return gd;
    }
}