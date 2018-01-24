package nl.lennartklein.uurtjefactuurtje;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

/**
 * See companies of this user
 */
public class CompaniesFragment extends Fragment implements View.OnClickListener {

    // Authentication
    private FirebaseAuth auth;
    private FirebaseUser currentUser;

    // Database references
    private DatabaseReference db;
    private DatabaseReference dbUsersMe;
    private DatabaseReference dbCompaniesMe;

    // UI references
    private Context mContext;
    private ProgressBar progressWheel;
    private RecyclerView companiesList;
    private TextView emptyCompaniesList;
    private Button addCompanyButton;
    private RelativeLayout itemMyCompany;
    private TextView nameMyCompany;
    private ImageButton buttonMyCompany;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_companies, container, false);

        setAuth();

        // Set database references
        db = FirebaseDatabase.getInstance().getReference();
        dbCompaniesMe = db.child("companies").child(currentUser.getUid());
        dbUsersMe = db.child("users").child(currentUser.getUid());

        // Set UI references
        mContext = getActivity().getApplicationContext();
        progressWheel = view.findViewById(R.id.list_loader);
        companiesList = view.findViewById(R.id.list_companies);
        emptyCompaniesList = view.findViewById(R.id.list_companies_empty);
        addCompanyButton = view.findViewById(R.id.action_add_company);
        itemMyCompany = view.findViewById(R.id.my_company);
        nameMyCompany = view.findViewById(R.id.my_company_name);
        buttonMyCompany = view.findViewById(R.id.action_show_my_company);

        // Set click listeners
        addCompanyButton.setOnClickListener(this);
        itemMyCompany.setOnClickListener(this);
        loadMyCompany();

        initiateCompaniesList();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        // Initialise the list of companies
        populateCompaniesList();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.action_add_company:
                openCompanyFragment();
                break;
            case R.id.my_company:
                buttonMyCompany.performClick();
                startActivity(new Intent(mContext, SettingsActivity.class));
                break;
        }
    }

    private void loadMyCompany() {
        dbUsersMe.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                if (user != null) {
                    String companyName = user.getCompanyName();
                    if (!companyName.equals("")) {
                        nameMyCompany.setText(user.getCompanyName());
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void openCompanyFragment() {
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        AddCompanyFragment dialog = new AddCompanyFragment();
        dialog.show(transaction, "AddCompany");
    }

    /**
     * Construct the list of companies
     */
    private void initiateCompaniesList() {
        // Construct the list
        RecyclerView.LayoutManager manager = new LinearLayoutManager(mContext);
        companiesList.setLayoutManager(manager);
    }

    /**
     * Fill the list with companies
     */
    private void populateCompaniesList() {
        inProgress(true);

        // Create an adapter
        FirebaseRecyclerAdapter<Company, CompanyRow> adapter =
                new FirebaseRecyclerAdapter<Company, CompanyRow>(
                Company.class,
                R.layout.list_item_company,
                CompaniesFragment.CompanyRow.class,
                dbCompaniesMe
        ) {
            @Override
            protected void populateViewHolder(CompanyRow row, Company company, final int position) {

                // Fill the row
                row.setName(company.getName());
                row.setButton();

                // Set click listener
                row.actionDelete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dbCompaniesMe.child(getRef(position).getKey()).setValue(null);
                    }
                });

                inProgress(false);

                // Update amount in list
                checkAmount(companiesList.getAdapter().getItemCount());

            }
        };

        // Set the adapter
        companiesList.setAdapter(adapter);

        // Update amount in list
        checkAmount(companiesList.getAdapter().getItemCount());
    }

    /**
     * View holder for a company row
     */
    public static class CompanyRow extends RecyclerView.ViewHolder {
        View view;
        ImageButton actionDelete;

        public CompanyRow(View view) {
            super(view);
            this.view = view;
        }

        public void setName(String name) {
            TextView tvName = view.findViewById(R.id.company_name);
            tvName.setText(name);
        }

        public void setButton() {
            this.actionDelete = view.findViewById(R.id.action_delete);
        }

    }

    private void checkAmount(int amount) {
        if (amount == 0) {
            emptyCompaniesList.setVisibility(View.VISIBLE);
            companiesList.setVisibility(View.INVISIBLE);
        } else {
            emptyCompaniesList.setVisibility(View.INVISIBLE);
            companiesList.setVisibility(View.VISIBLE);
        }
    }

    private void inProgress(boolean loading) {
        if (loading) {
            progressWheel.setVisibility(View.VISIBLE);
            companiesList.setVisibility(View.INVISIBLE);
        } else {
            progressWheel.setVisibility(View.INVISIBLE);
            companiesList.setVisibility(View.VISIBLE);
        }
    }

    private void setAuth() {
        auth = FirebaseAuth.getInstance();
        currentUser = auth.getCurrentUser();
    }
}
