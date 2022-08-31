package com.bignerdranch.android.moviegallery.chat.room.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class Peer {
    @PrimaryKey(autoGenerate = false)
    public Integer uid;

    @ColumnInfo(name = "nickname")
    public String nickname;

    @ColumnInfo(name = "avatar")
    public String avatar;

    public Peer() {
    }
}
