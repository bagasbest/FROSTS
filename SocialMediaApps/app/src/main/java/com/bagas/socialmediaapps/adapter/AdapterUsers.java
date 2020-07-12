package com.bagas.socialmediaapps.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bagas.socialmediaapps.ChatActivity;
import com.bagas.socialmediaapps.ThereProfileActivity;
import com.bagas.socialmediaapps.model.ModelUser;
import com.bagas.socialmediaapps.R;
import com.bagas.socialmediaapps.notification.Data;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.List;

public class AdapterUsers extends RecyclerView.Adapter<AdapterUsers.MyHolder>{

    Context context;
    List<ModelUser> userList;

    //for getting current user's id
    FirebaseAuth firebaseAuth;
    String myUid;

    public AdapterUsers(Context context, List<ModelUser> userList) {
        this.context = context;
        this.userList = userList;

        firebaseAuth = FirebaseAuth.getInstance();
        myUid = firebaseAuth.getUid();

    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //inflate layput (row_user.xml)
        View view = LayoutInflater.from(context).inflate(R.layout.row_users, parent, false);

        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, final int position) {
        //getData
        final String hisUID = userList.get(position).getUid();
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

        holder.blockIv.setImageResource(R.drawable.ic_unblock_24dp);
        // check if each user if blocked or not
        checkIsBlocked(hisUID, holder, position);


        //handle item click
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setItems(new String[]{"Profile", "Chat"}, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(which == 0) {
                            Intent intent = new Intent(context, ThereProfileActivity.class);
                            intent.putExtra("uid", hisUID);
                            context.startActivity(intent);
                        } if(which == 1) {
                            //click user from user list to start chatting
                            //start activity by putting UID of receiver
                            //we will use that UID to identify the user are we going to chat
                            inBlockedOrNot(hisUID);
                        }
                    }
                });
                builder.create().show();
            }
        });

        //click to block or unblock user
        holder.blockIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    if(userList.get(position).isBlocked()){
                        unBlockedUser(hisUID);
                    } else {
                        blockedUser(hisUID);
                    }
            }
        });

    }

    private void inBlockedOrNot(final String hisUID){
        //first check if sender (Cuurent user) is blocked by receiver or not
        //Logic: if uid of the sender (current user) exist in "BlockedUsers" of receiver then sender is blocked, otherwise not
        //if blocked then just display a message but can't send message
        //if not blocked then simply start the chat activity
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(hisUID).child("BlockedUsers").orderByChild("uid").equalTo(myUid)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for(DataSnapshot ds : snapshot.getChildren()){
                            if(ds.exists()){
                                Toast.makeText(context, "You're Blocked by user, can't send message",Toast.LENGTH_SHORT).show();
                                return;
                            }
                        }
                        //not blocked
                        Intent intent = new Intent(context, ChatActivity.class);
                        intent.putExtra("hisUID", hisUID);
                        context.startActivity(intent);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void checkIsBlocked(String hisUID, final MyHolder holder, final int position) {
        //check each user, if blocked or not
        //if uid of the user exist in "BlockedUsers" then that user is blocked, otherwise not
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(myUid).child("BlockedUsers").orderByChild("uid").equalTo(hisUID)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for(DataSnapshot ds : snapshot.getChildren()){
                            if(ds.exists()){
                                holder.blockIv.setImageResource(R.drawable.ic_block_black_24dp);
                                userList.get(position).setBlocked(true);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }


    private void blockedUser(String hisUID) {
        //blocked user by adding uid to current user's BlockedUsers node
        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put("uid", hisUID);

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(myUid).child("BlockedUsers").child(hisUID).setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        //blocked successful
                        Toast.makeText(context, "Blocked successfuly...", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(context, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

    }

    private void unBlockedUser(String hisUID) {
        //unblocked user by adding uid to current user's BlockedUsers node

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(myUid).child("BlockedUsers").orderByChild("uid").equalTo(hisUID)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for(DataSnapshot ds : snapshot.getChildren()){
                            if(ds.exists()){
                                //remove blocked user data from current user's BlockedUser list
                                ds.getRef().removeValue()
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                //Unblocked successfully
                                                Toast.makeText(context, "Unblocked successfully...", Toast.LENGTH_SHORT).show();
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Toast.makeText(context, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    //viewHolder
    static class MyHolder extends RecyclerView.ViewHolder {

        ImageView avatarIv, blockIv;
        TextView mNameTv;

        public MyHolder(@NonNull View itemView) {
            super(itemView);

            //init views
            avatarIv = itemView.findViewById(R.id.avatarIv_users);
            mNameTv = itemView.findViewById(R.id.nameTv_users);
            blockIv = itemView.findViewById(R.id.blockIv);

        }
    }
}
