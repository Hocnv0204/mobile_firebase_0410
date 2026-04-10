package com.hocnv.mobile_0410.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import com.hocnv.mobile_0410.MainActivity;
import com.hocnv.mobile_0410.R;
import com.hocnv.mobile_0410.data.FirestoreRefs;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        auth = FirebaseAuth.getInstance();

        EditText etEmail = findViewById(R.id.etEmail);
        EditText etPassword = findViewById(R.id.etPassword);
        Button btnLogin = findViewById(R.id.btnLogin);
        TextView tvGoRegister = findViewById(R.id.tvGoRegister);

        btnLogin.setOnClickListener(v -> {
            String email = etEmail.getText() == null ? "" : etEmail.getText().toString().trim();
            String password = etPassword.getText() == null ? "" : etPassword.getText().toString();

            if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
                Toast.makeText(this, "Vui lòng nhập email và mật khẩu.", Toast.LENGTH_SHORT).show();
                return;
            }

            btnLogin.setEnabled(false);
            auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        btnLogin.setEnabled(true);
                        if (task.isSuccessful()) {
                            saveFcmToken();
                            startActivity(new Intent(this, MainActivity.class));
                            finish();
                        } else {
                            String msg = task.getException() != null ? task.getException().getMessage() : "Đăng nhập thất bại.";
                            Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
                        }
                    });
        });

        tvGoRegister.setOnClickListener(v -> startActivity(new Intent(this, RegisterActivity.class)));
    }

    private void saveFcmToken() {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) return;
        FirebaseMessaging.getInstance().getToken()
                .addOnSuccessListener(token -> {
                    Map<String, Object> update = new HashMap<>();
                    update.put("fcmToken", token);
                    FirebaseFirestore.getInstance()
                            .collection(FirestoreRefs.COL_USERS)
                            .document(user.getUid())
                            .set(update, SetOptions.merge());
                });
    }
}
