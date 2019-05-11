////////////////////////////////////////////////////////////////////////////////
// Title        EditWorkHoursFragment
// Parent       EditWorkActivity
//
// Date         February 1 2018
// Author       Lennart J Klein  (info@lennartklein.nl)
// Project      UurtjeFactuurtje
// Assignment   App Studio, University of Amsterdam
////////////////////////////////////////////////////////////////////////////////

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
import java.util.Locale;

/**
 * A form for adding man hours to the work form
 */
public class EditWorkHoursFragment extends Fragment implements View.OnClickListener {

    // UI references
    private Context mContext;
    private EditText fieldTimeStart;
    private EditText fieldTimeEnd;
    private EditText fieldDate;
    private EditText fieldDescription;
    private TextView fieldPrice;
    private Button actionInsert;
    private Button actionCancel;

    // Parent data
    private Project project;
    private Work work;

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
    private String startTime;
    private String endTime;
    private String description;
    private double price = 0.00;
    private DecimalFormat currency = new DecimalFormat("0.00");

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getContext();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_edit_work_hours, container, false);

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
        fieldDate.setOnClickListener(this);
        fieldPrice.setOnClickListener(this);
        fieldDescription.setOnClickListener(this);

        EditWorkActivity parent = (EditWorkActivity) getActivity();
        if (parent != null) {
            project = parent.fetchProject();
            work = parent.fetchWork();
        }

        setDateAndTimes(work.getDate(), work.getHours(), work.getStartTime(), work.getEndTime());
        initiatePriceField(work.getPrice());
        setDescription(work.getDescription());

        return view;
    }

    @Override
    public void onClick(View view) {
        setPriceField();

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

    private void setDescription(String description) {
        fieldDescription.setText(description);
    }

    private void setDateAndTimes(String date, double hours, String startTime, String endTime) {
        Calendar cal = Calendar.getInstance();

        if (date == null) {
            year = cal.get(Calendar.YEAR);
            month = cal.get(Calendar.MONTH);
            day = cal.get(Calendar.DAY_OF_MONTH);
        } else {
            String dateSplit[] = date.split("-");
            year = Integer.parseInt(dateSplit[0]);
            month = Integer.parseInt(dateSplit[1]) - 1;
            day = Integer.parseInt(dateSplit[2]);
        }

        setDateField();

        if (startTime != null && endTime != null) {
            String startTimeSplit[] = startTime.split(":");
            startHour = Integer.parseInt(startTimeSplit[0]);
            startMinute = Integer.parseInt(startTimeSplit[1]);

            String endTimeSplit[] = endTime.split(":");
            endHour = Integer.parseInt(endTimeSplit[0]);
            endMinute = Integer.parseInt(endTimeSplit[1]);

        } else if (startTime == null && hours != 0) {
            startHour = cal.get(Calendar.HOUR_OF_DAY);
            startMinute = cal.get(Calendar.MINUTE);

            Double hoursDouble = Double.valueOf(hours);
            cal.add(Calendar.HOUR, hoursDouble.intValue());
            int minutes = (int) ((hoursDouble - hoursDouble.intValue()) * 60);
            cal.add(Calendar.MINUTE, minutes);

            endHour = cal.get(Calendar.HOUR_OF_DAY);
            endMinute = cal.get(Calendar.MINUTE);

        } else {
            startHour = cal.get(Calendar.HOUR_OF_DAY);
            startMinute = cal.get(Calendar.MINUTE);
            endHour = cal.get(Calendar.HOUR_OF_DAY);
            endMinute = cal.get(Calendar.MINUTE);
        }

        setTimeFields();

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
        startTime = String.format(Locale.getDefault(),"%1$02d:%2$02d", startHour, startMinute);
        fieldTimeStart.setText(startTime);

        endTime = String.format(Locale.getDefault(),"%1$02d:%2$02d", endHour, endMinute);
        fieldTimeEnd.setText(endTime);

        double deltaHours = (endHour - startHour);
        double deltaMinutes = ((endMinute - startMinute) / 60.0);
        hours = deltaHours + deltaMinutes;
    }

    private void initiatePriceField(double fetchedPrice) {
        price = fetchedPrice;
        setPriceField();
    }

    private void setPriceField() {
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

    public String getStartTime() {
        return startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public String getPrice() {
        return String.valueOf(price);
    }
}
