package com.bagas.socialmediaapps;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;

import com.bagas.socialmediaapps.adapter.AdapterParticipantsAdd;
import com.bagas.socialmediaapps.model.ModelUser;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class GroupParticipantAddActivity extends AppCompatActivity {

    //init RecyclerView
    private RecyclerView userRv;

    private ActionBar actionBar;

    private String groupId, myGroupRole;

    //firebase
    FirebaseAuth firebaseAuth;

    private ArrayList<ModelUser> userList;
    private AdapterParticipantsAdd adapterParticipantsAdd;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_participant_add);

        actionBar = getSupportActionBar();
        actionBar.setTitle("Add Participants");
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);

        firebaseAuth = FirebaseAuth.getInstance();

        Intent intent = getIntent();
        groupId = intent.getStringExtra("groupId");
        loadGroupInfo();



        //casting view
        userRv = findViewById(R.id.userRv);

    }

    private void getAllUsers() {
        //init list
        userList = new ArrayList<>();
        //load users from db
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userList.clear();
                for(DataSnapshot ds : snapshot.getChildren()){
                    ModelUser modelUser = ds.getValue(ModelUser.class);

                    //getAllUser accept currently signed in
                    if(!firebaseAuth.getUid().equals(modelUser.getUid())){
                        //not my uid
                        userList.add(modelUser);
                    }
                }

                //setup adapter
                adapterParticipantsAdd = new AdapterParticipantsAdd(GroupParticipantAddActivity.this, userList, ""+groupId, ""+myGroupRole);
                //set adapter to recycler view
                userRv.setAdapter(adapterParticipantsAdd);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void loadGroupInfo() {
        final DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Groups");

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
        ref.orderByChild("groupId").equalTo(groupId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for(DataSnapshot ds : snapshot.getChildren()){
                            String groupId = ""+ds.child("groupId").getValue();
                            final String groupTitle = ""+ds.child("groupTitle").getValue();
                            String groupDescription = ""+ds.child("groupDescription").getValue();
                            String groupIcon = ""+ds.child("groupIcon").getValue();
                            String createdBy = ""+ds.child("createdBy").getValue();
                            String timestamp = ""+ds.child("timestamp").getValue();

                            actionBar.setTitle("Add Participants");

                            reference.child(groupId).child("participants").child(firebaseAuth.getUid())
                                    .addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            if(snapshot.exists()){
                                                myGroupRole = ""+snapshot.child("role").getValue();
                                                actionBar.setTitle(groupTitle + "("+myGroupRole+")");

                                                getAllUsers();
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
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }
}
