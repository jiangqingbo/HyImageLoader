package com.huyunit.imageloader.sample;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.huyunit.imageloader.lengthimage.LengthImageView;

public class MainActivity extends AppCompatActivity {

    private LengthImageView mImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mImageView = findViewById(R.id.lengthImageView);

        String assetsImageString = "length.png";

        mImageView.setImageAssetsString(assetsImageString);

    }
}
