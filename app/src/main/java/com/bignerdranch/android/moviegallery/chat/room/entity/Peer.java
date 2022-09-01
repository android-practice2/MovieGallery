package com.bignerdranch.android.moviegallery.chat.room.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
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

    @Ignore
    public Peer(Integer uid, String nickname, String avatar) {
        this.uid = uid;
        this.nickname = nickname;
        this.avatar = avatar;
    }
}
