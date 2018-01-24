package nl.lennartklein.uurtjefactuurtje;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.weiwangcn.betterspinner.library.BetterSpinner;

import java.util.ArrayList;
import java.util.List;

public class AddWorkFragment extends DialogFragment implements View.OnClickListener {

    // Authentication
    private FirebaseAuth auth;
    private FirebaseUser currentUser;

    // Database references
    private DatabaseReference db;
    private DatabaseReference dbProjectsMe;
    private DatabaseReference dbWorkMe;

    // UI references
    private Context mContext;
    private TabLayout tabs;
    private TabsAdapter tabsAdapter;
    private ViewPager pageContainer;
    private BetterSpinner fieldProject;
    private Button actionInsert;
    private Button actionCancel;

    // Data
    private List<Project> projects;
    private List<String> projectNames;
    private ArrayAdapter<String> projectsAdapter;
    private Project project;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getContext();

        setAuth();

        // Database references
        db = PersistentDatabase.getReference();
        dbProjectsMe = db.child("projects").child(currentUser.getUid());
        dbWorkMe = db.child("work").child(currentUser.getUid());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_work, container, false);

        // Set UI references
        tabs = view.findViewById(R.id.tabs);
        pageContainer = view.findViewById(R.id.container);
        fieldProject = view.findViewById(R.id.field_project);
        actionInsert = view.findViewById(R.id.action_add_work);
        actionCancel = view.findViewById(R.id.action_cancel);

        // Set click listeners
        actionInsert.setOnClickListener(this);
        actionCancel.setOnClickListener(this);

        setTabs();

        // Get and set data
        getProjects();

        return view;
    }

    private void setAuth() {
        auth = FirebaseAuth.getInstance();
        currentUser = auth.getCurrentUser();
    }

    /**
     * Initiates the tabbed layout
     */
    private void setTabs() {
        // Create the adapter that will return a fragment for each of the sections of the activity.
        tabsAdapter = new TabsAdapter(getChildFragmentManager());

        // Set up the ViewPager with the sections adapter.
        pageContainer.setAdapter(tabsAdapter);

        // Set on click listeners
        pageContainer.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabs));
        tabs.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(pageContainer));
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
            switch (position) {
                case 0:
                    currentFragment = new AddWorkHoursFragment();
                    return currentFragment;
                case 1:
                    currentFragment = new AddWorkProductFragment();
                    return currentFragment;
            }
            currentFragment = new AddWorkHoursFragment();
            return currentFragment;
        }

        @Override
        public int getCount() {
            // Show 2 total pages.
            return 2;
        }

        private Fragment getCurrentFragment() {
            return currentFragment;
        }
    }

    private void getProjects() {
        projects = new ArrayList<>();
        projectNames = new ArrayList<>();
        projectsAdapter = new ArrayAdapter<>(mContext, R.layout.spinner_item, projectNames);
        fieldProject.setAdapter(projectsAdapter);

        dbProjectsMe.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                for (DataSnapshot projectSnapshot: dataSnapshot.getChildren()) {
                    Project project = projectSnapshot.getValue(Project.class);

                    if (project != null) {
                        project.setId(projectSnapshot.getKey());

                        projects.add(project);

                        if (project.getName() != null) {
                            projectNames.add(project.getName());
                        }
                    }
                }

                projectsAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d("Data", String.valueOf(databaseError));
            }
        });
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.action_add_work:
                validateFields();
                break;
            case R.id.action_cancel:
                closeFragment();
                break;
        }
    }

    public Project fetchProject() {
        String projectName = fieldProject.getText().toString();
        if (!projectName.equals("")) {
            int projectPosition = projectNames.indexOf(projectName);
            return projects.get(projectPosition);
        } else {
            return null;
        }
    }

    public void validateFields() {

        // Reset previous errors
        fieldProject.setError(null);
        boolean cancel = false;
        View focusView = null;

        // Fetch values
        String projectName = fieldProject.getText().toString();
        String price = "0";
        String hours = "0";
        String description = "";
        String date = "";

        // Fetch values from sub forms
        Fragment page = getChildFragmentManager().findFragmentByTag("android:switcher:" +
                R.id.container + ":" + pageContainer.getCurrentItem());
        switch (pageContainer.getCurrentItem()) {
            case 0:
                AddWorkHoursFragment hoursForm = (AddWorkHoursFragment) page;
                hours = hoursForm.getHours();
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
            insertInDatabase(price, hours, description, date);
        }
    }

    public void insertInDatabase(String price, String hours, String description, String date) {

        project = fetchProject();

        Work work = new Work();
        work.setDescription(description);
        work.setDate(date);
        work.setHours(Double.parseDouble(hours));
        work.setPrice(Double.parseDouble(price));
        work.setUserId(currentUser.getUid());
        work.setProjectId(project.getId());
        work.setPaid(0);

        dbWorkMe.child(project.getId()).push().setValue(work);

        Toast.makeText(mContext,
                mContext.getResources().getString(R.string.note_work_added),
                Toast.LENGTH_SHORT).show();

        openProject();

        closeFragment();
    }

    public void openProject() {
        Intent projectIntent = new Intent(mContext, ProjectActivity.class);
        projectIntent.putExtra("PROJECT_KEY", project.getId());
        startActivity(projectIntent);
    }

    public void closeFragment() {
        dismiss();
    }
}
