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
 * View holder for a invoice list item
 */
public class InvoiceItem extends RecyclerView.ViewHolder {
    View view;

    public InvoiceItem(View view) {
        super(view);
        this.view = view;
    }

    public void setDate(String date) {
        TextView tvDate = view.findViewById(R.id.invoice_date);

        if (date != null) {
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
    }

    void setInvoiceNr(String invoiceNr) {
        TextView tvInvoiceNr = view.findViewById(R.id.invoice_nr);
        tvInvoiceNr.setText("#" + invoiceNr);
    }

    public void setPrice(double price) {
        TextView tvPrice = view.findViewById(R.id.invoice_price);
        DecimalFormat currency = new DecimalFormat("0.00");
        String convertedPrice = "€  " + currency.format(price);
        tvPrice.setText(convertedPrice);
    }

}