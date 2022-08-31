package com.bignerdranch.android.moviegallery.chat.repository;

import androidx.lifecycle.LiveData;

import com.bignerdranch.android.moviegallery.chat.room.CommunicationDao;
import com.bignerdranch.android.moviegallery.chat.room.entity.Communication;
import com.bignerdranch.android.moviegallery.chat.room.projection.CommunicationDetail;

import java.util.List;

import javax.inject.Inject;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.functions.Consumer;
import io.reactivex.rxjava3.functions.Function;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class CommunicationRepository {

    private final CommunicationDao mCommunicationDao;

    @Inject
    public CommunicationRepository(CommunicationDao communicationDao) {
        mCommunicationDao = communicationDao;
    }

    public LiveData<List<CommunicationDetail>> liveData() {
        return mCommunicationDao.liveData();
    }

    public Completable insertAll(Communication... communications) {
        return mCommunicationDao.insertAll(communications)
                .subscribeOn(Schedulers.io());
    }

    public Completable clearUnread(int uid) {
        return mCommunicationDao.selectById(uid)
                .subscribeOn(Schedulers.io())
                .flatMapCompletable(
                        new Function<Communication, Completable>() {
                            @Override
                            public Completable apply(Communication communication) throws Throwable {
                                communication.unread = 0;
                                return mCommunicationDao.update(communication);

                            }
                        }
                );


    }
}
