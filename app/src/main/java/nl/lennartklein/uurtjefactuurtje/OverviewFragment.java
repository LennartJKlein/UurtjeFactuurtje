package nl.lennartklein.uurtjefactuurtje;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * An overview of projects and recent costs
 */
public class OverviewFragment extends Fragment implements View.OnClickListener {

    // Authentication
    private FirebaseAuth auth;
    private FirebaseUser currentUser;

    // UI references
    private Context mContext;
    private ProgressBar progressWheel;
    private FloatingActionMenu floatingMenu;
    private RecyclerView projectsList;
    private TextView emptyProjectsList;
    private RecyclerView costsList;
    private TextView emptyCostsList;
    private TextView currentDate;

    // Database references
    private DatabaseReference db;
    private DatabaseReference dbProjectsMe;
    private DatabaseReference dbCostsMe;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_overview, container, false);

        setAuth();

        // Set database references
        db = PersistentDatabase.getReference();
        dbProjectsMe = db.child("projects").child(currentUser.getUid());
        dbCostsMe = db.child("costs").child(currentUser.getUid());

        // Set UI references
        mContext = getActivity().getApplicationContext();
        progressWheel = view.findViewById(R.id.list_loader);
        projectsList = view.findViewById(R.id.list_projects);
        emptyProjectsList = view.findViewById(R.id.list_projects_empty);
        costsList = view.findViewById(R.id.list_costs);
        emptyCostsList = view.findViewById(R.id.list_costs_empty);
        floatingMenu = view.findViewById(R.id.action_add_data);
        currentDate = view.findViewById(R.id.current_date);
        FloatingActionButton addWork = view.findViewById(R.id.action_add_work);
        FloatingActionButton addCost = view.findViewById(R.id.action_add_cost);
        FloatingActionButton addProject = view.findViewById(R.id.action_add_project);

        // Populate lists
        initiateProjectsList();
        initiateCostsList();

        // Set click listeners
        addWork.setOnClickListener(this);
        addCost.setOnClickListener(this);
        addProject.setOnClickListener(this);
        floatingMenu.setClosedOnTouchOutside(true);

        showCurrentDate();

        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
    }

    @Override
    public void onStart() {
        super.onStart();

        populateProjectsList();
        populateCostsList();
    }

    @Override
    public void onResume() {
        super.onResume();

        populateProjectsList();
        populateCostsList();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.action_add_work:
                openWorkFragment();
                break;
            case R.id.action_add_cost:
                openCostFragment();
                break;
            case R.id.action_add_project:
                openProjectFragment();
                break;
        }

        floatingMenu.close(true);
    }

    /**
     * Sets the authentication variables
     */
    private void setAuth() {
        auth = FirebaseAuth.getInstance();
        currentUser = auth.getCurrentUser();
    }

    private void openWorkFragment() {
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        AddWorkFragment dialog = new AddWorkFragment();
        dialog.show(transaction, "AddWork");
    }

    private void openCostFragment() {
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        AddCostFragment dialog = new AddCostFragment();
        dialog.show(transaction, "AddCost");
    }

    private void openProjectFragment() {
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        AddProjectFragment dialog = new AddProjectFragment();
        dialog.show(transaction, "AddProject");
    }

    /**
     * Construct the list of projects
     */
    private void initiateProjectsList() {
        // Construct the list
        RecyclerView.LayoutManager manager = new LinearLayoutManager(mContext,
                LinearLayoutManager.VERTICAL, true);
        projectsList.setLayoutManager(manager);

        // Add divider line
        DividerItemDecoration divider = new DividerItemDecoration(projectsList.getContext(),1);
        projectsList.addItemDecoration(divider);

        inProgress(true);
    }

    /**
     * Fill the list with projects
     */
    private void populateProjectsList() {
        inProgress(true);

        // Create an adapter
        FirebaseRecyclerAdapter<Project, ProjectRow> adapter =
                new FirebaseRecyclerAdapter<Project, ProjectRow>(
                Project.class,
                R.layout.list_item_project,
                ProjectRow.class,
                dbProjectsMe
        ) {
            @Override
            protected void populateViewHolder(final ProjectRow row, Project project, int position) {
                // Fill the row
                row.setName(project.getName());
                row.setCompany(project.getCompanyName());

                String invoiceDate = ((invoiceDate = project.getLastInvoice()) != null) ? invoiceDate : "-";
                String format = getResources().getString(R.string.placeholder_date_invoice);
                row.setInvoice(invoiceDate, format);
                row.setKey(getRef(position).getKey());

                // Set click listener
                row.view.setOnClickListener(new View.OnClickListener(){
                    @Override
                    public void onClick(View view) {
                        Intent projectIntent = new Intent(mContext, ProjectActivity.class);
                        projectIntent.putExtra("PROJECT_KEY", row.getKey());
                        startActivity(projectIntent);
                    }
                });

                inProgress(false);

                // Update amount in list
                checkAmountProjects(projectsList.getAdapter().getItemCount());
            }
        };

        // Set the adapter
        projectsList.setAdapter(adapter);

        // Update amount in list
        checkAmountProjects(projectsList.getAdapter().getItemCount());

        inProgress(false);
    }

    /**
     * View holder for a project row
     */
    public static class ProjectRow extends RecyclerView.ViewHolder {
        View view;
        String key;

        public ProjectRow(View view) {
            super(view);
            this.view = view;
        }

        public void setKey(String key) {
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

        public void setInvoice(String last_invoice, String format) {
            TextView tvInvoice = view.findViewById(R.id.project_last_invoice);
            String formattedDate = String.format(format, last_invoice);
            tvInvoice.setText(formattedDate);
        }
    }

    /**
     * Construct the list of costs
     */
    private void initiateCostsList() {
        // Construct the list
        RecyclerView.LayoutManager manager = new LinearLayoutManager(mContext,
                LinearLayoutManager.VERTICAL, true);
        costsList.setLayoutManager(manager);

        // Add divider line
        DividerItemDecoration divider = new DividerItemDecoration(costsList.getContext(),1);
        costsList.addItemDecoration(divider);

        inProgress(true);
    }

    /**
     * Fill the list with costs
     */
    private void populateCostsList() {
        inProgress(true);

        // Create an adapter
        FirebaseRecyclerAdapter<Cost, CostRow> adapter =
                new FirebaseRecyclerAdapter<Cost, CostRow>(
                        Cost.class,
                        R.layout.list_item_cost,
                        CostRow.class,
                        dbCostsMe.orderByChild("date")
                ) {
                    @Override
                    protected void populateViewHolder(final CostRow row, final Cost cost, int position) {
                        // Fill the row
                        row.setDate(cost.getDate());
                        row.setDescription(cost.getDescription());
                        row.setCompany(cost.getCompanyName());
                        row.setInvoiceNr(cost.getInvoiceNr());
                        row.setPrice(cost.getPrice());
                        cost.setKey(getRef(position).getKey());

                        // Set click listener
                        row.view.setOnLongClickListener(new View.OnLongClickListener(){
                            @Override
                            public boolean onLongClick(View view) {
                                verifyDeleteCost(cost);
                                return false;
                            }
                        });

                        inProgress(false);

                        // Update amount in list
                        checkAmountCosts(costsList.getAdapter().getItemCount());
                    }
                };

        // Set the adapter
        costsList.setAdapter(adapter);

        // Update amount in list
        checkAmountCosts(costsList.getAdapter().getItemCount());

        inProgress(false);
    }

    /**
     * View holder for a cost row
     */
    public static class CostRow extends RecyclerView.ViewHolder {
        View view;

        public CostRow(View view) {
            super(view);
            this.view = view;
        }

        public void setDate(String date) {
            TextView tvDate = view.findViewById(R.id.cost_date);

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

    private void checkAmountProjects(int amount) {
        Log.d("amount", String.valueOf(amount));
        if (amount == 0) {
            emptyProjectsList.setVisibility(View.VISIBLE);
            projectsList.setVisibility(View.INVISIBLE);
        } else {
            emptyProjectsList.setVisibility(View.INVISIBLE);
            projectsList.setVisibility(View.VISIBLE);
        }
    }

    private void checkAmountCosts(int amount) {
        if (amount == 0) {
            emptyCostsList.setVisibility(View.VISIBLE);
            costsList.setVisibility(View.INVISIBLE);
        } else {
            emptyCostsList.setVisibility(View.INVISIBLE);
            costsList.setVisibility(View.VISIBLE);
        }
    }

    private void inProgress(boolean loading) {
        if (loading) {
            progressWheel.setVisibility(View.VISIBLE);
            projectsList.setVisibility(View.INVISIBLE);
            emptyProjectsList.setVisibility(View.INVISIBLE);
            costsList.setVisibility(View.INVISIBLE);
            emptyCostsList.setVisibility(View.INVISIBLE);
        } else {
            progressWheel.setVisibility(View.INVISIBLE);
            projectsList.setVisibility(View.VISIBLE);
            costsList.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Shows a dialog to verify the deletion of a cost
     */
    private void verifyDeleteCost(final Cost cost) {
        Log.d("Cost", "Clicked");
        // Set up dialog
        AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
        alert.setTitle(getString(R.string.note_verify_delete_cost));

        // Create a delete button
        alert.setPositiveButton(R.string.action_delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                deleteCost(cost);
            }
        });

        // Create a cancel button
        alert.setNegativeButton(R.string.action_cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });
        alert.show();
    }

    /**
     * Deletes a cost from the database
     */
    private void deleteCost(Cost cost) {
        dbCostsMe.child(cost.getKey()).setValue(null);
    }

    private void showCurrentDate() {
        Calendar c = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("dd MMMM, yyyy");
        String formattedToday = getResources().getString(
                R.string.placeholder_current_date,
                df.format(c.getTime())
        );
        currentDate.setText(formattedToday);
    }

}
