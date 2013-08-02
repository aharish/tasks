/**
 * Copyright (c) 2012 Todoroo Inc
 *
 * See the file "LICENSE" for the full license governing this code.
 */
package com.todoroo.astrid.api;


import android.content.ContentValues;
import android.os.Parcel;
import android.os.Parcelable;

import com.todoroo.andlib.sql.QueryTemplate;

public class FilterWithUpdate extends FilterWithCustomIntent {

    /**
     * Update image URL
     */
    public String imageUrl = null;

    /**
     * Update message text
     */
    public String updateText = null;

    protected FilterWithUpdate() {
    }

    public FilterWithUpdate(String listingTitle, String title,
                            QueryTemplate sqlQuery, ContentValues valuesForNewTasks) {
        super(listingTitle, title, sqlQuery, valuesForNewTasks);
    }

    public FilterWithUpdate(String listingTitle, String title,
                            String sqlQuery, ContentValues valuesForNewTasks) {
        super(listingTitle, title, sqlQuery, valuesForNewTasks);
    }

    // --- parcelable

    /**
     * {@inheritDoc}
     */
    @Override
    public int describeContents() {
        return 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeString(imageUrl);
        dest.writeString(updateText);
    }

    @Override
    public void readFromParcel(Parcel source) {
        super.readFromParcel(source);
        imageUrl = source.readString();
        updateText = source.readString();
    }

    /**
     * Parcelable Creator Object
     */

    public static final Parcelable.Creator<FilterWithUpdate> CREATOR = new Parcelable.Creator<FilterWithUpdate>() {

        /**
         * {@inheritDoc}
         */
        @Override
        public FilterWithUpdate createFromParcel(Parcel source) {
            FilterWithUpdate item = new FilterWithUpdate();
            item.readFromParcel(source);
            return item;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public FilterWithUpdate[] newArray(int size) {
            return new FilterWithUpdate[size];
        }

    };

}
