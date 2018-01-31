package nl.lennartklein.uurtjefactuurtje;

import android.app.DatePickerDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.weiwangcn.betterspinner.library.BetterSpinner;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class AddCostFragment extends DialogFragment implements View.OnClickListener {

    // Authentication
    private FirebaseAuth auth;
    private FirebaseUser currentUser;

    // Database references
    private DatabaseReference db;
    private DatabaseReference dbCostsMe;
    private DatabaseReference dbCompaniesMe;

    // UI references
    private Context mContext;
    private EditText fieldDescription;
    private BetterSpinner fieldRelations;
    private EditText fieldInvoiceNr;
    private TextView fieldDate;
    private EditText fieldEuros;
    private EditText fieldCents;
    private Button actionAdd;
    private Button actionCancel;

    // Data
    private List<Company> relations;
    private List<String> relationNames;
    private ArrayAdapter<String> relationsAdapter;
    private Project project;
    private String date;
    private int year;
    private int month;
    private int day;
    final private double TAX_RATE = 0.21;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getContext();

        setAuth();

        // Database references
        db = PersistentDatabase.getReference();
        dbCompaniesMe = db.child("companies").child(currentUser.getUid());
        dbCostsMe = db.child("costs").child(currentUser.getUid());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_cost, container, false);

        // Set UI references
        fieldDescription = view.findViewById(R.id.field_description);
        fieldRelations = view.findViewById(R.id.field_company);
        fieldDate = view.findViewById(R.id.field_date);
        fieldInvoiceNr = view.findViewById(R.id.field_invoice_nr);
        fieldEuros = view.findViewById(R.id.field_price_euros);
        fieldCents = view.findViewById(R.id.field_price_cents);
        actionAdd = view.findViewById(R.id.action_add);
        actionCancel = view.findViewById(R.id.action_cancel);

        // Set click listeners
        fieldDate.setOnClickListener(this);
        actionAdd.setOnClickListener(this);
        actionCancel.setOnClickListener(this);

        initiateDatePicker();

        getRelations();

        return view;
    }

    private void setAuth() {
        auth = FirebaseAuth.getInstance();
        currentUser = auth.getCurrentUser();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.field_date:
                showDatePicker();
                break;
            case R.id.action_add:
                validateFields();
                break;
            case R.id.action_cancel:
                closeFragment();
                break;
        }
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

    private void initiateDatePicker() {
        Calendar cal = Calendar.getInstance();
        year = cal.get(Calendar.YEAR);
        month = cal.get(Calendar.MONTH);
        day = cal.get(Calendar.DAY_OF_MONTH);
    }

    private void showDatePicker() {
        DatePickerDialog picker = new DatePickerDialog(
                mContext,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datePicker, int i, int i1, int i2) {
                        year = i;
                        month = i1;
                        day = i2;
                        setDateField();
                    }
                },
                year, month, day);
        picker.show();
    }

    private void setDateField() {
        date = year + "-" + (month + 1) + "-" + day;
        fieldDate.setText(date);
    }

    public void closeFragment() {
        dismiss();
    }



    public void validateFields() {

        // Reset previous errors
        fieldDescription.setError(null);
        fieldRelations.setError(null);
        fieldDate.setError(null);
        fieldInvoiceNr.setError(null);
        boolean cancel = false;
        View focusView = null;

        // Store values
        final String description = fieldDescription.getText().toString();
        final String companyName = fieldRelations.getText().toString();
        final String date = fieldDate.getText().toString();
        final String invoiceNr = fieldInvoiceNr.getText().toString();
        final String priceEuros = fieldEuros.getText().toString();
        final String priceCents = fieldCents.getText().toString();
        final String price = priceEuros + "." + priceCents;

        // Check for a valid inputs
        if (TextUtils.isEmpty(description)) {
            fieldDescription.setError(getString(R.string.error_field_required));
            focusView = fieldDescription;
            cancel = true;
        }
        if (TextUtils.isEmpty(companyName)) {
            fieldRelations.setError(getString(R.string.error_field_required));
            focusView = fieldRelations;
            cancel = true;
        }
        if (TextUtils.isEmpty(date)) {
            fieldDate.setError(getString(R.string.error_field_required));
            focusView = fieldDate;
            cancel = true;
        }
        if (TextUtils.isEmpty(invoiceNr)) {
            fieldInvoiceNr.setError(getString(R.string.error_field_required));
            focusView = fieldInvoiceNr;
            cancel = true;
        }

        if (cancel) {
            focusView.requestFocus();
        } else {
            insertInDatabase(description, companyName, date, invoiceNr, price);
        }
    }

    public void insertInDatabase(String description, String companyName, String date,
                                 String invoiceNr, String price) {

        DatabaseReference dbCostNew = dbCostsMe.push();

        Company company = fetchCompany(companyName);

        // Build the project object
        Cost cost = new Cost();
        cost.setPrice(Double.parseDouble(price));
        cost.setBtw(Math.round((cost.getPrice() * TAX_RATE) * 100.0) / 100.0);
        cost.setPrice(Double.parseDouble(price) + cost.getBtw());
        cost.setCompanyId(company.getId());
        cost.setCompanyName(company.getName());
        cost.setDate(date);
        cost.setDescription(description);
        cost.setInvoiceNr(invoiceNr);
        cost.setUserId(currentUser.getUid());

        dbCostNew.setValue(cost);

        closeFragment();
    }

    public Company fetchCompany(String companyName) {
        if (!companyName.equals("")) {
            int companyPosition = relationNames.indexOf(companyName);
            return relations.get(companyPosition);
        } else {
            return null;
        }
    }
}
