package com.kuassivi.progressprofile;

import android.animation.ValueAnimator;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.kuassivi.view.ProgressProfileView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final TextView percentage = (TextView) findViewById(R.id.percentage);

        final ProgressProfileView profile = (ProgressProfileView) findViewById(R.id.profile);
        profile.getAnimator().setInterpolator(new AccelerateDecelerateInterpolator());
        profile.setProgress(0);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                profile.setProgress(38.5f);
                // Using Glide as usual
                Glide.with(MainActivity.this)
                        .load("http://lorempixel.com/500/500/people/1")
                        .placeholder(R.drawable.ic_icon_user_default)
                        .fitCenter() // Fit and center the bitmap
                        .into(profile);

                // Show the current percentage animated
                profile.getAnimator().addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        int absValue = Float.valueOf(animation.getAnimatedValue().toString()).intValue();
                        percentage.setText("Completed: " + absValue + "%");
                    }
                });

                profile.startAnimation();
            }
        }, 2000);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
