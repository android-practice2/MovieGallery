package com.bignerdranch.android.moviegallery.chat.room;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import com.bignerdranch.android.moviegallery.chat.room.entity.Communication;
import com.bignerdranch.android.moviegallery.chat.room.entity.Message;
import com.bignerdranch.android.moviegallery.chat.room.entity.Peer;

@Database(entities = {Communication.class, Message.class, Peer.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    public abstract CommunicationDao getCommunicationDao();

    public abstract MessageDao getMessageDao();

    public abstract PeerDao getPeerDao();


}
