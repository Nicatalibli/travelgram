package com.example.nicat.travelgram.Adapter;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.nicat.travelgram.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class PostClass extends ArrayAdapter<String> {

    private final ArrayList<String> userImage;
    private final ArrayList<String> userImageDate;
    private final Activity context;


    public PostClass(ArrayList<String> userImage,ArrayList<String> userImageDate, Activity context) {
        super(context,R.layout.custom_view,userImage);
        this.userImage = userImage;
        this.userImageDate = userImageDate;
        this.context = context;
    }


    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        LayoutInflater layoutInflater = context.getLayoutInflater();
        View customview = layoutInflater.inflate(R.layout.custom_view,null,true);

        ImageView imageView = customview.findViewById(R.id.imageviewCustomView);
        TextView photodate = customview.findViewById(R.id.dateCustomView);

        Picasso.get().load(userImage.get(position)).resize(1000,700).centerCrop().into(imageView);
        photodate.setText(userImageDate.get(position));


        return customview;
    }


}
