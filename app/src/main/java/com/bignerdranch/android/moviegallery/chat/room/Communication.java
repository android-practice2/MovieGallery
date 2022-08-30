package com.bignerdranch.android.moviegallery.chat.room;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class Communication {
    @PrimaryKey
    public int id;//uid

    @ColumnInfo(name = "unreadCount")
    public int unreadCount;


}
