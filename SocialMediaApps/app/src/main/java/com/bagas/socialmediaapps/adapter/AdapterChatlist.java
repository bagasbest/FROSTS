package com.bagas.socialmediaapps.adapter;

import android.content.Context;
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
import com.bagas.socialmediaapps.R;
import com.bagas.socialmediaapps.model.ModelUser;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.List;

public class AdapterChatlist extends RecyclerView.Adapter<AdapterChatlist.MyHolder> {


    Context context;
    List<ModelUser> userList;
    private HashMap<String, Object> lastMessageMap;
    //for getting current user's id
    FirebaseAuth firebaseAuth;
    String myUid;

    public AdapterChatlist(Context context, List<ModelUser> userList) {
        this.context = context;
        this.userList = userList;
        lastMessageMap = new HashMap<>();

        firebaseAuth = FirebaseAuth.getInstance();
        myUid = firebaseAuth.getUid();
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //inflate layout roe_chatlist.xml
        View view = LayoutInflater.from(context).inflate(R.layout.row_chatlist, parent, false);
        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, int position) {
        //get data
        final String hisUid = userList.get(position).getUid();
        String userImage = userList.get(position).getImage();
        String userName = userList.get(position).getName();
        String lastMessage = (String) lastMessageMap.get(hisUid);

        //set data
        if(lastMessage == null || lastMessage.equals("default")){
            holder.lastMessageTv.setVisibility(View.GONE);
        }
        else {
            holder.lastMessageTv.setVisibility(View.VISIBLE);
            holder.lastMessageTv.setText(lastMessage);
        }

        holder.nameTv.setText(userName);

        try {
            Glide.with(holder.itemView.getContext()).load(userImage).placeholder(R.drawable.ic_default_img).into(holder.profileIv);
        }catch (Exception e){
            Picasso.get().load(R.drawable.ic_default_img).into(holder.profileIv);
        }

        //set online status of other users in chat list
        if(userList.get(position).getOnlineStatus().equals("online")){
            holder.onlineStatusTv.setImageResource(R.drawable.circle_online);
        }
        else {
            holder.onlineStatusTv.setImageResource(R.drawable.circle_offline);

        }

        //handle click of user in chatlist
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                inBlockedOrNot(hisUid);


            }
        });
    }

    private void inBlockedOrNot(final String hisUid) {
        //first check if sender (Cuurent user) is blocked by receiver or not
        //Logic: if uid of the sender (current user) exist in "BlockedUsers" of receiver then sender is blocked, otherwise not
        //if blocked then just display a message but can't send message
        //if not blocked then simply start the chat activity
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(hisUid).child("BlockedUsers").orderByChild("uid").equalTo(myUid)
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
                        intent.putExtra("hisUID", hisUid);
                        context.startActivity(intent);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    public void setLastMessageMap(String userId, String lastMessage){
        lastMessageMap.put(userId, lastMessage);

    }

    @Override
    public int getItemCount() {
        return userList.size(); //size of list
    }

    class MyHolder extends RecyclerView.ViewHolder{

        //views of row_chatlist.xml
        ImageView profileIv, onlineStatusTv;
        TextView nameTv, lastMessageTv;

        public MyHolder(@NonNull View itemView) {
            super(itemView);

            //init views
            profileIv = itemView.findViewById(R.id.profileivIv);
            onlineStatusTv = itemView.findViewById(R.id.onlineStatusIv);
            nameTv = itemView.findViewById(R.id.nameTv);
            lastMessageTv = itemView.findViewById(R.id.lastMessageTv);
        }
    }
}
