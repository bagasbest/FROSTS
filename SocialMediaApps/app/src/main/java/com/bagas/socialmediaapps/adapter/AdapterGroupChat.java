package com.bagas.socialmediaapps.adapter;

import android.content.Context;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bagas.socialmediaapps.GroupChatActivity;
import com.bagas.socialmediaapps.R;
import com.bagas.socialmediaapps.model.ModelGroupChat;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class AdapterGroupChat extends  RecyclerView.Adapter<AdapterGroupChat.HolderGroupChat> {
    public static final int MSG_TYPE_LEFT = 0;
    public static final int MSG_TYPE_RIGHT = 1;

    private Context context;
    private ArrayList<ModelGroupChat> modelGroupChatArrayList;

    private FirebaseAuth firebaseAuth;


    public AdapterGroupChat(Context context, ArrayList<ModelGroupChat> modelGroupChatArrayList) {
        this.context = context;
        this.modelGroupChatArrayList = modelGroupChatArrayList;

        firebaseAuth = FirebaseAuth.getInstance();
    }



    @NonNull
    @Override
    public HolderGroupChat onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //inflate layout
        if(viewType == MSG_TYPE_RIGHT){
            View view = LayoutInflater.from(context).inflate(R.layout.row_groupchat_right, parent, false);
            return new HolderGroupChat(view);
        } else {
            View view = LayoutInflater.from(context).inflate(R.layout.row_groupchat_left, parent, false);
            return new HolderGroupChat(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull HolderGroupChat holder, int position) {
        ModelGroupChat modelGroupChat = modelGroupChatArrayList.get(position);
        String timestamp = modelGroupChat.getTimestamp();
        String message = modelGroupChat.getMessage();
        String senderUid = modelGroupChat.getSender();


        //convert time stamp to dd/mm/yy hh:mm am/pm
        Calendar calendar = Calendar.getInstance(Locale.ENGLISH);
        calendar.setTimeInMillis(Long.parseLong(timestamp));
        String dateTime = DateFormat.format("dd/MM/yyyy hh:mm aa", calendar).toString();



        //set data
        holder.messageTv.setText(message);
        holder.timeTv.setText(dateTime);


        setUsername(modelGroupChat, holder);
    }

    private void setUsername(ModelGroupChat modelGroupChat, final HolderGroupChat holder) {
        //get sender infor from uid in model
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.orderByChild("uid").equalTo(modelGroupChat.getSender())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for(DataSnapshot ds : snapshot.getChildren()){
                            String name = ""+ds.child("name").getValue();

                            holder.namaTv.setText(name);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    @Override
    public int getItemCount() {
        return modelGroupChatArrayList.size();
    }

    @Override
    public int getItemViewType(int position) {
        if(modelGroupChatArrayList.get(position).getSender().equals(firebaseAuth.getUid())){
            return MSG_TYPE_RIGHT;
        }else{
            return MSG_TYPE_LEFT;
        }
    }

    class HolderGroupChat extends RecyclerView.ViewHolder {

        private TextView namaTv, messageTv, timeTv;


        public HolderGroupChat(@NonNull View itemView) {
            super(itemView);

            namaTv = itemView.findViewById(R.id.nameTv);
            messageTv = itemView.findViewById(R.id.messageTv);
            timeTv = itemView.findViewById(R.id.timeTv);

        }
    }
}
