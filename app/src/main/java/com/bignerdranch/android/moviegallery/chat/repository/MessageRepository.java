package com.bignerdranch.android.moviegallery.chat.repository;

import androidx.paging.PagingSource;

import com.bignerdranch.android.moviegallery.chat.room.MessageDao;
import com.bignerdranch.android.moviegallery.chat.room.entity.Message;

import javax.inject.Inject;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class MessageRepository {

    private final MessageDao messageDao;

    @Inject
    public MessageRepository(MessageDao messageDao) {
        this.messageDao = messageDao;
    }

    public PagingSource<Integer, Message> pagingSource() {
        return messageDao.pagingSource();
    }

    public Completable insertAll(Message... users) {
        return messageDao.insertAll(users)
                .subscribeOn(Schedulers.io())
                ;

    }

}
