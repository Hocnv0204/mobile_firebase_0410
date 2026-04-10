package com.hocnv.mobile_0410.home;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.hocnv.mobile_0410.R;
import com.hocnv.mobile_0410.data.FirestoreRefs;
import com.hocnv.mobile_0410.data.models.Movie;
import com.hocnv.mobile_0410.movies.MovieAdapter;
import com.hocnv.mobile_0410.movies.MovieDetailActivity;
import com.hocnv.mobile_0410.seed.SeedDataActivity;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private final List<Movie> movies = new ArrayList<>();
    private MovieAdapter adapter;

    private ProgressBar progress;
    private TextView tvEmpty;
    private RecyclerView rvMovies;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        progress = view.findViewById(R.id.progress);
        tvEmpty = view.findViewById(R.id.tvEmpty);
        rvMovies = view.findViewById(R.id.rvMovies);
        Button btnSeed = view.findViewById(R.id.btnSeed);

        rvMovies.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new MovieAdapter(movies, movie -> {
            Intent intent = new Intent(requireContext(), MovieDetailActivity.class);
            intent.putExtra(MovieDetailActivity.EXTRA_MOVIE_ID, movie.id);
            startActivity(intent);
        });
        rvMovies.setAdapter(adapter);

        btnSeed.setOnClickListener(v ->
                startActivity(new Intent(requireContext(), SeedDataActivity.class)));
    }

    @Override
    public void onStart() {
        super.onStart();
        loadMovies();
    }

    private void loadMovies() {
        progress.setVisibility(View.VISIBLE);
        tvEmpty.setVisibility(View.GONE);

        FirebaseFirestore.getInstance()
                .collection(FirestoreRefs.COL_MOVIES)
                .whereEqualTo("isNowShowing", true)
                .get()
                .addOnSuccessListener(snap -> {
                    movies.clear();
                    for (DocumentSnapshot doc : snap.getDocuments()) {
                        Movie m = doc.toObject(Movie.class);
                        if (m != null) {
                            m.id = doc.getId();
                            movies.add(m);
                        }
                    }
                    adapter.notifyDataSetChanged();
                    progress.setVisibility(View.GONE);
                    tvEmpty.setVisibility(movies.isEmpty() ? View.VISIBLE : View.GONE);
                })
                .addOnFailureListener(e -> {
                    progress.setVisibility(View.GONE);
                    tvEmpty.setVisibility(movies.isEmpty() ? View.VISIBLE : View.GONE);
                    Toast.makeText(requireContext(),
                            "Tải phim thất bại: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                });
    }
}
