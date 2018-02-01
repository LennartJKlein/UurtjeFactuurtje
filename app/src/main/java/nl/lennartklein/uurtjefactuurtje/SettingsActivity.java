////////////////////////////////////////////////////////////////////////////////
// Title        SettingsActivity
//
// Date         February 1 2018
// Author       Lennart J Klein  (info@lennartklein.nl)
// Project      UurtjeFactuurtje
// Assignment   App Studio, University of Amsterdam
////////////////////////////////////////////////////////////////////////////////

package nl.lennartklein.uurtjefactuurtje;

import android.content.Intent;
import android.support.design.widget.TabLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

/**
 * Contains tabs with settings for the user (and it's company)
 */

public class SettingsActivity extends AppCompatActivity {

    // Authentication
    private FirebaseAuth auth;
    private FirebaseUser current_user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        setAuth();

        setToolbar();

        setTabs();

    }

    /**
     * Sets the FireBase authentication and current user
     */
    private void setAuth() {
        auth = FirebaseAuth.getInstance();
        current_user = auth.getCurrentUser();
    }

    /**
     * Sets the toolbar for displaying a back button
     */
    private void setToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    /**
     * Creates a options menu in the toolbar
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.settings_menu, menu);
        return true;
    }

    /**
     * Finishes an activity when back button is pressed
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.menu_logout:
                signOut();
                break;
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void signOut() {
        auth.signOut();
        finish();
        startActivity(new Intent(this, LoginActivity.class));
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }

    /**
     * Initiates the tabbed layout
     */
    private void setTabs() {
        // Create the adapter that will return a fragment for each of the sections of the activity.
        TabsAdapter adapter = new TabsAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        ViewPager pageContainer = findViewById(R.id.container);
        pageContainer.setAdapter(adapter);

        TabLayout tabs = findViewById(R.id.tabs);

        pageContainer.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabs));
        tabs.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(pageContainer));
    }

    /**
     * Returns a fragment corresponding to the tab's positions.
     */
    public class TabsAdapter extends FragmentPagerAdapter {

        TabsAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return new SettingsCompanyFragment();
                case 1:
                    return new SettingsNumbersFragment();
                case 2:
                    return new SettingsUserFragment();
            }
            return new SettingsCompanyFragment();
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 3;
        }
    }
}
