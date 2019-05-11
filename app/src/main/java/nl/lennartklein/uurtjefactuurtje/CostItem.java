package nl.lennartklein.uurtjefactuurtje;


import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * View holder for a cost list item
 */
public class CostItem extends RecyclerView.ViewHolder {
    View view;

    public CostItem(View view) {
        super(view);
        this.view = view;
    }

    public void setDate(String date) {
        TextView tvDate = view.findViewById(R.id.cost_date);

        if (!date.equals("")) {
            SimpleDateFormat formatIn = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            SimpleDateFormat formatOut = new SimpleDateFormat("dd MMM", Locale.getDefault());
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
        TextView tvDescription = view.findViewById(R.id.cost_description);
        tvDescription.setText(description);
    }

    public void setCompany(String company) {
        TextView tvCompany = view.findViewById(R.id.cost_company);
        tvCompany.setText(company);
    }

    public void setInvoiceNr(String invoiceNr) {
        TextView tvInvoiceNr = view.findViewById(R.id.cost_invoice_nr);
        tvInvoiceNr.setText(invoiceNr);
    }

    public void setPrice(double price) {
        TextView tvPrice = view.findViewById(R.id.cost_price);
        DecimalFormat currency = new DecimalFormat("0.00");
        String convertedPrice = "€  " + currency.format(price);
        tvPrice.setText(convertedPrice);
    }

}