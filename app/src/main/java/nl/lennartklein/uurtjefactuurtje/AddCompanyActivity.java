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
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;

/**
 * A dialog with a form to add a company (relation)
 */

public class AddCompanyActivity extends AppCompatActivity implements View.OnClickListener {

    // Global references
    private Context mContext;
    private Resources res;
    private EditText fieldName;
    private EditText fieldContact;
    private EditText fieldMail;
    private EditText fieldPostal;
    private EditText fieldStreet;
    private EditText fieldStreetNr;
    private EditText fieldCity;
    private Button actionInsert;
    private Button actionCancel;

    // Database references
    private DatabaseReference db;
    private DatabaseReference dbCompanies;
    private FirebaseAuth auth;
    private FirebaseUser currentUser;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_company);

        setAuth();

        setReferences();

        // UI references
        mContext = this;
        res = mContext.getResources();
        fieldName = findViewById(R.id.field_name);
        fieldContact = findViewById(R.id.field_contact);
        fieldMail = findViewById(R.id.field_mail);
        fieldPostal = findViewById(R.id.field_postal);
        fieldStreet = findViewById(R.id.field_street);
        fieldStreetNr = findViewById(R.id.field_street_nr);
        fieldCity = findViewById(R.id.field_city);
        actionInsert = findViewById(R.id.action_create_company);
        actionCancel = findViewById(R.id.action_cancel);

        // Click listeners
        actionInsert.setOnClickListener(this);
        actionCancel.setOnClickListener(this);
    }

    private void setReferences() {
        db = PersistentDatabase.getReference();
        dbCompanies = db.child("companies");
    }

    /**
     * Sets the FireBase authentication and current user
     */
    private void setAuth() {
        auth = FirebaseAuth.getInstance();
        currentUser = auth.getCurrentUser();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.action_create_company:
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
        fieldContact.setError(null);
        fieldMail.setError(null);
        fieldPostal.setError(null);
        fieldStreet.setError(null);
        fieldStreetNr.setError(null);
        fieldCity.setError(null);

        // Store values at the time of the login attempt
        final String name = fieldName.getText().toString();
        final String contact = fieldContact.getText().toString();
        final String mail = fieldMail.getText().toString();
        final String postal = fieldPostal.getText().toString();
        final String street = fieldStreet.getText().toString();
        final String streetNr = fieldStreetNr.getText().toString();
        final String city = fieldCity.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check fields
        if (TextUtils.isEmpty(name)) {
            fieldName.setError(getString(R.string.error_field_required));
            focusView = fieldName;
            cancel = true;
        }
        if (TextUtils.isEmpty(contact)) {
            fieldContact.setError(getString(R.string.error_field_required));
            focusView = fieldContact;
            cancel = true;
        }
        if (TextUtils.isEmpty(mail)) {
            fieldMail.setError(getString(R.string.error_field_required));
            focusView = fieldMail;
            cancel = true;
        }
        if (TextUtils.isEmpty(postal)) {
            fieldPostal.setError(getString(R.string.error_field_required));
            focusView = fieldPostal;
            cancel = true;
        }
        if (TextUtils.isEmpty(street)) {
            fieldStreet.setError(getString(R.string.error_field_required));
            focusView = fieldStreet;
            cancel = true;
        }
        if (TextUtils.isEmpty(streetNr)) {
            fieldStreetNr.setError(getString(R.string.error_field_required));
            focusView = fieldStreetNr;
            cancel = true;
        }
        if (TextUtils.isEmpty(city)) {
            fieldCity.setError(getString(R.string.error_field_required));
            focusView = fieldCity;
            cancel = true;
        }

        if (cancel) {
            focusView.requestFocus();
        } else {
            insertInDatabase(city, contact, mail, name, postal, street, streetNr);
        }
    }

    /**
     * Inserts a new company in the database
     */
    public void insertInDatabase(String city, String contact, String mail, String name,
                                 String postal, String street, String streetNr) {

        Company company = new Company();
        company.setUserId(currentUser.getUid());
        company.setCity(city);
        company.setContact(contact);
        company.setMail(mail);
        company.setName(name);
        company.setPostal(postal);
        company.setStreet(street);
        company.setStreetNr(streetNr);

        dbCompanies.child(currentUser.getUid()).push().setValue(company);

        closeActivity();
    }

    public void closeActivity() {
        finish();
    }

}
