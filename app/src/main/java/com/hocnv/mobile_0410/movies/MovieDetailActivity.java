package com.hocnv.mobile_0410.movies;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.hocnv.mobile_0410.R;
import com.hocnv.mobile_0410.data.FirestoreRefs;
import com.hocnv.mobile_0410.data.models.Movie;
import com.hocnv.mobile_0410.data.models.Showtime;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MovieDetailActivity extends AppCompatActivity {
    public static final String EXTRA_MOVIE_ID = "movie_id";

    private final List<Showtime> showtimes = new ArrayList<>();
    private ShowtimeAdapter adapter;

    private TextView tvTitle;
    private TextView tvDesc;
    private TextView tvDuration;
    private ProgressBar progress;
    private TextView tvEmpty;

    private String movieId;
    private Movie movie;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_movie_detail);

        movieId = getIntent().getStringExtra(EXTRA_MOVIE_ID);
        if (movieId == null || movieId.trim().isEmpty()) {
            Toast.makeText(this, "Thiếu movieId.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        tvTitle = findViewById(R.id.tvTitle);
        tvDesc = findViewById(R.id.tvDesc);
        tvDuration = findViewById(R.id.tvDuration);
        progress = findViewById(R.id.progress);
        tvEmpty = findViewById(R.id.tvEmpty);

        RecyclerView rv = findViewById(R.id.rvShowtimes);
        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ShowtimeAdapter(showtimes, st -> {
            Intent i = new Intent(this, BookTicketActivity.class);
            i.putExtra(BookTicketActivity.EXTRA_MOVIE_ID, movieId);
            i.putExtra(BookTicketActivity.EXTRA_SHOWTIME_ID, st.id);
            i.putExtra(BookTicketActivity.EXTRA_THEATER_ID, st.theaterId);
            i.putExtra(BookTicketActivity.EXTRA_SHOWTIME_START,
                    st.startTime != null ? st.startTime.toDate().getTime() : -1L);
            if (movie != null) i.putExtra(BookTicketActivity.EXTRA_MOVIE_TITLE, movie.title);
            startActivity(i);
        });
        rv.setAdapter(adapter);
    }

    @Override
    protected void onStart() {
        super.onStart();
        loadMovieAndShowtimes();
    }

    private void loadMovieAndShowtimes() {
        progress.setVisibility(View.VISIBLE);
        tvEmpty.setVisibility(View.GONE);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection(FirestoreRefs.COL_MOVIES)
                .document(movieId)
                .get()
                .addOnSuccessListener(doc -> {
                    Movie m = doc.toObject(Movie.class);
                    if (m != null) {
                        m.id = doc.getId();
                        movie = m;
                        tvTitle.setText(m.title == null ? "" : m.title);
                        tvDesc.setText(m.description == null ? "" : m.description);
                        tvDuration.setText("Thời lượng: " + m.duration + " phút");
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Tải phim thất bại: " + e.getMessage(), Toast.LENGTH_LONG).show());

        db.collection(FirestoreRefs.COL_SHOWTIMES)
                .whereEqualTo("movieId", movieId)
                .get()
                .addOnSuccessListener(snap -> {
                    showtimes.clear();
                    for (com.google.firebase.firestore.DocumentSnapshot doc : snap.getDocuments()) {
                        Showtime st = doc.toObject(Showtime.class);
                        if (st == null) continue;
                        st.id = doc.getId();
                        showtimes.add(st);
                    }
                    // Sort theo startTime ở client
                    Collections.sort(showtimes, (a, b) -> {
                        if (a.startTime == null || b.startTime == null) return 0;
                        return a.startTime.compareTo(b.startTime);
                    });
                    adapter.notifyDataSetChanged();
                    progress.setVisibility(View.GONE);
                    tvEmpty.setVisibility(showtimes.isEmpty() ? View.VISIBLE : View.GONE);
                })
                .addOnFailureListener(e -> {
                    progress.setVisibility(View.GONE);
                    Toast.makeText(this, "Tải lịch chiếu thất bại: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    tvEmpty.setVisibility(showtimes.isEmpty() ? View.VISIBLE : View.GONE);
                });
    }
}

