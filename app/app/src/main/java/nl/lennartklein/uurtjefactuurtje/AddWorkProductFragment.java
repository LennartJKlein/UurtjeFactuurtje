package nl.lennartklein.uurtjefactuurtje;

import android.app.DatePickerDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;

import java.util.Calendar;

/**
 * A form for adding sold products to a project
 */
public class AddWorkProductFragment extends Fragment implements View.OnClickListener {

    // UI references
    private Context mContext;
    private TextView fieldDate;
    private EditText fieldDescription;
    private EditText fieldPrice;
    private EditText fieldCents;
    private Button actionInsert;
    private Button actionCancel;

    // Data
    private String date;
    private int year;
    private int month;
    private int day;
    private String description;
    private String price;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getContext();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_work_product, container, false);

        // Set UI references
        fieldDate = view.findViewById(R.id.field_date);
        fieldDescription = view.findViewById(R.id.field_description);
        fieldPrice = view.findViewById(R.id.field_price);
        fieldCents = view.findViewById(R.id.field_price_cents);
        actionInsert = view.findViewById(R.id.action_create_project);
        actionCancel = view.findViewById(R.id.action_cancel);

        fieldDate.setOnClickListener(this);

        initiateDatePicker();

        return view;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.field_date:
                showDatePicker();
                break;
        }
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

    public String getDate() {
        setDateField();
        return date;
    }

    public String getDescription() {
        description = fieldDescription.getText().toString();
        return description;
    }

    public String getPrice() {
        String euro = fieldPrice.getText().toString();
        String cents = fieldCents.getText().toString();
        price = euro + "." + cents;
        return price;
    }
}
