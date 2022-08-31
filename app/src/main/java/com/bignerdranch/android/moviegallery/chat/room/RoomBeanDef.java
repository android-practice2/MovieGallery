package com.bignerdranch.android.moviegallery.chat.room;

import android.app.Application;

import androidx.room.Room;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;

@Module
@InstallIn(SingletonComponent.class)
public class RoomBeanDef {

    @Provides
    @Singleton
    public static AppDatabase sAppDatabase(Application context) {
        final AppDatabase database = Room.databaseBuilder(context, AppDatabase.class, "room_db")
                .build();
        return database;
    }

    @Provides
    @Singleton
    public static MessageDao sMessageDao(AppDatabase database) {
        return database.getMessageDao();
    }

    @Provides
    @Singleton
    public static CommunicationDao sCommunicationDao(AppDatabase database) {
        return database.getCommunicationDao();
    }

    @Provides
    @Singleton
    public static PeerDao sUserDao(AppDatabase database) {
        return database.getPeerDao();
    }

}
