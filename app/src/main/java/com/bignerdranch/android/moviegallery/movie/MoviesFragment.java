package com.bignerdranch.android.moviegallery.movie;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.paging.CombinedLoadStates;
import androidx.paging.LoadState;
import androidx.paging.PagingDataAdapter;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.bignerdranch.android.moviegallery.BaseFragment;
import com.bignerdranch.android.moviegallery.MyLoadStateAdapter;
import com.bignerdranch.android.moviegallery.R;
import com.bignerdranch.android.moviegallery.databinding.FragmentMoviesBinding;
import com.bignerdranch.android.moviegallery.integration.model.Movie;
import com.bignerdranch.android.moviegallery.util.MovieDiff;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import autodispose2.AutoDispose;
import autodispose2.androidx.lifecycle.AndroidLifecycleScopeProvider;
import dagger.hilt.android.AndroidEntryPoint;
import kotlin.Unit;
import kotlin.jvm.functions.Function1;

/**
 * do not remove me , here is a good example for paging 3
 * <p>
 * A simple {@link Fragment} subclass.
 * create an instance of this fragment.
 */
@AndroidEntryPoint
@kotlinx.coroutines.ExperimentalCoroutinesApi
public class MoviesFragment extends BaseFragment {
    private static final String TAG = "MoviesFragment";
    public static final String PREFER_QUERY = "prefer_query";

    private MovieViewModel mMovieViewModel;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "onCreate");
        setHasOptionsMenu(true);


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.i(TAG, "onCreateView");
        View layout = inflater.inflate(R.layout.fragment_movies, container, false);

        RecyclerView recycler_view = layout.findViewById(R.id.recycler_view);

        MovieAdapter adapter = new MovieAdapter(new MovieDiff());

        recycler_view.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL));
        recycler_view.setAdapter(adapter.withLoadStateFooter(new MyLoadStateAdapter(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                adapter.retry();
            }
        })));

        recycler_view.setLayoutManager(new LinearLayoutManager(getActivity()));

        addDefaultBlankView(layout, adapter);

        mMovieViewModel = new ViewModelProvider(this).get(MovieViewModel.class);

        mMovieViewModel.getFlowable()
                .to(AutoDispose.autoDisposable(AndroidLifecycleScopeProvider.from(this)))
                .subscribe(
                        s -> {
                            adapter.submitData(getLifecycle(), s);
                        }
                );

        return layout;
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "onDestroy");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.i(TAG, "onDestroyView");
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.i(TAG, "resumed");

    }

    @Override
    public void onPause() {
        super.onPause();
        Log.i(TAG, "paused");
    }

    private void addDefaultBlankView(View layout, MovieAdapter adapter) {
        FragmentMoviesBinding binding = FragmentMoviesBinding.bind(layout);
        binding.retryButton.setOnClickListener(v ->
                adapter.retry());

        adapter.addLoadStateListener(new Function1<CombinedLoadStates, Unit>() {
            @Override
            public Unit invoke(CombinedLoadStates combinedLoadStates) {

                boolean isEmpty = !(combinedLoadStates.getRefresh() instanceof LoadState.NotLoading)
                        && adapter.getItemCount() == 0;
                binding.emptyList.setVisibility(isEmpty ? View.VISIBLE : View.GONE);

                binding.progressBar.setVisibility(
                        combinedLoadStates.getRefresh() instanceof LoadState.Loading ?
                                View.VISIBLE : View.GONE
                );

                binding.retryButton.setVisibility(
                        combinedLoadStates.getRefresh() instanceof LoadState.Error ?
                                View.VISIBLE : View.GONE
                );

                binding.recyclerView.setVisibility(
                        combinedLoadStates.getRefresh() instanceof LoadState.NotLoading?
                                View.VISIBLE : View.GONE

                );

                LoadState loadState;
                if ((loadState=combinedLoadStates.getRefresh()) instanceof LoadState.Error
                ||(loadState=combinedLoadStates.getAppend()) instanceof LoadState.Error
                ||(loadState=combinedLoadStates.getPrepend()) instanceof LoadState.Error
                ){
                    LoadState.Error error=(LoadState.Error)loadState;
                    Toast.makeText(getContext(),"error: "+error.getError().getLocalizedMessage(), Toast.LENGTH_LONG).show();
                }


                return Unit.INSTANCE;
            }
        });
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.overflow_menu, menu);

        MenuItem item = menu.findItem(R.id.search_menu_item);
        SearchView searchView = (SearchView) item.getActionView();
        searchView.setSubmitButtonEnabled(true);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                MovieViewModel.MoviePagingSource moviePagingSource = mMovieViewModel.getMoviePagingSource();
                mMovieViewModel.setQuery(query);

                PreferenceManager.getDefaultSharedPreferences(getActivity())
                        .edit()
                        .putString(PREFER_QUERY, query)
                        .apply();

                if (moviePagingSource != null) {
                    moviePagingSource.invalidate();
                    searchView.onActionViewCollapsed();
                    return true;
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                String query = PreferenceManager.getDefaultSharedPreferences(getActivity())
                        .getString(PREFER_QUERY, "");
                searchView.setQuery(query, false);
                return false;
            }
        });
    }


    private class MovieAdapter extends PagingDataAdapter<Movie, MovieViewHolder> {

        public MovieAdapter(@NonNull DiffUtil.ItemCallback<Movie> diffCallback) {
            super(diffCallback);
        }

        @NonNull
        @Override
        public MovieViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View layout = LayoutInflater.from(getActivity()).inflate(R.layout.view_holder_movie, parent, false);
            return new MovieViewHolder(layout);
        }

        @Override
        public void onBindViewHolder(@NonNull MovieViewHolder holder, int position) {
            Movie item = getItem(position);
            holder.bind(item);
        }
    }

    private class MovieViewHolder extends RecyclerView.ViewHolder {

        private final com.bignerdranch.android.moviegallery.databinding.ViewHolderMovieBinding mMovieBinding;

        public MovieViewHolder(@NonNull View itemView) {
            super(itemView);
            mMovieBinding = com.bignerdranch.android.moviegallery.databinding.ViewHolderMovieBinding.bind(itemView);

        }


        public void bind(Movie item) {
            if (item == null) {
                return;
            }
            Glide.with(requireContext())
                    .load("https://image.tmdb.org/t/p/w500" + item.getPoster_path())
//                    .placeholder(R.drawable.ic_image_placeholder)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(mMovieBinding.posterImage);

            mMovieBinding.movieTitle.setText(item.getTitle());
            mMovieBinding.releaseDate.setText(item.getRelease_date());
            mMovieBinding.popularity.setText(String.valueOf(item.getPopularity()));
            mMovieBinding.voteAverage.setText(String.valueOf(item.getVote_average()) + "/" + String.valueOf(item.getVote_count()));
        }
    }
}