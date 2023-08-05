package com.example.chatbtl.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.chatbtl.R;
import com.example.chatbtl.adapters.ChatAdapter;
import com.example.chatbtl.databinding.ActivityChatBinding;
import com.example.chatbtl.models.ChatMessage;
import com.example.chatbtl.models.User;
import com.example.chatbtl.utilities.Constants;
import com.example.chatbtl.utilities.PreferenceManager;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class ChatActivity extends BaseActivity {

    private ActivityChatBinding binding;
    private PreferenceManager preferenceManager;
    private FirebaseFirestore database;
    private List<ChatMessage> chatMessages;
    private ChatAdapter chatAdapter;
    // receiverUser là người nhận tin nhắn của chủ (người khách)
    private User receiverUser;
    private String curUserId, conversionId = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        initAll();
        loadDatas();
        setListeners();
        listenMessages();
    }

    // Khởi tạo các biến toàn cục
    private void initAll() {
        preferenceManager = new PreferenceManager(getApplicationContext());
        curUserId = preferenceManager.getString(Constants.KEY_USER_ID);
        chatMessages = new ArrayList<>();
        receiverUser = (User) getIntent().getSerializableExtra(Constants.KEY_USER);
        chatAdapter = new ChatAdapter(chatMessages, curUserId, receiverUser);
        binding.recyclerMessage.setAdapter(chatAdapter);
        database = FirebaseFirestore.getInstance();
        getConversionId();
    }

    // Điền các dữ liệu vòa giao diện
    private void loadDatas() {
        listenStatus(receiverUser.getId());
        binding.textName.setText(receiverUser.getName());
        // Giải mã ảnh về bitmap
        byte[] bytes = Base64.decode(receiverUser.getImage(), Base64.DEFAULT);
        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        binding.imageProfile.setImageBitmap(bitmap);

        // Kiểm tra nếu người nhận là bạn hay không
        if (receiverUser.getFriendIds().contains(curUserId)) {
            binding.buttonAddFriend.setVisibility(View.INVISIBLE);
            binding.textAlert.setVisibility(View.INVISIBLE);
            binding.inputMessage.setEnabled(true);
        } else {
            binding.buttonAddFriend.setVisibility(View.VISIBLE);
            binding.textAlert.setVisibility(View.VISIBLE);
            binding.inputMessage.setEnabled(false);
        }
    }

    // Lắng nghe sự thay đổi online/ offline và thay vào giao diện
    private void listenStatus(String id) {
        database.collection(Constants.KEY_COLLECTION_USERS)
                .document(id)
                .addSnapshotListener((value, error) -> {
                    if (error != null)
                        return;

                    if (value != null) {
                        Boolean isOnline = (Boolean) value.get(Constants.KEY_ONLINE);
                        if (isOnline) {
                            binding.textActive.setText("Online");
                            binding.textActive.setTextColor(getResources().getColor(R.color.online));
                        } else {
                            binding.textActive.setText("Offline");
                            binding.textActive.setTextColor(getResources().getColor(R.color.offline));
                        }

                        receiverUser.setFriendIds((List<String>) value.get(Constants.KEY_FRIEND_IDS));
                        if (receiverUser.getFriendIds().contains(curUserId)) {
                            binding.buttonAddFriend.setVisibility(View.INVISIBLE);
                            binding.textAlert.setVisibility(View.INVISIBLE);
                            binding.inputMessage.setEnabled(true);
                        } else {
                            binding.buttonAddFriend.setVisibility(View.VISIBLE);
                            binding.textAlert.setVisibility(View.VISIBLE);
                            binding.inputMessage.setEnabled(false);
                        }
                    }
                });
    }

    private void setListeners() {
        binding.buttonBack.setOnClickListener(v -> onBackPressed());
        binding.buttonSent.setOnClickListener(v -> {
            // Kiểm tra nếu đã nhập tin nhắn thì gửi tin
            if (!binding.inputMessage.getText().toString().trim().isEmpty())
                sentMessage();
        });

        binding.imageProfile.setOnClickListener(v -> {
            Intent intent = new Intent(ChatActivity.this, ProfileActivity.class);
            intent.putExtra(Constants.KEY_USER, receiverUser);
            startActivity(intent);
        });
        binding.textName.setOnClickListener(v -> {
            Intent intent = new Intent(ChatActivity.this, ProfileActivity.class);
            intent.putExtra(Constants.KEY_USER, receiverUser);
            startActivity(intent);
        });

        binding.buttonAddFriend.setOnClickListener(view -> {
            addFriend();
        });
//       Call
//        binding.buttonCall.setOnClickListener(v -> {
//            Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + receiverUser.getPhone()));
//            startActivity(intent);
//        });
    }

    private void addFriend() {
        FirebaseFirestore database = FirebaseFirestore.getInstance();

        // === Kết bạn bên chủ ===
        // Update list friends của chủ lên firebase
        DocumentReference curUserDoc = database.collection(Constants.KEY_COLLECTION_USERS)
                .document(curUserId);
        // FieldValue.arrayUnion thêm phần tử vào field có kiểu mảng
        curUserDoc.update(Constants.KEY_FRIEND_IDS, FieldValue.arrayUnion(receiverUser.getId()));
        // Cập nhật lại list friends của khách về preferenceManager
        curUserDoc.get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                List<String> friendIds = (List<String>) task.getResult().get(Constants.KEY_FRIEND_IDS);
                preferenceManager.putString(Constants.KEY_FRIEND_IDS, String.join("-", friendIds));
            }
        });

        // === Kết bạn bên khách ===
        // Update list friends của khách lên firebase
        DocumentReference othUserDoc = database.collection(Constants.KEY_COLLECTION_USERS)
                .document(receiverUser.getId());
        // FieldValue.arrayUnion thêm phần tử vào field có kiểu mảng
        othUserDoc.update(Constants.KEY_FRIEND_IDS, FieldValue.arrayUnion(curUserId));
        // Cập nhật lại list friends của khách về biến receiverUser
        othUserDoc.get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                receiverUser.setFriendIds((List<String>) task.getResult().get(Constants.KEY_FRIEND_IDS));
            }
        });

        binding.buttonAddFriend.setVisibility(View.INVISIBLE);
        binding.textAlert.setVisibility(View.INVISIBLE);
        binding.inputMessage.setEnabled(true);

        Toast.makeText(getApplicationContext(),"Befriended " + receiverUser.getName(), Toast.LENGTH_SHORT).show();
    }

    // Gửi tin nhắn
    private void sentMessage() {
        HashMap<String, Object> message = new HashMap<>();
        message.put(Constants.KEY_SENDER_ID, curUserId);
        message.put(Constants.KEY_RECEIVER_ID, receiverUser.getId());
        message.put(Constants.KEY_MESSAGE, binding.inputMessage.getText().toString().trim());
        message.put(Constants.KEY_TIMESTAMP, new Date());
        // Thêm tin nhắn lên firebase
        database.collection(Constants.KEY_COLLECTION_CHAT)
                .add(message);

        // Lấy conversionId để kiểm tra đã có hội thoại giữa 2 người chưa
        getConversionId();
        if (conversionId != null) {
            // Nếu đã tồn tại cuộc hội thoại giữa 2 người thì cập nhật lại last message và timestamp
            updateConversion(binding.inputMessage.getText().toString());
        } else {
            // Nếu chưa có hội thoại giữa 2 người -> thêm cuộc hội thoại mới
            HashMap<String, Object> conversion = new HashMap<>();
            conversion.put(Constants.KEY_SENDER_ID, curUserId);
            conversion.put(Constants.KEY_SENDER_NAME, preferenceManager.getString(Constants.KEY_NAME));
            conversion.put(Constants.KEY_SENDER_IMAGE, preferenceManager.getString(Constants.KEY_IMAGE));
            conversion.put(Constants.KEY_RECEIVER_ID, receiverUser.getId());
            conversion.put(Constants.KEY_RECEIVER_NAME, receiverUser.getName());
            conversion.put(Constants.KEY_RECEIVER_IMAGE, receiverUser.getImage());
            conversion.put(Constants.KEY_LAST_MESSAGE, binding.inputMessage.getText().toString().trim());
            conversion.put(Constants.KEY_TIMESTAMP, new Date());
            addConversion(conversion);
        }

        // Gửi tin xong thì xóa text trong ô soạn tin
        binding.inputMessage.setText(null);
    }

    // Lấy conversionId (id hội thoại)
    private void getConversionId() {
        // Conversion gồm id giữa 2 người nhắn tin với nhau
        // Kiểm tra xem nếu 2 người currentUser và receiverUser có nhắn với nhau không
        database.collection(Constants.KEY_COLLECTION_CONVERSATIONS)
                .whereEqualTo(Constants.KEY_SENDER_ID, curUserId)
                .whereEqualTo(Constants.KEY_RECEIVER_ID, receiverUser.getId())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null && task.getResult().getDocuments().size() > 0) {
                        DocumentSnapshot documentSnapshot = task.getResult().getDocuments().get(0);
                        conversionId = documentSnapshot.getId();
                    }
                });

        database.collection(Constants.KEY_COLLECTION_CONVERSATIONS)
                .whereEqualTo(Constants.KEY_RECEIVER_ID, curUserId)
                .whereEqualTo(Constants.KEY_SENDER_ID, receiverUser.getId())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null && task.getResult().getDocuments().size() > 0) {
                        DocumentSnapshot documentSnapshot = task.getResult().getDocuments().get(0);
                        conversionId = documentSnapshot.getId();
                    }
                });
    }

    private void addConversion(HashMap<String, Object> conversion) {
        database.collection(Constants.KEY_COLLECTION_CONVERSATIONS)
                .add(conversion)
                .addOnSuccessListener(documentReference -> conversionId = documentReference.getId());
    }

    private void updateConversion(String message) {
        HashMap<String, Object> updates = new HashMap<>();
        updates.put(Constants.KEY_LAST_MESSAGE, message);
        updates.put(Constants.KEY_TIMESTAMP, new Date());
        database.collection(Constants.KEY_COLLECTION_CONVERSATIONS)
                .document(conversionId)
                .update(updates);
    }

    // Theo dõi thay đổi của dữ liệu chat
    private void listenMessages() {
        database.collection(Constants.KEY_COLLECTION_CHAT)
                .whereEqualTo(Constants.KEY_SENDER_ID, curUserId)
                .whereEqualTo(Constants.KEY_RECEIVER_ID, receiverUser.getId())
                .addSnapshotListener(snapshotEventListener);

        database.collection(Constants.KEY_COLLECTION_CHAT)
                .whereEqualTo(Constants.KEY_SENDER_ID, receiverUser.getId())
                .whereEqualTo(Constants.KEY_RECEIVER_ID, curUserId)
                .addSnapshotListener(snapshotEventListener);
    }

    // Code khi có sự thay đổi của conversion
    private final EventListener<QuerySnapshot> snapshotEventListener = (value, error) -> {
        if (error != null)
            return;

        int count = chatMessages.size();
        // Lặp qua tất cả những conversion thay đổi và thêm vào chatMessages
        for (DocumentChange documentChange : value.getDocumentChanges()) {
            QueryDocumentSnapshot queryDocumentSnapshot = documentChange.getDocument();
            if (documentChange.getType() == DocumentChange.Type.ADDED) {
                ChatMessage chatMessage = new ChatMessage();
                chatMessage.setChatId(queryDocumentSnapshot.getId());
                chatMessage.setSenderId(queryDocumentSnapshot.getString(Constants.KEY_SENDER_ID));
                chatMessage.setReceiverId(queryDocumentSnapshot.getString(Constants.KEY_RECEIVER_ID));
                chatMessage.setMessage(queryDocumentSnapshot.getString(Constants.KEY_MESSAGE));
                chatMessage.setTimeStamp(convertDate(queryDocumentSnapshot.getDate(Constants.KEY_TIMESTAMP)));
                chatMessage.setDateObj(queryDocumentSnapshot.getDate(Constants.KEY_TIMESTAMP));
                chatMessages.add(chatMessage);
            }
        }
        // Sắp xếp lại chatMesssages -> thời gian gửi tin mới thì xếp sau
        Collections.sort(chatMessages, (message1, message2) -> message1.getDateObj().compareTo(message2.getDateObj()));

        // Cập cập lại chatAdapter - chatAdapter ăn theo chatMessages
        if (count == 0) {
            // Tin nhắn đầu tiên
            chatAdapter.notifyDataSetChanged();
        } else {
            // Tin nhắn tiếp theo
            chatAdapter.notifyItemRangeChanged(chatMessages.size(), chatMessages.size());
            binding.recyclerMessage.smoothScrollToPosition(chatMessages.size() - 1);
        }

        binding.recyclerMessage.setVisibility(View.VISIBLE);

        binding.progressBar.setVisibility(View.GONE);
    };

    private String convertDate(Date date) {
        return new SimpleDateFormat("HH:mm, dd/MM/yyyy", Locale.getDefault()).format(date);
    }


}