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

package com.onegravity.rteditor.media.choose;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.onegravity.rteditor.R;
import com.onegravity.rteditor.api.RTMediaFactory;
import com.onegravity.rteditor.api.media.RTAudio;
import com.onegravity.rteditor.api.media.RTGif;
import com.onegravity.rteditor.api.media.RTImage;
import com.onegravity.rteditor.api.media.RTVideo;
import com.onegravity.rteditor.media.MonitoredActivity;
import com.onegravity.rteditor.media.choose.processor.GifProcessor;
import com.onegravity.rteditor.media.choose.processor.GifProcessor.GifProcessorListener;
import com.onegravity.rteditor.utils.Constants.MediaAction;

class GifChooserManager extends MediaChooserManager implements GifProcessorListener {

    public interface GifChooserListener extends MediaChooserListener {
        /**
         * Callback method to inform the caller that an gif file has been processed
         */
        public void onGifChosen(RTGif gif);
    }

    private GifChooserListener mListener;

    GifChooserManager(MonitoredActivity activity, MediaAction mediaAction,
                      RTMediaFactory<RTImage, RTGif, RTAudio, RTVideo> mediaFactory,
                      GifChooserListener listener, Bundle savedInstanceState) {
        super(activity, mediaAction, mediaFactory, listener, savedInstanceState);
        mListener = listener;
    }

    @SuppressWarnings("incomplete-switch")
    @Override
    boolean chooseMedia() throws IllegalArgumentException {
        Log.d("test", "choser");
        if (mListener == null) {
            throw new IllegalArgumentException("GifChooserListener cannot be null");
        }

        Intent intent = new Intent(Intent.ACTION_GET_CONTENT, null)
                .addCategory(Intent.CATEGORY_OPENABLE)
                .setType("image/gif");
        String title = mActivity.getString(R.string.rte_pick_gif);
        startActivity(Intent.createChooser(intent, title));
        return true;
    }


    @SuppressWarnings("incomplete-switch")
    @Override
    void processMedia(MediaAction mediaAction, Intent data) {
        String originalFile = determineOriginalFile(data);
        if (originalFile != null) {
            GifProcessor processor = new GifProcessor(originalFile, mMediaFactory, this);
            startBackgroundJob(processor);
        }
    }


    @Override
    /* GifProcessorListener */
    public void onGifProcessed(RTGif gif) {
        if (mListener != null) {
            mListener.onGifChosen(gif);
        }
    }

}