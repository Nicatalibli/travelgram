package com.example.nicat.travelgram.Adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.nicat.travelgram.Model.Post;
import com.example.nicat.travelgram.Model.postuser;
import com.example.nicat.travelgram.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.ViewHolder>{

    public Context mContext;
    public List<Post> mPost;

    private FirebaseUser firebaseUser;

    public PostAdapter(Context mContext, List<Post> mPost) {
        this.mContext = mContext;
        this.mPost = mPost;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {

        View view = LayoutInflater.from(mContext).inflate(R.layout.post_item,viewGroup,false);

        return new PostAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        Post post = mPost.get(i);

        Glide.with(mContext).load(post.getPhotourl()).into(viewHolder.post_image);
        viewHolder.photodate.setText(post.getPhotodate());


        /*if(post.getDescription().equals("")){
            viewHolder.description.setVisibility(View.GONE);
        } else {
            viewHolder.description.setVisibility(View.VISIBLE);
            viewHolder.description.setText(post.getDescription());
        }
        */


        publisherInfo(viewHolder.image_profile,viewHolder.username,post.getPublisher());

    }

    @Override
    public int getItemCount() {
        return mPost.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        public ImageView image_profile,post_image;
        public TextView username,publisher,description,photodate;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);


            image_profile = itemView.findViewById(R.id.image_profile);
            post_image = itemView.findViewById(R.id.post_image);
            username = itemView.findViewById(R.id.username);
            photodate = itemView.findViewById(R.id.photodate);
            //description = itemView.findViewById(R.id.description);

        }



    }

    private void publisherInfo(final ImageView image_profile, final TextView username, String userid){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("user").child(userid);

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                postuser postuser =  dataSnapshot.getValue(postuser.class);
                Glide.with(mContext).load(postuser.getProfilepicture()).into(image_profile);
                username.setText(postuser.getUsername());
                //publiser.setText(postuser.getUsername());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


}
