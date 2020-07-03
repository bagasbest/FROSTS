package com.bagas.socialmediaapps.adapter;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.telecom.Call;
import android.text.format.DateFormat;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bagas.socialmediaapps.AddPostActivity;
import com.bagas.socialmediaapps.PostDetailActivity;
import com.bagas.socialmediaapps.R;
import com.bagas.socialmediaapps.ThereProfileActivity;
import com.bagas.socialmediaapps.model.ModelPost;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class AdapterPost extends RecyclerView.Adapter<AdapterPost.MyHolder> {

    Context context;
    List<ModelPost> postList;

    String myUid;

    private DatabaseReference likesRef; //for likes database node
    private DatabaseReference postRef1; //reference of post

    private boolean mProcessLike = false;

    private ProgressDialog progressDialog;

    public AdapterPost(Context context, List<ModelPost> postList) {
        this.context = context;
        this.postList = postList;
        myUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        likesRef =FirebaseDatabase.getInstance().getReference("Likes");
        postRef1 = FirebaseDatabase.getInstance().getReference("Posts");
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //inflate layout row.xml
        View view = LayoutInflater.from(context).inflate(R.layout.row, parent, false);

        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final MyHolder holder, final int position) {
        //getData
        final String uid = postList.get(position).getUid();
        String uEmail = postList.get(position).getuEmail();
        String uName = postList.get(position).getuName();
        String uDp = postList.get(position).getuDp();
        final String pId = postList.get(position).getpId();
        String pTitle = postList.get(position).getpTitle();
        String pDescription = postList.get(position).getpDescription();
        final String pImage = postList.get(position).getpImage();
        String pTimeStamp = postList.get(position).getpTime();
        String pLikes = postList.get(position).getpLikes(); //contains total number of like for a post
        String pComments = postList.get(position).getpComments(); //contains total number of like for a post


        Calendar calendar = Calendar.getInstance(Locale.getDefault());
        calendar.setTimeInMillis(Long.parseLong(pTimeStamp));
        String pTime = DateFormat.format("dd/MM/yyyy hh:mm aa", calendar).toString();

        //setData
        holder.uNameTv.setText(uName);
        holder.pTimeTv.setText(pTime);
        holder.pTitleTv.setText(pTitle);
        holder.pDescriptionTv.setText(pDescription);
        holder.pLikesTv.setText(pLikes + " Likes");
        holder.pCommentsTv.setText(pComments + " Comments");
        //set likes for each post
        setLikses(holder, pId);

        //setUser Dp
        try {
            Glide.with(holder.itemView.getContext()).load(uDp).into(holder.uPicture);
        }catch (Exception e) {
            Picasso.get().load(R.drawable.ic_default_img).into(holder.uPicture);
        }

        //setUser post image

        if(pImage.equals("noImage")) {
            holder.pImageIv.setVisibility(View.GONE);
        } else {
            //show image
            holder.pImageIv.setVisibility(View.VISIBLE); //make sure to correct this
            try {
                Glide.with(holder.itemView.getContext()).load(pImage).into(holder.pImageIv);
            }catch (Exception e) {

            }
        }




        //handle more button click
        holder.moreBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showMoreOptions(holder.moreBtn, uid, myUid, pId, pImage);
            }
        });

        //handle like button click
        holder.likeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final int pLikes = Integer.parseInt(postList.get(position).getpLikes());
                mProcessLike = true;
                //get id of the post clicked
                final String postId = postList.get(position).getpId();
                likesRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(mProcessLike){
                            if(snapshot.child(postId).hasChild(myUid)){
                                //already liked, so remove like
                                postRef1.child(postId).child("pLikes").setValue(""+(pLikes-1));
                                likesRef.child(postId).child(myUid).removeValue();
                                mProcessLike = false;
                            } else {
                                //not like, then liked it
                                postRef1.child(postId).child("pLikes").setValue(""+(pLikes+1));
                                likesRef.child(postId).child(myUid).setValue("Liked");
                                mProcessLike = false;
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
        });

        //handle comment button click
        holder.commentBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //start post detail activity
                Intent intent = new Intent(context, PostDetailActivity.class);
                intent.putExtra("postId", pId);
                context.startActivity(intent);
            }
        });

        //handle share button click
        holder.shareBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(context, "Share post...", Toast.LENGTH_SHORT).show();
            }
        });

        holder.profileLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, ThereProfileActivity.class);
                intent.putExtra("uid", uid);
                context.startActivity(intent);
            }
        });




    }

    private void setLikses(final MyHolder holder, final String postKey) {
        likesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.child(postKey).hasChild(myUid)){
                    //user has liked this post
                    holder.likeBtn.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_liked, 0,0,0);
                    holder.likeBtn.setText("Liked");
                } else {
                    //user hasn't like post
                    holder.likeBtn.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_thumb_up_black_24dp, 0,0,0);
                    holder.likeBtn.setText("Like");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void showMoreOptions(ImageButton moreBtn, String uid, String myUid, final String pId, final String pImage) {
        //creating popup menu currently having option Delete, we will add more options later
        PopupMenu popupMenu = new PopupMenu(context, moreBtn, Gravity.END);

        //show delete option in only post(s) of currently, signed-in user
        if(uid.equals(myUid)) {
            //add items in menu
            popupMenu.getMenu().add(Menu.NONE,0, 0, "Delete");
            popupMenu.getMenu().add(Menu.NONE,1, 0, "Edit post");
        }
        popupMenu.getMenu().add(Menu.NONE,2,0,"View Detail");

        //item click listenerr
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int id = item.getItemId();
                if(id==0){
                    //delete is clicked
                    beginDelete(pId, pImage);
                } else if(id==1){
                    //edit is clicked
                    Intent intent = new Intent(context, AddPostActivity.class);
                    intent.putExtra("key", "editPost");
                    intent.putExtra("editPostId", pId);
                    context.startActivity(intent);
                }
                else if(id==2){
                    //start post detail activity
                    Intent intent = new Intent(context, PostDetailActivity.class);
                    intent.putExtra("postId", pId);
                    context.startActivity(intent);
                }
                return false;
            }
        });
        //show menu
        popupMenu.show();
    }

    private void beginDelete(String pId, String pImage) {
        //post can be with image or without image
        if(pImage.equals("noImage")){
            //post is without image
            deleteWithoutImage(pId);
        } else {
            //post with image
            deleteWithImage(pId, pImage);
        }
    }



    private void deleteWithImage(final String pId, String pImage) {
        //progress dialog
        progressDialog = new ProgressDialog(context);
        progressDialog();

        StorageReference picRef = FirebaseStorage.getInstance().getReferenceFromUrl(pImage);
        picRef.delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        //image deleted, now delete database
                        Query query = FirebaseDatabase.getInstance().getReference("Posts")
                                .orderByChild("pId").equalTo(pId);
                        query.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                for (DataSnapshot ds : snapshot.getChildren()) {
                                    //remove value
                                    ds.getRef().removeValue();

                                }
                                progressDialog.dismiss();
                                Toast.makeText(context, "Deleted succesfully...", Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                progressDialog.dismiss();
                                Toast.makeText(context, error.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //image can't go furher
                        progressDialog.dismiss();
                        Toast.makeText(context, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });


    }


    private void deleteWithoutImage(final String pId) {
        progressDialog = new ProgressDialog(context);
        progressDialog();
        Query query = FirebaseDatabase.getInstance().getReference("Posts")
                .orderByChild("pId").equalTo(pId);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()) {
                    //remove value
                    ds.getRef().removeValue();
                }
                progressDialog.dismiss();
                Toast.makeText(context, "Deleted succesfully...", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressDialog.dismiss();
                Toast.makeText(context, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void progressDialog() {
        progressDialog.show();
        progressDialog.setContentView(R.layout.progress_dialog);
        progressDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
    }


    @Override
    public int getItemCount() {
        return postList.size();
    }

    //view holder class
    class MyHolder extends RecyclerView.ViewHolder {

        //init view from row.xml
        ImageView uPicture, pImageIv;
        TextView uNameTv, pTimeTv, pTitleTv, pDescriptionTv, pLikesTv, pCommentsTv;
        Button likeBtn, commentBtn, shareBtn;
        ImageButton moreBtn;
        LinearLayout profileLayout;


        public MyHolder(@NonNull View itemView) {
            super(itemView);

            //init views
            uPicture = itemView.findViewById(R.id.uPictureIv);
            pImageIv = itemView.findViewById(R.id.pImageIv);
            uNameTv = itemView.findViewById(R.id.uNameTv);
            pTimeTv = itemView.findViewById(R.id.pTimeTv);
            pTitleTv = itemView.findViewById(R.id.pTitleTv);
            pDescriptionTv = itemView.findViewById(R.id.pDecsTv);
            pLikesTv = itemView.findViewById(R.id.pLikesTv);
            pCommentsTv = itemView.findViewById(R.id.pCommentsTv);
            likeBtn = itemView.findViewById(R.id.likeBtn);
            commentBtn = itemView.findViewById(R.id.commentBtn);
            shareBtn = itemView.findViewById(R.id.shareBtn);
            moreBtn = itemView.findViewById(R.id.moreBtn);
            profileLayout = itemView.findViewById(R.id.profileLayout);

        }
    }
}
