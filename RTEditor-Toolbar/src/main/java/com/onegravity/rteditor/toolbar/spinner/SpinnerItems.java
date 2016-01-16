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

package com.onegravity.rteditor.toolbar.spinner;

import java.util.ArrayList;
import java.util.List;

/**
 * A collection of SpinnerItem objects.
 * It's used to populate the SpinnerItemAdapter.
 */
public class SpinnerItems<T extends SpinnerItem> {

    private List<T> mItems = new ArrayList<T>();
    private int mSelectedItem = -1;

    /**
     * Constructor for default values (no entries, no selected entries)
     */
    public SpinnerItems() {
    }

    /**
     * @param items        The list of SpinnerItem objects to display in the spinner
     * @param selectedItem The index of the selected item or -1 if none has been selected yet
     */
    public SpinnerItems(List<T> items, int selectedItem) {
        mItems = items;
        mSelectedItem = selectedItem;
    }

    public synchronized void add(T item) {
        getItemsInternal().add(item);
    }

    public synchronized void clear() {
        getItemsInternal().clear();
    }

    public synchronized void setItems(List<T> items) {
        mItems = items;
    }

    public synchronized List<T> getItems() {
        return getItemsInternal();
    }

    public synchronized int size() {
        return getItemsInternal().size();
    }

    public void setSelectedItem(int selectedItem) {
        mSelectedItem = selectedItem;
    }

    public int getSelectedItem() {
        return mSelectedItem;
    }

    // lazy initialization
    private synchronized List<T> getItemsInternal() {
        if (mItems == null) mItems = new ArrayList<T>();
        return mItems;
    }
}