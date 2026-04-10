package com.hocnv.mobile_0410.booking;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.hocnv.mobile_0410.R;
import com.hocnv.mobile_0410.data.FirestoreRefs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SeatSelectionActivity extends AppCompatActivity {

    public static final String EXTRA_SHOWTIME_ID = "showtime_id";
    public static final String EXTRA_MOVIE_ID = "movie_id";
    public static final String EXTRA_THEATER_ID = "theater_id";
    public static final String EXTRA_MOVIE_TITLE = "movie_title";
    public static final String EXTRA_PRICE = "price";
    public static final String EXTRA_SHOWTIME_START = "showtime_start";

    private static final int MAX_SELECTED = 5;
    private static final int TOTAL_SEATS = 100;
    private static final int COLUMNS = 10;
    private static final String[] ROW_LABELS = {"A", "B", "C", "D", "E", "F", "G", "H", "I", "J"};

    private GridView gvSeats;
    private TextView tvSelected;
    private Button btnConfirm;

    private String showtimeId;
    private String movieId;
    private String theaterId;
    private String movieTitle;
    private double price;
    private long showtimeStart;

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private ListenerRegistration seatListener;

    private final List<String> seatLabels = new ArrayList<>();
    private Map<String, Boolean> seatMap = new HashMap<>();
    private final Set<String> selectedSeats = new HashSet<>();

    private SeatAdapter seatAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seat_selection);

        showtimeId = getIntent().getStringExtra(EXTRA_SHOWTIME_ID);
        movieId = getIntent().getStringExtra(EXTRA_MOVIE_ID);
        theaterId = getIntent().getStringExtra(EXTRA_THEATER_ID);
        movieTitle = getIntent().getStringExtra(EXTRA_MOVIE_TITLE);
        price = getIntent().getDoubleExtra(EXTRA_PRICE, 0);
        showtimeStart = getIntent().getLongExtra(EXTRA_SHOWTIME_START, 0);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        gvSeats = findViewById(R.id.gvSeats);
        tvSelected = findViewById(R.id.tvSelected);
        btnConfirm = findViewById(R.id.btnConfirm);

        // Build seat labels A1-J10
        for (String row : ROW_LABELS) {
            for (int col = 1; col <= COLUMNS; col++) {
                seatLabels.add(row + col);
            }
        }

        seatAdapter = new SeatAdapter();
        gvSeats.setAdapter(seatAdapter);

        gvSeats.setOnItemClickListener((parent, view, position, id) -> {
            String label = seatLabels.get(position);

            // If seat is booked, ignore click
            Boolean booked = seatMap.get(label);
            if (booked != null && booked) {
                return;
            }

            if (selectedSeats.contains(label)) {
                selectedSeats.remove(label);
            } else {
                if (selectedSeats.size() >= MAX_SELECTED) {
                    Toast.makeText(this, "T\u1ED1i \u0111a " + MAX_SELECTED + " gh\u1EBF",
                            Toast.LENGTH_SHORT).show();
                    return;
                }
                selectedSeats.add(label);
            }

            updateSelectedUI();
            seatAdapter.notifyDataSetChanged();
        });

        btnConfirm.setOnClickListener(v -> onConfirm());

        listenSeatMap();
    }

    private void listenSeatMap() {
        DocumentReference docRef = db.collection(FirestoreRefs.COL_SHOWTIMES).document(showtimeId);
        seatListener = docRef.addSnapshotListener((snapshot, error) -> {
            if (error != null || snapshot == null || !snapshot.exists()) return;

            @SuppressWarnings("unchecked")
            Map<String, Boolean> map = (Map<String, Boolean>) snapshot.get("seatMap");
            if (map != null) {
                seatMap = map;
            } else {
                seatMap = new HashMap<>();
            }

            // Remove any selected seats that have been booked by others
            selectedSeats.removeIf(s -> {
                Boolean b = seatMap.get(s);
                return b != null && b;
            });

            updateSelectedUI();
            seatAdapter.notifyDataSetChanged();
        });
    }

    private void updateSelectedUI() {
        if (selectedSeats.isEmpty()) {
            tvSelected.setText("Gh\u1EBF \u0111\u00E3 ch\u1ECDn: ch\u01B0a ch\u1ECDn");
            btnConfirm.setEnabled(false);
        } else {
            List<String> sorted = new ArrayList<>(selectedSeats);
            sorted.sort(String::compareTo);
            tvSelected.setText("Gh\u1EBF \u0111\u00E3 ch\u1ECDn: " + String.join(", ", sorted));
            btnConfirm.setEnabled(true);
        }
    }

    private void onConfirm() {
        ArrayList<String> seats = new ArrayList<>(selectedSeats);
        seats.sort(String::compareTo);

        Intent intent = new Intent(this, BookingConfirmActivity.class);
        intent.putExtra(BookingConfirmActivity.EXTRA_SHOWTIME_ID, showtimeId);
        intent.putExtra(BookingConfirmActivity.EXTRA_MOVIE_ID, movieId);
        intent.putExtra(BookingConfirmActivity.EXTRA_THEATER_ID, theaterId);
        intent.putExtra(BookingConfirmActivity.EXTRA_MOVIE_TITLE, movieTitle);
        intent.putExtra(BookingConfirmActivity.EXTRA_PRICE, price);
        intent.putExtra(BookingConfirmActivity.EXTRA_SHOWTIME_START, showtimeStart);
        intent.putStringArrayListExtra(BookingConfirmActivity.EXTRA_SEATS, seats);
        startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (seatListener != null) {
            seatListener.remove();
        }
    }

    // ---- Inner adapter for GridView ----
    private class SeatAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return seatLabels.size();
        }

        @Override
        public Object getItem(int position) {
            return seatLabels.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            TextView tv;
            if (convertView instanceof TextView) {
                tv = (TextView) convertView;
            } else {
                tv = new TextView(SeatSelectionActivity.this);
                tv.setGravity(android.view.Gravity.CENTER);
                tv.setPadding(4, 12, 4, 12);
                tv.setTextSize(11);
            }

            String label = seatLabels.get(position);
            tv.setText(label);

            Boolean booked = seatMap.get(label);
            if (booked != null && booked) {
                // Booked - red
                tv.setBackgroundColor(Color.parseColor("#F44336"));
                tv.setTextColor(Color.WHITE);
            } else if (selectedSeats.contains(label)) {
                // Selected by user - green
                tv.setBackgroundColor(Color.parseColor("#4CAF50"));
                tv.setTextColor(Color.WHITE);
            } else {
                // Available - white
                tv.setBackgroundColor(Color.WHITE);
                tv.setTextColor(Color.BLACK);
            }

            return tv;
        }
    }
}
