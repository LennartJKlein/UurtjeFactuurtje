package nl.lennartklein.uurtjefactuurtje;

import android.content.res.Resources;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * View holder for a work list item
 */
public class WorkItem extends RecyclerView.ViewHolder {
    View view;

    public WorkItem(View view) {
        super(view);
        this.view = view;
    }

    public void setDate(String date) {
        TextView tvDate = view.findViewById(R.id.work_date);

        if (!date.equals("")) {
            SimpleDateFormat formatIn = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            SimpleDateFormat formatOut = new SimpleDateFormat("d MMM", Locale.getDefault());
            try {
                Date convertedDate = formatIn.parse(date);
                date = formatOut.format(convertedDate);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            tvDate.setText(date);
        } else {
            tvDate.setVisibility(View.INVISIBLE);
        }
    }

    public void setDescription(String description) {
        TextView tvDescription = view.findViewById(R.id.work_description);
        tvDescription.setText(description);
    }

    void setHours(double hours, Resources res) {
        TextView tvHours = view.findViewById(R.id.work_hours);
        if (hours > 0) {
            DecimalFormat format = new DecimalFormat("0.00");
            String convertedHours = String.valueOf(format.format(hours));
            convertedHours = res.getString(R.string.placeholder_hours, convertedHours);
            tvHours.setText(convertedHours);
        } else {
            tvHours.setVisibility(View.GONE);
        }
    }

    public void setPrice(double price) {
        TextView tvPrice = view.findViewById(R.id.work_price);
        DecimalFormat currency = new DecimalFormat("0.00");
        String convertedPrice = "€  " + currency.format(price);
        tvPrice.setText(convertedPrice);
    }

}