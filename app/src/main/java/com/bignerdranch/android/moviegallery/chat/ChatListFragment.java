package com.bignerdranch.android.moviegallery.chat;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.bignerdranch.android.moviegallery.BaseFragment;
import com.bignerdranch.android.moviegallery.R;
import com.bignerdranch.android.moviegallery.chat.room.projection.CommunicationDetail;
import com.bignerdranch.android.moviegallery.databinding.FragmentChatListBinding;
import com.bignerdranch.android.moviegallery.databinding.ViewHolderChatListItemBinding;
import com.bumptech.glide.Glide;

import java.util.List;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class ChatListFragment extends BaseFragment {

    private com.bignerdranch.android.moviegallery.databinding.FragmentChatListBinding mBinding;

    public ChatListFragment() {
        super(R.layout.fragment_chat_list);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = super.onCreateView(inflater, container, savedInstanceState);
        mBinding = FragmentChatListBinding.bind(view);

        final ChatListViewModel viewModel = new ViewModelProvider(this).get(ChatListViewModel.class);


        ChatListAdapter adapter = new ChatListAdapter();
        mBinding.recyclerView.setAdapter(adapter);
        mBinding.recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        viewModel.getLiveData().observe(getViewLifecycleOwner(), new Observer<List<CommunicationDetail>>() {
            @Override
            public void onChanged(List<CommunicationDetail> communications) {
                adapter.submitList(communications);

            }
        });


        return view;
    }

    public class ChatListAdapter extends ListAdapter<CommunicationDetail, ChatListViewHolder> {


        protected ChatListAdapter() {
            super(new DiffUtil.ItemCallback<CommunicationDetail>() {
                @Override
                public boolean areItemsTheSame(@NonNull CommunicationDetail oldItem, @NonNull CommunicationDetail newItem) {
                    return oldItem.id == newItem.id;
                }

                @Override
                public boolean areContentsTheSame(@NonNull CommunicationDetail oldItem, @NonNull CommunicationDetail newItem) {
                    return oldItem.equals(newItem);
                }
            });
        }

        @NonNull
        @Override
        public ChatListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            final View inflate = LayoutInflater.from(getContext()).inflate(R.layout.view_holder_chat_list_item, parent, false);
            return new ChatListViewHolder(inflate);
        }

        @Override
        public void onBindViewHolder(@NonNull ChatListViewHolder holder, int position) {
            holder.bind(getItem(position));
        }
    }

    public class ChatListViewHolder extends RecyclerView.ViewHolder {

        private final com.bignerdranch.android.moviegallery.databinding.ViewHolderChatListItemBinding mBinding;

        public ChatListViewHolder(@NonNull View itemView) {
            super(itemView);
            mBinding = ViewHolderChatListItemBinding.bind(itemView);
        }

        public void bind(CommunicationDetail item) {
            mBinding.uid.setText(String.valueOf(item.id));
            Glide.with(requireContext())
                    .load(item.avatar)
                    .into(mBinding.avatar);

            mBinding.nickname.setText(String.valueOf(item.nickname));
            if (item.unread > 0) {
                mBinding.unread.setText(String.valueOf(item.unread));
                mBinding.unread.setVisibility(View.VISIBLE);

            } else {
                mBinding.unread.setVisibility(View.GONE);
            }
            mBinding.getRoot().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final Intent intent = ChatActivity.newIntent(requireContext(), item.id);
                    startActivity(intent);
                }
            });
        }
    }
}
