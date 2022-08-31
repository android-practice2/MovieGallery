package com.bignerdranch.android.moviegallery.chat;

import static autodispose2.AutoDispose.autoDisposable;

import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
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
import com.bignerdranch.android.moviegallery.chat.room.entity.Communication;
import com.bignerdranch.android.moviegallery.chat.room.entity.Message;
import com.bignerdranch.android.moviegallery.chat.room.entity.Peer;
import com.bignerdranch.android.moviegallery.constants.Constants;
import com.bignerdranch.android.moviegallery.databinding.ActivityChatBinding;
import com.bignerdranch.android.moviegallery.databinding.ViewHolderMessagePeerBinding;
import com.bignerdranch.android.moviegallery.integration.AppClient;
import com.bignerdranch.android.moviegallery.integration.model.ChatPostMsg;
import com.bignerdranch.android.moviegallery.mqtt.AppMqttClient;
import com.bumptech.glide.Glide;

import java.util.UUID;
import java.util.concurrent.CountDownLatch;

import javax.inject.Inject;

import autodispose2.androidx.lifecycle.AndroidLifecycleScopeProvider;
import dagger.hilt.android.AndroidEntryPoint;
import io.reactivex.rxjava3.functions.Consumer;
import io.reactivex.rxjava3.schedulers.Schedulers;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

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
    @Inject
    AppClient mAppClient;

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

        upsertCommunicationList();


        mLatch = new CountDownLatch(2);
        mPeerRepository.fetchById(mPeerUid)
                .subscribeOn(Schedulers.io())
                .to(autoDisposable(AndroidLifecycleScopeProvider.from(getLifecycle())))
                .subscribe(new Consumer<Peer>() {
                    @Override
                    public void accept(Peer peer) throws Throwable {
                        mPeer = peer;
                        mLatch.countDown();
                    }
                });

        mPeerRepository.fetchById(mUid)
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
                InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                inputMethodManager.hideSoftInputFromWindow(v.getWindowToken(), 0);
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

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            final View v = mBinding.recyclerView;
            Rect rect = new Rect();
            v.getGlobalVisibleRect(rect);
            if (rect.contains((int) ev.getRawX(), (int) ev.getRawY())) {
                Log.d(TAG, "clear_editText_focus");
                InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                inputMethodManager.hideSoftInputFromWindow(v.getWindowToken(), 0);

                goneMenuLayout();
            }
        }

        return super.dispatchTouchEvent(ev);
    }


    private void upsertCommunicationList() {
        Communication communication = new Communication();
        communication.id = mPeerUid;
        communication.unread = 0;
        mCommunicationRepository.insertAll(communication)
                .to(autoDisposable(AndroidLifecycleScopeProvider.from(getLifecycle())))
                .subscribe()
        ;
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
                    setSenBtnVisible();
                }

            }
        });

        mBinding.sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String text = mBinding.textInput.getText().toString();
                mBinding.textInput.setText("");
                setSendBtnGone();
                Message message = new Message(
                        mUid, Message.TYPE_ME, text
                );
                mMessageRepository.insertAll(message).subscribe();

                ChatPostMsg msg = new ChatPostMsg(mUid, mPeerUid, text);
                mAppClient.postMsg(msg).enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                    }

                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {
                        Log.e(TAG, "publish_msg error", t);
                        Toast.makeText(ChatActivity.this, "publish_msg error", Toast.LENGTH_SHORT).show();
                    }
                });

            }
        });

    }


    private void goneMenuLayout() {
        mBinding.menuLayout.setVisibility(View.GONE);

    }

    private void flipMenuLayoutVisibility() {
        if (mBinding.menuLayout.getVisibility() == View.GONE) {
            mBinding.menuLayout.setVisibility(View.VISIBLE);

        } else {
            mBinding.menuLayout.setVisibility(View.GONE);

        }
    }


    private void setupRecyclerView() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        mBinding.recyclerView.setLayoutManager(layoutManager);
        MessageAdapter adapter = new MessageAdapter();
        mBinding.recyclerView.setAdapter(adapter);

        mViewModel.getFlowable()
                .subscribeOn(Schedulers.io())
                .to(autoDisposable(AndroidLifecycleScopeProvider.from(getLifecycle())))
                .subscribe(new Consumer<PagingData<Message>>() {
                    @Override
                    public void accept(PagingData<Message> pagingData) throws Throwable {
                        mLatch.await();
                        adapter.submitData(getLifecycle(), pagingData);

                    }
                });
        adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                final int position = positionStart + itemCount - 1;
                final Message item = adapter.findItem(position);

                Log.i(TAG, "onItemRangeInserted"
                        + ",positionStart:" + positionStart
                        + ",itemCount:" + itemCount
                );
                if (item == null) {
                    Log.i(TAG, "item_is_null position:" + position);
                    return;
                }
                if (item.type == Message.TYPE_ME) {
                    mBinding.recyclerView.scrollToPosition(mBinding.recyclerView.getAdapter().getItemCount() - 1);
                }
            }
        });
    }

    private void setSenBtnVisible() {
        mBinding.menuBtn.setVisibility(View.GONE);
        mBinding.sendBtn.setVisibility(View.VISIBLE);
    }

    private void setSendBtnGone() {
        mBinding.menuBtn.setVisibility(View.VISIBLE);
        mBinding.sendBtn.setVisibility(View.GONE);
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

        public Message findItem(int position) {
            return super.getItem(position);
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

            final Message item = getItem(position);
            if (item == null) {
                return;
            }
            holder.bind(item);
        }

        @Override
        public int getItemViewType(int position) {
            final Message item = getItem(position);
            if (item == null) {
                Log.i(TAG, "item_is_null position:" + position);
                return Message.TYPE_PEER;
            }
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
