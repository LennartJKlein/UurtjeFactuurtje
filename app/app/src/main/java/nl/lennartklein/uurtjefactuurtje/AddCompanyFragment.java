package nl.lennartklein.uurtjefactuurtje;


import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


public class AddCompanyFragment extends DialogFragment implements View.OnClickListener {

    // Global references
    private Context mContext;
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
        mContext = getContext();

        setAuth();

        // Database references
        db = PersistentDatabase.getReference();
        dbCompanies = db.child("companies");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_add_company, container, false);

        // UI references
        fieldName = v.findViewById(R.id.field_name);
        fieldContact = v.findViewById(R.id.field_contact);
        fieldMail = v.findViewById(R.id.field_mail);
        fieldPostal = v.findViewById(R.id.field_postal);
        fieldStreet = v.findViewById(R.id.field_street);
        fieldStreetNr = v.findViewById(R.id.field_street_nr);
        fieldCity = v.findViewById(R.id.field_city);
        actionInsert = v.findViewById(R.id.action_create_company);
        actionCancel = v.findViewById(R.id.action_cancel);

        // Click listeners
        actionInsert.setOnClickListener(this);
        actionCancel.setOnClickListener(this);

        return v;
    }

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
                closeFragment();
                break;
        }
    }

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

        closeFragment();
    }

    public void closeFragment() {
        dismiss();
    }

}
