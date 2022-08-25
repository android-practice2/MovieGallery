package com.bignerdranch.android.moviegallery.friends;

import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.paging.CombinedLoadStates;
import androidx.paging.LoadState;
import androidx.paging.PagingDataAdapter;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bignerdranch.android.moviegallery.BaseFragment;
import com.bignerdranch.android.moviegallery.MyLoadStateAdapter;
import com.bignerdranch.android.moviegallery.PersonDetailActivity;
import com.bignerdranch.android.moviegallery.R;
import com.bignerdranch.android.moviegallery.constants.Constants;
import com.bignerdranch.android.moviegallery.databinding.FragmentFriendsBinding;
import com.bignerdranch.android.moviegallery.databinding.ViewHolderFriendsBinding;
import com.bignerdranch.android.moviegallery.integration.AppClient;
import com.bignerdranch.android.moviegallery.integration.model.User;
import com.bignerdranch.android.moviegallery.util.UserDiff;
import com.bumptech.glide.Glide;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import kotlin.Unit;
import kotlin.jvm.functions.Function1;

@AndroidEntryPoint
public class FriendsFragment extends BaseFragment {
    @Inject
    AppClient mAppClient;

    FriendsViewModel mFriendsViewModel;

    private com.bignerdranch.android.moviegallery.databinding.FragmentFriendsBinding mBinding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View inflate = inflater.inflate(R.layout.fragment_friends, container, false);
        mBinding = FragmentFriendsBinding.bind(inflate);

        mFriendsViewModel = new ViewModelProvider(this).get(FriendsViewModel.class);

        int uid = PreferenceManager.getDefaultSharedPreferences(getActivity())
                .getInt(Constants.PF_UID, -1);
        if (uid < 0) {
            Toast.makeText(getActivity(), "require login", Toast.LENGTH_SHORT).show();
            return inflate;
        }

        mBinding.recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        FriendsAdapter adapter = new FriendsAdapter(new UserDiff());
        mBinding.recyclerView.setAdapter(
                adapter.withLoadStateFooter(new MyLoadStateAdapter(v -> adapter.retry()))
        );
        mFriendsViewModel.subscribe(getLifecycle(), adapter, uid);

        mBinding.swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                adapter.refresh();
            }
        });

        adapter.addLoadStateListener(new Function1<CombinedLoadStates, Unit>() {
            @Override
            public Unit invoke(CombinedLoadStates combinedLoadStates) {
                if (combinedLoadStates.getRefresh() instanceof LoadState.NotLoading) {
                    mBinding.swipeRefreshLayout.setRefreshing(false);
                }
                return Unit.INSTANCE;

            }
        });

        return inflate;
    }

    public class FriendsAdapter extends PagingDataAdapter<User, FriendsViewHolder> {


        public FriendsAdapter(@NonNull DiffUtil.ItemCallback<User> diffCallback) {
            super(diffCallback);
        }

        @NonNull
        @Override
        public FriendsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new FriendsViewHolder(parent);
        }

        @Override
        public void onBindViewHolder(@NonNull FriendsViewHolder holder, int position) {
            holder.bind(getItem(position));
        }
    }

    public class FriendsViewHolder extends RecyclerView.ViewHolder {

        private final com.bignerdranch.android.moviegallery.databinding.ViewHolderFriendsBinding mBind;

        public FriendsViewHolder(@NonNull ViewGroup parent) {
            super(LayoutInflater.from(getActivity()).inflate(R.layout.view_holder_friends, parent, false));
            mBind = ViewHolderFriendsBinding.bind(super.itemView);
            super.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    TextView uid_text = v.findViewById(R.id.uid);
                    int uid = Integer.parseInt(uid_text.getText().toString());
                    Intent intent = PersonDetailActivity.newIntent(getActivity(), uid);
                    startActivity(intent);

                }
            });

        }

        public void bind(User item) {
            if (item.getAvatar() != null) {
                Glide.with(getActivity().getApplicationContext())
                        .load(item.getAvatar())
                        .into(mBind.avatarImage);

            } else {
                Glide.with(getActivity().getApplicationContext())
                        .load(R.drawable.ic_person)
                        .into(mBind.avatarImage);
            }

            mBind.uid.setText(item.getUid().toString());
            mBind.nickname.setText(item.getNickname().toString());
        }
    }
}
