package com.hocnv.mobile_0410.tickets;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.hocnv.mobile_0410.R;
import com.hocnv.mobile_0410.data.FirestoreRefs;
import com.hocnv.mobile_0410.data.models.Ticket;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MyTicketsFragment extends Fragment {

    private ProgressBar progress;
    private TextView tvEmpty;
    private RecyclerView rvTickets;
    private final List<Map<String, Object>> ticketItems = new ArrayList<>();
    private TicketAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_my_tickets, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        progress = view.findViewById(R.id.progress);
        tvEmpty = view.findViewById(R.id.tvEmpty);
        rvTickets = view.findViewById(R.id.rvTickets);

        adapter = new TicketAdapter(ticketItems, ticketId -> {
            Intent intent = new Intent(requireContext(), TicketDetailActivity.class);
            intent.putExtra(TicketDetailActivity.EXTRA_TICKET_ID, ticketId);
            startActivity(intent);
        });

        rvTickets.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvTickets.setAdapter(adapter);

        loadTickets();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadTickets();
    }

    private void loadTickets() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            progress.setVisibility(View.GONE);
            tvEmpty.setVisibility(View.VISIBLE);
            return;
        }

        progress.setVisibility(View.VISIBLE);
        tvEmpty.setVisibility(View.GONE);
        rvTickets.setVisibility(View.GONE);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection(FirestoreRefs.COL_TICKETS)
                .whereEqualTo("userId", user.getUid())
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!isAdded()) return;

                    ticketItems.clear();
                    List<DocumentSnapshot> docs = querySnapshot.getDocuments();

                    if (docs.isEmpty()) {
                        progress.setVisibility(View.GONE);
                        tvEmpty.setVisibility(View.VISIBLE);
                        rvTickets.setVisibility(View.GONE);
                        adapter.notifyDataSetChanged();
                        return;
                    }

                    final int[] remaining = {docs.size()};

                    for (DocumentSnapshot doc : docs) {
                        Ticket ticket = doc.toObject(Ticket.class);
                        if (ticket == null) {
                            remaining[0]--;
                            continue;
                        }
                        ticket.id = doc.getId();

                        // Load movie title
                        String movieId = ticket.movieId;
                        if (movieId != null) {
                            db.collection(FirestoreRefs.COL_MOVIES)
                                    .document(movieId)
                                    .get()
                                    .addOnSuccessListener(movieDoc -> {
                                        if (!isAdded()) return;
                                        String movieTitle = movieDoc.getString("title");
                                        Map<String, Object> item = new HashMap<>();
                                        item.put("ticket", ticket);
                                        item.put("movieTitle", movieTitle != null ? movieTitle : "");
                                        ticketItems.add(item);

                                        remaining[0]--;
                                        if (remaining[0] <= 0) {
                                            showResults();
                                        }
                                    })
                                    .addOnFailureListener(e -> {
                                        if (!isAdded()) return;
                                        Map<String, Object> item = new HashMap<>();
                                        item.put("ticket", ticket);
                                        item.put("movieTitle", "");
                                        ticketItems.add(item);

                                        remaining[0]--;
                                        if (remaining[0] <= 0) {
                                            showResults();
                                        }
                                    });
                        } else {
                            Map<String, Object> item = new HashMap<>();
                            item.put("ticket", ticket);
                            item.put("movieTitle", "");
                            ticketItems.add(item);

                            remaining[0]--;
                            if (remaining[0] <= 0) {
                                showResults();
                            }
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    if (!isAdded()) return;
                    progress.setVisibility(View.GONE);
                    tvEmpty.setVisibility(View.VISIBLE);
                    Toast.makeText(requireContext(), "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void showResults() {
        progress.setVisibility(View.GONE);
        if (ticketItems.isEmpty()) {
            tvEmpty.setVisibility(View.VISIBLE);
            rvTickets.setVisibility(View.GONE);
        } else {
            tvEmpty.setVisibility(View.GONE);
            rvTickets.setVisibility(View.VISIBLE);
        }
        adapter.notifyDataSetChanged();
    }
}
