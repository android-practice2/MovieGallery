package com.bignerdranch.android.moviegallery.chat.room.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import java.util.Objects;

@Entity
public class Message {
    public static final int TYPE_PEER = 0;
    public static final int TYPE_ME = 1;

    @PrimaryKey(autoGenerate = true)
    public int id;

    @ColumnInfo(name = "uid")
    public int uid;

    /**
     * @see Message#TYPE_PEER
     * @see Message#TYPE_ME
     */
    @ColumnInfo(name = "type")
    public int type;

    @ColumnInfo(name = "content")
    public String content;

    public Message() {
    }

    @Ignore
    public Message(int uid, int type, String content) {
        this.uid = uid;
        this.type = type;
        this.content = content;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Message message = (Message) o;
        return id == message.id && uid == message.uid && type == message.type && Objects.equals(content, message.content);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, uid, type, content);
    }
}
