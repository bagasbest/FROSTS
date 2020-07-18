package com.bagas.socialmediaapps;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Adapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bagas.socialmediaapps.adapter.AdapterGroupChat;
import com.bagas.socialmediaapps.model.ModelGroupChat;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.HashMap;

public class GroupChatActivity extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;

    private String groupId, myGroupRole="";
    private ImageView groupIconIv;
    private ImageButton attachBtn, sendBtn;
    private TextView groupTitleTv, groupDescrioptionTv, groupTotalParticipantsTv;
    private EditText messageEt;
    private RecyclerView chatRv;

    private ArrayList<ModelGroupChat> groupChatArrayList;
    private AdapterGroupChat adapterGroupChat;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_chat);

        setTitle("");

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle("");

        groupIconIv = findViewById(R.id.groupIconIv);
        attachBtn = findViewById(R.id.attachBtn);
        sendBtn = findViewById(R.id.sendBtn);
        groupTitleTv = findViewById(R.id.groupTitleTv);
        messageEt = findViewById(R.id.messageEt);
        sendBtn = findViewById(R.id.sendBtn);
        chatRv = findViewById(R.id.chatRv);
        groupDescrioptionTv = findViewById(R.id.groupDesctiptionTv);


        //get id from selected group
        Intent intent = getIntent();
        groupId = intent.getStringExtra("groupId");

        firebaseAuth = FirebaseAuth.getInstance();
        loadGroupInfo();

        loadGroupMessages();

        loadGroupRole();


        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //input data
                String message = messageEt.getText().toString().trim();
                //validate
                if(TextUtils.isEmpty(message)){
                    Toast.makeText(GroupChatActivity.this, "Can't be empty...", Toast.LENGTH_SHORT).show();
                } else {
                    sendMessage(message);
                }
            }
        });


    }

    private void loadGroupRole() {
        final DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
        ref.child(groupId).child("participants").orderByChild("uid").equalTo(firebaseAuth.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for(DataSnapshot ds : snapshot.getChildren()){
                            myGroupRole = ""+ds.child("role").getValue();
                            //refresh menu items
                            invalidateOptionsMenu();

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void loadGroupMessages() {
        //init list
        groupChatArrayList = new ArrayList<>();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
        ref.child(groupId).child("messages")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        groupChatArrayList.clear();
                        for(DataSnapshot ds : snapshot.getChildren()){
                            ModelGroupChat model = ds.getValue(ModelGroupChat.class);
                            groupChatArrayList.add(model);
                        }
                        //adapter
                        adapterGroupChat = new AdapterGroupChat(GroupChatActivity.this, groupChatArrayList);
                        //set to recyclerView
                        chatRv.setAdapter(adapterGroupChat);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void sendMessage(String message) {
        String timestamp = ""+System.currentTimeMillis();

        //setup message data
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("sender", "" + firebaseAuth.getUid());
        hashMap.put("message", ""+message);
        hashMap.put("timestamp", ""+timestamp);
        hashMap.put("type", "text");

        //add in db
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
        ref.child(groupId).child("messages").child(timestamp)
                .setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        //message sent
                        //clear messageEt
                        messageEt.setText("");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(GroupChatActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

    }

    private void loadGroupInfo() {


        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
        ref.orderByChild("groupId").equalTo(groupId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for(DataSnapshot ds : snapshot.getChildren()){
                            String groupTitle = "" + ds.child("groupTitle").getValue();
                            String groupDescription = "" +ds.child("groupDescription").getValue();
                            String groupIcon = ""+ds.child("groupIcon").getValue();
                            String timestamp = ""+ds.child("timestamp").getValue();
                            String createdBy = ""+ds.child("createdBy").getValue();

                            groupTitleTv.setText(groupTitle);
                            groupDescrioptionTv.setText(groupDescription);
                            try {
                                Glide.with(GroupChatActivity.this).load(groupIcon).placeholder(R.drawable.ic_group_black_24dp).into(groupIconIv);
                            }catch (Exception e){
                                groupIconIv.setImageResource(R.drawable.ic_group_black_24dp);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        menu.findItem(R.id.action_create_group).setVisible(false);
        menu.findItem(R.id.action_hotchat).setVisible(false);
        menu.findItem(R.id.action_logout).setVisible(false);
        menu.findItem(R.id.action_add).setVisible(false);
        menu.findItem(R.id.action_search).setVisible(false);

        if(myGroupRole.equals("creator") || myGroupRole.equals("admin")){
            //in admin / creator, show add person option
            menu.findItem(R.id.action_add_participant).setVisible(true);
        } else {
            menu.findItem(R.id.action_add_participant).setVisible(false);

        }

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if(id == R.id.action_add_participant){
            Intent intent = new Intent(this, GroupParticipantAddActivity.class);
            intent.putExtra("groupId", groupId);
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }
}
