package com.bignerdranch.android.moviegallery.chat.repository;

import com.bignerdranch.android.moviegallery.chat.room.AppDatabase;
import com.bignerdranch.android.moviegallery.chat.room.CommunicationDao;
import com.bignerdranch.android.moviegallery.chat.room.MessageDao;
import com.bignerdranch.android.moviegallery.chat.room.PeerDao;
import com.bignerdranch.android.moviegallery.http.AppClient;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;

@Module
@InstallIn({SingletonComponent.class})
public class RepositoryBeanDef {

    @Provides
    @Singleton
    public static MessageRepository sMessageRepository(MessageDao dao) {
        return new MessageRepository(dao);
    }

    @Provides
    @Singleton
    public static PeerRepository sPeerRepository(PeerDao dao, AppClient appClient, AppDatabase appDatabase) {
        return new PeerRepository(dao, appClient, appDatabase);
    }

    @Provides
    @Singleton
    public static CommunicationRepository sCommunicationRepository(CommunicationDao dao) {
        return new CommunicationRepository(dao);
    }


}
