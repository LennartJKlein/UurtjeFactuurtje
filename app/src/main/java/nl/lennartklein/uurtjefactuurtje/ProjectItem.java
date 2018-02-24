package nl.lennartklein.uurtjefactuurtje;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

/**
 * View holder for a project list item
 */
public class ProjectItem extends RecyclerView.ViewHolder {
    View view;
    String key;

    public ProjectItem(View view) {
        super(view);
        this.view = view;
    }

    void setKey(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }

    public void setName(String name) {
        TextView tvName = view.findViewById(R.id.project_name);
        tvName.setText(name);
    }

    public void setCompany(String company_name) {
        TextView tvCompany = view.findViewById(R.id.project_company);
        tvCompany.setText(company_name);
    }

    void setInvoice(String last_invoice, String format) {
        TextView tvInvoice = view.findViewById(R.id.project_last_invoice);
        String formattedDate = String.format(format, last_invoice);
        tvInvoice.setText(formattedDate);
    }
}