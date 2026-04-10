package com.hocnv.mobile_0410.seed;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;
import com.hocnv.mobile_0410.R;
import com.hocnv.mobile_0410.data.FirestoreRefs;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class SeedDataActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_seed);

        TextView tvInfo = findViewById(R.id.tvInfo);
        Button btnSeed = findViewById(R.id.btnSeed);
        ProgressBar progress = findViewById(R.id.progress);

        tvInfo.setText("Tạo dữ liệu mẫu cho: movies, theaters, showtimes.\n"
                + "Bao gồm: 5 phim, 3 rạp, 12 suất chiếu (có seatMap 10×10).\n"
                + "(Lưu ý: cần cấu hình Firebase + Firestore trước)");

        btnSeed.setOnClickListener(v -> {
            btnSeed.setEnabled(false);
            progress.setVisibility(View.VISIBLE);

            FirebaseFirestore db = FirebaseFirestore.getInstance();
            WriteBatch batch = db.batch();

            // ========== THEATERS ==========
            String[] theaterIds = {"theater001", "theater002", "theater003"};
            seedTheater(batch, db, theaterIds[0], "CGV Vincom Center",
                    "72 Lê Thánh Tôn, Q.1", "Hồ Chí Minh", 100);
            seedTheater(batch, db, theaterIds[1], "Lotte Cinema Nowzone",
                    "235 Nguyễn Văn Cừ, Q.1", "Hồ Chí Minh", 100);
            seedTheater(batch, db, theaterIds[2], "Galaxy Nguyễn Du",
                    "116 Nguyễn Du, Q.1", "Hồ Chí Minh", 100);

            // ========== MOVIES ==========
            String[] movieIds = {"movie001", "movie002", "movie003", "movie004", "movie005"};

            seedMovie(batch, db, movieIds[0],
                    "Avengers: Endgame",
                    "Sau sự tàn phá của Thanos, các siêu anh hùng còn lại phải tập hợp một lần nữa để hoàn tác hành động của hắn và khôi phục trật tự vũ trụ.",
                    "Action", 181, 4.8f,
                    "https://image.tmdb.org/t/p/w500/or06FN3Dka5tukK1e9sl16pB3iy.jpg",
                    "https://www.youtube.com/watch?v=TcMBFSGVi1c",
                    2026, Calendar.APRIL, 1, true);

            seedMovie(batch, db, movieIds[1],
                    "Spider-Man: No Way Home",
                    "Peter Parker tìm kiếm sự giúp đỡ từ Doctor Strange khi danh tính của anh bị tiết lộ, dẫn đến sự hỗn loạn đa vũ trụ.",
                    "Action", 148, 4.7f,
                    "https://image.tmdb.org/t/p/w500/1g0dhYtq4irTY1GPXvft6k4YLjm.jpg",
                    "https://www.youtube.com/watch?v=JfVOs4VSpmA",
                    2026, Calendar.APRIL, 5, true);

            seedMovie(batch, db, movieIds[2],
                    "Dune: Part Two",
                    "Paul Atreides hợp tác với người Fremen để trả thù những kẻ đã phá hủy gia đình anh, đồng thời cố ngăn chặn một tương lai tàn khốc.",
                    "Sci-Fi", 166, 4.6f,
                    "https://image.tmdb.org/t/p/w500/8b8R8l88Qje9dn9OE8PY05Nez7S.jpg",
                    "https://www.youtube.com/watch?v=Way9Dexny3w",
                    2026, Calendar.MARCH, 15, true);

            seedMovie(batch, db, movieIds[3],
                    "Inside Out 2",
                    "Riley bước vào tuổi dậy thì và đối mặt với những cảm xúc mới: Lo Âu, Ghen Tị, Chán Nản và Xấu Hổ.",
                    "Animation", 100, 4.5f,
                    "https://image.tmdb.org/t/p/w500/vpnVM9B6NMmQpWeZvzLvDESb2QY.jpg",
                    "https://www.youtube.com/watch?v=LEjhY15eCx0",
                    2026, Calendar.APRIL, 10, true);

            seedMovie(batch, db, movieIds[4],
                    "Oppenheimer",
                    "Câu chuyện về J. Robert Oppenheimer và vai trò của ông trong việc phát triển bom nguyên tử trong Thế chiến II.",
                    "Drama", 180, 4.9f,
                    "https://image.tmdb.org/t/p/w500/8Gxv8gSFCU0XGDykEGv7zR1n2ua.jpg",
                    "https://www.youtube.com/watch?v=uYPbbksJxIg",
                    2026, Calendar.MARCH, 20, true);

            // ========== SHOWTIMES ==========
            int showtimeCount = 1;
            // {movieIndex, theaterIndex, dayOffset, hour, durationMinutes, priceThousand}
            int[][] schedule = {
                    {0, 0, 1, 14, 181, 90},
                    {0, 1, 1, 19, 181, 100},
                    {1, 0, 1, 16, 148, 85},
                    {1, 2, 2, 20, 148, 95},
                    {2, 1, 2, 14, 166, 90},
                    {2, 2, 2, 19, 166, 100},
                    {3, 0, 3, 10, 100, 75},
                    {3, 1, 3, 15, 100, 80},
                    {4, 2, 1, 18, 180, 110},
                    {4, 0, 3, 20, 180, 105},
                    {0, 2, 4, 14, 181, 90},
                    {1, 1, 4, 19, 148, 95},
            };

            Map<String, Boolean> seatMap = generateSeatMap();

            for (int[] s : schedule) {
                String stId = String.format("showtime%03d", showtimeCount++);
                String mId = movieIds[s[0]];
                String tId = theaterIds[s[1]];
                int dayOffset = s[2];
                int hour = s[3];
                int durationMin = s[4];
                double price = s[5] * 1000.0;

                Calendar calStart = Calendar.getInstance();
                calStart.add(Calendar.DAY_OF_MONTH, dayOffset);
                calStart.set(Calendar.HOUR_OF_DAY, hour);
                calStart.set(Calendar.MINUTE, 0);
                calStart.set(Calendar.SECOND, 0);
                calStart.set(Calendar.MILLISECOND, 0);

                Calendar calEnd = (Calendar) calStart.clone();
                calEnd.add(Calendar.MINUTE, durationMin);

                Timestamp startTime = new Timestamp(calStart.getTime());
                Timestamp endTime = new Timestamp(calEnd.getTime());

                Map<String, Object> showtime = new HashMap<>();
                showtime.put("movieId", mId);
                showtime.put("theaterId", tId);
                showtime.put("startTime", startTime);
                showtime.put("endTime", endTime);
                showtime.put("price", price);
                showtime.put("availableSeats", 100);
                showtime.put("seatMap", new HashMap<>(seatMap));

                batch.set(db.collection(FirestoreRefs.COL_SHOWTIMES).document(stId), showtime);
            }

            // ========== COMMIT ==========
            batch.commit()
                    .addOnSuccessListener(unused -> {
                        progress.setVisibility(View.GONE);
                        Toast.makeText(this,
                                "Seed thành công!\n5 phim, 3 rạp, 12 suất chiếu.",
                                Toast.LENGTH_LONG).show();
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        btnSeed.setEnabled(true);
                        progress.setVisibility(View.GONE);
                        Toast.makeText(this,
                                "Seed thất bại: " + e.getMessage(),
                                Toast.LENGTH_LONG).show();
                    });
        });
    }

    private void seedTheater(WriteBatch batch, FirebaseFirestore db,
                             String id, String name, String address, String city, int totalSeats) {
        Map<String, Object> data = new HashMap<>();
        data.put("name", name);
        data.put("address", address);
        data.put("city", city);
        data.put("totalSeats", totalSeats);
        batch.set(db.collection(FirestoreRefs.COL_THEATERS).document(id), data);
    }

    private void seedMovie(WriteBatch batch, FirebaseFirestore db,
                           String id, String title, String description, String genre,
                           int duration, float rating, String posterUrl, String trailerUrl,
                           int year, int month, int day, boolean isNowShowing) {
        Calendar cal = Calendar.getInstance();
        cal.set(year, month, day, 0, 0, 0);
        cal.set(Calendar.MILLISECOND, 0);

        Map<String, Object> data = new HashMap<>();
        data.put("title", title);
        data.put("description", description);
        data.put("genre", genre);
        data.put("duration", duration);
        data.put("rating", rating);
        data.put("posterUrl", posterUrl);
        data.put("trailerUrl", trailerUrl);
        data.put("releaseDate", new Timestamp(cal.getTime()));
        data.put("isNowShowing", isNowShowing);
        batch.set(db.collection(FirestoreRefs.COL_MOVIES).document(id), data);
    }

    /** Tạo seatMap 10 hàng (A-J) × 10 cột (1-10), tất cả = false (chưa đặt) */
    private Map<String, Boolean> generateSeatMap() {
        Map<String, Boolean> map = new HashMap<>();
        String[] rows = {"A", "B", "C", "D", "E", "F", "G", "H", "I", "J"};
        for (String row : rows) {
            for (int col = 1; col <= 10; col++) {
                map.put(row + col, false);
            }
        }
        return map;
    }
}
