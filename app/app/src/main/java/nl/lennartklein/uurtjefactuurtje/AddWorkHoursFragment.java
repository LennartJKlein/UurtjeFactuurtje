package nl.lennartklein.uurtjefactuurtje;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
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
import android.widget.TimePicker;

import java.text.DecimalFormat;
import java.util.Calendar;

/**
 * A form for adding man hours to a project
 */
public class AddWorkHoursFragment extends Fragment implements View.OnClickListener {

    // UI references
    private Context mContext;
    private EditText fieldTimeStart;
    private EditText fieldTimeEnd;
    private EditText fieldDate;
    private EditText fieldDescription;
    private TextView fieldPrice;
    private Button actionInsert;
    private Button actionCancel;

    // Data
    private String date;
    private int startHour;
    private int startMinute;
    private int endHour;
    private int endMinute;
    private int year;
    private int month;
    private int day;
    private double hours;
    private String description;
    private double price = 0.00;
    DecimalFormat currency = new DecimalFormat("0.00");

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getContext();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_work_hours, container, false);

        // Set UI references
        fieldTimeStart = view.findViewById(R.id.field_time_start);
        fieldTimeEnd = view.findViewById(R.id.field_time_end);
        fieldDate = view.findViewById(R.id.field_date);
        fieldDescription = view.findViewById(R.id.field_description);
        fieldPrice = view.findViewById(R.id.field_price);
        actionInsert = view.findViewById(R.id.action_create_project);
        actionCancel = view.findViewById(R.id.action_cancel);

        fieldDate.setOnClickListener(this);
        fieldTimeStart.setOnClickListener(this);
        fieldTimeEnd.setOnClickListener(this);

        initiatePickerData();

        setPriceField();

        return view;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.field_date:
                showDatePicker();
                break;
            case R.id.field_time_start:
                showStartTimePicker();
                break;
            case R.id.field_time_end:
                showEndTimePicker();
                break;
        }
    }

    private void initiatePickerData() {
        Calendar cal = Calendar.getInstance();
        year = cal.get(Calendar.YEAR);
        month = cal.get(Calendar.MONTH);
        day = cal.get(Calendar.DAY_OF_MONTH);

        startHour = cal.get(Calendar.HOUR_OF_DAY);
        startMinute = cal.get(Calendar.MINUTE);
        endHour = cal.get(Calendar.HOUR_OF_DAY);
        endMinute = cal.get(Calendar.MINUTE);
    }

    private void showStartTimePicker() {
        TimePickerDialog picker = new TimePickerDialog(
                mContext,
                new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int i, int i1) {
                        startHour = i;
                        startMinute = i1;
                        setTimeFields();
                        setPriceField();
                    }
                }, startHour, startMinute, true);
        picker.show();
    }

    private void showEndTimePicker() {
        TimePickerDialog picker = new TimePickerDialog(
                mContext,
                new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int i, int i1) {
                        endHour = i;
                        endMinute = i1;
                        setTimeFields();
                        setPriceField();
                    }
                }, endHour, endMinute, true);
        picker.show();
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
                        setPriceField();
                    }
                },
                year, month, day);
        picker.show();
    }

    private void setDateField() {
        date = year + "-" + (month + 1) + "-" + day;
        fieldDate.setText(date);
    }

    private void setTimeFields() {
        String startTime = String.format("%1$02d:%2$02d", startHour, startMinute);
        fieldTimeStart.setText(startTime);

        String endTime = String.format("%1$02d:%2$02d", endHour, endMinute);
        fieldTimeEnd.setText(endTime);

        double deltaHours = (endHour - startHour);
        double deltaMinutes = ((endMinute - startMinute) / 60.0);
        hours = deltaHours + deltaMinutes;
    }

    private void setPriceField() {
        AddWorkFragment fragment = (AddWorkFragment) getParentFragment();
        Project project = fragment.fetchProject();
        if (project != null) {
            price = hours * project.getHourPrice();
        } else {
            price = hours * 0;
        }
        fieldPrice.setText(mContext.getResources().getString(
                R.string.placeholder_currency,
                currency.format(price)));
    }

    public String getDate() {
        setDateField();
        return date;
    }

    public String getDescription() {
        description = fieldDescription.getText().toString();
        return description;
    }

    public String getHours() {
        return String.valueOf(hours);
    }

    public String getPrice() {
        return String.valueOf(price);
    }
}
