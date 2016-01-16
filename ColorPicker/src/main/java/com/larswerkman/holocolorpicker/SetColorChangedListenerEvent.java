/*
 * Copyright (C) 2015-2016 Emanuel Moecklin
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

package com.larswerkman.holocolorpicker;

public class SetColorChangedListenerEvent {

    /**
     * The is is used to map the publisher to the subscriber.
     * The subscriber provides the id and the publisher uses it to register its listener.
     */
    private final int mId;

    private final OnColorChangedListener mListener;

    public SetColorChangedListenerEvent(int id, OnColorChangedListener listener) {
        mId = id;
        mListener = listener;
    }

    public int getId() {
        return mId;
    }

    public OnColorChangedListener getOnColorChangedListener() {
        return mListener;
    }

}