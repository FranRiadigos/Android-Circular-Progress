package com.franriadigos.demo;

import android.os.Bundle;
import android.os.Handler;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.franriadigos.view.ImageViewCircularProgress;
import com.squareup.picasso.Picasso;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final TextView percentage = findViewById(R.id.percentage);

        final ImageViewCircularProgress profile = findViewById(R.id.profile);

        // Using an Image Loader as usual
        Picasso.get()
                .load("http://i.imgur.com/DvpvklR.png")
                .placeholder(R.drawable.ic_icon_user_default)
                .into(profile);

        profile.getAnimator().setInterpolator(new AccelerateDecelerateInterpolator());
        profile.setProgress(0);

        new Handler().postDelayed(() -> {
            // Waits for 2 sec and then updates the percentage
            profile.setProgress(78.5f);

            // Show the current percentage while animating
            profile.getAnimator()
                   .addUpdateListener(animation -> {
                       int absValue = Float
                               .valueOf(animation.getAnimatedValue().toString())
                               .intValue();
                       if(percentage != null) {
                           percentage.setText("Completed: " + absValue + "%");
                       }
                   });

            profile.startAnimation();

        }, 2000);
    }
}
