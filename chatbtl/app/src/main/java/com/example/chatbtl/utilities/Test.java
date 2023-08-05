package com.example.chatbtl.utilities;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.InputType;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.chatbtl.R;
import com.example.chatbtl.activities.FriendsActivity;
import com.example.chatbtl.activities.MainActivity;
import com.example.chatbtl.activities.ProfileActivity;
import com.example.chatbtl.activities.SignUpActivity;
import com.example.chatbtl.databinding.ActivitySignInBinding;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class Test {

    public class SignInActivity extends AppCompatActivity {

        private ActivitySignInBinding binding;
        private PreferenceManager preferenceManager;
//        private Boolean isSaveLogin = false;
//        private SharedPreferences sharedPreferences;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            binding = ActivitySignInBinding.inflate(getLayoutInflater());
            preferenceManager = new PreferenceManager(getApplicationContext());
//            sharedPreferences =
//                    getApplicationContext().getSharedPreferences("login", Context.MODE_PRIVATE);
//
//            if (sharedPreferences.getBoolean("isSaveLogin", false)) {
//                binding.inputPhone.setText(sharedPreferences.getString("phone", null));
//                binding.inputPassword.setText(sharedPreferences.getString("password", null));
//                binding.cboxSaveLogin.setChecked(true);
//            }

            if (preferenceManager.getBoolean(Constants.KEY_IS_SIGNED_IN)) {
                startActivity(new Intent(getApplicationContext(), MainActivity.class));
                finish();
            }
            setContentView(binding.getRoot());
            setListeners();
        }

        private void setListeners() {
            binding.buttonShowPassword.setOnClickListener(v -> {
                if (binding.inputPassword.getInputType() == (InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD)) {
                    binding.inputPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_NORMAL);
                    binding.buttonShowPassword.setBackgroundResource(R.drawable.ic_eye);
                } else {
                    binding.inputPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    binding.buttonShowPassword.setBackgroundResource(R.drawable.ic_eye_slash);
                }
                binding.inputPassword.setSelection(binding.inputPassword.getText().length());
            });
            binding.textSignUp.setOnClickListener(v -> startActivity(new Intent(getApplicationContext(), SignUpActivity.class)));
            binding.buttonSignIn.setOnClickListener(v -> {
                if (isValidSignInDetails()) {
                    signIn();
                }
            });

//        binding.cboxSaveLogin.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//            @Override
//            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
//                isSaveLogin = b;
//            }
//        });
        }

        private void signIn() {
            loading(true);

            FirebaseFirestore database = FirebaseFirestore.getInstance();
            database.collection(Constants.KEY_COLLECTION_USERS)
                    .whereEqualTo(Constants.KEY_PHONE, binding.inputPhone.getText().toString())
                    .whereEqualTo(Constants.KEY_PASSWORD, binding.inputPassword.getText().toString())
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful() && task.getResult() != null && task.getResult().getDocuments().size() > 0) {
                            DocumentSnapshot documentSnapshot = task.getResult().getDocuments().get(0);
                            preferenceManager.putBoolean(Constants.KEY_IS_SIGNED_IN, true);
                            preferenceManager.putString(Constants.KEY_USER_ID, documentSnapshot.getId());
                            preferenceManager.putString(Constants.KEY_NAME, documentSnapshot.getString(Constants.KEY_NAME));
                            preferenceManager.putString(Constants.KEY_PHONE, documentSnapshot.getString(Constants.KEY_PHONE));
                            preferenceManager.putString(Constants.KEY_IMAGE, documentSnapshot.getString(Constants.KEY_IMAGE));
                            List<String> friendIds = (List<String>) documentSnapshot.get(Constants.KEY_FRIEND_IDS);
                            preferenceManager.putString(Constants.KEY_FRIEND_IDS, String.join("-", friendIds));

                            startActivity(new Intent(getApplicationContext(), MainActivity.class));
                            finish();
                        } else {
                            loading(false);
                            showToast("Incorrect phone number or password");
                        }
                    });

            // Nhớ sdt và mật khẩu
