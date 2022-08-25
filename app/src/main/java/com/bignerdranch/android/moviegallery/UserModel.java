package com.bignerdranch.android.moviegallery;

import androidx.lifecycle.ViewModel;

/**
 * capsulate user info
 */
public class UserModel extends ViewModel {
    private int mUid = -1;

    public void setUid(int uid) {
        mUid = uid;
    }

    public int getUid() {
        return mUid;
    }
}
