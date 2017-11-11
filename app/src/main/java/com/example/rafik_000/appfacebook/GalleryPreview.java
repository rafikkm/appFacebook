package com.example.rafik_000.appfacebook;

import com.bumptech.glide.Glide;

import java.io.File;
import java.net.URL;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;


public class GalleryPreview extends AppCompatActivity {

    ImageView GalleryPreviewImg;
    String path;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.gallery_preview);
        Intent intent = getIntent();
        path = intent.getStringExtra("path");
        GalleryPreviewImg = (ImageView) findViewById(R.id.GalleryPreviewImg);

        try {
        Glide.with(GalleryPreview.this)
                .load(new URL(path)) // Uri of the picture
                .into(GalleryPreviewImg);}
        catch (Exception e) {}

    }
}