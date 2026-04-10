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

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.hocnv.mobile_0410.MainActivity;
import com.hocnv.mobile_0410.R;
import com.hocnv.mobile_0410.data.FirestoreRefs;
import com.hocnv.mobile_0410.data.models.AppUser;

public class RegisterActivity extends AppCompatActivity {
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);

        auth = FirebaseAuth.getInstance();

        EditText etEmail = findViewById(R.id.etEmail);
        EditText etPassword = findViewById(R.id.etPassword);
        EditText etDisplayName = findViewById(R.id.etDisplayName);
        Button btnRegister = findViewById(R.id.btnRegister);
        TextView tvGoLogin = findViewById(R.id.tvGoLogin);

        btnRegister.setOnClickListener(v -> {
            String email = etEmail.getText() == null ? "" : etEmail.getText().toString().trim();
            String password = etPassword.getText() == null ? "" : etPassword.getText().toString();
            String displayName = etDisplayName.getText() == null ? "" : etDisplayName.getText().toString().trim();

            if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password) || TextUtils.isEmpty(displayName)) {
                Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin.", Toast.LENGTH_SHORT).show();
                return;
            }
            if (password.length() < 6) {
                Toast.makeText(this, "Mật khẩu tối thiểu 6 ký tự.", Toast.LENGTH_SHORT).show();
                return;
            }

            btnRegister.setEnabled(false);
            auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        btnRegister.setEnabled(true);
                        if (!task.isSuccessful()) {
                            String msg = task.getException() != null ? task.getException().getMessage() : "Đăng ký thất bại.";
                            Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
                            return;
                        }

                        FirebaseUser user = auth.getCurrentUser();
                        if (user == null) {
                            Toast.makeText(this, "Không lấy được user.", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        AppUser appUser = new AppUser(user.getUid(), email, displayName, null, Timestamp.now());
                        FirebaseFirestore.getInstance()
                                .collection(FirestoreRefs.COL_USERS)
                                .document(user.getUid())
                                .set(appUser)
                                .addOnSuccessListener(unused -> {
                                    Toast.makeText(this, "Đăng ký thành công!", Toast.LENGTH_SHORT).show();
                                    startActivity(new Intent(this, MainActivity.class)
                                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
                                    finish();
                                })
                                .addOnFailureListener(e -> Toast.makeText(this, "Lưu user thất bại: " + e.getMessage(), Toast.LENGTH_LONG).show());
                    });
        });

        tvGoLogin.setOnClickListener(v -> finish());
    }
}

