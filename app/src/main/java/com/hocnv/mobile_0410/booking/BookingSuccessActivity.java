package com.hocnv.mobile_0410.booking;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.hocnv.mobile_0410.MainActivity;
import com.hocnv.mobile_0410.R;

public class BookingSuccessActivity extends AppCompatActivity {

    public static final String EXTRA_TICKET_ID = "ticket_id";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_booking_success);

        String ticketId = getIntent().getStringExtra(EXTRA_TICKET_ID);

        TextView tvTicketId = findViewById(R.id.tvTicketId);
        Button btnViewTickets = findViewById(R.id.btnViewTickets);

        tvTicketId.setText("Mã vé: " + (ticketId != null ? ticketId : ""));

        btnViewTickets.setOnClickListener(v -> {
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.putExtra("tab", "tickets");
            startActivity(intent);
            finish();
        });
    }
}
