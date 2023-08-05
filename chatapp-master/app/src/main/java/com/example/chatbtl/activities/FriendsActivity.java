package com.example.chatbtl.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Toast;

import com.example.chatbtl.R;
import com.example.chatbtl.adapters.UserAdapter;
import com.example.chatbtl.databinding.ActivityFriendsBinding;
import com.example.chatbtl.interfaces.UserInterface;
import com.example.chatbtl.models.User;
import com.example.chatbtl.utilities.Constants;
import com.example.chatbtl.utilities.PreferenceManager;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class FriendsActivity extends BaseActivity implements UserInterface {

    private ActivityFriendsBinding binding;
    private PreferenceManager preferenceManager;
    private UserAdapter userAdapter;
    private List<User> friends;
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityFriendsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        initAll();
        loadFriends();
        setListeners();
        listenStatus();
    }

    private void initAll() {
        preferenceManager = new PreferenceManager(getApplicationContext());
        currentUserId = preferenceManager.getString(Constants.KEY_USER_ID);
        friends = new ArrayList<>();
        userAdapter = new UserAdapter(friends, this);
        binding.recyclerPeople.setAdapter(userAdapter);
        String action = getIntent().getStringExtra("action");
        if (action != null && action.equals("search")) {
            binding.inputSearch.setVisibility(View.VISIBLE);
            binding.inputAddFriend.setVisibility(View.INVISIBLE);
            binding.inputSearch.requestFocus();
        }
    }

    private void loadFriends() {
        loading(true);
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        database.collection(Constants.KEY_COLLECTION_USERS)
                .whereArrayContains(Constants.KEY_FRIEND_IDS, currentUserId)
                .get()
                .addOnCompleteListener(task -> {
                    loading(false);

                    if (task.isSuccessful() && task.getResult() != null) {

                        for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                                User user = new User();
                                user.setId(queryDocumentSnapshot.getId());
                                user.setName(queryDocumentSnapshot.getString(Constants.KEY_NAME));
                                user.setPhone(queryDocumentSnapshot.getString(Constants.KEY_PHONE));
                                user.setImage(queryDocumentSnapshot.getString(Constants.KEY_IMAGE));
                                user.setFriendIds((List<String>) queryDocumentSnapshot.get(Constants.KEY_FRIEND_IDS));
                                user.setOnline(queryDocumentSnapshot.getBoolean(Constants.KEY_ONLINE));
                                friends.add(user);
                        }

                        if (friends.size() > 0) {
                            userAdapter.notifyDataSetChanged();
                            binding.recyclerPeople.setVisibility(View.VISIBLE);
                        } else {
                            showToast("0 tim thay");
                        }
                    } else {
                        showToast("Lỗi r");
                    }
                });
    }

    private void setListeners() {
        binding.buttonBack.setOnClickListener(v ->
                startActivity(new Intent(FriendsActivity.this, MainActivity.class)));
        binding.buttonMessages.setOnClickListener(v ->
                startActivity(new Intent(FriendsActivity.this, MainActivity.class)));

        binding.inputSearch.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus)
                binding.buttonCancel.setVisibility(View.VISIBLE);
        });
        binding.inputSearch.setOnKeyListener((v, keyCode, keyEvent) -> {
            if (keyEvent.getAction() == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
                searchFriends(friends, binding.inputSearch.getText().toString());
                return true;
            }
            return false;
        });

        binding.buttonAddNewFriend.setOnClickListener(v -> {
            binding.inputSearch.setVisibility(View.INVISIBLE);
            binding.inputAddFriend.setVisibility(View.VISIBLE);
        });

        binding.inputAddFriend.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus)
                binding.buttonCancel.setVisibility(View.VISIBLE);
        });
        binding.inputAddFriend.setOnKeyListener((v, keyCode, keyEvent) -> {
            if (keyEvent.getAction() == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
                searchPeoples(binding.inputAddFriend.getText().toString());
                return true;
            }
            return false;
        });

        binding.buttonCancel.setOnClickListener(v -> {
            binding.buttonCancel.setVisibility(View.INVISIBLE);
            binding.inputSearch.setText("");
            binding.inputSearch.clearFocus();
            if (friends.size() > 0) {
                UserAdapter userAdapter = new UserAdapter(friends, this);
                binding.recyclerPeople.setAdapter(userAdapter);
                binding.recyclerPeople.setVisibility(View.VISIBLE);
            } else {
                showToast("0 tim thay");
            }

            if (binding.inputAddFriend.getVisibility() == View.VISIBLE) {
                binding.inputSearch.setVisibility(View.VISIBLE);
                binding.inputAddFriend.setVisibility(View.INVISIBLE);
            }
        });

        // Sắp xếp bạn theo tên tăng dần (friend1.getName().compareTo(friend2.getName())
        // Sắp xếp bạn theo tên giảm dần (friend2.getName().compareTo(friend1.getName())
        // Sắp xếp bạn theo SDT tăng dần (friend1.getPhone().compareTo(friend2.getPhone())
