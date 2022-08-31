package com.bignerdranch.android.moviegallery.chat.room.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.Objects;

@Entity
public class Communication {
    @PrimaryKey(autoGenerate = false)
    public int id;//uid

    @ColumnInfo(name = "unread")
    public int unread;


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Communication that = (Communication) o;
        return id == that.id && unread == that.unread;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, unread);
    }
}
