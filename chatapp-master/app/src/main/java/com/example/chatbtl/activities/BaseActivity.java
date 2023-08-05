package com.example.chatbtl.activities;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.chatbtl.utilities.Constants;
import com.example.chatbtl.utilities.PreferenceManager;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

// Activity nền cho các activity khác extends (kế thừa)
// Dùng để thay đổi trạng thái online của người dùng
public class BaseActivity extends AppCompatActivity {

    private DocumentReference document;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PreferenceManager preferenceManager = new PreferenceManager(getApplicationContext());
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        document = database.collection(Constants.KEY_COLLECTION_USERS)
                .document(preferenceManager.getString(Constants.KEY_USER_ID));
    }

    // Khi thoát khỏi màn hình
    @Override
    protected void onPause() {
        super.onPause();
        document.update(Constants.KEY_ONLINE, false);
    }

    // Khi vào lại màn hình
    @Override
    protected void onResume() {
        super.onResume();
        document.update(Constants.KEY_ONLINE, true);
    }


}
