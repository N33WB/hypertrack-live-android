package io.hypertrack.sendeta.view;

import android.app.TaskStackBuilder;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.hypertrack.hyperlog.HyperLog;
import com.hypertrack.lib.HyperTrack;
import com.hypertrack.lib.callbacks.HyperTrackCallback;
import com.hypertrack.lib.internal.consumer.view.Placeline.PlacelineFragment;
import com.hypertrack.lib.models.ErrorResponse;
import com.hypertrack.lib.models.SuccessResponse;

import io.hypertrack.sendeta.R;
import io.hypertrack.sendeta.store.ActionManager;

/**
 * Created by Aman Jain on 24/05/17.
 */

public class Placeline extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = Placeline.class.getSimpleName();
    PlacelineFragment placelineFragment;
    FloatingActionButton floatingActionButton;
    private DrawerLayout drawer;
    NavigationView navigationView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_placeline);

        initUI();
    }

    private void initUI() {
        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        navigationView = (NavigationView) findViewById(R.id.nav_view);
        if (HyperTrack.isTracking()) {
            navigationView.getMenu().findItem(R.id.start_tracking_toggle).setTitle(R.string.stop_tracking);
        }
        navigationView.setNavigationItemSelectedListener(this);
        placelineFragment = (PlacelineFragment) getSupportFragmentManager().findFragmentById(R.id.placeline_fragment);
        floatingActionButton = (FloatingActionButton) findViewById(R.id.floatingActionButton);
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Placeline.this, Home.class);
                intent.putExtra("class_from", Placeline.class.getSimpleName());
                startActivity(intent);
            }
        });
        placelineFragment.setToolbarIconOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawer.openDrawer(Gravity.LEFT);
            }
        });
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        drawer.closeDrawers();
        if (item.getItemId() == R.id.edit_profile)
            startActivity(new Intent(this, Profile.class));

        else if (item.getItemId() == R.id.start_tracking_toggle) {
            if (ActionManager.getSharedManager(this).shouldRestoreState()) {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Can't do stop tracking.");
                builder.setMessage("Ongoing location sharing trip is active. Stop trip first.");
                builder.setNegativeButton("No", null);
                builder.setPositiveButton("Goto live trip",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                Intent intent = new Intent(Placeline.this, Home.class).
                                        setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

                                TaskStackBuilder.create(Placeline.this)
                                        .addNextIntentWithParentStack(intent)
                                        .startActivities();
                            }
                        });
                builder.show();
                return true;
            }
            startHyperTrackTracking();
        }
        return true;
    }

    private void startHyperTrackTracking() {
        if (!HyperTrack.isTracking()) {
            HyperTrack.resumeTracking(new HyperTrackCallback() {
                @Override
                public void onSuccess(@NonNull SuccessResponse response) {
                    navigationView.getMenu().findItem(R.id.start_tracking_toggle).setTitle(R.string.stop_tracking);
                    Toast.makeText(Placeline.this, "Tracking started successfully.", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onError(@NonNull ErrorResponse errorResponse) {
                    HyperLog.e(TAG, errorResponse.getErrorMessage());
                    Toast.makeText(Placeline.this, "Tracking started Failed." +
                            errorResponse.getErrorMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            HyperTrack.pauseTracking();
            navigationView.getMenu().findItem(R.id.start_tracking_toggle).setTitle(R.string.start_tracking);
            Toast.makeText(this, "Tracking stopped successfully.", Toast.LENGTH_SHORT).show();
        }

        navigationView.setNavigationItemSelectedListener(this);
    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(Gravity.LEFT)) {
            drawer.closeDrawers();
            return;
        }

        if (placelineFragment.onBackPressed())
            return;
        super.onBackPressed();
    }
}