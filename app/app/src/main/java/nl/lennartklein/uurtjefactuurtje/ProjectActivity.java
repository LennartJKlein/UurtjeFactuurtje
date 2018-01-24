package nl.lennartklein.uurtjefactuurtje;

import android.content.Context;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.Serializable;

public class ProjectActivity extends AppCompatActivity {

    // Authentication
    private FirebaseAuth auth;
    private FirebaseUser currentUser;

    // Database references
    private DatabaseReference db;
    private DatabaseReference dbProjectsMe;

    // Data
    Project project;

    // UI references
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_project);

        setAuth();

        // Set UI references
        toolbar = findViewById(R.id.toolbar);

        // Set database references
        db = PersistentDatabase.getReference();
        dbProjectsMe = db.child("projects").child(currentUser.getUid());

        // Get data
        String projectKey = getIntent().getExtras().getString("PROJECT_KEY");
        fetchProject(projectKey);

        setBackButton();

    }

    private void fetchProject(String key) {
        dbProjectsMe.child(key).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                project = dataSnapshot.getValue(Project.class);
                project.setId(dataSnapshot.getKey());

                toolbar.setTitle(project.getName());

                initiatePager();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(ProjectActivity.this,
                        getResources().getString(R.string.error_no_projects), Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void initiatePager() {
        // UI references
        ViewPager mViewPager = findViewById(R.id.container);
        TabLayout tabLayout = findViewById(R.id.tabs);

        // Create the adapter that will return a fragment for each of the sections of the activity.
        ProjectActivity.SectionsPagerAdapter mSectionsPagerAdapter =
                new ProjectActivity.SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(mViewPager));
    }

    /**
     * Returns a fragment corresponding to the tab's positions.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            Bundle bundle = new Bundle();
            bundle.putSerializable("PROJECT", project);

            switch (position) {
                case 0:
                    ProjectWorkFragment fragmentWork = new ProjectWorkFragment();
                    fragmentWork.setArguments(bundle);
                    return fragmentWork;
                case 1:
                    ProjectInvoicesFragment fragmentInvoice = new ProjectInvoicesFragment();
                    fragmentInvoice.setArguments(bundle);
                    return fragmentInvoice;
                case 2:
                    ProjectInfoFragment fragmentInfo = new ProjectInfoFragment();
                    fragmentInfo.setArguments(bundle);
                    return fragmentInfo;
            }
            return null;
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 3;
        }
    }

    private void setAuth() {
        auth = FirebaseAuth.getInstance();
        currentUser = auth.getCurrentUser();
    }

    /**
     * Set back button in toolbar
     */
    private void setBackButton() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    /**
     * Creates a options menu in the toolbar
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.project_menu, menu);
        return true;
    }

    /**
     * Finish activity when back button in toolbar is pressed
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.menu_delete:
                verifyDelete();
                break;
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public void verifyDelete() {
        //TODO: verify
        deleteProject();
    }

    public void deleteProject() {
        dbProjectsMe.child(project.getId()).setValue(null);
        finish();
    }
}
