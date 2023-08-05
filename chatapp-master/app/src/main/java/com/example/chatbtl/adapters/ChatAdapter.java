package com.example.chatbtl.adapters;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chatbtl.databinding.ItemReceiverMessageBinding;
import com.example.chatbtl.databinding.ItemSentMessageBinding;
import com.example.chatbtl.models.ChatMessage;
import com.example.chatbtl.models.User;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

    private List<ChatMessage> chatMessages;
    private String senderId;
    private User receiverUser;

    public final int TYPE_SENT = 1;
    public final int TYPE_RECEIVER = 2;

    public ChatAdapter(List<ChatMessage> chatMessages, String senderId, User receiverUser) {
        this.chatMessages = chatMessages;
        this.senderId = senderId;
        this.receiverUser = receiverUser;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_SENT) {
            return new SentMessageViewHolder(
                ItemSentMessageBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false)
            );
        } else {
            return new ReceiverMessageViewHolder(
                    ItemReceiverMessageBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false)
            );
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (getItemViewType(position) == TYPE_SENT) {
            ((SentMessageViewHolder) holder).setData(chatMessages.get(position));
        } else {
            ((ReceiverMessageViewHolder) holder).setData(chatMessages.get(position), receiverUser);
        }
    }

    @Override
    public int getItemCount() {
        return chatMessages.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (chatMessages.get(position).getSenderId().equals(senderId)) {
            // Là người gửi (chủ)
            return TYPE_SENT;
        } else {
            // Là người nhận (khách)
            return TYPE_RECEIVER;
        }
    }

    public class SentMessageViewHolder extends RecyclerView.ViewHolder {

        private ItemSentMessageBinding binding;

        SentMessageViewHolder(ItemSentMessageBinding itemSentMessageBinding) {
            super(itemSentMessageBinding.getRoot());
            binding = itemSentMessageBinding;
        }

        void setData(ChatMessage chatMessage) {
            binding.textMessage.setText(chatMessage.getMessage());
            binding.textDate.setText(convertDate(chatMessage.getDateObj()));
        }

    }

    public class ReceiverMessageViewHolder extends RecyclerView.ViewHolder {

        private ItemReceiverMessageBinding binding;

        ReceiverMessageViewHolder(ItemReceiverMessageBinding itemReceiverMessageBinding) {
            super(itemReceiverMessageBinding.getRoot());
            binding = itemReceiverMessageBinding;
        }

        void setData(ChatMessage chatMessage, User receiverUser) {
            binding.textMessage.setText(chatMessage.getMessage());
            binding.textDate.setText(convertDate(chatMessage.getDateObj()));
            byte[] bytes = Base64.decode(receiverUser.getImage(), Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
            binding.imageProfile.setImageBitmap(bitmap);
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
