package com.bignerdranch.android.moviegallery.chat.room.projection;

import java.util.Objects;

public class CommunicationDetail {
    public int id;//uid
    public int unread;
    public String nickname;
    public String avatar;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CommunicationDetail that = (CommunicationDetail) o;
        return id == that.id && unread == that.unread && Objects.equals(nickname, that.nickname) && Objects.equals(avatar, that.avatar);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, unread, nickname, avatar);
    }
}
