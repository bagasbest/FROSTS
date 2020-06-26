package com.bagas.socialmediaapps;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import org.w3c.dom.Text;

import java.util.List;

public class AdapterUsers extends RecyclerView.Adapter<AdapterUsers.MyHolder>{

    Context context;
    List<ModelUser> userList;

    public AdapterUsers(Context context, List<ModelUser> userList) {
        this.context = context;
        this.userList = userList;
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //inflate layput (row_user.xml)
        View view = LayoutInflater.from(context).inflate(R.layout.row_users, parent, false);

        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, int position) {
        //getData
        String userImage = userList.get(position).getImage();
        final String userName = userList.get(position).getName();
//        String userEmail = userList.get(position).getEmail();

        //getData
        holder.mNameTv.setText(userName);

        try {
            Glide.with(holder.itemView.getContext()).load(userImage).placeholder(R.drawable.ic_default_img).into(holder.avatarIv);
        } catch (Exception e) {
            e.getMessage();
        }

        //handle item click
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(context, ""+userName, Toast.LENGTH_SHORT).show();
            }
        });

    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    //viewHolder
    static class MyHolder extends RecyclerView.ViewHolder {

        ImageView avatarIv;
        TextView mNameTv;

        public MyHolder(@NonNull View itemView) {
            super(itemView);

            //init views
            avatarIv = itemView.findViewById(R.id.avatarIv_users);
            mNameTv = itemView.findViewById(R.id.nameTv_users);

        }
    }
}
