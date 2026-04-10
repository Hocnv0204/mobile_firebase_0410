package com.hocnv.mobile_0410.tickets;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.hocnv.mobile_0410.R;
import com.hocnv.mobile_0410.data.models.Ticket;
import com.hocnv.mobile_0410.util.TimeUtils;

import java.util.List;
import java.util.Map;

public class TicketAdapter extends RecyclerView.Adapter<TicketAdapter.ViewHolder> {

    public interface OnTicketClickListener {
        void onTicketClick(String ticketId);
    }

    private final List<Map<String, Object>> items;
    private final OnTicketClickListener listener;

    public TicketAdapter(List<Map<String, Object>> items, OnTicketClickListener listener) {
        this.items = items;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_ticket, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Map<String, Object> item = items.get(position);
        Ticket ticket = (Ticket) item.get("ticket");
        String movieTitle = (String) item.get("movieTitle");

        if (ticket == null) return;

        holder.tvMovieTitle.setText(movieTitle != null ? movieTitle : "");

        if (ticket.bookingTime != null) {
            holder.tvShowtime.setText("Thời gian: " +
                    TimeUtils.formatDateTime(ticket.bookingTime.toDate().getTime()));
        } else {
            holder.tvShowtime.setText("");
        }

        if (ticket.seats != null) {
            holder.tvSeats.setText("Ghế: " + String.join(", ", ticket.seats));
        } else {
            holder.tvSeats.setText("");
        }

        holder.tvStatus.setText(ticket.status != null ? ticket.status : "");
        if (ticket.status != null) {
            switch (ticket.status) {
                case "CONFIRMED":
                    holder.tvStatus.setTextColor(Color.parseColor("#4CAF50"));
                    break;
                case "CANCELLED":
                    holder.tvStatus.setTextColor(Color.parseColor("#F44336"));
                    break;
                default:
                    holder.tvStatus.setTextColor(Color.GRAY);
                    break;
            }
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null && ticket.id != null) {
                listener.onTicketClick(ticket.id);
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView tvMovieTitle;
        final TextView tvShowtime;
        final TextView tvSeats;
        final TextView tvStatus;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMovieTitle = itemView.findViewById(R.id.tvMovieTitle);
            tvShowtime = itemView.findViewById(R.id.tvShowtime);
            tvSeats = itemView.findViewById(R.id.tvSeats);
            tvStatus = itemView.findViewById(R.id.tvStatus);
        }
    }
}
