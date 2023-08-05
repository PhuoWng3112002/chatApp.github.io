package com.example.chatbtl.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;

import com.example.chatbtl.adapters.ConversionAdapter;
import com.example.chatbtl.databinding.ActivityMainBinding;
import com.example.chatbtl.interfaces.ConversionInterface;
import com.example.chatbtl.models.ChatMessage;
import com.example.chatbtl.models.User;
import com.example.chatbtl.utilities.Constants;
import com.example.chatbtl.utilities.PreferenceManager;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class MainActivity extends BaseActivity implements ConversionInterface{

    private ActivityMainBinding binding;
    private PreferenceManager preferenceManager;
    private FirebaseFirestore database;
    private List<ChatMessage> conversions;
    private ConversionAdapter conversionAdapter;
    private String currentUserId;
    private List<String> listConversionIds;
    private List<User> listConversionUsers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        initAll();
        loadUserDatas();
        listenConversion();
        setListeners();
        listenUserStatus();
    }

    private void initAll() {
        preferenceManager = new PreferenceManager(getApplicationContext());
        currentUserId = preferenceManager.getString(Constants.KEY_USER_ID);
        database = FirebaseFirestore.getInstance();
        conversions = new ArrayList<>();
        listConversionIds = new ArrayList<>();
        listConversionUsers = new ArrayList<>();
        conversionAdapter = new ConversionAdapter(conversions, listConversionUsers, this);
        binding.recyclerConversion.setAdapter(conversionAdapter);
    }

    private void setListeners() {
        binding.layoutSearch.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, FriendsActivity.class);
            intent.putExtra("action", "search");
            startActivity(intent);
        });
        binding.buttonFriends.setOnClickListener(v ->
                startActivity(new Intent(getApplicationContext(), FriendsActivity.class)));
        binding.imageProfile.setOnClickListener(v ->
                startActivity(new Intent(getApplicationContext(), ProfileActivity.class)));

    }

    private void loadUserDatas() {
        byte[] bytes = Base64.decode(preferenceManager.getString(Constants.KEY_IMAGE), Base64.DEFAULT);
        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        binding.imageProfile.setImageBitmap(bitmap);
    }

    private void listenConversion() {
        database.collection(Constants.KEY_COLLECTION_CONVERSATIONS)
                .whereEqualTo(Constants.KEY_SENDER_ID, currentUserId)
                .addSnapshotListener(eventListener);
        database.collection(Constants.KEY_COLLECTION_CONVERSATIONS)
                .whereEqualTo(Constants.KEY_RECEIVER_ID, currentUserId)
                .addSnapshotListener(eventListener);
    }

    private final EventListener<QuerySnapshot> eventListener = (value, error) -> {
        if (error != null) {
            return;
        }

        if (value != null) {
            for (DocumentChange documentChange : value.getDocumentChanges()) {
                QueryDocumentSnapshot queryDocumentSnapshot = documentChange.getDocument();

                String senderId = queryDocumentSnapshot.getString(Constants.KEY_SENDER_ID);
                String receiverId = queryDocumentSnapshot.getString(Constants.KEY_RECEIVER_ID);

                if (documentChange.getType() == DocumentChange.Type.ADDED) {
                    ChatMessage chatMessage = new ChatMessage();
                    chatMessage.setSenderId(senderId);
                    chatMessage.setReceiverId(receiverId);
                    if (currentUserId.equals(senderId)) {
                        chatMessage.setConversionId(queryDocumentSnapshot.getString(Constants.KEY_RECEIVER_ID));
                        chatMessage.setConversionName(queryDocumentSnapshot.getString(Constants.KEY_RECEIVER_NAME));
                        chatMessage.setConversionImage(queryDocumentSnapshot.getString(Constants.KEY_RECEIVER_IMAGE));
                    } else {
                        chatMessage.setConversionId(queryDocumentSnapshot.getString(Constants.KEY_SENDER_ID));
                        chatMessage.setConversionName(queryDocumentSnapshot.getString(Constants.KEY_SENDER_NAME));
                        chatMessage.setConversionImage(queryDocumentSnapshot.getString(Constants.KEY_SENDER_IMAGE));
                    }
                    chatMessage.setMessage(queryDocumentSnapshot.getString(Constants.KEY_LAST_MESSAGE));
                    chatMessage.setDateObj(queryDocumentSnapshot.getDate(Constants.KEY_TIMESTAMP));
                    conversions.add(chatMessage);
                    listConversionIds.add(chatMessage.getConversionId());
                } else if (documentChange.getType() == DocumentChange.Type.MODIFIED) {
                    for (int i = 0; i < conversions.size(); i++) {
                        if (conversions.get(i).getSenderId().equals(senderId) && conversions.get(i).getReceiverId().equals(receiverId)) {
                            conversions.get(i).setMessage(queryDocumentSnapshot.getString(Constants.KEY_LAST_MESSAGE));
                            conversions.get(i).setDateObj(queryDocumentSnapshot.getDate(Constants.KEY_TIMESTAMP));
                            break;
                        }
                    }
                }
            }
            Collections.sort(conversions, (conversion1, conversion2) -> conversion2.getDateObj().compareTo(conversion1.getDateObj()));
            getListUserConversion();
            conversionAdapter.notifyDataSetChanged();
            binding.recyclerConversion.smoothScrollToPosition(0);
            binding.recyclerConversion.setVisibility(View.VISIBLE);
            binding.progressBar.setVisibility(View.GONE);
        }
    };

    private void getListUserConversion() {
        listConversionIds = new ArrayList<>();
        for (ChatMessage conversion : conversions) {
            listConversionIds.add(conversion.getConversionId());
        }
        if (listConversionIds.size() != 0) {
            database.collection(Constants.KEY_COLLECTION_USERS)
                    .whereIn(FieldPath.documentId(), listConversionIds)
                    .get()
                    .addOnCompleteListener(task -> {
                        for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                            User user = new User();
                            user.setId(queryDocumentSnapshot.getId());
                            user.setName(queryDocumentSnapshot.getString(Constants.KEY_NAME));
                            user.setPhone(queryDocumentSnapshot.getString(Constants.KEY_PHONE));
                            user.setImage(queryDocumentSnapshot.getString(Constants.KEY_IMAGE));
                            user.setFriendIds((List<String>) queryDocumentSnapshot.get(Constants.KEY_FRIEND_IDS));
                            user.setOnline(queryDocumentSnapshot.getBoolean(Constants.KEY_ONLINE));
                            listConversionUsers.add(user);
                        }

                        conversionAdapter.notifyDataSetChanged();
                    });
        }
    }

    private void listenUserStatus() {
        database.collection(Constants.KEY_COLLECTION_USERS)
                .addSnapshotListener((value, error) -> {
                    if (error != null)
                        return;

                    if (value != null) {
                        for (DocumentChange documentChange : value.getDocumentChanges()) {
                            QueryDocumentSnapshot queryDocumentSnapshot = documentChange.getDocument();
                            for (int i=0; i<listConversionUsers.size(); i++) {
                                if (listConversionUsers.get(i).getId().equals(queryDocumentSnapshot.getId())) {
                                    listConversionUsers.get(i).setOnline((Boolean) queryDocumentSnapshot.get(Constants.KEY_ONLINE));
                                    conversionAdapter.notifyItemChanged(getIndex(queryDocumentSnapshot.getId()));
                                    break;
                                }
                            }
                        }
                    }
                });
    }

    private int getIndex(String id) {
        for (int i=0; i<conversions.size(); i++) {
            if (conversions.get(i).getConversionId().equals(id)) {
                Log.d("index", conversions.get(i).getConversionName());
                return i;
            }
        }
        return -1;
    }

    @Override
    public void onConversionClicked(User user) {
        Intent intent = new Intent(getApplicationContext(), ChatActivity.class);
        intent.putExtra(Constants.KEY_USER, user);
        startActivity(intent);
    }
}