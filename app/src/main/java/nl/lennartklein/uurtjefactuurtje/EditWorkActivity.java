////////////////////////////////////////////////////////////////////////////////
// Title        AddWorkActivity
// Parent       OverviewFragment / ProjectWorkFragment
//
// Date         February 1 2018
// Author       Lennart J Klein  (info@lennartklein.nl)
// Project      UurtjeFactuurtje
// Assignment   App Studio, University of Amsterdam
////////////////////////////////////////////////////////////////////////////////

package nl.lennartklein.uurtjefactuurtje;

import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabItem;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;

/**
 * A form for editing work of a project
 */

public class EditWorkActivity extends AppCompatActivity implements View.OnClickListener {

    // Authentication
    private FirebaseAuth auth;
    private FirebaseUser currentUser;

    // Database references
    private DatabaseReference db;
    private DatabaseReference dbProjectsMe;
    private DatabaseReference dbWorkMe;

    // UI references
    private Context mContext;
    private Resources res;
    private TabLayout tabs;
    private TabsAdapter tabsAdapter;
    private TabItem tabHours;
    private TabItem tabProduct;
    private ViewPager pageContainer;
    private TextView fieldProject;
    private Button actionSave;
    private Button actionCancel;
    private ImageButton actionDelete;

    // Data
    private Work work;
    private Project project;
    private boolean hoursSet;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_work);

        setAuth();

        setReferences();

        // Set UI references
        mContext = this;
        res = mContext.getResources();
        tabs = findViewById(R.id.tabs);
        tabHours = findViewById(R.id.tab_work_hours);
        tabProduct = findViewById(R.id.tab_work_product);
        pageContainer = findViewById(R.id.container);
        fieldProject = findViewById(R.id.field_project);
        actionSave = findViewById(R.id.action_save);
        actionCancel = findViewById(R.id.action_cancel);
        actionDelete = findViewById(R.id.action_delete);

        // Set click listeners
        actionSave.setOnClickListener(this);
        actionCancel.setOnClickListener(this);
        actionDelete.setOnClickListener(this);

        // Get information from intent
        if (getIntent().getExtras() != null) {
            Bundle bundle = getIntent().getExtras();
            if (bundle != null) {
                project = (Project) bundle.getSerializable("PROJECT");
                work = (Work) bundle.getSerializable("WORK");
            }
        }

        if (project != null) {
            setProjectName(project.getName());
        }

        if (work != null) {
            // Get boolean if hours are set in work
            hoursSet = !(work.getHours() == 0);
        }

        setTabs(hoursSet);
    }

    /**
     * Sets the FireBase authentication and current user
     */
    private void setAuth() {
        auth = FirebaseAuth.getInstance();
        currentUser = auth.getCurrentUser();
    }

    private void setReferences() {
        db = PersistentDatabase.getReference();
        dbProjectsMe = db.child("projects").child(currentUser.getUid());
        dbWorkMe = db.child("work").child(currentUser.getUid());
    }

    /**
     * Initiates the tabbed layout
     */
    private void setTabs(boolean hoursSet) {
        // Create the adapter that will return a fragment for each of the sections of the activity.
        tabsAdapter = new TabsAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        if (pageContainer != null) {
            pageContainer.setAdapter(tabsAdapter);

            // Hide tabs
            tabs.setVisibility(View.GONE);
        }
    }

    /**
     * Returns a fragment corresponding to the tab's positions.
     */
    public class TabsAdapter extends FragmentPagerAdapter {

        private Fragment currentFragment;

        TabsAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            if (hoursSet) {
                currentFragment = new EditWorkHoursFragment();
            } else {
                currentFragment = new EditWorkProductFragment();
            }
            return currentFragment;
        }

        @Override
        public int getCount() {
            // Show 1 total pages.
            return 1;
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.action_add_work:
                validateFields();
                break;
            case R.id.action_cancel:
                closeActivity();
                break;
            case R.id.action_delete:
                deleteWork(work);
                break;
        }
    }

    private void setProjectName(String projectName) {
        if (fieldProject != null && !projectName.equals("")) {
            fieldProject.setText(projectName);
        }
    }

    /**
     * Gets object of selected project
     */
    public Project fetchProject() {
        if (project != null) {
            return project;
        } else {
            return null;
        }
    }

    /**
     * Gets object of selected work
     */
    public Work fetchWork() {
        if (work != null) {
            return work;
        } else {
            return null;
        }
    }

    /**
     * Validates the user's input
     */
    public void validateFields() {

        // Reset previous errors
        fieldProject.setError(null);
        boolean cancel = false;
        View focusView = null;

        // Fetch values
        String projectName = fieldProject.getText().toString();
        String price = "0";
        String hours = "0";
        String startTime = "";
        String endTime = "";
        String description = "";
        String date = "";

        // Fetch values from sub forms
        Fragment page = getSupportFragmentManager().findFragmentByTag("android:switcher:" +
                R.id.container + ":" + pageContainer.getCurrentItem());
        switch (pageContainer.getCurrentItem()) {
            case 0:
                AddWorkHoursFragment hoursForm = (AddWorkHoursFragment) page;
                hours = hoursForm.getHours();
                startTime = hoursForm.getStartTime();
                endTime = hoursForm.getEndTime();
                date = hoursForm.getDate();
                description = hoursForm.getDescription();
                price = hoursForm.getPrice();
                break;
            case 1:
                AddWorkProductFragment productForm = (AddWorkProductFragment) page;
                price = productForm.getPrice();
                date = productForm.getDate();
                description = productForm.getDescription();
                break;
        }

        if (TextUtils.isEmpty(projectName)) {
            fieldProject.setError(getString(R.string.error_field_required));
            focusView = fieldProject;
            cancel = true;
        }

        if (TextUtils.isEmpty(projectName)) {
            fieldProject.setError(getString(R.string.error_field_required));
            focusView = fieldProject;
            cancel = true;
        }

        if (cancel) {
            focusView.requestFocus();
        } else {
            updateToDatabase(price, hours, description, date, startTime, endTime);
        }
    }

    /**
     * Inserts new work in the database
     */
    public void updateToDatabase(String price, String hours, String description, String date,
                                 String startTime, String endTime) {

        Work work = new Work();
        work.setDescription(description);
        work.setDate(date);
        work.setHours(Double.parseDouble(hours));
        work.setPrice(Double.parseDouble(price));
        work.setUserId(currentUser.getUid());
        work.setProjectId(project.getId());

        dbWorkMe.child(project.getId()).child("unpaid").push().setValue(work);

        Toast.makeText(mContext,
                mContext.getResources().getString(R.string.note_work_added),
                Toast.LENGTH_SHORT).show();

        closeActivity();
    }

    public void deleteWork(Work work) {
        Log.d("Action", "delete");
        // Todo
    }

    public void closeActivity() {
        Log.d("Action", "close");
        finish();
    }
}
