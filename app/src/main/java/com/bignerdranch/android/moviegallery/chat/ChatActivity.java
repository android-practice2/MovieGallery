package com.bignerdranch.android.moviegallery.chat;

import static autodispose2.AutoDispose.autoDisposable;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.paging.PagingData;
import androidx.paging.PagingDataAdapter;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bignerdranch.android.moviegallery.BaseActivity;
import com.bignerdranch.android.moviegallery.R;
import com.bignerdranch.android.moviegallery.chat.repository.CommunicationRepository;
import com.bignerdranch.android.moviegallery.chat.repository.MessageRepository;
import com.bignerdranch.android.moviegallery.chat.repository.PeerRepository;
import com.bignerdranch.android.moviegallery.chat.room.entity.Message;
import com.bignerdranch.android.moviegallery.chat.room.entity.Peer;
import com.bignerdranch.android.moviegallery.constants.Constants;
import com.bignerdranch.android.moviegallery.databinding.ActivityChatBinding;
import com.bignerdranch.android.moviegallery.databinding.ViewHolderMessagePeerBinding;
import com.bignerdranch.android.moviegallery.mqtt.AppMqttClient;
import com.bumptech.glide.Glide;

import org.eclipse.paho.client.mqttv3.MqttClient;

import java.util.UUID;
import java.util.concurrent.CountDownLatch;

import javax.inject.Inject;

import autodispose2.androidx.lifecycle.AndroidLifecycleScopeProvider;
import dagger.hilt.android.AndroidEntryPoint;
import io.reactivex.rxjava3.core.MaybeObserver;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.functions.Consumer;
import io.reactivex.rxjava3.schedulers.Schedulers;

@AndroidEntryPoint
public class ChatActivity extends BaseActivity {

    private int mPeerUid;
    private Peer mPeer;
    private Peer mMe;

    private com.bignerdranch.android.moviegallery.databinding.ActivityChatBinding mBinding;
    private String mRoom;
    private ChatViewModel mViewModel;

    @Inject
    AppMqttClient mAppMqttClient;

    @Inject
    PeerRepository mPeerRepository;
    @Inject
    MessageRepository mMessageRepository;
    @Inject
    CommunicationRepository mCommunicationRepository;

    private CountDownLatch mLatch;


