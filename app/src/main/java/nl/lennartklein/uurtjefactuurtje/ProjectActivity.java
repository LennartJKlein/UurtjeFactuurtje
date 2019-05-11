////////////////////////////////////////////////////////////////////////////////
// Title        ProjectActivity
//
// Date         February 1 2018
// Author       Lennart J Klein  (info@lennartklein.nl)
// Project      UurtjeFactuurtje
// Assignment   App Studio, University of Amsterdam
////////////////////////////////////////////////////////////////////////////////

package nl.lennartklein.uurtjefactuurtje;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
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
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

import static android.content.ContentValues.TAG;

/**
 * Containing the tabbed pages (and global functions) for a project
 */

public class ProjectActivity extends AppCompatActivity implements View.OnClickListener {

    // Authentication
    private FirebaseAuth auth;
    private FirebaseUser currentUser;

    // Database references
    private DatabaseReference db;
    private DatabaseReference dbUsersMe;
    private DatabaseReference dbWorkMe;
    private DatabaseReference dbProjectsMe;
    private DatabaseReference dbInvoicesMe;

    // Data
    private User user;
    private Project project;
    private double TAX_RATE = 0.21;

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

        checkPermissions();

        // Check if invoice index needs a reset (new year)
        new Thread(new Runnable() {
            public void run() {
                checkInvoiceIndex();
            }
        }).start();

