package com.bignerdranch.android.moviegallery.chat.repository;


import com.bignerdranch.android.moviegallery.chat.room.PeerDao;
import com.bignerdranch.android.moviegallery.chat.room.entity.Peer;
import com.bignerdranch.android.moviegallery.integration.AppClient;
import com.bignerdranch.android.moviegallery.integration.model.UserGetDetailResponse;
import com.fasterxml.jackson.databind.util.BeanUtil;

import javax.inject.Inject;

import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.core.SingleSource;
import io.reactivex.rxjava3.functions.Function;
import io.reactivex.rxjava3.schedulers.Schedulers;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PeerRepository {
    private final PeerDao peerDao;
    private final AppClient mAppClient;


    @Inject
    public PeerRepository(PeerDao peerDao, AppClient appClient) {
        this.peerDao = peerDao;
        mAppClient = appClient;
    }

    public Single<Peer> selectById(int uid) {
        return peerDao.selectById(uid)
                .subscribeOn(Schedulers.io())
                .onErrorResumeNext(new Function<Throwable, SingleSource<? extends Peer>>() {
                    @Override
                    public SingleSource<? extends Peer> apply(Throwable throwable) throws Throwable {

                        final UserGetDetailResponse body = mAppClient.getDetail(uid)
                                .execute().body();

                        Peer peer = new Peer();
                        peer.uid = body.getUid();
                        peer.avatar = body.getAvatar();
                        peer.nickname = body.getNickname();

                        peerDao.insert(peer).subscribe();

                        return Single.just(peer);
                    }

                })
                ;

    }

}
