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

import java.util.List;

public class ShowtimeAdapter extends RecyclerView.Adapter<ShowtimeAdapter.VH> {
    public interface OnShowtimeClickListener {
        void onClick(Showtime st);
    }

    private final List<Showtime> items;
    private final OnShowtimeClickListener listener;

    public ShowtimeAdapter(List<Showtime> items, OnShowtimeClickListener listener) {
        this.items = items;
        this.listener = listener;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_showtime, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        Showtime st = items.get(position);
        holder.tvTime.setText(st.startTime != null
                ? TimeUtils.formatDateTime(st.startTime.toDate().getTime()) : "");
        holder.tvTheater.setText(st.theaterId == null ? "" : ("Rạp: " + st.theaterId));
        holder.itemView.setOnClickListener(v -> listener.onClick(st));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvTime;
        TextView tvTheater;

        VH(@NonNull View itemView) {
            super(itemView);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvTheater = itemView.findViewById(R.id.tvTheater);
        }
    }
}

