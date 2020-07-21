package com.bagas.socialmediaapps;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bagas.socialmediaapps.adapter.AdapterParticipantsAdd;
import com.bagas.socialmediaapps.model.ModelUser;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class GroupInfoActivity extends AppCompatActivity {

    private ActionBar actionBar;

    private ImageView groupIconIv;
    private TextView descriptionTv, createdByTv, editGroup, leaveGroupTv, participantsTv, addParticipantTv;
    private RecyclerView participantRv;

    private String groupId;
    private String myGroupRole= "";

    private FirebaseAuth firebaseAuth;

    private ArrayList<ModelUser> userArrayList;
    private AdapterParticipantsAdd adapterParticipantsAdd;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_info);

        actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);

        final Intent intent = getIntent();
        groupId = intent.getStringExtra("groupId");

        groupIconIv = findViewById(R.id.groupIconIv);
        descriptionTv = findViewById(R.id.groupDescriptionTv);
        createdByTv = findViewById(R.id.createdByTv);
        editGroup = findViewById(R.id.editGroupTv);
        leaveGroupTv = findViewById(R.id.leaveGroupTv);
        participantsTv = findViewById(R.id.participantsTv);
        participantRv = findViewById(R.id.participantsRv);
        addParticipantTv = findViewById(R.id.addParticipantTv);

        firebaseAuth = FirebaseAuth.getInstance();

        loadGroupInfo();
        loadMyGroupRole();

        editGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent1 = new Intent(GroupInfoActivity.this, GroupParticipantAddActivity.class);
                intent1.putExtra("groupId", groupId);
                startActivity(intent1);
            }
        });

    }

    private void loadMyGroupRole() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
        ref.child(groupId).child("participants").orderByChild("uid")
                .equalTo(firebaseAuth.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for(DataSnapshot ds : snapshot.getChildren()){
                            myGroupRole = ""+ds.child("role").getValue();
                            actionBar.setSubtitle(firebaseAuth.getCurrentUser().getEmail() + "(" + myGroupRole + ")");

                            if(myGroupRole.equals("participant")){
                                editGroup.setVisibility(View.GONE);
                                addParticipantTv.setVisibility(View.GONE);
                                leaveGroupTv.setText("Leave Group");
                            }
                            else if(myGroupRole.equals("admin")){
                                editGroup.setVisibility(View.GONE);
                                addParticipantTv.setVisibility(View.VISIBLE);
                                leaveGroupTv.setText("Leave Group");
                            }else {
                                editGroup.setVisibility(View.VISIBLE);
                                addParticipantTv.setVisibility(View.VISIBLE);
                                leaveGroupTv.setText("Delete Group");
                            }

                        }
                        loadParticipant();
                    }


                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void loadParticipant() {
        userArrayList = new ArrayList<>();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
        ref.child(groupId).child("participants")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        userArrayList.clear();
                        for(DataSnapshot ds : snapshot.getChildren()){
                            //get uid from group > participants
                            String uid = ""+ds.child("uid").getValue();

                            //get info user using uid we got abowe
                            DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
                            reference.orderByChild("uid").equalTo(uid).addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    for(DataSnapshot ds : snapshot.getChildren()){
                                        ModelUser modelUser = ds.getValue(ModelUser.class);

                                        userArrayList.add(modelUser);
                                    }
                                    //adapter
                                    adapterParticipantsAdd = new AdapterParticipantsAdd(GroupInfoActivity.this, userArrayList, groupId, myGroupRole);
                                    participantRv.setAdapter(adapterParticipantsAdd);
                                    participantsTv.setText("Participants ("+userArrayList.size() + " People)");
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

    private void loadGroupInfo() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
        ref.orderByChild("groupId").equalTo(groupId)
        .addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot ds : snapshot.getChildren()){
                    String groupId = ""+ds.child("groupId").getValue();
                    String groupTitle = ""+ds.child("groupTitle").getValue();
                    String groupDescription = ""+ds.child("groupDescription").getValue();
                    String groupIcon = ""+ds.child("groupIcon").getValue();
                    String createdBy = ""+ds.child("createdBy").getValue();
                    String timeStamp = ""+ds.child("timestamp").getValue();

                    Calendar calendar = Calendar.getInstance(Locale.ENGLISH);
                    calendar.setTimeInMillis(Long.parseLong(timeStamp));
                    String dateTime = DateFormat.format("dd/MM/yyyy hh:mm aa",calendar).toString();

                    loadCreatorInfo(dateTime, createdBy);

                    //set group info
                    actionBar.setTitle(groupTitle);
                    descriptionTv.setText(groupDescription);

                    try {
                        Glide.with(GroupInfoActivity.this).load(groupIcon).placeholder(R.drawable.ic_group_black_24dp).into(groupIconIv);
                    } catch (Exception e){
                        groupIconIv.setImageResource(R.drawable.ic_group_black_24dp);
                    }


                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void loadCreatorInfo(final String dateTime, String createdBy) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.orderByChild("uid").equalTo(createdBy).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot ds : snapshot.getChildren()){
                    String name = ""+ds.child("name").getValue();
                    createdByTv.setText("Created by " + name + " on" + dateTime);


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
