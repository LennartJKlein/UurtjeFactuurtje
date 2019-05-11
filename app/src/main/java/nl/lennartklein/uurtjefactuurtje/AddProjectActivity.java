////////////////////////////////////////////////////////////////////////////////
// Title        AddCompanyActivity
// Parent       CompaniesFragment
//
// Date         February 1 2018
// Author       Lennart J Klein  (info@lennartklein.nl)
// Project      UurtjeFactuurtje
// Assignment   App Studio, University of Amsterdam
////////////////////////////////////////////////////////////////////////////////

package nl.lennartklein.uurtjefactuurtje;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.weiwangcn.betterspinner.library.BetterSpinner;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * A dialog with a form to add a project
 */

public class AddProjectActivity extends AppCompatActivity implements View.OnClickListener {

    // Authentication
    private FirebaseAuth auth;
    private FirebaseUser currentUser;

    // Database references
    private DatabaseReference db;
    private DatabaseReference dbCompaniesMe;
    private DatabaseReference dbProjectsMe;

    // UI references
    private Context mContext;
    private Resources res;
    private EditText fieldName;
    private BetterSpinner fieldRelations;
    private EditText fieldHourPrice;
    private ImageButton actionAddCompany;
    private Button actionInsert;
    private Button actionCancel;

    // Data
    private List<Company> relations;
    private List<String> relationNames;
    private ArrayAdapter<String> relationsAdapter;
    private Project project;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_project);

        setAuth();

        setReferences();

        // Set UI references
        mContext = this;
        res = mContext.getResources();
        fieldName = findViewById(R.id.field_name);
        fieldRelations = findViewById(R.id.field_company);
        fieldHourPrice = findViewById(R.id.field_hour_price);
        actionAddCompany = findViewById(R.id.action_create_company);
        actionInsert = findViewById(R.id.action_create_project);
        actionCancel = findViewById(R.id.action_cancel);

        // Set click listeners
        fieldRelations.setOnClickListener(this);
        actionAddCompany.setOnClickListener(this);
        actionInsert.setOnClickListener(this);
        actionCancel.setOnClickListener(this);

        getRelations();
    }

    private void setReferences() {
        db = PersistentDatabase.getReference();
        dbCompaniesMe = db.child("companies").child(currentUser.getUid());
        dbProjectsMe = db.child("projects").child(currentUser.getUid());
    }

    /**
     * Sets the FireBase authentication and current user
     */
    private void setAuth() {
        auth = FirebaseAuth.getInstance();
        currentUser = auth.getCurrentUser();
    }

    /**
     * Fetches all the relations of this user
     */
    private void getRelations() {
        relations = new ArrayList<>();
        relationNames = new ArrayList<>();
        relationsAdapter = new ArrayAdapter<>(mContext, android.R.layout.simple_dropdown_item_1line, relationNames);
        fieldRelations.setAdapter(relationsAdapter);

        dbCompaniesMe.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                for (DataSnapshot companySnapshot: dataSnapshot.getChildren()) {
                    Company company = companySnapshot.getValue(Company.class);

                    if (company != null) {
                        company.setId(companySnapshot.getKey());

                        relations.add(company);

                        if (company.getName() != null) {
                            relationNames.add(company.getName());
                        }
                    }
                }

                relationsAdapter.notifyDataSetChanged();
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
            case R.id.field_company:
                getRelations();
                break;
            case R.id.action_create_company:
                startAddCompanyActivity();
                break;
            case R.id.action_create_project:
                validateFields();
                break;
            case R.id.action_cancel:
                closeActivity();
                break;
        }
    }

    /**
     * Validates the user's input
     */
    public void validateFields() {

        // Reset previous errors
        fieldName.setError(null);
        fieldRelations.setError(null);
        fieldHourPrice.setError(null);
        boolean cancel = false;
        View focusView = null;

        // Store values
        final String name = fieldName.getText().toString();
        final String companyName = fieldRelations.getText().toString();
        final String hourPrice = fieldHourPrice.getText().toString();

        // Check for a valid fields
        if (TextUtils.isEmpty(name)) {
            fieldName.setError(getString(R.string.error_field_required));
            focusView = fieldName;
            cancel = true;
        }
        if (TextUtils.isEmpty(companyName)) {
            fieldRelations.setError(getString(R.string.error_field_required));
            focusView = fieldRelations;
            cancel = true;
        }
        if (TextUtils.isEmpty(hourPrice)) {
            fieldHourPrice.setError(getString(R.string.error_field_required));
            focusView = fieldHourPrice;
            cancel = true;
        }

        if (cancel) {
            focusView.requestFocus();
        } else {
            insertInDatabase(name, companyName, hourPrice);
        }
    }

    /**
     * Inserts a new project in the database
     */
    public void insertInDatabase(String name, String companyName, String hourPrice) {

        DatabaseReference dbProjectNew = dbProjectsMe.push();

        Company company = fetchCompany(companyName);

        // Build the project object
        project = new Project();
        project.setId(dbProjectNew.getKey());
        project.setCompanyName(companyName);
        project.setHourPrice(Double.parseDouble(hourPrice));
        project.setName(name);
        project.setUserId(currentUser.getUid());
        project.setDate(getDateToday());
        project.setCompanyId(company.getId());
        project.setStatus(1);

        dbProjectNew.setValue(project);

        openProject();

        closeActivity();
    }

    /**
     * Returns the company object for this name
     */
    public Company fetchCompany(String companyName) {
        if (!companyName.equals("")) {
            int companyPosition = relationNames.indexOf(companyName);
            return relations.get(companyPosition);
        } else {
            return null;
        }
    }

    /**
     * Returns the current date
     */
    public String getDateToday() {
        Calendar c = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        return df.format(c.getTime());
    }

    /**
     * Opens the activity of this project
     */
    public void openProject() {
        Intent projectIntent = new Intent(mContext, ProjectActivity.class);
        projectIntent.putExtra("PROJECT_KEY", project.getId());
        startActivity(projectIntent);
    }

    public void closeActivity() {
        finish();
    }

    /**
     * Opens a dialog fragment to add a company
     */
    private void startAddCompanyActivity() {
        Intent addCompanyIntent = new Intent(mContext, AddCompanyActivity.class);
        startActivity(addCompanyIntent);
    }
}
