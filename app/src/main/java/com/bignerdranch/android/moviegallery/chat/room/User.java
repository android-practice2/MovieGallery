package com.bignerdranch.android.moviegallery.chat.room;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class User {
    @PrimaryKey
    public Integer uid;

    @ColumnInfo(name = "nickname")
    public String nickname;

    @ColumnInfo(name = "avatar")
    public String avatar;
}
