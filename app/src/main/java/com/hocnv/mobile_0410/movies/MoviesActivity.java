package com.hocnv.mobile_0410.movies;

import android.content.Intent;
import android.os.Bundle;
import android.os.Build;
import android.Manifest;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.hocnv.mobile_0410.R;
import com.hocnv.mobile_0410.data.FirestoreRefs;
import com.hocnv.mobile_0410.data.models.Movie;
import com.hocnv.mobile_0410.seed.SeedDataActivity;

import java.util.ArrayList;
import java.util.List;

public class MoviesActivity extends AppCompatActivity {
    private final List<Movie> items = new ArrayList<>();
    private MovieAdapter adapter;

    private ProgressBar progress;
    private TextView tvEmpty;

    private static final int REQ_NOTI = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_movies);

        setSupportActionBar(findViewById(R.id.toolbar));

        progress = findViewById(R.id.progress);
        tvEmpty = findViewById(R.id.tvEmpty);

        RecyclerView rv = findViewById(R.id.rvMovies);
        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new MovieAdapter(items, movie -> {
            Intent i = new Intent(this, MovieDetailActivity.class);
            i.putExtra(MovieDetailActivity.EXTRA_MOVIE_ID, movie.id);
            startActivity(i);
        });
        rv.setAdapter(adapter);

        Button btnSeed = findViewById(R.id.btnSeed);
        btnSeed.setOnClickListener(v -> startActivity(new Intent(this, SeedDataActivity.class)));

        Button btnLogout = findViewById(R.id.btnLogout);
        btnLogout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            finish();
        });

        maybeRequestNotificationPermission();
    }

    private void maybeRequestNotificationPermission() {
        if (Build.VERSION.SDK_INT < 33) return;
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                == android.content.pm.PackageManager.PERMISSION_GRANTED) {
            return;
        }
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, REQ_NOTI);
    }

    @Override
    protected void onStart() {
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
                    items.clear();
                    for (com.google.firebase.firestore.DocumentSnapshot doc : snap.getDocuments()) {
                        Movie m = doc.toObject(Movie.class);
                        if (m == null) continue;
                        m.id = doc.getId();
                        items.add(m);
                    }
                    adapter.notifyDataSetChanged();
                    progress.setVisibility(View.GONE);
                    tvEmpty.setVisibility(items.isEmpty() ? View.VISIBLE : View.GONE);
                })
                .addOnFailureListener(e -> {
                    progress.setVisibility(View.GONE);
                    Toast.makeText(this, "Tải phim thất bại: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    tvEmpty.setVisibility(items.isEmpty() ? View.VISIBLE : View.GONE);
                });
    }

}

