package com.bignerdranch.android.moviegallery.chat.repository;


import android.util.Log;

import com.bignerdranch.android.moviegallery.chat.room.AppDatabase;
import com.bignerdranch.android.moviegallery.chat.room.PeerDao;
import com.bignerdranch.android.moviegallery.chat.room.entity.Peer;
import com.bignerdranch.android.moviegallery.integration.AppClient;
import com.bignerdranch.android.moviegallery.integration.model.UserGetDetailResponse;

import java.io.IOException;

import javax.inject.Inject;

import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.core.SingleObserver;
import io.reactivex.rxjava3.core.SingleSource;
import io.reactivex.rxjava3.functions.Action;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class PeerRepository {
    private final PeerDao peerDao;
    private final AppClient mAppClient;
    private final AppDatabase mAppDatabase;


    @Inject
    public PeerRepository(PeerDao peerDao, AppClient appClient, AppDatabase appDatabase) {
        this.peerDao = peerDao;
        mAppClient = appClient;
        mAppDatabase = appDatabase;
    }

    public Single<Peer> fetchById(int uid) {
        return peerDao.selectByIdAsync(uid)
                .subscribeOn(Schedulers.io())
                .switchIfEmpty(new SingleSource<Peer>() {
                    @Override
                    public void subscribe(@NonNull SingleObserver<? super Peer> observer) {
                        try {
                            UserGetDetailResponse body = mAppClient.getDetail(uid)
                                    .execute().body();
                            Peer peer = new Peer();
                            peer.uid = body.getUid();
                            peer.avatar = body.getAvatar();
                            peer.nickname = body.getNickname();

                            peerDao.insertAsync(peer).subscribe();

                            Single.just(peer).subscribe(observer);
                        } catch (IOException e) {
                            observer.onError(e);
                            e.printStackTrace();
                        }


                    }
                })

                ;

    }

    public Completable updateIfPresent(Peer peer) {
        return Completable.fromRunnable(new Runnable() {
            @Override
            public void run() {
                mAppDatabase.getTransactionExecutor()
                        .execute(new Runnable() {
                            @Override
                            public void run() {
                                Log.i(PeerRepository.this.getClass().getSimpleName(), "update_local_peer:" + peer);
                                final Peer ePeer = peerDao.selectByIdAsync(peer.uid).blockingGet();
                                if (ePeer == null) {
                                    Log.i(PeerRepository.this.getClass().getSimpleName(), "update_local_peer ignore, peer_not_exist:" + peer);
                                    return;
                                }
                                peerDao.updateAsync(peer)
                                        .doOnComplete(new Action() {
                                            @Override
                                            public void run() throws Throwable {
                                                Log.i(PeerRepository.this.getClass().getSimpleName(), "update_local_peer success:" + peer);
                                            }
                                        })
                                        .subscribe();

                            }
                        });

            }
        }).subscribeOn(Schedulers.io())
                ;

    }

}
