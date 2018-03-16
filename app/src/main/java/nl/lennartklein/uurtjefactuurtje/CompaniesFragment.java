////////////////////////////////////////////////////////////////////////////////
// Title        CompaniesFragment
// Parent       MainActivity
//
// Date         February 1 2018
// Author       Lennart J Klein  (info@lennartklein.nl)
// Project      UurtjeFactuurtje
// Assignment   App Studio, University of Amsterdam
////////////////////////////////////////////////////////////////////////////////

package nl.lennartklein.uurtjefactuurtje;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

/**
 * The companies related to this user (including it's own)
 */
public class CompaniesFragment extends Fragment implements View.OnClickListener {

    // Authentication
    private FirebaseAuth auth;
    private FirebaseUser currentUser;

    // Database references
    private DatabaseReference db;
    private DatabaseReference dbUsersMe;
    private DatabaseReference dbCompaniesMe;
    private Query queryCompanies;

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

        setReferences();

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


    /**
     * Sets the FireBase authentication and current user
     */
    private void setAuth() {
        auth = FirebaseAuth.getInstance();
        currentUser = auth.getCurrentUser();
    }

    private void setReferences() {
        db = PersistentDatabase.getReference();
        dbUsersMe = db.child("users").child(currentUser.getUid());
        dbCompaniesMe = db.child("companies").child(currentUser.getUid());
        queryCompanies = dbCompaniesMe;
    }

    /**
     * Opens a dialog fragment to add a company
     */
    private void openCompanyFragment() {
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        AddCompanyFragment dialog = new AddCompanyFragment();
        dialog.show(transaction, "AddCompany");
    }

    /**
     * Constructs the list of companies
     */
    private void initiateCompaniesList() {
        // Construct the list
        RecyclerView.LayoutManager manager = new LinearLayoutManager(mContext);
        companiesList.setLayoutManager(manager);
    }

    /**
     * Fills the list with companies
     */
    private void populateCompaniesList() {
        inProgress(true);

        FirebaseRecyclerOptions<Company> options =
                new FirebaseRecyclerOptions.Builder<Company>()
                        .setQuery(queryCompanies, Company.class)
                        .build();

        FirebaseRecyclerAdapter adapter =
                new FirebaseRecyclerAdapter<Company, CompanyItem>(options) {

                    @Override
                    public CompanyItem onCreateViewHolder(ViewGroup parent, int viewType) {
                        View view = LayoutInflater.from(parent.getContext())
                                .inflate(R.layout.list_item_company, parent, false);
                        return new CompanyItem(view);
                    }

                    @Override
                    protected void onBindViewHolder(CompanyItem row, final int position, Company company) {
                        row.setName(company.getName());
                        row.setButton();
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
    public static class CompanyItem extends RecyclerView.ViewHolder {
        View view;
        ImageButton actionDelete;

        public CompanyItem(View view) {
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

    /**
     * Updates UI based on items in the list
     */
    private void checkAmount(int amount) {
        if (amount == 0) {
            emptyCompaniesList.setVisibility(View.VISIBLE);
            companiesList.setVisibility(View.INVISIBLE);
        } else {
            emptyCompaniesList.setVisibility(View.INVISIBLE);
            companiesList.setVisibility(View.VISIBLE);
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
                Log.d("Firebase", String.valueOf(databaseError));
            }
        });
    }

    /**
     * Show wheel when in progress
     */
    private void inProgress(boolean loading) {
        if (loading) {
            progressWheel.setVisibility(View.VISIBLE);
        } else {
            progressWheel.setVisibility(View.INVISIBLE);
        }
    }
}
