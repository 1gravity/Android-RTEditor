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

package com.onegravity.rteditor.utils;

import android.text.Spanned;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class finds the Paragraphs in a Spanned text.
 * <p>
 * We need this for all paragraph formatting. While it's optimized for
 * performance, it should still be used with caution.
 */
public class RTLayout implements Serializable {
    private static final long serialVersionUID = 2210969820444215580L;

    private static final Pattern LINEBREAK_PATTERN = Pattern.compile("\\r\\n|\\r|\\n");

    private int mNrOfLines = 0;
    private ArrayList<Paragraph> mParagraphs = new ArrayList<Paragraph>();
    ;

    public RTLayout(Spanned spanned) {
        if (spanned != null) {
            String s = spanned.toString();

            // remove the trailing line feeds
            int len = s.length();
            char c = len > 0 ? s.charAt(len - 1) : '-';
            while (len > 0 && (c == '\n' || c == '\r')) {
                len--;
                if (len > 0) c = s.charAt(len - 1);
            }

            // now find the line breaks and the according lines / paragraphs
            mNrOfLines = 1;
            Matcher m = LINEBREAK_PATTERN.matcher(s.substring(0, len));
            int groupStart = 0;
            while (m.find()) {
                mParagraphs.add(new Paragraph(groupStart, m.end(), mNrOfLines == 1, false));    // the line feeds are part of the paragraph
                groupStart = m.end();
                mNrOfLines++;
            }
            if (groupStart < len) {
                mParagraphs.add(new Paragraph(groupStart, len, mNrOfLines == 1, true));
            }
            else if (len == 0) {
                mParagraphs.add(new Paragraph(0, 0, true, true));
            }
        }
    }

    public List<Paragraph> getParagraphs() {
        return mParagraphs;
    }

    public int getLineForOffset(int offset) {
        int lineNr = 0;
        while (lineNr < mNrOfLines && offset >= mParagraphs.get(lineNr).end()) {
            lineNr++;
        }
        return Math.min(Math.max(0, lineNr), mParagraphs.size() - 1);
    }

    public int getLineStart(int line) {
        return mNrOfLines == 0 || line < 0 ? 0 :
                line < mNrOfLines ? mParagraphs.get(line).start() :
                        mParagraphs.get(mNrOfLines - 1).end() - 1;
    }

    public int getLineEnd(int line) {
        return mNrOfLines == 0 || line < 0 ? 0 :
                line < mNrOfLines ? mParagraphs.get(line).end() :
                        mParagraphs.get(mNrOfLines - 1).end() - 1;
    }
}