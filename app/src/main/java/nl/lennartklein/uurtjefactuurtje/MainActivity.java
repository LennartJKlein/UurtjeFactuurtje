////////////////////////////////////////////////////////////////////////////////
// Title        MainActivity
//
// Date         February 1 2018
// Author       Lennart J Klein  (info@lennartklein.nl)
// Project      UurtjeFactuurtje
// Assignment   App Studio, University of Amsterdam
////////////////////////////////////////////////////////////////////////////////

package nl.lennartklein.uurtjefactuurtje;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.SyncStateContract;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataClient;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.MessageClient;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * Contains the main tabs of the app
 */

public class MainActivity extends AppCompatActivity  {

    // Authentication
    private FirebaseAuth auth;
    private FirebaseUser currentUser;

    // UI references
    private Fragment activeFragment;
    private int activeItem;
    private BottomNavigationView mainMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Register the local broadcast receiver
        IntentFilter messageFilter = new IntentFilter(Intent.ACTION_SEND);
        MessageReceiver messageReceiver = new MessageReceiver();
        LocalBroadcastManager.getInstance(this).registerReceiver(messageReceiver, messageFilter);

        setAuth();

        initiateBottomNavigation();

        // Hide actionbar
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        getSupportFragmentManager().putFragment(outState, "activeFragment", activeFragment);
        outState.putInt("activeTab", activeItem);
    }

    @Override
    protected void onRestoreInstanceState(Bundle inState) {
        super.onRestoreInstanceState(inState);
        showFragment(getSupportFragmentManager().getFragment(inState, "activeFragment"));
        activeItem = inState.getInt("activeTab");
        resumeLastFragment();
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Resume at previous fragment
        resumeLastFragment();
    }

    public class MessageReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String message = intent.getStringExtra("message");
            Log.d("DataLayer", "Main activity received message: " + message);

            switch (message) {
                case "send":
                    new SendThread(getApplicationContext(), MainActivity.this,
                            "/message", "projects send to wear").start();
                    break;
            }
        }
    }

    /**
     * Find the bottom navigation and initiate a click listener
     */
    private void initiateBottomNavigation() {
        // Set UI references
        mainMenu = findViewById(R.id.main_menu);
        mainMenu.setOnNavigationItemSelectedListener(bottomNavigationListener);

        // Activate the first fragment
        Menu menu = mainMenu.getMenu();
        activateFragment(menu.getItem(0));
    }

    /**
     * On selecting an item in the bottom navigation
     */
    private BottomNavigationView.OnNavigationItemSelectedListener bottomNavigationListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(MenuItem item) {
            activateFragment(item);
            return true;
        }
    };

    /**
     * Create the selected type of fragment
     * @param item Item that is selected.
     */
    protected void activateFragment(MenuItem item) {
        item.setChecked(true);
        activeItem = item.getItemId();

        switch (item.getItemId()) {
            case R.id.main_menu_overview:
                showFragment(new OverviewFragment());
                break;

            case R.id.main_menu_btw:
                showFragment(new TaxFragment());
                break;

            case R.id.main_menu_relations:
                showFragment(new CompaniesFragment());
                break;
        }
    }

    /**
     * Method to push any fragment into the page.
     * @param fragment An instance of Fragment to show into the page.
     */
    protected void showFragment(Fragment fragment) {
        if (fragment == null) {
            return;
        }
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.setCustomAnimations(R.anim.fade_in, R.anim.fade_out);
        ft.replace(R.id.page, fragment);
        ft.commit();

        activeFragment = fragment;
    }

    private void resumeLastFragment() {
        if (activeFragment != null) {
            mainMenu.setSelectedItemId(activeItem);
            showFragment(activeFragment);
        }
    }

    /**
     * Sets the FireBase authentication and current user
     */
    private void setAuth() {
        auth = FirebaseAuth.getInstance();
        currentUser = auth.getCurrentUser();

        if (currentUser != null) {
            Log.d("Account", currentUser.getEmail());

        } else {
            signOut();
        }
    }

    public void signOut() {
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }

}