//        binding.buttonSort.setOnClickListener(v -> {
//            Collections.sort(friends, (friend1, friend2) -> friend1.getName().compareTo(friend2.getName()));
//            userAdapter.notifyDataSetChanged();
//        });

    }

    private void searchFriends(List<User> friends, String name) {
        List<User> friendSearches = new ArrayList<>();
        for (User friend : friends) {
            if (friend.getName().toLowerCase().contains(name.toLowerCase())) {
                friendSearches.add(friend);
            }
        }
        UserAdapter userAdapter = new UserAdapter(friendSearches, this);
        binding.recyclerPeople.setAdapter(userAdapter);
        binding.recyclerPeople.setVisibility(View.VISIBLE);
    }

    private void searchPeoples(String phone) {
        loading(true);
        List<User> peopleSearches = new ArrayList<>();
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        database.collection(Constants.KEY_COLLECTION_USERS)
                .whereEqualTo(Constants.KEY_PHONE, phone)
                .get()
                .addOnCompleteListener(task -> {
                    loading(false);

                    if (task.isSuccessful() && task.getResult() != null && task.getResult().size() > 0) {
                        DocumentSnapshot documentSnapshot = task.getResult().getDocuments().get(0);
                        User user = new User();
                        user.setId(documentSnapshot.getId());
                        user.setName(documentSnapshot.getString(Constants.KEY_NAME));
                        user.setPhone(documentSnapshot.getString(Constants.KEY_PHONE));
                        user.setImage(documentSnapshot.getString(Constants.KEY_IMAGE));
                        user.setFriendIds((List<String>) documentSnapshot.get(Constants.KEY_FRIEND_IDS));
                        user.setOnline(documentSnapshot.getBoolean(Constants.KEY_ONLINE));
                        peopleSearches.add(user);
                    } else {
                        showToast("Không tìm thấy");
                    }
                    UserAdapter userAdapter = new UserAdapter(peopleSearches, this);
                    binding.recyclerPeople.setAdapter(userAdapter);
                    binding.recyclerPeople.setVisibility(View.VISIBLE);
                });
    }

    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    private void loading(Boolean isLoading) {
        if (isLoading) {
            binding.progressBar.setVisibility(View.VISIBLE);
        } else {
            binding.progressBar.setVisibility(View.INVISIBLE);
        }
    }

    private void listenStatus() {
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        database.collection(Constants.KEY_COLLECTION_USERS)
                .whereArrayContains(Constants.KEY_FRIEND_IDS, currentUserId)
                .addSnapshotListener((value, error) -> {
                    if (error != null)
                        return;

                    if (value != null) {
                        for (DocumentChange documentChange : value.getDocumentChanges()) {
                            QueryDocumentSnapshot queryDocumentSnapshot = documentChange.getDocument();
                            for (int i=0; i<friends.size(); i++) {
                                if (friends.get(i).getId().equals(queryDocumentSnapshot.getId())) {
                                    friends.get(i).setOnline((Boolean) queryDocumentSnapshot.get(Constants.KEY_ONLINE));
                                    userAdapter.notifyItemChanged(i);
                                    break;
                                }
                            }
                        }
                    }
                });
    }

    @Override
    public void onUserClicked(User user) {
        Intent intent = new Intent(getApplicationContext(), ChatActivity.class);
        intent.putExtra(Constants.KEY_USER, user);
        startActivity(intent);
    }
}