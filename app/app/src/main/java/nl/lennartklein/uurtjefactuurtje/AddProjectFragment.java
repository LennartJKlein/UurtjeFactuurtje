package nl.lennartklein.uurtjefactuurtje;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.weiwangcn.betterspinner.library.BetterSpinner;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class AddProjectFragment extends DialogFragment implements View.OnClickListener {

    // Authentication
    private FirebaseAuth auth;
    private FirebaseUser currentUser;

    // Database references
    private DatabaseReference db;
    private DatabaseReference dbCompaniesMe;
    private DatabaseReference dbProjectsMe;

    // UI references
    private Context mContext;
    private EditText fieldName;
    private BetterSpinner fieldRelations;
    private EditText fieldHourPrice;
    private Button actionInsert;
    private Button actionCancel;

    // Data
    private List<Company> relations;
    private List<String> relationNames;
    private ArrayAdapter<String> relationsAdapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getContext();

        setAuth();

        // Database references
        db = FirebaseDatabase.getInstance().getReference();
        dbCompaniesMe = db.child("companies").child(currentUser.getUid());
        dbProjectsMe = db.child("projects").child(currentUser.getUid());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_project, container, false);

        // Set UI references
        fieldName = view.findViewById(R.id.field_name);
        fieldRelations = view.findViewById(R.id.field_company);
        fieldHourPrice = view.findViewById(R.id.field_hour_price);
        actionInsert = view.findViewById(R.id.action_create_project);
        actionCancel = view.findViewById(R.id.action_cancel);

        // Set click listeners
        actionInsert.setOnClickListener(this);
        actionCancel.setOnClickListener(this);

        // Get and set data
        getRelations();

        return view;
    }

    private void setAuth() {
        auth = FirebaseAuth.getInstance();
        currentUser = auth.getCurrentUser();
    }

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
            case R.id.action_create_project:
                validateFields();
                break;
            case R.id.action_cancel:
                closeFragment();
                break;
        }
    }

    public void validateFields() {

        // Reset previous errors
        fieldName.setError(null);
        fieldRelations.setError(null);
        fieldHourPrice.setError(null);
        boolean cancel = false;
        View focusView = null;

        // Store values at the time of the login attempt
        final String name = fieldName.getText().toString();
        final String companyName = fieldRelations.getText().toString();
        final String hourPrice = fieldHourPrice.getText().toString();

        // Check for a valid name, if the user entered one
        if (TextUtils.isEmpty(name)) {
            fieldName.setError(getString(R.string.error_field_required));
            focusView = fieldName;
            cancel = true;
        }

        // Check for a valid company
        if (TextUtils.isEmpty(companyName)) {
            fieldRelations.setError(getString(R.string.error_field_required));
            focusView = fieldRelations;
            cancel = true;
        }

        // Check for a valid hour price
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

    public void insertInDatabase(String name, String companyName, String hourPrice) {

        int companyPosition = relationNames.indexOf(companyName);
        Company company = relations.get(companyPosition);

        Project project = new Project();
        project.setCompanyName(companyName);
        project.setHourPrice(Double.parseDouble(hourPrice));
        project.setName(name);
        project.setUserId(currentUser.getUid());
        project.setDate(getDateToday());
        project.setCompanyId(company.getId());

        dbProjectsMe.push().setValue(project);

        closeFragment();
    }

    public String getDateToday() {
        Calendar c = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        return df.format(c.getTime());
    }

    public void closeFragment() {
        dismiss();
    }
}
