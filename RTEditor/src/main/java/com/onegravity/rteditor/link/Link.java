/*
 * Copyright (C) 2016 Emanuel Moecklin
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

package com.onegravity.rteditor.link;

/**
 * The Link class describes a link (link text and an URL).
 */
public class Link {
    final private String mLinkText;
    final private String mUrl;

    Link(String linkText, String url) {
        mLinkText = linkText;
        mUrl = url;
    }

    public String getLinkText() {
        return mLinkText;
    }

    public String getUrl() {
        return mUrl;
    }

    public boolean isValid() {
        return mUrl != null && mUrl.length() > 0 && mLinkText != null && mLinkText.length() > 0;
    }
}

