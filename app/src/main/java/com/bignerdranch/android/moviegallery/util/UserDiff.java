package com.bignerdranch.android.moviegallery.util;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;

import com.bignerdranch.android.moviegallery.integration.model.User;

public class UserDiff extends DiffUtil.ItemCallback<User>{
    @Override
    public boolean areItemsTheSame(@NonNull User oldItem, @NonNull User newItem) {
        return oldItem.getUid().equals(newItem.getUid());
    }

    @Override
    public boolean areContentsTheSame(@NonNull User oldItem, @NonNull User newItem) {
        return oldItem.equals(newItem);
    }
}
