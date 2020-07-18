package com.bagas.socialmediaapps.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bagas.socialmediaapps.GroupChatActivity;
import com.bagas.socialmediaapps.R;
import com.bagas.socialmediaapps.model.ModelGroupChatList;
import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class AdapterGroupChatList extends RecyclerView.Adapter<AdapterGroupChatList.HolderGroupChatList> {

    private Context context;
    private ArrayList<ModelGroupChatList> groupChatLists;

    public AdapterGroupChatList(Context context, ArrayList<ModelGroupChatList> groupChatLists) {
        this.context = context;
        this.groupChatLists = groupChatLists;
    }

    @NonNull
    @Override
    public HolderGroupChatList onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //inflate layout
        View view = LayoutInflater.from(context).inflate(R.layout.row_groupchat, parent, false);
        return new HolderGroupChatList(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final HolderGroupChatList holder, int position) {
        //getData
        ModelGroupChatList modelGroupChatList = groupChatLists.get(position);
        final String groupId = modelGroupChatList.getGroupId();
        String groupIcon = modelGroupChatList.getGroupIcon();
        String groupTitle = modelGroupChatList.getGroupTitle();

        holder.nameTv.setText("");
        holder.messageTv.setText("");
        holder.timeTv.setText("");


        //load last message and message-time
        loadLastMessage(modelGroupChatList, holder);

        //set data
        holder.groupTitleTv.setText(groupTitle);
        try {
            Glide.with(context).load(groupIcon).placeholder(R.drawable.ic_group_black_24dp).into(holder.groupIconIv);
        } catch (Exception e){
            holder.groupIconIv.setImageResource(R.drawable.ic_group_black_24dp);
        }

        //handle group click
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //open group chat
                Intent intent = new Intent(context, GroupChatActivity.class);
                intent.putExtra("groupId", groupId);
                context.startActivity(intent);
            }
        });
    }

    private void loadLastMessage(ModelGroupChatList modelGroupChatList, final HolderGroupChatList holder) {
        //get last message from group
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
        ref.child(modelGroupChatList.getGroupId()).child("messages").limitToLast(1)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for(DataSnapshot ds : snapshot.getChildren()){
                            String message = ""+ds.child("message").getValue();
                            String timestamp = ""+ds.child("timestamp").getValue();
                            String sender = ""+ds.child("sender").getValue();

                            //Convert time
                            Calendar calendar = Calendar.getInstance(Locale.ENGLISH);
                            calendar.setTimeInMillis(Long.parseLong(timestamp));
                            String dateTime = DateFormat.format("dd/MMM/yyyy hh:mm aa", calendar).toString();

                            holder.messageTv.setText(message);
                            holder.timeTv.setText(dateTime);

                            //get into sender of last message
                            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
                            ref.orderByChild("uid").equalTo(sender)
                                    .addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            for(DataSnapshot ds : snapshot.getChildren()) {
                                                String name = ""+ds.child("name").getValue();
                                                holder.nameTv.setText(name);
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {

                                        }
                                    });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    @Override
    public int getItemCount() {
        return groupChatLists.size();
    }


    //view holder class
    class HolderGroupChatList extends RecyclerView.ViewHolder{

        private ImageView groupIconIv;
        private TextView groupTitleTv, nameTv, messageTv, timeTv;

        public HolderGroupChatList(@NonNull View itemView) {
            super(itemView);

            groupIconIv = itemView.findViewById(R.id.groupIconIv);
            groupTitleTv = itemView.findViewById(R.id.groupTitleTv);
            nameTv = itemView.findViewById(R.id.nameTv);
            messageTv = itemView.findViewById(R.id.messageTv);
            timeTv = itemView.findViewById(R.id.timeTv);
        }
    }
}
