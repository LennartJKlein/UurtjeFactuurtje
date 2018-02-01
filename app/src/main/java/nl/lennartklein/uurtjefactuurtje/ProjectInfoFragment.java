////////////////////////////////////////////////////////////////////////////////
// Title        ProjectInfoFragment
// Parent       ProjectActivity
//
// Date         February 1 2018
// Author       Lennart J Klein  (info@lennartklein.nl)
// Project      UurtjeFactuurtje
// Assignment   App Studio, University of Amsterdam
////////////////////////////////////////////////////////////////////////////////

package nl.lennartklein.uurtjefactuurtje;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;

import java.util.Locale;

/**
 * Page with information about the project
 */
public class ProjectInfoFragment extends Fragment {

    // Authentication
    private FirebaseAuth auth;
    private FirebaseUser currentUser;

    // UI references
    private Context mContext;
    private TextView tvCompany;
    private TextView tvHourPrice;
    private TextView tvDate;

    // Database references
    private DatabaseReference db;
    private DatabaseReference dbProjectsMe;

    // Data
    Project project;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_project_info, container, false);

        setAuth();

        // Set UI references
        mContext = getActivity();
        tvCompany = view.findViewById(R.id.project_company);
        tvHourPrice = view.findViewById(R.id.project_hour_price);
        tvDate = view.findViewById(R.id.project_date);

        // Set database references
        db = PersistentDatabase.getReference();
        dbProjectsMe = db.child("projects").child(currentUser.getUid());

        // Get data
        project = (Project) getArguments().getSerializable("PROJECT");

        // Set data
        tvCompany.setText(project.getCompanyName());
        String hourPrice = String.format(Locale.getDefault(), "%.2f", project.getHourPrice());
        tvHourPrice.setText(getResources().getString(R.string.placeholder_currency, hourPrice));
        tvDate.setText(project.getDate());

        return view;
    }

    private void setAuth() {
        auth = FirebaseAuth.getInstance();
        currentUser = auth.getCurrentUser();
    }
}
