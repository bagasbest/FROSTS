package com.bagas.socialmediaapps.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bagas.socialmediaapps.R;
import com.bagas.socialmediaapps.model.ModelUser;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;

public class AdapterParticipantsAdd extends RecyclerView.Adapter<AdapterParticipantsAdd.HolderParticipantsAdd> {

    private Context context;
    private ArrayList<ModelUser> userList;
    String groupId, myGroupRole;

    public AdapterParticipantsAdd(Context context, ArrayList<ModelUser> userList, String groupId, String myGroupRole) {
        this.context = context;
        this.userList = userList;
        this.groupId = groupId;
        this.myGroupRole = myGroupRole;
    }

    @NonNull
    @Override
    public HolderParticipantsAdd onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
       View view = LayoutInflater.from(context).inflate(R.layout.row_participant_add, parent, false);
        return new HolderParticipantsAdd(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HolderParticipantsAdd holder, final int position) {
        //getData
        final ModelUser model = userList.get(position);
        String name = model.getName();
        String image = model.getImage();
        final String uid = model.getUid();


        //set data
        holder.nameTv.setText(name);

        try {
            Glide.with(context).load(image).placeholder(R.drawable.ic_default_img).into(holder.avatarIv);
        } catch (Exception e) {
            holder.avatarIv.setImageResource(R.drawable.ic_default_img);
        }

        checIfAlreadyExist(model, holder);

        //handle click
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
                ref.child(groupId).child("participants").child(uid)
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if(snapshot.exists()){
                                    //user exist / not participant
                                    String previousRole = ""+snapshot.child("role").getValue();

                                    //option to display in dialog
                                    String[]  options;

                                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                                    builder.setTitle("Choose option");
                                    if(myGroupRole.equals("creator")){
                                        if(previousRole.equals("admin")){
                                            //in creator, he is admin
                                            options = new String[] {"Remove Admin", "Remove User"};
                                            builder.setItems(options, new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    //handle item clicks
                                                    if(which == 0){
                                                        //remove admin clicked
                                                        removeAdmin(model);
                                                    } else {
                                                        //remove user clicked
                                                        removeParticipants(model);
                                                    }
                                                }
                                            }).show();
                                        }
                                        else if(previousRole.equals("participant")){
                                            //in creatoe, he is participants
                                            options = new String[] {"Make Admin", "Remove user"};
                                            builder.setItems(options, new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    //handle items clicks
                                                    if(which == 0){
                                                        //make admin clicked
                                                        makeAdmin(model);
                                                    } else {
                                                        //remove user clicked
                                                        removeParticipants(model);
                                                    }
                                                }
                                            }).show();
                                        }
                                    } else if(myGroupRole.equals("admin")){
                                        if(previousRole.equals("creator")){
                                            //in admin, he is creator
                                            Toast.makeText(context, "Creeator in group", Toast.LENGTH_SHORT).show();
                                        }
                                        else if(previousRole.equals("admin")){
                                            //in admin, he is admin
                                            options = new String[] {"Remove Admin", "Remove User"};
                                            builder.setItems(options, new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    //handle item clicks
                                                    if(which == 0){
                                                        //remove admin clicked
                                                        removeAdmin(model);
                                                    } else {
                                                        //remove user clicked
                                                        removeParticipants(model);
                                                    }
                                                }
                                            }).show();
                                        }
                                        else if(previousRole.equals("participant")){
                                            //in admin, he is participant
                                            options = new String[]{"Make Admin", "Remove user"};
                                            builder.setItems(options, new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    //make admin clicked
                                                    if(which == 0){
                                                        makeAdmin(model);
                                                    }else {
                                                        removeParticipants(model);
                                                    }
                                                }
                                            }).show();
                                        }
                                    }
                                } else{
                                    //user doesn't exist/not-participant: add
                                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                                    builder.setTitle("Add Participant")
                                            .setMessage("Add this user in this group?")
                                            .setPositiveButton("ADD", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    addParticipant(model);
                                                }
                                            })
                                            .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                  dialog.dismiss();
                                                }
                                            }).show();
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
            }
        });

    }

    private void addParticipant(ModelUser model) {
        //setUp user data
        String timestamp = ""+System.currentTimeMillis();
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("uid", model.getUid());
        hashMap.put("role", "participant");
        hashMap.put("timestamp", timestamp);

        //add that user to group
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
        ref.child(groupId).child("participants").child(model.getUid()).setValue(hashMap)
               .addOnSuccessListener(new OnSuccessListener<Void>() {
                   @Override
                   public void onSuccess(Void aVoid) {
                       //added successfully
                       Toast.makeText(context, "Added successfully...", Toast.LENGTH_SHORT).show();
                   }
               })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(context, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void makeAdmin(ModelUser model) {
        //setup data
        String timestamp = ""+System.currentTimeMillis();
        HashMap <String, Object> hashMap = new HashMap<>();
        hashMap.put("role","admin");
        //update in db

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
        ref.child(groupId).child("participants").child(model.getUid()).updateChildren(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        //added successfully
                        Toast.makeText(context, "This user update to admin successfully...", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(context, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

    }

    private void removeParticipants(ModelUser model) {
        //remove user
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
        ref.child(groupId).child("participants").child(model.getUid()).removeValue()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(context, "Remove user successfully...", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(context, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void removeAdmin(ModelUser model) {
        //setup data - remove just status - not user
        HashMap <String, Object> hashMap = new HashMap<>();
        hashMap.put("role","participant");
        //update in db

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
        ref.child(groupId).child("participants").child(model.getUid()).updateChildren(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        //added successfully
                        Toast.makeText(context, "This user no longer admin", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(context, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void checIfAlreadyExist(ModelUser model, final HolderParticipantsAdd holder) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
        ref.child(groupId).child("participants").child(model.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.exists()){
                            String hisRole = ""+snapshot.child("role").getValue();
                            holder.statusTv.setText(hisRole);
                        }else {
                            holder.statusTv.setText("");
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

    class HolderParticipantsAdd extends RecyclerView.ViewHolder {

        private ImageView avatarIv;
        private TextView nameTv, statusTv;

        public HolderParticipantsAdd(@NonNull View itemView) {
            super(itemView);
            avatarIv = itemView.findViewById(R.id.avatarIv);
            nameTv = itemView.findViewById(R.id.nameTv);
            statusTv = itemView.findViewById(R.id.statusTv);



        }
    }
}
