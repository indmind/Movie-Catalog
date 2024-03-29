package com.indmind.moviecataloguetwo.fragments;


import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.TextView;

import com.indmind.moviecataloguetwo.R;
import com.indmind.moviecataloguetwo.adapters.ListMovieAdapter;
import com.indmind.moviecataloguetwo.models.Movie;
import com.indmind.moviecataloguetwo.viewmodels.DiscoverMoviesViewModel;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import jp.wasabeef.recyclerview.adapters.ScaleInAnimationAdapter;

/**
 * A simple {@link Fragment} subclass.
 */
public class MoviesFragment extends Fragment implements SearchView.OnQueryTextListener {
    private final int page_number = 2;
    @BindView(R.id.rv_movie_container)
    RecyclerView movieContainer;
    @BindView(R.id.pb_movie)
    ProgressBar pbMovie;
    @BindView(R.id.sv_movie)
    SearchView svMovie;
    @BindView(R.id.tv_movie_not_found)
    TextView tvNotFound;
    private ListMovieAdapter mAdapter;
    private final Observer<ArrayList<Movie>> moviesChangeObserver = new Observer<ArrayList<Movie>>() {
        @Override
        public void onChanged(@Nullable ArrayList<Movie> movies) {
            if (movies != null) {
                mAdapter.setMovies(movies);
                setProgressBarVisibility(false);

                if (movies.size() <= 0) {
                    tvNotFound.setVisibility(View.VISIBLE);
                } else {
                    tvNotFound.setVisibility(View.GONE);
                }
            }
        }
    };
    private DiscoverMoviesViewModel mMovieViewModel;
    private Handler mHandler;

    public MoviesFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_movies, container, false);

        ButterKnife.bind(this, view);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mAdapter = new ListMovieAdapter(getActivity());
        mHandler = new Handler();

        movieContainer.setLayoutManager(new LinearLayoutManager(getContext()));
        movieContainer.setAdapter(new ScaleInAnimationAdapter(mAdapter));

        svMovie.setOnQueryTextListener(this);

        mMovieViewModel = ViewModelProviders.of(this).get(DiscoverMoviesViewModel.class);
        mMovieViewModel.getAllMovies().observe(getViewLifecycleOwner(), moviesChangeObserver);

        if (mAdapter.getItemCount() <= 0 && (tvNotFound.getVisibility() == View.GONE)) {
            mMovieViewModel.loadMovies(page_number);
        }
    }

    private void setProgressBarVisibility(Boolean state) {
        if (state) pbMovie.setVisibility(View.VISIBLE);
        else pbMovie.setVisibility(View.GONE);
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        if (query.trim().isEmpty()) return false;

        setProgressBarVisibility(true);
        mAdapter.clearMovies();
        tvNotFound.setVisibility(View.GONE);

        mMovieViewModel.searchMovies(query);

        movieContainer.smoothScrollToPosition(0);
        return true;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        mHandler.removeCallbacksAndMessages(null);

        mHandler.postDelayed(() -> {
            if (TextUtils.isEmpty(newText)) {
                mMovieViewModel.loadMovies(page_number);
            } else {
                mMovieViewModel.searchMovies(newText);
            }
        }, 300);

        return true;
    }
}