        // Get data for this project
        String projectKey = getIntent().getExtras().getString("PROJECT_KEY");
        if (projectKey != null) {
            fetchProject(projectKey);
            fetchUser();
        } else {
            finish();
        }
    }

    /**
     * Sets the FireBase authentication and current user
     */
    private void setAuth() {
        auth = FirebaseAuth.getInstance();
        currentUser = auth.getCurrentUser();
    }

    /**
     * Sets the database references
     */
    private void setReferences() {
        db = PersistentDatabase.getReference();
        dbUsersMe = db.child("users").child(currentUser.getUid());
        dbProjectsMe = db.child("projects").child(currentUser.getUid());
        dbWorkMe = db.child("work").child(currentUser.getUid());
        dbInvoicesMe = db.child("invoices").child(currentUser.getUid());
    }

    /**
     * Gets the project from the database
     *
     * @param key: the unique FireBase key of the project
     */
    private void fetchProject(final String key) {
        dbProjectsMe.child(key).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                project = dataSnapshot.getValue(Project.class);
                project.setId(key);

                toolbar.setTitle(project.getName());

                initiatePager();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(mContext, getString(R.string.error_no_projects),
                        Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    /**
     * Gets user information from the database
     */
    private void fetchUser() {
        dbUsersMe.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                user = dataSnapshot.getValue(User.class);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(mContext, getString(R.string.error_no_data),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Initiates the tabs layout
     */
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
     * Handles the onClicks in this activity
     *
     * @param view: the clicked view
     */
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.toolbar:
                changeProjectName();
                break;
        }
    }

    /**
     * Sets back button in toolbar
     */
    private void setBackButton() {
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
        getMenuInflater().inflate(R.menu.project_menu, menu);
        return true;
    }

    /**
     * Handles clicking in the toolbar menu
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_change_name:
                changeProjectName();
                break;
            case R.id.menu_archive:
                verifyArchive();
                break;
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Shows a dialog to change the name of the project
     */
    private void changeProjectName() {
        // Set up dialog
        AlertDialog.Builder alert = new AlertDialog.Builder(mContext);
        alert.setTitle(getString(R.string.title_change_name));

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

        // Create a archive button
        alert.setPositiveButton(R.string.action_ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // Save input to the database
                String name = input.getText().toString();
                dbProjectsMe.child(project.getId()).child("name").setValue(name);

                // Update UI
                project.setName(name);
                toolbar.setTitle(name);
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

    /**
     * Shows a dialog to verify the deletion of this project
     */
    public void verifyArchive() {
        // Set up dialog
        AlertDialog.Builder alert = new AlertDialog.Builder(mContext);
        alert.setTitle(getString(R.string.note_verify_archive_project));

        // Create a archive button
        alert.setPositiveButton(R.string.action_archive, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                archiveProject();
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

    /**
     * Archives the current project from the database
     */
    public void archiveProject() {
        dbProjectsMe.child(project.getId()).child("status").setValue(0);
        finish();
    }

    /**
     * Create an invoice for this project
     */
    public void createNewInvoice() {
        checkPermissions();
        Toast.makeText(mContext, getString(R.string.generating_invoice), Toast.LENGTH_SHORT).show();

        // Create invoiceNumber string of year and index
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        String invoiceNr = currentYear + String.format(
                Locale.getDefault(),
                "%03d",
                user.getInvoiceNumber());

        // Create new invoice object
        Invoice invoice = new Invoice();
        invoice.setInvoiceNr(invoiceNr);
        invoice.setProjectId(project.getId());
        invoice.setDate(getDateToday(0));
        invoice.setEndDate(getDateToday(Integer.valueOf(user.getPayDue())));
        invoice.setUserId(currentUser.getUid());
        invoice.setCompanyId(project.getCompanyId());

        // Push new invoice to database and fetch its key
        String key = insertIntoDatabase(invoice);
        invoice.setKey(key);

        // Add the date of the invoice to the project
        project.setLastInvoice(invoice.getDate());
        dbProjectsMe.child(project.getId()).child("lastInvoice").setValue(invoice.getDate());

        // Add 1 to invoiceNumber of user
        setInvoiceIndex(user.getInvoiceNumber() + 1);

        // Calculate the totals of the invoice
        fetchData(invoice);
    }

    /**
     * Fetches all invoice information
     */
    private void fetchData(final Invoice invoice) {
        db.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot data) {
                // Get Id's for this invoice
                String userId = invoice.getUserId();
                String companyId = invoice.getCompanyId();
                String projectId = invoice.getProjectId();

                // Set user
                invoice.setUser(user);

                // Set project
                invoice.setProject(project);

                // Fetch receiver company
                Company company = data.child("companies").child(userId).child(companyId)
                        .getValue(Company.class);
                invoice.setCompany(company);
                Log.d("Data", invoice.getCompany().getName());

                // Fetch all unpaid work for this project
                DatabaseReference dbWorkThis = dbWorkMe.child(projectId);
                DataSnapshot dataWorks = data.child("work").child(userId).child(projectId).child("unpaid");
                for (DataSnapshot workSnapshot : dataWorks.getChildren()) {
                    Work work = workSnapshot.getValue(Work.class);
                    if (work != null) {

                        // Add this work to the invoice
                        invoice.addWork(work);
                        work.setInvoiceId(invoice.getKey());

                        // Set this work as 'paid'
                        dbWorkThis.child("unpaid").child(workSnapshot.getKey()).setValue(null);
                        dbWorkThis.child("paid").push().setValue(work);
                    }
                }

                calculatePriceAndBuild(invoice);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
            }
        });
    }

    private void calculatePriceAndBuild(Invoice invoice) {
        double totalPrice = 0.00;
        for (Work work : invoice.getWorks()) {
            if (work != null) {
                totalPrice += work.getPrice();
            }
        }
        double btw = Math.round((totalPrice * TAX_RATE) * 100.0) / 100.0;
        totalPrice = Math.round((totalPrice + btw) * 100.0) / 100.0;

        invoice.setBtw(btw);
        invoice.setTotalPrice(totalPrice);
        dbInvoicesMe.child(invoice.getKey()).child("btw").setValue(btw);
        dbInvoicesMe.child(invoice.getKey()).child("totalPrice").setValue(totalPrice);

        writeInvoiceDocument(invoice);
    }

    private void writeInvoiceDocument(Invoice invoice) {
        // Generate file
        GenerateInvoicePdf writer = new GenerateInvoicePdf(this, mContext, invoice);
        String writerPath = writer.createFile();
        invoice.setFilePath(writerPath);

        insertPathIntoDatabase(invoice);
    }

    /**
     * Inserts an invoice object to the database
     *
     * @param invoice: Invoice object
     * @return key of the pushed object
     */
    public String insertIntoDatabase(Invoice invoice) {
        DatabaseReference dbInvoiceNew = dbInvoicesMe.push();
        dbInvoiceNew.setValue(invoice);

        return dbInvoiceNew.getKey();
    }

    public void insertPathIntoDatabase(Invoice invoice) {
        if (invoice.getFilePath() != null) {
            dbInvoicesMe.child(invoice.getKey()).child("filePath").setValue(invoice.getFilePath());
        }
    }

    private void checkPermissions() {
        if (ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0x3);

            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0x3);
            }
        }
    }

    private void checkInvoiceIndex() {
        dbInvoicesMe.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // See if any invoice has been made this year. Otherwise, reset index to 0
                boolean resetInvoiceIndex = true;
                for (DataSnapshot invoiceSnapshot : dataSnapshot.getChildren()) {
                    Invoice invoice = invoiceSnapshot.getValue(Invoice.class);
                    if (invoice != null) {
                        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
                        if (isDateInYear(invoice.getDate(), currentYear)) {
                            resetInvoiceIndex = false;
                        }
                    }
                }

                if (resetInvoiceIndex) {
                    setInvoiceIndex(0);
                    Log.d("Account", "InvoiceNr has been reset for this book year");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
            }
        });
    }

    private void setInvoiceIndex (int index) {
        dbUsersMe.child("invoiceNumber").setValue(index);
    }

    /**
     * Returns the date of today
     *
     * @param extraDays: additional days to the future
     * @return formatted date
     */
    public String getDateToday(int extraDays) {
        Calendar c = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        c.add(Calendar.DATE, extraDays);
        return df.format(c.getTime());
    }

    private boolean isDateInYear(String dateString, int year) {
        SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        Calendar calendar = new GregorianCalendar();
        Date date = null;
        try {
            if (dateString != null) {
                date = inputFormat.parse(dateString);
                calendar.setTime(date);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }

        if (date != null) {
            if (calendar.get(Calendar.YEAR) == year) {
                return true;
            }
        }

        return false;
    }
}
