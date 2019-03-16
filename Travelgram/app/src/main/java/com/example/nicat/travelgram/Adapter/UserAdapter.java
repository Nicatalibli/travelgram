package com.example.nicat.travelgram.Adapter;

import android.content.Context;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.nicat.travelgram.Model.postuser;
import com.example.nicat.travelgram.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mikhaellopez.circularimageview.CircularImageView;

import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder> {

    private Context mContext;
    private List<postuser> mUsers;

    private FirebaseUser firebaseUser;

    public UserAdapter(Context mContext, List<postuser> mUsers) {
        this.mContext = mContext;
        this.mUsers = mUsers;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.user_item,viewGroup,false);
        return new UserAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder viewHolder, int i) {

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        final postuser postuser = mUsers.get(i);

        viewHolder.btn_follow.setVisibility(View.VISIBLE);

        viewHolder.username.setText(postuser.getUsername());
        viewHolder.fullname.setText(postuser.getNamesurname());
        Glide.with(mContext).load(postuser.getProfilepicture()).into(viewHolder.image_profile);
        isFollowing(postuser.getUserid(), viewHolder.btn_follow);

         if(postuser.getUserid().equals(firebaseUser.getUid())) {
            viewHolder.btn_follow.setVisibility(View.GONE);
        }


        viewHolder.btn_follow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(viewHolder.btn_follow.getText().toString().equals("follow")){
                    FirebaseDatabase.getInstance().getReference().child("Follow").child(firebaseUser.getUid())
                            .child("following").child(postuser.getUserid()).setValue(true);
                    FirebaseDatabase.getInstance().getReference().child("Follow").child(postuser.getUserid())
                            .child("followers").child(firebaseUser.getUid()).setValue(true);
                } else {
                    FirebaseDatabase.getInstance().getReference().child("Follow").child(firebaseUser.getUid())
                            .child("following").child(postuser.getUserid()).removeValue();
                    FirebaseDatabase.getInstance().getReference().child("Follow").child(postuser.getUserid())
                            .child("followers").child(firebaseUser.getUid()).removeValue();
                }


            }
        });


    }

    @Override
    public int getItemCount() {
        return  mUsers.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        public TextView username;
        public TextView fullname;
        public CircularImageView image_profile;
        public Button btn_follow;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            username = itemView.findViewById(R.id.username);
            fullname = itemView.findViewById(R.id.fullname);
            image_profile = itemView.findViewById(R.id.image_profile);
            btn_follow = itemView.findViewById(R.id.btn_follow);
        }
    }

    private void isFollowing(final String userid, final Button button){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference()
                .child("Follow").child(firebaseUser.getUid()).child("following");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.child(userid).exists()){
                    button.setText("following");
                } else {
                    button.setText("follow");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

}
