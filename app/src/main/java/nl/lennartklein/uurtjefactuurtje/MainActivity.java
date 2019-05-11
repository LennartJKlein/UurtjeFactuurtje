////////////////////////////////////////////////////////////////////////////////
// Title        MainActivity
//
// Date         February 1 2018
// Author       Lennart J Klein  (info@lennartklein.nl)
// Project      UurtjeFactuurtje
// Assignment   App Studio, University of Amsterdam
////////////////////////////////////////////////////////////////////////////////

package nl.lennartklein.uurtjefactuurtje;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

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
        if (activeFragment != null) {
            getSupportFragmentManager().putFragment(outState, "activeFragment", activeFragment);
            outState.putInt("activeTab", activeItem);
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle inState) {
        super.onRestoreInstanceState(inState);
        if (activeFragment != null) {
            showFragment(getSupportFragmentManager().getFragment(inState, "activeFragment"));
            activeItem = inState.getInt("activeTab");
            resumeLastFragment();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Resume at previous fragment
        resumeLastFragment();
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
        //ft.setCustomAnimations(R.anim.fade_in, R.anim.fade_out);
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
