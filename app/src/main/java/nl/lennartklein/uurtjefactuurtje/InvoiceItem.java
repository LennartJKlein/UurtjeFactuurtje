package nl.lennartklein.uurtjefactuurtje;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
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
    ProgressBar progressWheel;
    ImageButton actionOpen;

    public InvoiceItem(View view) {
        super(view);
        this.view = view;

        progressWheel = view.findViewById(R.id.loader_file);
        actionOpen = view.findViewById(R.id.action_show_invoice);
    }

    void setDate(String date) {
        TextView tvDate = view.findViewById(R.id.invoice_date);

        if (date != null) {
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
    }

    void setInvoiceNr(String invoiceNr) {
        TextView tvInvoiceNr = view.findViewById(R.id.invoice_nr);
        tvInvoiceNr.setText("#" + invoiceNr);
    }

    void setPrice(double price) {
        TextView tvPrice = view.findViewById(R.id.invoice_price);
        DecimalFormat currency = new DecimalFormat("0.00");
        String convertedPrice = "€  " + currency.format(price);
        tvPrice.setText(convertedPrice);
    }

    void setCompany(String company) {
        TextView tvCompany = view.findViewById(R.id.invoice_company);
        tvCompany.setText(company);
    }
}