package com.bignerdranch.android.moviegallery.chat;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.bignerdranch.android.moviegallery.chat.repository.CommunicationRepository;
import com.bignerdranch.android.moviegallery.chat.room.CommunicationDao;
import com.bignerdranch.android.moviegallery.chat.room.projection.CommunicationDetail;

import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class ChatListViewModel extends ViewModel {

    private final CommunicationRepository mCommunicationRepository;
    private final LiveData<List<CommunicationDetail>> mLiveData;

    @Inject
    public ChatListViewModel(CommunicationRepository communicationRepository) {
        mCommunicationRepository = communicationRepository;
        mLiveData = this.mCommunicationRepository.liveData();

    }

    public LiveData<List<CommunicationDetail>> getLiveData() {
        return mLiveData;
    }
}
