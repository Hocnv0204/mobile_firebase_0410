package com.hocnv.mobile_0410.movies;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.hocnv.mobile_0410.R;
import com.hocnv.mobile_0410.data.FirestoreRefs;
import com.hocnv.mobile_0410.data.models.Ticket;
import com.hocnv.mobile_0410.notifications.ShowtimeReminderWorker;
import com.hocnv.mobile_0410.util.TimeUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class BookTicketActivity extends AppCompatActivity {
    public static final String EXTRA_MOVIE_ID = "movie_id";
    public static final String EXTRA_SHOWTIME_ID = "showtime_id";
    public static final String EXTRA_THEATER_ID = "theater_id";
    public static final String EXTRA_SHOWTIME_START = "showtime_start";
    public static final String EXTRA_MOVIE_TITLE = "movie_title";

    private String movieId;
    private String showtimeId;
    private String theaterId;
    private long showtimeStart;
    private String movieTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_book_ticket);

        movieId = getIntent().getStringExtra(EXTRA_MOVIE_ID);
        showtimeId = getIntent().getStringExtra(EXTRA_SHOWTIME_ID);
        theaterId = getIntent().getStringExtra(EXTRA_THEATER_ID);
        showtimeStart = getIntent().getLongExtra(EXTRA_SHOWTIME_START, -1);
        movieTitle = getIntent().getStringExtra(EXTRA_MOVIE_TITLE);

        TextView tvInfo = findViewById(R.id.tvInfo);
        EditText etQuantity = findViewById(R.id.etQuantity);
        Button btnBook = findViewById(R.id.btnBook);
        ProgressBar progress = findViewById(R.id.progress);

        tvInfo.setText(
                "Phim: " + (movieTitle == null ? "" : movieTitle) +
                        "\nSuất chiếu: " + (showtimeStart > 0 ? TimeUtils.formatDateTime(showtimeStart) : "") +
                        "\nRạp: " + (theaterId == null ? "" : theaterId)
        );

        btnBook.setOnClickListener(v -> {
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if (user == null) {
                Toast.makeText(this, "Bạn chưa đăng nhập.", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }
            if (TextUtils.isEmpty(movieId) || TextUtils.isEmpty(showtimeId) || TextUtils.isEmpty(theaterId) || showtimeStart <= 0) {
                Toast.makeText(this, "Thiếu dữ liệu đặt vé.", Toast.LENGTH_SHORT).show();
                return;
            }

            String qRaw = etQuantity.getText() == null ? "" : etQuantity.getText().toString().trim();
            int q;
            try {
                q = Integer.parseInt(qRaw);
            } catch (Exception e) {
                q = 0;
            }
            if (q <= 0) {
                Toast.makeText(this, "Số lượng vé phải > 0.", Toast.LENGTH_SHORT).show();
                return;
            }

            btnBook.setEnabled(false);
            progress.setVisibility(View.VISIBLE);

            // Tạo danh sách ghế tạm (sẽ thay bằng SeatSelectionActivity sau)
            List<String> seats = new ArrayList<>();
            for (int i = 1; i <= q; i++) {
                seats.add("A" + i);
            }
            double totalPrice = q * 90000.0;

            String ticketId = UUID.randomUUID().toString();
            Ticket ticket = new Ticket(
                    ticketId,
                    user.getUid(),
                    movieId,
                    showtimeId,
                    theaterId,
                    seats,
                    totalPrice,
                    "CONFIRMED",
                    Timestamp.now(),
                    null
            );

            FirebaseFirestore.getInstance()
                    .collection(FirestoreRefs.COL_TICKETS)
                    .document(ticketId)
                    .set(ticket)
                    .addOnSuccessListener(unused -> {
                        scheduleReminder(movieTitle, showtimeStart);
                        progress.setVisibility(View.GONE);
                        Toast.makeText(this, "Đặt vé thành công!", Toast.LENGTH_LONG).show();
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        btnBook.setEnabled(true);
                        progress.setVisibility(View.GONE);
                        Toast.makeText(this, "Đặt vé thất bại: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    });
        });
    }

    private void scheduleReminder(String title, long showtimeMillis) {
        // Nhắc trước 30 phút; nếu đã sát/qua giờ thì nhắc ngay.
        long now = System.currentTimeMillis();
        long remindAt = showtimeMillis - TimeUnit.MINUTES.toMillis(30);
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

