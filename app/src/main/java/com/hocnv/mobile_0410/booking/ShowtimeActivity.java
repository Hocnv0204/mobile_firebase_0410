package com.hocnv.mobile_0410.booking;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.hocnv.mobile_0410.R;
import com.hocnv.mobile_0410.data.FirestoreRefs;
import com.hocnv.mobile_0410.data.models.Showtime;
import com.hocnv.mobile_0410.movies.ShowtimeAdapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class ShowtimeActivity extends AppCompatActivity {

    public static final String EXTRA_MOVIE_ID = "movie_id";
    public static final String EXTRA_MOVIE_TITLE = "movie_title";

    private ProgressBar progress;
    private TextView tvEmpty;
    private RecyclerView rvShowtimes;

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final List<Map<String, Object>> itemList = new ArrayList<>();
    private ShowtimeAdapter adapter;

    private String movieId;
    private String movieTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_showtime);

        movieId = getIntent().getStringExtra(EXTRA_MOVIE_ID);
        movieTitle = getIntent().getStringExtra(EXTRA_MOVIE_TITLE);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        progress = findViewById(R.id.progress);
        tvEmpty = findViewById(R.id.tvEmpty);
        rvShowtimes = findViewById(R.id.rvShowtimes);

        rvShowtimes.setLayoutManager(new LinearLayoutManager(this));

        adapter = new ShowtimeAdapter(itemList, this::onShowtimeClick);
        rvShowtimes.setAdapter(adapter);

        loadShowtimes();
    }

    private void loadShowtimes() {
        progress.setVisibility(View.VISIBLE);
        tvEmpty.setVisibility(View.GONE);
        rvShowtimes.setVisibility(View.GONE);

        db.collection(FirestoreRefs.COL_SHOWTIMES)
                .whereEqualTo("movieId", movieId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<Showtime> showtimes = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        Showtime st = doc.toObject(Showtime.class);
                        st.id = doc.getId();
                        showtimes.add(st);
                    }

                    if (showtimes.isEmpty()) {
                        progress.setVisibility(View.GONE);
                        tvEmpty.setVisibility(View.VISIBLE);
                        return;
                    }

                    // Sort by startTime client-side
                    Collections.sort(showtimes, (a, b) -> {
                        if (a.startTime == null && b.startTime == null) return 0;
                        if (a.startTime == null) return 1;
                        if (b.startTime == null) return -1;
                        return a.startTime.compareTo(b.startTime);
                    });

                    // Load theater names for each showtime
                    AtomicInteger remaining = new AtomicInteger(showtimes.size());
                    Map<String, String> theaterNameCache = new HashMap<>();

                    for (Showtime st : showtimes) {
                        if (theaterNameCache.containsKey(st.theaterId)) {
                            Map<String, Object> item = new HashMap<>();
                            item.put("showtime", st);
                            item.put("theaterName", theaterNameCache.get(st.theaterId));
                            itemList.add(item);
                            if (remaining.decrementAndGet() == 0) {
                                onAllLoaded();
                            }
                        } else {
                            db.collection(FirestoreRefs.COL_THEATERS)
                                    .document(st.theaterId)
                                    .get()
                                    .addOnSuccessListener(theaterDoc -> {
                                        String name = theaterDoc.exists()
                                                ? theaterDoc.getString("name")
                                                : st.theaterId;
                                        theaterNameCache.put(st.theaterId, name);

                                        Map<String, Object> item = new HashMap<>();
                                        item.put("showtime", st);
                                        item.put("theaterName", name);
                                        itemList.add(item);

                                        if (remaining.decrementAndGet() == 0) {
                                            onAllLoaded();
                                        }
                                    })
                                    .addOnFailureListener(e -> {
                                        Map<String, Object> item = new HashMap<>();
                                        item.put("showtime", st);
                                        item.put("theaterName", st.theaterId);
                                        itemList.add(item);

                                        if (remaining.decrementAndGet() == 0) {
                                            onAllLoaded();
                                        }
                                    });
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    progress.setVisibility(View.GONE);
                    tvEmpty.setVisibility(View.VISIBLE);
                });
    }

    private void onAllLoaded() {
        // Re-sort itemList to maintain startTime order after async theater loads
        Collections.sort(itemList, (a, b) -> {
            Showtime sa = (Showtime) a.get("showtime");
            Showtime sb = (Showtime) b.get("showtime");
            if (sa.startTime == null && sb.startTime == null) return 0;
            if (sa.startTime == null) return 1;
            if (sb.startTime == null) return -1;
            return sa.startTime.compareTo(sb.startTime);
        });

        progress.setVisibility(View.GONE);
        rvShowtimes.setVisibility(View.VISIBLE);
        adapter.notifyDataSetChanged();
    }

    private void onShowtimeClick(Showtime st) {
        Intent intent = new Intent(this, SeatSelectionActivity.class);
        intent.putExtra(SeatSelectionActivity.EXTRA_SHOWTIME_ID, st.id);
        intent.putExtra(SeatSelectionActivity.EXTRA_MOVIE_ID, movieId);
        intent.putExtra(SeatSelectionActivity.EXTRA_THEATER_ID, st.theaterId);
        intent.putExtra(SeatSelectionActivity.EXTRA_MOVIE_TITLE, movieTitle);
        intent.putExtra(SeatSelectionActivity.EXTRA_PRICE, st.price);
        intent.putExtra(SeatSelectionActivity.EXTRA_SHOWTIME_START,
                st.startTime != null ? st.startTime.toDate().getTime() : 0L);
        startActivity(intent);
    }
}