    public static Intent newIntent(Context context, Integer peerUid) {
        Intent intent = new Intent(context, ChatActivity.class);
        intent.putExtra(Constants.EXTRA_PEER_UID, peerUid);
        return intent;

    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = ActivityChatBinding.inflate(getLayoutInflater());
        View root = mBinding.getRoot();
        setContentView(root);
        mPeerUid = getIntent().getIntExtra(Constants.EXTRA_PEER_UID, -1);
        mCommunicationRepository.clearUnread(mPeerUid)
                .to(autoDisposable(AndroidLifecycleScopeProvider.from(getLifecycle())))
                .subscribe();

        mLatch = new CountDownLatch(2);
        mPeerRepository.selectById(mPeerUid)
                .subscribeOn(Schedulers.io())
                .to(autoDisposable(AndroidLifecycleScopeProvider.from(getLifecycle())))
                .subscribe(new Consumer<Peer>() {
                    @Override
                    public void accept(Peer peer) throws Throwable {
                        mPeer = peer;
                        mLatch.countDown();
                    }
                });
        mPeerRepository.selectById(mUid)
                .subscribeOn(Schedulers.io())
                .to(autoDisposable(AndroidLifecycleScopeProvider.from(getLifecycle())))
                .subscribe(new Consumer<Peer>() {
                    @Override
                    public void accept(Peer peer) throws Throwable {
                        mMe = peer;
                        mLatch.countDown();
                    }
                });


        mViewModel = new ViewModelProvider(this).get(ChatViewModel.class);

        setupRecyclerView();

        setupIM();

        mBinding.menuBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                flipMenuLayoutVisibility();
            }
        });

        mBinding.menuLayout.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    flipMenuLayoutVisibility();
                }
            }
        });


        mBinding.videoChatBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mRoom = UUID.randomUUID().toString().replace("-", "");
                Intent intent = VideoActivity.newIntent(ChatActivity.this, mPeerUid, mRoom, true);
                startActivity(intent);

            }
        });


    }

    private void setupIM() {
        mBinding.textInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (!s.toString().isEmpty()) {
                    flipSendBtnVisibility();
                }

            }
        });

        mBinding.sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String text = mBinding.textInput.getText().toString();
                mBinding.textInput.setText("");
                flipSendBtnVisibility();
                Message message = new Message(
                        mPeerUid, Message.TYPE_PEER, text
                );

                mAppMqttClient.publish(mPeerUid, message)
                        .subscribe(new MaybeObserver<Void>() {
                            @Override
                            public void onSubscribe(@io.reactivex.rxjava3.annotations.NonNull Disposable d) {

                            }

                            @Override
                            public void onSuccess(@io.reactivex.rxjava3.annotations.NonNull Void unused) {

                            }

                            @Override
                            public void onError(@io.reactivex.rxjava3.annotations.NonNull Throwable e) {
                                Toast.makeText(ChatActivity.this, "publish_msg error", Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onComplete() {

                                mMessageRepository.insertAll(message).subscribe();


                            }
                        })
                ;
            }
        });

    }

    private void flipMenuLayoutVisibility() {
        if (mBinding.menuLayout.getVisibility() == View.GONE) {
            mBinding.menuLayout.setVisibility(View.VISIBLE);

        } else {
            mBinding.menuLayout.setVisibility(View.GONE);

        }
    }

    private void setupRecyclerView() {
        mBinding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        final MessageAdapter adapter = new MessageAdapter();
        mBinding.recyclerView.setAdapter(adapter);

        mViewModel.getFlowable()
                .subscribeOn(Schedulers.io())
                .to(autoDisposable(AndroidLifecycleScopeProvider.from(getLifecycle())))
                .subscribe(new Consumer<PagingData<Message>>() {
                    @Override
                    public void accept(PagingData<Message> messagePagingData) throws Throwable {
                        mLatch.await();
                        adapter.submitData(getLifecycle(), messagePagingData);

                    }
                });
    }

    private void flipSendBtnVisibility() {
        if (mBinding.menuBtn.getVisibility() == View.VISIBLE) {
            mBinding.menuBtn.setVisibility(View.GONE);
            mBinding.sendBtn.setVisibility(View.VISIBLE);
        } else {
            mBinding.menuBtn.setVisibility(View.VISIBLE);
            mBinding.sendBtn.setVisibility(View.GONE);
        }

    }

    public class MessageAdapter extends PagingDataAdapter<Message, MessageViewHolder> {
        public MessageAdapter() {
            super(new DiffUtil.ItemCallback<Message>() {
                @Override
                public boolean areItemsTheSame(@NonNull Message oldItem, @NonNull Message newItem) {
                    return oldItem.id == newItem.id;
                }

                @Override
                public boolean areContentsTheSame(@NonNull Message oldItem, @NonNull Message newItem) {
                    return oldItem.equals(newItem);
                }
            });
        }

        @NonNull
        @Override
        public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            if (viewType == Message.TYPE_PEER) {
                final View view = LayoutInflater.from(ChatActivity.this).inflate(R.layout.view_holder_message_peer, parent, false);
                MessageViewHolder messageViewHolder = new MessageViewHolder(view);
                return messageViewHolder;
            } else {
                final View view = LayoutInflater.from(ChatActivity.this).inflate(R.layout.view_holder_message_me, parent, false);
                MessageViewHolder messageViewHolder = new MessageViewHolder(view);
                return messageViewHolder;
            }

        }

        @Override
        public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
            holder.bind(getItem(position));
        }

        @Override
        public int getItemViewType(int position) {
            final Message item = getItem(position);
            return item.type;
        }
    }

    public class MessageViewHolder extends RecyclerView.ViewHolder {

        private final com.bignerdranch.android.moviegallery.databinding.ViewHolderMessagePeerBinding mBinding;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            mBinding = ViewHolderMessagePeerBinding.bind(itemView);

        }

        public void bind(Message message) {
            if (message.type == Message.TYPE_PEER) {
                Glide.with(getApplicationContext())
                        .load(mPeer.avatar)
                        .into(mBinding.avatar);

            } else {
                Glide.with(getApplicationContext())
                        .load(mMe.avatar)
                        .into(mBinding.avatar);
            }

            mBinding.msgContent.setText(message.content);

        }
    }

}
