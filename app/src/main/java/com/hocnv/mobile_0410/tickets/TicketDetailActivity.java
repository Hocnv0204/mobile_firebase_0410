package com.hocnv.mobile_0410.tickets;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.zxing.BarcodeFormat;
import com.journeyapps.barcodescanner.BarcodeEncoder;
import com.hocnv.mobile_0410.R;
import com.hocnv.mobile_0410.data.FirestoreRefs;
import com.hocnv.mobile_0410.data.models.Ticket;
import com.hocnv.mobile_0410.util.TimeUtils;

import java.util.List;
import java.util.Map;

public class TicketDetailActivity extends AppCompatActivity {

    public static final String EXTRA_TICKET_ID = "ticket_id";

    private TextView tvMovieTitle, tvShowtime, tvTheater, tvSeats, tvStatus, tvPrice;
    private ImageView ivQrCode;
    private Button btnCancel;

    private String ticketId;
    private Ticket ticket;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_ticket_detail);

        ticketId = getIntent().getStringExtra(EXTRA_TICKET_ID);

        tvMovieTitle = findViewById(R.id.tvMovieTitle);
        tvShowtime = findViewById(R.id.tvShowtime);
        tvTheater = findViewById(R.id.tvTheater);
        tvSeats = findViewById(R.id.tvSeats);
        tvStatus = findViewById(R.id.tvStatus);
        tvPrice = findViewById(R.id.tvPrice);
        ivQrCode = findViewById(R.id.ivQrCode);
        btnCancel = findViewById(R.id.btnCancel);

        db = FirebaseFirestore.getInstance();

        if (ticketId == null || ticketId.isEmpty()) {
            Toast.makeText(this, "Không tìm thấy vé.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        loadTicket();

        btnCancel.setOnClickListener(v -> cancelTicket());
    }

    private void loadTicket() {
        db.collection(FirestoreRefs.COL_TICKETS)
                .document(ticketId)
                .get()
                .addOnSuccessListener(doc -> {
                    ticket = doc.toObject(Ticket.class);
                    if (ticket == null) {
                        Toast.makeText(this, "Không tìm thấy vé.", Toast.LENGTH_SHORT).show();
                        finish();
                        return;
                    }
                    ticket.id = doc.getId();
                    displayTicket();
                    loadMovieTitle();
                    loadTheaterName();
                    generateQrCode();
                    updateCancelButton();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    finish();
                });
    }

    private void displayTicket() {
        if (ticket.bookingTime != null) {
            tvShowtime.setText("Thời gian đặt: " +
                    TimeUtils.formatDateTime(ticket.bookingTime.toDate().getTime()));
        }

        if (ticket.seats != null) {
            tvSeats.setText("Ghế: " + String.join(", ", ticket.seats));
        }

        tvStatus.setText("Trạng thái: " + (ticket.status != null ? ticket.status : ""));
        if (ticket.status != null) {
            switch (ticket.status) {
                case "CONFIRMED":
                    tvStatus.setTextColor(Color.parseColor("#4CAF50"));
                    break;
                case "CANCELLED":
                    tvStatus.setTextColor(Color.parseColor("#F44336"));
                    break;
                default:
                    tvStatus.setTextColor(Color.GRAY);
                    break;
            }
        }

        tvPrice.setText(String.format("Giá: %,.0fđ", ticket.totalPrice));
    }

    private void loadMovieTitle() {
        if (ticket.movieId == null) return;
        db.collection(FirestoreRefs.COL_MOVIES)
                .document(ticket.movieId)
                .get()
                .addOnSuccessListener(doc -> {
                    String title = doc.getString("title");
                    tvMovieTitle.setText(title != null ? title : "");
                });
    }

    private void loadTheaterName() {
        if (ticket.theaterId == null) return;
        db.collection(FirestoreRefs.COL_THEATERS)
                .document(ticket.theaterId)
                .get()
                .addOnSuccessListener(doc -> {
                    String name = doc.getString("name");
                    tvTheater.setText("Rạp: " + (name != null ? name : ticket.theaterId));
                });
    }

    private void generateQrCode() {
        try {
            BarcodeEncoder encoder = new BarcodeEncoder();
            Bitmap bitmap = encoder.encodeBitmap(ticketId, BarcodeFormat.QR_CODE, 400, 400);
            ivQrCode.setImageBitmap(bitmap);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateCancelButton() {
        if (ticket.status != null && ticket.status.equals("CANCELLED")) {
            btnCancel.setEnabled(false);
            btnCancel.setText("Đã huỷ");
            return;
        }

        // Check if showtime startTime > now + 2 hours
        if (ticket.showtimeId != null) {
            db.collection(FirestoreRefs.COL_SHOWTIMES)
                    .document(ticket.showtimeId)
                    .get()
                    .addOnSuccessListener(doc -> {
                        Timestamp startTime = doc.getTimestamp("startTime");
                        if (startTime != null) {
                            long twoHoursFromNow = System.currentTimeMillis() + (2 * 60 * 60 * 1000L);
                            if (startTime.toDate().getTime() <= twoHoursFromNow) {
                                btnCancel.setEnabled(false);
                                btnCancel.setText("Không thể huỷ (sắp chiếu)");
                            }
                        }
                    });
        }
    }

    private void cancelTicket() {
        if (ticket == null) return;

        btnCancel.setEnabled(false);

        // Update ticket status to CANCELLED
        DocumentReference ticketRef = db.collection(FirestoreRefs.COL_TICKETS).document(ticketId);
        ticketRef.update("status", "CANCELLED")
                .addOnSuccessListener(unused -> {
                    // Restore seatMap
                    if (ticket.showtimeId != null && ticket.seats != null) {
                        restoreSeatMap();
                    }
                    ticket.status = "CANCELLED";
                    displayTicket();
                    btnCancel.setText("Đã huỷ");
                    Toast.makeText(this, "Huỷ vé thành công.", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    btnCancel.setEnabled(true);
                    Toast.makeText(this, "Huỷ vé thất bại: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void restoreSeatMap() {
        DocumentReference showtimeRef = db.collection(FirestoreRefs.COL_SHOWTIMES)
                .document(ticket.showtimeId);

        db.runTransaction(transaction -> {
            DocumentSnapshot snapshot = transaction.get(showtimeRef);

            @SuppressWarnings("unchecked")
            Map<String, Boolean> seatMap = (Map<String, Boolean>) snapshot.get("seatMap");
            if (seatMap == null) return null;

            for (String seat : ticket.seats) {
                seatMap.put(seat, false);
            }
            transaction.update(showtimeRef, "seatMap", seatMap);

            Long available = snapshot.getLong("availableSeats");
            int newAvailable = (available != null ? available.intValue() : 0) + ticket.seats.size();
            transaction.update(showtimeRef, "availableSeats", newAvailable);

            return null;
        });
    }
}
