package com.bignerdranch.android.moviegallery.util;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;

import com.bignerdranch.android.moviegallery.http.model.Movie;

public class MovieDiff extends DiffUtil.ItemCallback<Movie>{
    @Override
    public boolean areItemsTheSame(@NonNull Movie oldItem, @NonNull Movie newItem) {
        return oldItem.getId().equals(newItem.getId());
    }

    @Override
    public boolean areContentsTheSame(@NonNull Movie oldItem, @NonNull Movie newItem) {
        return oldItem.getId().equals(newItem.getId());

    }
}
