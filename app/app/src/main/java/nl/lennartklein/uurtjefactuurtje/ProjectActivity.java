package nl.lennartklein.uurtjefactuurtje;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Objects;

public class ProjectActivity extends AppCompatActivity implements View.OnClickListener {

    // Authentication
    private FirebaseAuth auth;
    private FirebaseUser currentUser;

    // Database references
    private DatabaseReference db;
    private DatabaseReference dbUsersMe;
    private DatabaseReference dbProjectsMe;
    private DatabaseReference dbInvoicesMe;
    private DatabaseReference dbCompaniesMe;

    // Data
    private User user;
    private Project project;

    // UI references
    private Context mContext;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_project);

        setAuth();

        setReferences();

        // Set UI
        mContext = this;
        toolbar = findViewById(R.id.toolbar);
        toolbar.setOnClickListener(this);
        setBackButton();

        // Get data for this project
        String projectKey = getIntent().getExtras().getString("PROJECT_KEY");
        if (projectKey != null) {
            fetchProject(projectKey);
            fetchUser();
        } else {
            finish();
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.action_create_invoice:
                changeProjectName();
                break;
        }
    }

    private void setAuth() {
        auth = FirebaseAuth.getInstance();
        currentUser = auth.getCurrentUser();
    }

    private void setReferences() {
        db = PersistentDatabase.getReference();
        dbUsersMe = db.child("users").child(currentUser.getUid());
        dbProjectsMe = db.child("projects").child(currentUser.getUid());
        dbInvoicesMe = db.child("invoices").child(currentUser.getUid());
        dbCompaniesMe = db.child("companies").child(currentUser.getUid());
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
                Toast.makeText(mContext, getResources().getString(R.string.error_no_projects),
                        Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void fetchUser() {
        dbUsersMe.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                user = dataSnapshot.getValue(User.class);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(mContext, getResources().getString(R.string.error_no_data),
                        Toast.LENGTH_SHORT).show();
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

    /**
     * Create an invoice for this project
     */
    public void createNewInvoice() {
        Toast.makeText(mContext, getString(R.string.generating_invoice), Toast.LENGTH_SHORT).show();

        // Add 1 to invoiceNumber of user
        long newInvoiceNumber = Long.valueOf(user.getInvoiceNumber()) + 1;
        dbUsersMe.child("invoiceNumber").setValue(String.valueOf(newInvoiceNumber));

        // Create new invoice object
        Invoice invoice = new Invoice();
        invoice.setDate(getDateToday(0));
        invoice.setEndDate(getDateToday(Integer.valueOf(user.getPayDue())));
        invoice.setInvoice_number(user.getInvoiceNumber() );
        invoice.setUserId(currentUser.getUid());
        invoice.setCompanyId(project.getCompanyId());
        invoice.setProjectId(project.getId());
        invoice.calculatePrice();

        // Initiate new file generator
        GeneratePdf writer = new GeneratePdf(mContext, invoice);
        writer.createFile();

        // Add invoice to database
        insertIntoDatabase(invoice);
    }

    public String getDateToday(int extraDays) {
        Calendar c = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        c.add(Calendar.DATE, extraDays);
        return df.format(c.getTime());
    }

    public void insertIntoDatabase(Invoice invoice) {
        dbInvoicesMe.push().setValue(invoice);
    }

    private void changeProjectName() {
        // Set up dialog
        AlertDialog.Builder alert = new AlertDialog.Builder(mContext);
        alert.setTitle(R.string.title_change_name);

        // Create input field
        final EditText input = new EditText(mContext);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setSingleLine();
        LinearLayout linearLayout = new LinearLayout(mContext);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        layoutParams.gravity = Gravity.CENTER;
        input.setLayoutParams(layoutParams);

        // Set input field
        input.setText(project.getName(), TextView.BufferType.EDITABLE);
        linearLayout.addView(input);
        linearLayout.setPadding(36, 0, 36, 0);
        alert.setView(linearLayout);

        // Create a delete button
        alert.setPositiveButton(R.string.action_ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // Save input to the database
                String name = input.getText().toString();
                dbProjectsMe.child(project.getId()).child("name").setValue(name);
            }
        });

        // Create a cancel button
        alert.setNegativeButton(R.string.action_cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });
        alert.show();
    }
}
