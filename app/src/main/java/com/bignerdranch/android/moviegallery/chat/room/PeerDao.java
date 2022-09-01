package com.bignerdranch.android.moviegallery.chat.room;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.bignerdranch.android.moviegallery.chat.room.entity.Peer;

import java.util.List;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;

@Dao
public interface PeerDao {


    @Query("SELECT * FROM Peer WHERE uid =:uid")
    Maybe<Peer> selectByIdAsync(int uid);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Completable insertAsync(Peer peer);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    Completable updateAsync(Peer peer);
}