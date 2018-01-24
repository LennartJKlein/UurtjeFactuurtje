package nl.lennartklein.uurtjefactuurtje;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

/**
 * Settings form for company information
 */
public class SettingsCompanyFragment extends Fragment implements View.OnClickListener {

    // Authentication
    private FirebaseAuth auth;
    private FirebaseUser currentUser;

    // Database references
    private DatabaseReference db;
    private DatabaseReference dbUsersMe;

    // UI references
    private Context mContext;
    private EditText fieldName;
    private EditText fieldPostal;
    private EditText fieldStreetNr;
    private EditText fieldStreet;
    private EditText fieldCity;
    private Button buttonSave;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings_company, container, false);

        setAuth();

        // Set database references
        db = FirebaseDatabase.getInstance().getReference();
        dbUsersMe = db.child("users").child(currentUser.getUid());

        // Set UI references
        mContext = getActivity();
        fieldName = view.findViewById(R.id.field_name);
        fieldPostal = view.findViewById(R.id.field_postal);
        fieldStreetNr = view.findViewById(R.id.field_street_nr);
        fieldStreet = view.findViewById(R.id.field_street);
        fieldCity = view.findViewById(R.id.field_city);
        buttonSave = view.findViewById(R.id.action_save_continue);

        presetFields();

        buttonSave.setOnClickListener(this);

        return view;
    }

    private void setAuth() {
        auth = FirebaseAuth.getInstance();
        currentUser = auth.getCurrentUser();
    }

    private void presetFields() {
        dbUsersMe.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                if (user != null) {
                    setTextIfEmpty(fieldName, user.getCompanyName());
                    setTextIfEmpty(fieldPostal, user.getPostal());
                    setTextIfEmpty(fieldStreetNr, user.getStreetNr());
                    setTextIfEmpty(fieldStreet, user.getStreet());
                    setTextIfEmpty(fieldCity, user.getCity());
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d("Data", mContext.getResources().getString(R.string.error_no_data));
            }
        });
    }

    private void setTextIfEmpty(EditText view, String value) {
        if (view.getText().toString().equals("")) {
            if (value != null && !value.equals("")) {
                view.setText(value);
            }
        }
    }

    private void saveFields() {
        String companyName = fieldName.getText().toString();
        if (!companyName.equals("")) {
            dbUsersMe.child("companyName").setValue(companyName);
        }

        String postal = fieldPostal.getText().toString();
        if (!postal.equals("")) {
            dbUsersMe.child("postal").setValue(postal);
        }

        String streetNr = fieldStreetNr.getText().toString();
        if (!streetNr.equals("")) {
            dbUsersMe.child("streetNr").setValue(streetNr);
        }

        String street = fieldStreet.getText().toString();
        if (!street.equals("")) {
            dbUsersMe.child("street").setValue(street);
        }

        String city = fieldCity.getText().toString();
        if (!city.equals("")) {
            dbUsersMe.child("city").setValue(city);
        }

        Toast.makeText(mContext, mContext.getResources().getString(R.string.note_saved), Toast.LENGTH_SHORT).show();
    }

    private void openNextTab() {
        ViewPager pager = getActivity().findViewById(R.id.container);
        pager.setCurrentItem(1);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.action_save_continue:
                saveFields();
                openNextTab();
                break;
        }
    }
}
