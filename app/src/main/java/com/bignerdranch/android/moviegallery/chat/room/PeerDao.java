package com.bignerdranch.android.moviegallery.chat.room;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.bignerdranch.android.moviegallery.chat.room.entity.Peer;

import java.util.List;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;

@Dao
public interface PeerDao {


    @Query("SELECT * FROM Peer WHERE uid =:uid")
    Single<Peer> selectById(int uid);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Completable insert(Peer peer);


}