//        SharedPreferences.Editor editor = sharedPreferences.edit();
//        editor.putBoolean("isSaveLogin", isSaveLogin);
//
//      // Nếu tich vào ô save login
//        if (isSaveLogin) {
//            editor.putString("phone", binding.inputPhone.getText().toString());
//            editor.putString("password", binding.inputPassword.getText().toString());
//        }
//
//        editor.apply();
        }

        private Boolean isValidSignInDetails() {
            if (binding.inputPhone.getText().toString().trim().isEmpty()) {
                showToast("Please enter your phone number");
                return false;
            } else if (!Pattern.matches("(84|0[3|5|7|8|9])+([0-9]{8})", binding.inputPhone.getText().toString())) {
                showToast("Invalid phone number");
                return false;
            } else if (binding.inputPassword.getText().toString().trim().isEmpty()) {
                showToast("Please enter your password");
                return false;
            } else {
                return true;
            }
        }

        private void showToast(String message) {
            Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
        }

        private void loading(Boolean isLoading) {
            if (isLoading) {
                binding.buttonSignIn.setVisibility(View.INVISIBLE);
                binding.progressBar.setVisibility(View.VISIBLE);
            } else {
                binding.buttonSignIn.setVisibility(View.VISIBLE);
                binding.progressBar.setVisibility(View.INVISIBLE);
            }
        }
    }


    //////// Check đăng ký trùng sdt -> SignUpActivity




//    private Boolean isValidSignUpDetails() {
//        if (encodedImage == null) {
//            showToast("Please enter your image profile");
//            return false;
//        } else if (binding.inputName.getText().toString().trim().isEmpty()) {
//            showToast("Please enter your name");
//            return false;
//        } else if (binding.inputPhone.getText().toString().trim().isEmpty()) {
//            showToast("Please enter your phone number");
//            return false;
//        } else if (!Pattern.matches("(84|0[3|5|7|8|9])+([0-9]{8})", binding.inputPhone.getText().toString())) {
//            showToast("Invalid phone number");
//            return false;
//        } else if (checkExits()) {
//            showToast("This phone number already exists");
//            return false;
//        } else if (binding.inputPassword.getText().toString().trim().isEmpty()) {
//            showToast("Please enter your password");
//            return false;
//        } if (binding.inputConfirmPassword.getText().toString().trim().isEmpty()) {
//            showToast("Please enter confirm password");
//            return false;
//        } else if (!binding.inputPassword.getText().toString().equals(binding.inputConfirmPassword.getText().toString())) {
//            showToast("Password & confirm password must be same");
//            return false;
//        } else {
//            return true;
//        }
//    }
//
//    private Boolean checkExits() {
//        List<String> a = new ArrayList<>();
//        database.collection(Constants.KEY_COLLECTION_USERS)
//                .whereEqualTo(Constants.KEY_PHONE, binding.inputPhone.getText().toString().trim())
//                .get()
//                .addOnCompleteListener(task -> {
//                    if (task.isSuccessful() && task.getResult() != null) {
//                        a.add(task.getResult().getDocuments().get(0).getId());
//                    }
//                });
//        if (a.size() > 0) {
//            return false;
//        } else {
//            return true;
//        }
//    }



    private String a;




    //// Tạo dialog đăng xuất -> ProfileAcitivity


//    private void setListeners() {
//        if (otherUser == null) {
//            // Chủ tài khoản vào trang
//            binding.buttonBack.setOnClickListener(v ->
//                    startActivity(new Intent(getApplicationContext(), MainActivity.class)));
//            binding.buttonMain.setImageResource(R.drawable.ic_exit);
//            binding.buttonMain.setOnClickListener(v -> {
//                AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
//                alertDialog.setTitle("Sign out?")
//                            .setMessage("Are you sure to log out?")
//                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
//                                @Override
//                                public void onClick(DialogInterface dialogInterface, int i) {
//                                    signOut();
//                                }
//                            })
//                            .setNegativeButton("No", new DialogInterface.OnClickListener() {
//                                @Override
//                                public void onClick(DialogInterface dialogInterface, int i) {
//                                    dialogInterface.dismiss();
//                                }
//                            });
//                alertDialog.show();
//            });
//
//            binding.buttonChangeInfo.setVisibility(View.VISIBLE);
//            binding.buttonChangePassword.setVisibility(View.VISIBLE);
//            binding.buttonChangeImage.setVisibility(View.VISIBLE);
//        } else {
//            // Khởi tạo nút lần đầu
//            typeAction = preferenceManager.getString(Constants.KEY_FRIEND_IDS).contains(otherUser.getId()) ? "Unfriend" : "Addfriend";
//            setTypeAction(typeAction);
//
//            binding.buttonBack.setOnClickListener(v -> {
//                startActivity(new Intent(ProfileActivity.this, FriendsActivity.class));
//                finish();
//            });
//            //Người khác vào trang
//
//            binding.buttonChangeInfo.setVisibility(View.INVISIBLE);
//            binding.buttonChangePassword.setVisibility(View.INVISIBLE);
//            binding.buttonChangeImage.setVisibility(View.INVISIBLE);
//        }
//
//        binding.buttonChangeImage.setOnClickListener(v -> {
//            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
//            pickImage.launch(intent);
//        });
//    }





}
