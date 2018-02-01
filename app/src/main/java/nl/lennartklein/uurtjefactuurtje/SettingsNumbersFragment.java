////////////////////////////////////////////////////////////////////////////////
// Title        SettingsNumbersFragment
// Parent       SettingsActivity
//
// Date         February 1 2018
// Author       Lennart J Klein  (info@lennartklein.nl)
// Project      UurtjeFactuurtje
// Assignment   App Studio, University of Amsterdam
////////////////////////////////////////////////////////////////////////////////

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
import com.google.firebase.database.ValueEventListener;

/**
 * Settings form for numeral data
 */
public class SettingsNumbersFragment extends Fragment implements View.OnClickListener {

    // Authentication
    private FirebaseAuth auth;
    private FirebaseUser currentUser;

    // Database references
    private DatabaseReference db;
    private DatabaseReference dbUsersMe;

    // UI references
    private Context mContext;
    private EditText fieldWebsite;
    private EditText fieldBank;
    private EditText fieldPayDue;
    private EditText fieldBtw;
    private EditText fieldKvk;
    private Button buttonSave;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings_numbers, container, false);

        setAuth();

        // Set database references
        db = PersistentDatabase.getReference();
        dbUsersMe = db.child("users").child(currentUser.getUid());

        // Set UI references
        mContext = getActivity();
        fieldWebsite = view.findViewById(R.id.field_website);
        fieldBank = view.findViewById(R.id.field_bank);
        fieldPayDue = view.findViewById(R.id.field_pay_due);
        fieldBtw = view.findViewById(R.id.field_btw);
        fieldKvk = view.findViewById(R.id.field_kvk);
        buttonSave = view.findViewById(R.id.action_save_continue);

        presetFields();

        buttonSave.setOnClickListener(this);

        return view;
    }

    /**
     * Sets the FireBase authentication and current user
     */
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
                    setTextIfEmpty(fieldWebsite, user.getWebsite());
                    setTextIfEmpty(fieldBank, user.getBank());
                    setTextIfEmpty(fieldPayDue, user.getPayDue());
                    setTextIfEmpty(fieldBtw, user.getBtw());
                    setTextIfEmpty(fieldKvk, user.getKvk());
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

    /**
     * Validate fields and save them in the database
     */
    private void saveFields() {
        String website = fieldWebsite.getText().toString();
        if (!website.equals("")) {
            dbUsersMe.child("website").setValue(website);
        }

        String bank = fieldBank.getText().toString();
        if (!bank.equals("")) {
            dbUsersMe.child("bank").setValue(bank);
        }

        String payDue = fieldPayDue.getText().toString();
        if (!payDue.equals("")) {
            dbUsersMe.child("payDue").setValue(payDue);
        }

        String btw = fieldBtw.getText().toString();
        if (!btw.equals("")) {
            dbUsersMe.child("btw").setValue(btw);
        }

        String kvk = fieldKvk.getText().toString();
        if (!kvk.equals("")) {
            dbUsersMe.child("kvk").setValue(kvk);
        }

        Toast.makeText(mContext, mContext.getResources().getString(R.string.note_saved), Toast.LENGTH_SHORT).show();
    }

    private void openNextTab() {
        ViewPager pager = getActivity().findViewById(R.id.container);
        pager.setCurrentItem(2);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.action_save_continue:
                saveFields();
                openNextTab();
                break;
        }
    }
}