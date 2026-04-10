package com.hocnv.mobile_0410.notifications;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.hocnv.mobile_0410.data.FirestoreRefs;

import java.util.HashMap;
import java.util.Map;

public class AppFirebaseMessagingService extends FirebaseMessagingService {
    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        com.google.firebase.auth.FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;
        Map<String, Object> update = new HashMap<>();
        update.put("fcmToken", token);
        FirebaseFirestore.getInstance()
                .collection(FirestoreRefs.COL_USERS)
                .document(user.getUid())
                .set(update, com.google.firebase.firestore.SetOptions.merge());
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage message) {
        super.onMessageReceived(message);
        String title = null;
        String body = null;
        if (message.getNotification() != null) {
            title = message.getNotification().getTitle();
            body = message.getNotification().getBody();
        }
        if (title == null) title = "Thông báo";
        if (body == null) body = "Bạn có một thông báo mới.";

        int notificationId = (int) (System.currentTimeMillis() % Integer.MAX_VALUE);
        NotificationHelper.showReminder(getApplicationContext(), notificationId, title, body);
    }
}

