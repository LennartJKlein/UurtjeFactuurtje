package nl.lennartklein.uurtjefactuurtjewear;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

/**
 * View item for a project row
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

    String getKey() {
        return key;
    }

    void setName(String name) {
        TextView tvName = view.findViewById(R.id.project_name);
        tvName.setText(name);
    }

    void setCompany(String company_name) {
        TextView tvCompany = view.findViewById(R.id.project_company);
        tvCompany.setText(company_name);
    }

}