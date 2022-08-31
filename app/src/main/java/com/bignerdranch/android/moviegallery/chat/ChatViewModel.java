package com.bignerdranch.android.moviegallery.chat;

import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelKt;
import androidx.paging.Pager;
import androidx.paging.PagingConfig;
import androidx.paging.PagingData;
import androidx.paging.PagingSource;
import androidx.paging.rxjava3.PagingRx;

import com.bignerdranch.android.moviegallery.chat.repository.MessageRepository;
import com.bignerdranch.android.moviegallery.chat.room.MessageDao;
import com.bignerdranch.android.moviegallery.chat.room.entity.Message;
import com.bignerdranch.android.moviegallery.constants.Constants;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import io.reactivex.rxjava3.core.Flowable;
import kotlin.jvm.functions.Function0;

@HiltViewModel
public class ChatViewModel extends ViewModel {
    private Flowable<PagingData<Message>> mFlowable;

    private final MessageRepository mMessageRepository;


    @Inject
    public ChatViewModel(MessageRepository messageRepository) {
        mMessageRepository = messageRepository;

        Pager<Integer, Message> pager = new Pager<Integer, Message>(
                new PagingConfig(Constants.PAGE_SIZE)
                , new Function0<PagingSource<Integer, Message>>() {
            @Override
            public PagingSource<Integer, Message> invoke() {
                return mMessageRepository.pagingSource();
            }
        }
        );

        mFlowable =
                PagingRx.cachedIn(PagingRx.getFlowable(pager), ViewModelKt.getViewModelScope(this));

    }

    public Flowable<PagingData<Message>> getFlowable() {
        return mFlowable;
    }
}
