////////////////////////////////////////////////////////////////////////////////
// Title        SettingsUserFragment
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
 * Settings form for user information
 */
public class SettingsUserFragment extends Fragment implements View.OnClickListener {

    // Authentication
    private FirebaseAuth auth;
    private FirebaseUser currentUser;

    // Database references
    private DatabaseReference db;
    private DatabaseReference dbUsersMe;

    // UI references
    private Context mContext;
    private EditText fieldName;
    private EditText fieldMail;
    private EditText fieldPhone;
    private Button buttonSave;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings_user, container, false);

        setAuth();

        // Set database references
        db = PersistentDatabase.getReference();
        dbUsersMe = db.child("users").child(currentUser.getUid());

        // Set UI references
        mContext = getActivity();
        fieldName = view.findViewById(R.id.field_name);
        fieldMail = view.findViewById(R.id.field_mail);
        fieldPhone = view.findViewById(R.id.field_phone);
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
                    setTextIfEmpty(fieldName, user.getName());
                    setTextIfEmpty(fieldMail, user.getMail());
                    setTextIfEmpty(fieldPhone, user.getPhone());
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
        String name = fieldName.getText().toString();
        if (!name.equals("")) {
            dbUsersMe.child("name").setValue(name);
        }

        String mail = fieldMail.getText().toString();
        if (!mail.equals("")) {
            dbUsersMe.child("mail").setValue(mail);
        }

        String phone = fieldPhone.getText().toString();
        if (!phone.equals("")) {
            dbUsersMe.child("phone").setValue(phone);
        }

        Toast.makeText(mContext, mContext.getResources().getString(R.string.note_saved), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.action_save_continue:
                saveFields();
                getActivity().finish();
                break;
        }
    }
}
