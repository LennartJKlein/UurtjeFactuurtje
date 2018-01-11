package nl.lennartklein.uurtjefactuurtje;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

public class MainActivity extends AppCompatActivity {

    // Global references
    private Fragment activeFragment;
    private MenuItem activeItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initiateBottomNavigation();

        // Hide action bar
        getSupportActionBar().hide();
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Resume at previous fragment
        if (activeFragment != null) {
            activeItem.setChecked(true);
            showFragment(activeFragment);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        getSupportFragmentManager().putFragment(outState, "activeFragment", activeFragment);
    }

    @Override
    protected void onRestoreInstanceState(Bundle inState) {
        super.onRestoreInstanceState(inState);
        showFragment(getSupportFragmentManager().getFragment(inState, "activeFragment"));
    }

    /**
     * Find the bottom navigation and initiate a click listener
     */
    private void initiateBottomNavigation() {
        // Set UI references
        BottomNavigationView main_menu = findViewById(R.id.main_menu);
        main_menu.setOnNavigationItemSelectedListener(bottomNavigationListener);

        // Initiate select listener
        Menu menu = main_menu.getMenu();
        menu.getItem(3).setCheckable(false);

        // Activate the first fragment
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
        switch (item.getItemId()) {
            case R.id.main_menu_overview:
                item.setChecked(true);
                activeItem = item;
                showFragment(new OverviewFragment());
                break;

            case R.id.main_menu_btw:
                item.setChecked(true);
                activeItem = item;
                showFragment(new TaxFragment());
                break;

            case R.id.main_menu_relations:
                item.setChecked(true);
                activeItem = item;
                showFragment(new RelationsFragment());
                break;

            case R.id.main_menu_settings:
                startActivity(new Intent(this, SettingsActivity.class));
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


}
