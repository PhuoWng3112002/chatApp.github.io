package com.example.chatbtl.adapters;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chatbtl.R;
import com.example.chatbtl.databinding.ItemUserBinding;
import com.example.chatbtl.interfaces.UserInterface;
import com.example.chatbtl.models.User;

import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder>{

    private List<User> users;
    private UserInterface userInterface;

    public UserAdapter(List<User> users, UserInterface userInterface) {
        this.users = users;
        this.userInterface = userInterface;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemUserBinding itemUserBinding = ItemUserBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new UserViewHolder(itemUserBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        holder.setData(users.get(position));
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    public class UserViewHolder extends RecyclerView.ViewHolder {
        private final ItemUserBinding binding;

        UserViewHolder(ItemUserBinding itemUserBinding) {
            super(itemUserBinding.getRoot());
            binding = itemUserBinding;
        }

        void setData(User user) {
            byte[] bytes = Base64.decode(user.getImage(), Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
            binding.imageProfile.setImageBitmap(bitmap);
            binding.textName.setText(user.getName());
            binding.viewOnline.setBackgroundTintList(
                    ContextCompat.getColorStateList(binding.getRoot().getContext(),
                    user.getOnline() ?
                    R.color.online :
                    R.color.offline
            ));
            binding.getRoot().setOnClickListener(v -> userInterface.onUserClicked(user));
        }
    }

}
