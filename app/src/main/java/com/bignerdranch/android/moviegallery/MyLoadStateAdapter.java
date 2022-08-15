package com.bignerdranch.android.moviegallery;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.paging.LoadState;
import androidx.paging.LoadStateAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.bignerdranch.android.moviegallery.databinding.ViewHolderLoadStateBinding;

public class MyLoadStateAdapter extends LoadStateAdapter<MyLoadStateAdapter.LoadStateViewHolder> {

    private final View.OnClickListener retryCallback;

    public MyLoadStateAdapter(View.OnClickListener retryCallback) {
        this.retryCallback = retryCallback;
    }

    @Override
    public void onBindViewHolder(@NonNull LoadStateViewHolder loadStateViewHolder, @NonNull LoadState loadState) {

        loadStateViewHolder.bind(loadState);


    }

    @NonNull
    @Override
    public LoadStateViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, @NonNull LoadState loadState) {
        return new LoadStateViewHolder(viewGroup,retryCallback);
    }

    public static class LoadStateViewHolder extends RecyclerView.ViewHolder {

        private final ViewHolderLoadStateBinding mBinding;

        public LoadStateViewHolder(@NonNull ViewGroup parent,View.OnClickListener retryCallback) {
            super(LayoutInflater.from(parent.getContext()).inflate(R.layout.view_holder_load_state, parent, false));
            mBinding = ViewHolderLoadStateBinding.bind(super.itemView);
            mBinding.retryButton.setOnClickListener(retryCallback);
        }

        public void bind(LoadState loadState) {
            if (loadState instanceof LoadState.Error) {
                mBinding.errorMessage.setText(((LoadState.Error) loadState).getError().getLocalizedMessage());
            }
            mBinding.errorMessage.setVisibility(loadState instanceof LoadState.Error?View.VISIBLE:View.GONE);
            mBinding.retryButton.setVisibility(loadState instanceof LoadState.Error?View.VISIBLE:View.GONE);
            mBinding.progressBar.setVisibility(loadState instanceof LoadState.Loading ? View.VISIBLE : View.GONE);

        }
    }
}
