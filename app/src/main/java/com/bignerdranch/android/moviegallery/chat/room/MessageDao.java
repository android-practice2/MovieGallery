package com.bignerdranch.android.moviegallery.chat.room;

import androidx.paging.PagingSource;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.bignerdranch.android.moviegallery.chat.room.entity.Message;

import io.reactivex.rxjava3.core.Completable;

@Dao
public interface MessageDao {

    @Query("select * from Message")
    PagingSource<Integer, Message> pagingSource();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Completable insertAll(Message... users);

}