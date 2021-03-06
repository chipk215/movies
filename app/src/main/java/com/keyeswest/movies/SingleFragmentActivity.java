package com.keyeswest.movies;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;

/* **** ATTRIBUTION ****
 *
 * SingleFragmentActivity encapsulates the initialization of a
 * fragment when it is the only fragment used in an Activity.
 *
 * The concept and code came from:
 * 3rd Edition Android Programming
 * Big Nerd Ranch Guide by Philips, Stewart, and Marsicano
 *
 * I'm using as though it is a pattern for single fragment activities which I picked up
 * via independent study.
 *
 */

public abstract class SingleFragmentActivity extends AppCompatActivity {

    protected abstract Fragment createFragment();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fragment);

        FragmentManager fm = getSupportFragmentManager();
        Fragment fragment = fm.findFragmentById(R.id.fragment_container);

        if (fragment == null) {
            fragment = createFragment();
            fm.beginTransaction()
                    .add(R.id.fragment_container, fragment)
                    .commit();
        }
    }
}
