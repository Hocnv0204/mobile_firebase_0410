package com.hocnv.mobile_0410.profile;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.hocnv.mobile_0410.R;
import com.hocnv.mobile_0410.auth.LoginActivity;
import com.hocnv.mobile_0410.data.FirestoreRefs;
import com.hocnv.mobile_0410.seed.SeedDataActivity;

public class ProfileFragment extends Fragment {

    private ImageView ivAvatar;
    private TextView tvDisplayName;
    private TextView tvEmail;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ivAvatar = view.findViewById(R.id.ivAvatar);
        tvDisplayName = view.findViewById(R.id.tvDisplayName);
        tvEmail = view.findViewById(R.id.tvEmail);
        Button btnSeed = view.findViewById(R.id.btnSeed);
        Button btnLogout = view.findViewById(R.id.btnLogout);

        loadUserData();

        btnLogout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(requireContext(), LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            requireActivity().finish();
        });

        btnSeed.setOnClickListener(v ->
                startActivity(new Intent(requireContext(), SeedDataActivity.class)));
    }

    private void loadUserData() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        tvEmail.setText(user.getEmail() != null ? user.getEmail() : "");

        FirebaseFirestore.getInstance()
                .collection(FirestoreRefs.COL_USERS)
                .document(user.getUid())
                .get()
                .addOnSuccessListener(doc -> {
                    if (!isAdded()) return;
                    String name = doc.getString("displayName");
                    tvDisplayName.setText(name != null && !name.isEmpty() ? name : "Người dùng");

                    String photoUrl = doc.getString("photoUrl");
                    if (photoUrl != null && !photoUrl.isEmpty()) {
                        Glide.with(requireContext())
                                .load(photoUrl)
                                .circleCrop()
                                .placeholder(android.R.drawable.ic_menu_myplaces)
                                .into(ivAvatar);
                    }
                });
    }
}
