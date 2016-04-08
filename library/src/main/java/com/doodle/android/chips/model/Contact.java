/*
 * Copyright (C) 2016 Doodle AG.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.doodle.android.chips.model;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import java.io.Serializable;

public class Contact implements Comparable<Contact>, Serializable {

    @Nullable
    private final String mFirstName;

    @Nullable
    private final String mLastName;

    @NonNull
    private final String mEmailAddress;

    @Nullable
    private transient final Uri mAvatarUri;

    @NonNull
    private final String mDisplayName;

    @NonNull
    private final String mInitials;

    public Contact(@Nullable String firstName, @Nullable String lastName, @Nullable String displayName, @NonNull String emailAddress, @Nullable Uri avatarUri) {
        mFirstName = firstName;
        mLastName = lastName;
        mAvatarUri = avatarUri;
        mEmailAddress = emailAddress;

        if (!TextUtils.isEmpty(displayName)) {
            mDisplayName = displayName;
        } else if (TextUtils.isEmpty(mFirstName)) {
            if (TextUtils.isEmpty(mLastName)) {
                mDisplayName = mEmailAddress;
            } else {
                mDisplayName = mLastName;
            }
        } else if (TextUtils.isEmpty(mLastName)) {
            mDisplayName = mFirstName;
        } else {
            mDisplayName = mFirstName + " " + mLastName;
        }

        StringBuilder initialsBuilder = new StringBuilder();
        if (!TextUtils.isEmpty(mFirstName)) {
            initialsBuilder.append(Character.toUpperCase(mFirstName.charAt(0)));
        }
        if (!TextUtils.isEmpty(mLastName)) {
            initialsBuilder.append(Character.toUpperCase(mLastName.charAt(0)));
        }
        mInitials = initialsBuilder.toString();
    }

    @Nullable
    public String getFirstName() {
        return mFirstName;
    }

    @Nullable
    public String getLastName() {
        return mLastName;
    }

    @NonNull
    public String getEmailAddress() {
        return mEmailAddress;
    }

    @Nullable
    public Uri getAvatarUri() {
        return mAvatarUri;
    }

    @NonNull
    public String getDisplayName() {
        return mDisplayName;
    }

    @NonNull
    public String getInitials() {
        return mInitials;
    }

    @Override
    public int compareTo(final Contact another) {

        if (another == null) {
            return 1;
        }

        // compare whatever is the first visible component of the name
        String myString;
        if (mDisplayName != null) {
            myString = mDisplayName;
        } else if (mFirstName != null) {
            myString = mFirstName;
        } else {
            myString = mLastName;
        }

        String otherString;
        if (another.mDisplayName != null) {
            otherString = another.mDisplayName;
        } else if (another.mFirstName != null) {
            otherString = another.mFirstName;
        } else {
            otherString = another.mLastName;
        }

        int diff = compare(myString, otherString);
        if (diff != 0) {
            return diff;
        }

        if (another.mFirstName == null && mFirstName != null) {
            return 1;
        }
        if (another.mFirstName != null && mFirstName == null) {
            return -1;
        }

        if (another.mFirstName != null && mFirstName != null) {
            // both have first names, so we didn't yet compare last names
            diff = compare(mLastName, another.mLastName);
            if (diff != 0) {
                return diff;
            }
        }

        return mEmailAddress.compareTo(another.mEmailAddress);
    }

    private int compare(String myString, String otherString) {
        boolean isMineBlank = TextUtils.isEmpty(myString);
        boolean isOtherBlank = TextUtils.isEmpty(otherString);
        if (isMineBlank && isOtherBlank) {
            return 0;
        }
        if (isMineBlank) {
            return 1;
        }
        if (isOtherBlank) {
            return -1;
        }
        return myString.toLowerCase().compareTo(otherString.toLowerCase());
    }

    public boolean matches(CharSequence searchString) {
        String lowerCaseSearchString = searchString.toString().toLowerCase();
        return (mFirstName != null && mFirstName.toLowerCase().contains(lowerCaseSearchString)) ||
                (mLastName != null && mLastName.toLowerCase().contains(lowerCaseSearchString)) ||
                mEmailAddress.toLowerCase().contains(lowerCaseSearchString);
    }


    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Contact contact = (Contact) o;

        if (!mEmailAddress.equals(contact.mEmailAddress)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return mEmailAddress.hashCode();
    }

    @Override
    public String toString() {
        return "Contact{" +
                "mFirstName='" + mFirstName + '\'' +
                ", mLastName='" + mLastName + '\'' +
                ", mEmailAddress='" + mEmailAddress + '\'' +
                ", mAvatarUri=" + mAvatarUri +
                ", mDisplayName='" + mDisplayName + '\'' +
                ", mInitials='" + mInitials + '\'' +
                '}';
    }
}