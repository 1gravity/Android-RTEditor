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

package com.onegravity.rteditor.media.choose.processor;

import com.onegravity.rteditor.api.RTMediaFactory;
import com.onegravity.rteditor.api.media.RTAudio;
import com.onegravity.rteditor.api.media.RTGif;
import com.onegravity.rteditor.api.media.RTImage;
import com.onegravity.rteditor.api.media.RTMediaSource;
import com.onegravity.rteditor.api.media.RTMediaType;
import com.onegravity.rteditor.api.media.RTVideo;

import java.io.IOException;
import java.io.InputStream;

public class GifProcessor extends MediaProcessor {

    public interface GifProcessorListener extends MediaProcessorListener {
        public void onGifProcessed(RTGif image);
    }

    private GifProcessorListener mListener;

    public GifProcessor(String originalFile, RTMediaFactory<RTImage, RTGif, RTAudio, RTVideo> mediaFactory, GifProcessorListener listener) {
        super(originalFile, mediaFactory, listener);
        mListener = listener;
    }

    @Override
    protected void processMedia() throws IOException, Exception {
        InputStream in = super.getInputStream();
        if (in == null) {
            if (mListener != null) {
                mListener.onError("No file found to process");
            }
        } else {
            RTMediaSource source = new RTMediaSource(RTMediaType.GIF, in, getOriginalFile(), getMimeType());
            RTGif gif = mMediaFactory.createGif(source);
            if (gif != null && mListener != null) {
                mListener.onGifProcessed(gif);
            }
        }

    }

}