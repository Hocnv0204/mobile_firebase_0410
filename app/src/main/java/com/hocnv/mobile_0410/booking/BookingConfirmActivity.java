package com.hocnv.mobile_0410.booking;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.hocnv.mobile_0410.R;
import com.hocnv.mobile_0410.data.FirestoreRefs;
import com.hocnv.mobile_0410.notifications.ShowtimeReminderWorker;
import com.hocnv.mobile_0410.util.TimeUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class BookingConfirmActivity extends AppCompatActivity {

    public static final String EXTRA_MOVIE_ID = "movie_id";
    public static final String EXTRA_SHOWTIME_ID = "showtime_id";
    public static final String EXTRA_THEATER_ID = "theater_id";
    public static final String EXTRA_MOVIE_TITLE = "movie_title";
    public static final String EXTRA_SEATS = "seats";
    public static final String EXTRA_PRICE = "price";
    public static final String EXTRA_SHOWTIME_START = "showtime_start";

    private String movieId;
    private String showtimeId;
    private String theaterId;
    private String movieTitle;
    private ArrayList<String> seats;
    private double price;
    private long showtimeStart;

    private Button btnBook;
    private ProgressBar progress;
    private String theaterName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_booking_confirm);

        movieId = getIntent().getStringExtra(EXTRA_MOVIE_ID);
        showtimeId = getIntent().getStringExtra(EXTRA_SHOWTIME_ID);
        theaterId = getIntent().getStringExtra(EXTRA_THEATER_ID);
        movieTitle = getIntent().getStringExtra(EXTRA_MOVIE_TITLE);
        seats = getIntent().getStringArrayListExtra(EXTRA_SEATS);
        price = getIntent().getDoubleExtra(EXTRA_PRICE, 0);
        showtimeStart = getIntent().getLongExtra(EXTRA_SHOWTIME_START, -1);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        TextView tvMovieTitle = findViewById(R.id.tvMovieTitle);
        TextView tvShowtime = findViewById(R.id.tvShowtime);
        TextView tvTheater = findViewById(R.id.tvTheater);
        TextView tvSeats = findViewById(R.id.tvSeats);
        TextView tvTotalPrice = findViewById(R.id.tvTotalPrice);
        btnBook = findViewById(R.id.btnBook);
        progress = findViewById(R.id.progress);

        tvMovieTitle.setText(movieTitle != null ? movieTitle : "");
        tvShowtime.setText("Suất chiếu: " + (showtimeStart > 0 ? TimeUtils.formatDateTime(showtimeStart) : ""));
        tvSeats.setText("Ghế: " + (seats != null ? String.join(", ", seats) : ""));

        // Load tên rạp từ Firestore
        if (theaterId != null) {
            FirebaseFirestore.getInstance()
                    .collection(FirestoreRefs.COL_THEATERS)
                    .document(theaterId)
                    .get()
                    .addOnSuccessListener(doc -> {
                        theaterName = doc.getString("name");
                        tvTheater.setText("Rạp: " + (theaterName != null ? theaterName : theaterId));
                    })
                    .addOnFailureListener(e -> tvTheater.setText("Rạp: " + theaterId));
        } else {
            tvTheater.setText("Rạp: ");
        }
        double totalPrice = seats != null ? seats.size() * price : price;
        tvTotalPrice.setText(String.format("Tổng: %,.0fđ", totalPrice));

        btnBook.setOnClickListener(v -> bookTicket());
    }

    private void bookTicket() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "Bạn chưa đăng nhập.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        if (seats == null || seats.isEmpty()) {
            Toast.makeText(this, "Chưa chọn ghế.", Toast.LENGTH_SHORT).show();
            return;
        }

        btnBook.setEnabled(false);
        progress.setVisibility(View.VISIBLE);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference showtimeRef = db.collection(FirestoreRefs.COL_SHOWTIMES).document(showtimeId);

        db.runTransaction(transaction -> {
            DocumentSnapshot snapshot = transaction.get(showtimeRef);

            // Read seatMap
            @SuppressWarnings("unchecked")
            Map<String, Boolean> seatMap = (Map<String, Boolean>) snapshot.get("seatMap");
            if (seatMap == null) {
                throw new RuntimeException("Không tìm thấy sơ đồ ghế.");
            }

            // Check selected seats are still available
            for (String seat : seats) {
                Boolean taken = seatMap.get(seat);
                if (taken != null && taken) {
                    throw new RuntimeException("Ghế " + seat + " đã được đặt.");
                }
            }

            // Update seatMap (set seats to true)
            for (String seat : seats) {
                seatMap.put(seat, true);
            }
            transaction.update(showtimeRef, "seatMap", seatMap);

            // Decrease availableSeats
            Long available = snapshot.getLong("availableSeats");
            int newAvailable = (available != null ? available.intValue() : 0) - seats.size();
            transaction.update(showtimeRef, "availableSeats", Math.max(0, newAvailable));

            // Create ticket doc
            String ticketId = UUID.randomUUID().toString();
            Map<String, Object> ticketData = new HashMap<>();
            ticketData.put("id", ticketId);
            ticketData.put("userId", user.getUid());
            ticketData.put("movieId", movieId);
            ticketData.put("showtimeId", showtimeId);
            ticketData.put("theaterId", theaterId);
            ticketData.put("seats", seats);
            ticketData.put("totalPrice", seats.size() * price);
            ticketData.put("status", "CONFIRMED");
            ticketData.put("bookingTime", Timestamp.now());
            ticketData.put("fcmToken", null);

            DocumentReference ticketRef = db.collection(FirestoreRefs.COL_TICKETS).document(ticketId);
            transaction.set(ticketRef, ticketData);

            return ticketId;
        }).addOnSuccessListener(ticketId -> {
            progress.setVisibility(View.GONE);
            scheduleReminder(movieTitle, showtimeStart, theaterName);
            Intent intent = new Intent(this, BookingSuccessActivity.class);
            intent.putExtra(BookingSuccessActivity.EXTRA_TICKET_ID, ticketId);
            startActivity(intent);
            finish();
        }).addOnFailureListener(e -> {
            btnBook.setEnabled(true);
            progress.setVisibility(View.GONE);
            Toast.makeText(this, "Đặt vé thất bại: " + e.getMessage(), Toast.LENGTH_LONG).show();
        });
    }

    private void scheduleReminder(String title, long showtimeMillis) {
        long now = System.currentTimeMillis();
        long remindAt = showtimeMillis - TimeUnit.HOURS.toMillis(1);
        long delay = Math.max(0, remindAt - now);

        Data input = new Data.Builder()
                .putString(ShowtimeReminderWorker.KEY_MOVIE_TITLE, title == null ? "" : title)
                .putLong(ShowtimeReminderWorker.KEY_SHOWTIME_MILLIS, showtimeMillis)
                .build();

        OneTimeWorkRequest req = new OneTimeWorkRequest.Builder(ShowtimeReminderWorker.class)
                .setInputData(input)
                .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                .addTag("showtime_reminder")
                .build();

        WorkManager.getInstance(this).enqueue(req);
    }
}
