package com.example.chatbtl.adapters;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chatbtl.R;
import com.example.chatbtl.databinding.ItemConversionBinding;
import com.example.chatbtl.interfaces.ConversionInterface;
import com.example.chatbtl.models.ChatMessage;
import com.example.chatbtl.models.User;
import com.example.chatbtl.utilities.Constants;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class ConversionAdapter extends RecyclerView.Adapter<ConversionAdapter.ConversionViewHolder> {

    private List<ChatMessage> chatMessages;
    private ConversionInterface conversionInterface;
    private List<User> listConversionUsers;

    public ConversionAdapter(List<ChatMessage> chatMessages, List<User> listConversionUsers, ConversionInterface conversionInterface) {
        this.chatMessages = chatMessages;
        this.listConversionUsers = listConversionUsers;
        this.conversionInterface = conversionInterface;
    }

    @NonNull
    @Override
    public ConversionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ConversionViewHolder(
                ItemConversionBinding.inflate(
                        LayoutInflater.from(parent.getContext()),
                        parent,
                        false
                )
        );
    }

    @Override
    public void onBindViewHolder(@NonNull ConversionViewHolder holder, int position) {
        holder.setData(chatMessages.get(position));
    }

    @Override
    public int getItemCount() {
        return chatMessages.size();
    }


    public class ConversionViewHolder extends RecyclerView.ViewHolder {

        ItemConversionBinding binding;

        ConversionViewHolder(ItemConversionBinding itemConversionBinding) {
            super(itemConversionBinding.getRoot());
            binding = itemConversionBinding;
        }

        void setData(ChatMessage chatMessages) {
            byte[] bytes = Base64.decode(chatMessages.getConversionImage(), Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
            binding.imageProfile.setImageBitmap(bitmap);
            binding.textName.setText(chatMessages.getConversionName());
            binding.textNewMessage.setText(chatMessages.getMessage());
            binding.textTimeMessage.setText(convertDate(chatMessages.getDateObj()));

            for (User user : listConversionUsers) {
                if (user.getId().equals(chatMessages.getConversionId())) {
                    binding.viewOnline.setBackgroundTintList(
                        ContextCompat.getColorStateList(binding.getRoot().getContext(),
                                user.getOnline() ?
                                R.color.online :
                                R.color.offline
                    ));

                    binding.getRoot().setOnClickListener(v -> {
                        conversionInterface.onConversionClicked(user);
                    });

                    break;
                }
            }
        }

        void setData1(ChatMessage chatMessages) {
            byte[] bytes = Base64.decode(chatMessages.getConversionImage(), Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
            binding.imageProfile.setImageBitmap(bitmap);
            binding.textName.setText(chatMessages.getConversionName());
            binding.textNewMessage.setText(chatMessages.getMessage());
            binding.textTimeMessage.setText(convertDate(chatMessages.getDateObj()));
            Log.d("conID", chatMessages.getConversionId());

            User user = new User();
            FirebaseFirestore database = FirebaseFirestore.getInstance();
            database.collection(Constants.KEY_COLLECTION_USERS)
                    .document(chatMessages.getConversionId())
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            DocumentSnapshot documentSnapshot = task.getResult();
                            user.setId(documentSnapshot.getId());
                            user.setName(documentSnapshot.getString(Constants.KEY_NAME));
                            user.setPhone(documentSnapshot.getString(Constants.KEY_PHONE));
                            user.setImage(documentSnapshot.getString(Constants.KEY_IMAGE));
                            user.setFriendIds((List<String>) documentSnapshot.get(Constants.KEY_FRIEND_IDS));
                            user.setOnline(documentSnapshot.getBoolean(Constants.KEY_ONLINE));

                            if (user.getId() == null)
                                Log.d("alo", "null");
                            else
                                Log.d("alo", "00000");

                            binding.viewOnline.setBackgroundTintList(
                                ContextCompat.getColorStateList(binding.getRoot().getContext(),
                                        user.getOnline() ?
                                        R.color.online :
                                        R.color.offline
                            ));
                        }
                    });


            binding.getRoot().setOnClickListener(v -> {
                Log.d("click", user.getId());
                conversionInterface.onConversionClicked(user);
            });


        }
    }

    private String convertDate(Date date) {
        String pattern = "HH:mm, dd/MM/yyyy";
        int day = date.getDay(),
            month = date.getMonth(),
            year = date.getYear();
        Date curDate = new Date();
        if (day == curDate.getDay() && month == curDate.getMonth() && year == curDate.getYear()) {
            pattern = "HH:mm";
        } else if (date.getYear() == new Date().getYear()) {
            pattern = "HH:mm, dd/MM";
        }
        return new SimpleDateFormat(pattern).format(date);
    }
}
