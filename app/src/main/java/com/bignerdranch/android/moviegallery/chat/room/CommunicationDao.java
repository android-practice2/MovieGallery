package com.bignerdranch.android.moviegallery.chat.room;

import androidx.lifecycle.LiveData;
import androidx.paging.PagingSource;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.RewriteQueriesToDropUnusedColumns;
import androidx.room.Update;

import com.bignerdranch.android.moviegallery.chat.room.entity.Communication;
import com.bignerdranch.android.moviegallery.chat.room.projection.CommunicationDetail;

import java.util.List;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;

@Dao
public interface CommunicationDao {

    @Query("SELECT * FROM Communication c inner join Peer p on c.id=p.uid  ")
    @RewriteQueriesToDropUnusedColumns
    LiveData<List<CommunicationDetail>> liveData();


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Completable insertAll(Communication... communications);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    Completable update(Communication update);

    @Query("select * from Communication where id=:id")
    Single<Communication> selectById(int id);
}