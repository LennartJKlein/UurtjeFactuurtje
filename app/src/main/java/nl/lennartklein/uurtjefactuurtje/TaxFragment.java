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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.weiwangcn.betterspinner.library.BetterSpinner;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

import static android.content.ContentValues.TAG;

/**
 * Showing tax values according to the given dates
 */
public class TaxFragment extends Fragment {

    // Authentication
    private FirebaseAuth auth;
    private FirebaseUser currentUser;

    // Database references
    private DatabaseReference db;
    private DatabaseReference dbInvoicesMe;
    private DatabaseReference dbCostsMe;

    // UI references
    private Context mContext;
    private LinearLayout resultContainer;
    private TextView formTip;
    private BetterSpinner fieldQuarter;
    private BetterSpinner fieldYear;
    private TextView fieldPayPrice;
    private TextView fieldPayTax;
    private TextView fieldReceiveTax;

    // Data
    ArrayList<String> quarters;
    ArrayList<String> years;
    int quarter = 0;
    int year = 0;
    double services;
    double pay;
    double receive;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tax, container, false);

        setAuth();

        setReferences();

        // Set UI references
        mContext = getActivity();
        formTip = view.findViewById(R.id.form_tip);
        resultContainer = view.findViewById(R.id.result);
        fieldQuarter = view.findViewById(R.id.field_quarter);
        fieldYear = view.findViewById(R.id.field_year);
        fieldPayPrice = view.findViewById(R.id.field_pay_price);
        fieldPayTax = view.findViewById(R.id.field_pay_tax);
        fieldReceiveTax = view.findViewById(R.id.field_receive_tax);

        fillSpinners();

        taxCalculated(false);

        // Add listeners
        fieldQuarter.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                quarter = position + 1;
                calculateBtw();
            }
        });

        fieldYear.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                year = Integer.valueOf(years.get(position));
                calculateBtw();
            }
        });

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
        dbCostsMe = db.child("costs").child(currentUser.getUid());
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
            quarters.add(mContext.getResources().getString(
                    R.string.placeholder_quarter,
                    String.valueOf(i)));
        }
        quartersAdapter.notifyDataSetChanged();

        // Fill spinner with years
        int thisYear = Calendar.getInstance().get(Calendar.YEAR);
        for (int i = thisYear; i >= 1950; i--) {
            years.add(Integer.toString(i));
        }
        yearsAdapter.notifyDataSetChanged();
    }

    /**
     * Switches the visibility of the result view
     */
    private void taxCalculated(boolean calculated) {
        if (calculated) {
            formTip.setVisibility(View.GONE);
            resultContainer.setVisibility(View.VISIBLE);
        } else {
            formTip.setVisibility(View.VISIBLE);
            resultContainer.setVisibility(View.GONE);
        }
    }

    private void calculateBtw() {
        services = 0;
        pay = 0;
        receive = 0;

        if (quarter > 0 && year > 0) {

            dbInvoicesMe.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for (DataSnapshot invoiceSnapshot : dataSnapshot.getChildren()) {
                        Invoice invoice = invoiceSnapshot.getValue(Invoice.class);

                        if (invoice != null) {
                            if (dateInRange(invoice.getDate(), quarter, year)) {
                                pay += invoice.getBtw();
                                services += (invoice.getTotalPrice() - invoice.getBtw());
                            }
                        }
                    }

                    setTaxFields();
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
                }
            });

            dbCostsMe.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for (DataSnapshot costSnapshot : dataSnapshot.getChildren()) {
                        Cost cost = costSnapshot.getValue(Cost.class);

                        if (cost != null) {
                            if (dateInRange(cost.getDate(), quarter, year)) {
                                receive += cost.getBtw();
                            }
                        }
                    }

                    setTaxFields();
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
                }
            });

            taxCalculated(true);
        }
    }

    private boolean dateInRange(String dateString, int quarter, int year) {
        SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        Calendar calendar = new GregorianCalendar();
        Date date = null;
        try {
            date = inputFormat.parse(dateString);
            calendar.setTime(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        if (date != null) {
            if (calendar.get(Calendar.YEAR) == year) {
                if (monthInQuarter(calendar.get(Calendar.MONTH), quarter)) {
                    return true;
                }
            }
        }

        return false;
    }

    private boolean monthInQuarter(int month, int quarter) {
        int quarterOfMonth = (month / 3) + 1;
        if (quarterOfMonth == quarter) {
            return true;
        } else {
            return false;
        }
    }

    private void setTaxFields() {
        String valueServices = getResources().getString(
                R.string.placeholder_currency,
                String.valueOf(Math.round(services)));
        fieldPayPrice.setText(valueServices);

        String valuePay = getResources().getString(
                R.string.placeholder_currency,
                String.valueOf(Math.round(pay)));
        fieldPayTax.setText(String.valueOf(valuePay));

        String valueReceive = getResources().getString(
                R.string.placeholder_currency,
                String.valueOf(Math.round(receive)));
        fieldReceiveTax.setText(String.valueOf(valueReceive));
    }

}