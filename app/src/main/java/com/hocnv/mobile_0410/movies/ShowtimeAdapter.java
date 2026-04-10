package com.hocnv.mobile_0410.movies;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.hocnv.mobile_0410.R;
import com.hocnv.mobile_0410.data.models.Showtime;
import com.hocnv.mobile_0410.util.TimeUtils;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ShowtimeAdapter extends RecyclerView.Adapter<ShowtimeAdapter.VH> {

    public interface OnShowtimeClickListener {
        void onClick(Showtime st);
    }

    private final List<Map<String, Object>> items;
    private final OnShowtimeClickListener listener;

    private static final SimpleDateFormat DATE_FORMAT =
            new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
    private static final SimpleDateFormat TIME_FORMAT =
            new SimpleDateFormat("HH:mm", Locale.getDefault());
    private static final NumberFormat VND_FORMAT;

    static {
        VND_FORMAT = NumberFormat.getInstance(new Locale("vi", "VN"));
        VND_FORMAT.setGroupingUsed(true);
    }

    public ShowtimeAdapter(List<Map<String, Object>> items, OnShowtimeClickListener listener) {
        this.items = items;
        this.listener = listener;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_showtime, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        Map<String, Object> item = items.get(position);
        Showtime st = (Showtime) item.get("showtime");
        String theaterName = (String) item.get("theaterName");

        if (st.startTime != null) {
            long millis = st.startTime.toDate().getTime();
            holder.tvDate.setText(DATE_FORMAT.format(new java.util.Date(millis)));
            holder.tvTime.setText(TIME_FORMAT.format(new java.util.Date(millis)));
        } else {
            holder.tvDate.setText("");
            holder.tvTime.setText("");
        }

        holder.tvTheater.setText(theaterName != null ? theaterName : "");
        holder.tvPrice.setText(VND_FORMAT.format(st.price) + " VND");
        holder.tvSeats.setText("C\u00F2n " + st.availableSeats + " gh\u1EBF");

        holder.itemView.setOnClickListener(v -> listener.onClick(st));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvDate;
        TextView tvTime;
        TextView tvTheater;
        TextView tvPrice;
        TextView tvSeats;

        VH(@NonNull View itemView) {
            super(itemView);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvTheater = itemView.findViewById(R.id.tvTheater);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            tvSeats = itemView.findViewById(R.id.tvSeats);
        }
    }
}
