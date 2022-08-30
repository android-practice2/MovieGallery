package com.bignerdranch.android.moviegallery.chat.room;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class Message {
    @PrimaryKey
    public int id;

    @ColumnInfo(name = "uid")
    public int uid;

    @ColumnInfo(name = "tag")//m: me, p: peer
    public String tag;

    @ColumnInfo(name = "content")
    public String content;


}
