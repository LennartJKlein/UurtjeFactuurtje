package nl.lennartklein.uurtjefactuurtje;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

/**
 * A form for adding sold products to a project
 */
public class AddWorkProductFragment extends Fragment {

    // UI references
    private Context mContext;
    private Button actionInsert;
    private Button actionCancel;

    // Data
    String date;
    String description;
    String price;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getContext();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_work_product, container, false);

        // Set UI references
        actionInsert = view.findViewById(R.id.action_create_project);
        actionCancel = view.findViewById(R.id.action_cancel);

        return view;
    }

    public String getDate() {
        return date;
    }

    public String getDescription() {
        return description;
    }

    public String getPrice() {
        return price;
    }
}
