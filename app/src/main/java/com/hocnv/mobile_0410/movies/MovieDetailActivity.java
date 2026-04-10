package com.hocnv.mobile_0410.movies;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.FirebaseFirestore;
import com.hocnv.mobile_0410.R;
import com.hocnv.mobile_0410.booking.ShowtimeActivity;
import com.hocnv.mobile_0410.data.FirestoreRefs;
import com.hocnv.mobile_0410.data.models.Movie;

public class MovieDetailActivity extends AppCompatActivity {
    public static final String EXTRA_MOVIE_ID = "movie_id";

    private String movieId;

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

        ImageView ivPoster = findViewById(R.id.ivPoster);
        TextView tvTitle = findViewById(R.id.tvTitle);
        TextView tvGenre = findViewById(R.id.tvGenre);
        TextView tvRating = findViewById(R.id.tvRating);
        TextView tvDuration = findViewById(R.id.tvDuration);
        TextView tvDesc = findViewById(R.id.tvDesc);
        Button btnShowtimes = findViewById(R.id.btnShowtimes);
        ProgressBar progress = findViewById(R.id.progress);

        btnShowtimes.setVisibility(View.GONE);

        FirebaseFirestore.getInstance()
                .collection(FirestoreRefs.COL_MOVIES)
                .document(movieId)
                .get()
                .addOnSuccessListener(doc -> {
                    progress.setVisibility(View.GONE);
                    Movie m = doc.toObject(Movie.class);
                    if (m == null) {
                        Toast.makeText(this, "Không tìm thấy phim.", Toast.LENGTH_SHORT).show();
                        finish();
                        return;
                    }
                    m.id = doc.getId();

                    tvTitle.setText(m.title != null ? m.title : "");
                    tvGenre.setText(m.genre != null ? m.genre : "");
                    tvRating.setText("⭐ " + m.rating);
                    tvDuration.setText("Thời lượng: " + m.duration + " phút");
                    tvDesc.setText(m.description != null ? m.description : "");

                    if (m.posterUrl != null && !m.posterUrl.isEmpty()) {
                        Glide.with(this).load(m.posterUrl).into(ivPoster);
                    }

                    btnShowtimes.setVisibility(View.VISIBLE);
                    btnShowtimes.setOnClickListener(v -> {
                        Intent i = new Intent(this, ShowtimeActivity.class);
                        i.putExtra(ShowtimeActivity.EXTRA_MOVIE_ID, movieId);
                        i.putExtra(ShowtimeActivity.EXTRA_MOVIE_TITLE, m.title);
                        startActivity(i);
                    });
                })
                .addOnFailureListener(e -> {
                    progress.setVisibility(View.GONE);
                    Toast.makeText(this, "Tải phim thất bại: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }
}
