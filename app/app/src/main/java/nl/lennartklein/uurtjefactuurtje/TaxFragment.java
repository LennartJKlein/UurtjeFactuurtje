package nl.lennartklein.uurtjefactuurtje;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.weiwangcn.betterspinner.library.BetterSpinner;

import java.util.ArrayList;
import java.util.Calendar;

/**
 * Showing tax values according to the given dates
 */
public class TaxFragment extends Fragment implements TextWatcher {

    // Authentication
    private FirebaseAuth auth;
    private FirebaseUser currentUser;

    // Database references
    private DatabaseReference db;
    private DatabaseReference dbInvoicesMe;

    // UI references
    private Context mContext;
    private BetterSpinner fieldQuarter;
    private BetterSpinner fieldYear;

    // Data
    ArrayList<String> quarters;
    ArrayList<String> years;
    int quarter;
    int year;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tax, container, false);

        setAuth();

        setReferences();

        // Set UI references
        mContext = getActivity();
        fieldQuarter = view.findViewById(R.id.field_quarter);
        fieldYear = view.findViewById(R.id.field_year);

        fillSpinners();

        fieldQuarter.addTextChangedListener(this);
        fieldYear.addTextChangedListener(this);

        return view;
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
        dbInvoicesMe = db.child("invoices").child(currentUser.getUid());
    }

    /**
     * Fills the spinners with quarters and years
     */
    private void fillSpinners() {
        quarters = new ArrayList<>();
        ArrayAdapter<String> quartersAdapter = new ArrayAdapter<>(mContext, R.layout.spinner_item, quarters);
        fieldQuarter.setAdapter(quartersAdapter);

        years = new ArrayList<>();
        ArrayAdapter<String> yearsAdapter = new ArrayAdapter<>(mContext, R.layout.spinner_item, years);
        fieldYear.setAdapter(yearsAdapter);

        // Fill spinner with quarters
        for (int i = 1; i <= 4; i++) {
            quarters.add(mContext.getResources().getString(R.string.placeholder_quarter, i));
        }
        quartersAdapter.notifyDataSetChanged();

        // Fill spinner with years
        int thisYear = Calendar.getInstance().get(Calendar.YEAR);
        for (int i = thisYear; i >= 1950; i--) {
            years.add(Integer.toString(i));
        }
        yearsAdapter.notifyDataSetChanged();
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

    @Override
    public void afterTextChanged(Editable editable) {
        String selectedQuarter = fieldQuarter.getText().toString();
        if (!selectedQuarter.equals("")) {
            int quarter = quarters.indexOf(selectedQuarter);
        }

        String selectedYear = fieldYear.getText().toString();
        if (!selectedYear.equals("")) {
            int year = years.indexOf(selectedYear);
        }

        if (!selectedQuarter.equals("") && !selectedYear.equals("")) {
            calculateBtw();
        }
    }

    private void calculateBtw() {
        dbInvoicesMe.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

}