package nl.lennartklein.uurtjefactuurtje;

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
    private RecyclerView projectsList;
    private TextView emptyProjectsList;
    private FloatingActionMenu floatingMenu;

    // Database references
    private DatabaseReference db;
    private DatabaseReference dbProjectsMe;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_overview, container, false);

        setAuth();

        // Set database references
        db = FirebaseDatabase.getInstance().getReference();
        dbProjectsMe = db.child("projects").child(currentUser.getUid());

        // Set global references
        mContext = getActivity().getApplicationContext();
        projectsList = view.findViewById(R.id.list_projects);
        emptyProjectsList = view.findViewById(R.id.list_projects_empty);
        floatingMenu = view.findViewById(R.id.action_add_data);
        progressWheel = view.findViewById(R.id.list_loader);

        initiateProjectsList();
        initiateActionButtons(view);

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

        // Initialise the list of projects
        populateProjectsList();
    }

    @Override
    public void onResume() {
        super.onResume();

        // Refresh the list of projects
        populateProjectsList();
    }

    /**
     * Make menu of floating action button clickable
     */
    private void initiateActionButtons(View view) {
        FloatingActionButton addWork = view.findViewById(R.id.action_add_work);
        addWork.setOnClickListener(this);

        FloatingActionButton addCost = view.findViewById(R.id.action_add_cost);
        addCost.setOnClickListener(this);

        FloatingActionButton addProject = view.findViewById(R.id.action_add_project);
        addProject.setOnClickListener(this);

        floatingMenu.setClosedOnTouchOutside(true);
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
        RecyclerView.LayoutManager manager = new LinearLayoutManager(mContext);
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
                inProgress(true);

                // Fill the row
                row.setName(project.getName());
                row.setCompany(project.getCompanyName());

                String invoiceDate = ((invoiceDate = project.getLastInvoice()) != null) ? invoiceDate : "-";
                String format = getResources().getString(R.string.placeholder_date_invoice);
                row.setInvoice(invoiceDate, format);
                row.setId(getRef(position).getKey());

                // Set click listener
                row.view.setOnClickListener(new View.OnClickListener(){
                    @Override
                    public void onClick(View view) {
                        Intent projectIntent = new Intent(mContext, ProjectActivity.class);
                        projectIntent.putExtra("PROJECT_KEY", row.getId());
                        startActivity(projectIntent);
                    }
                });

                // Update amount in list
                checkAmount(projectsList.getAdapter().getItemCount());
            }
        };

        // Set the adapter
        projectsList.setAdapter(adapter);

        // Update amount in list
        checkAmount(projectsList.getAdapter().getItemCount());
    }

    /**
     * View holder for a project row
     */
    public static class ProjectRow extends RecyclerView.ViewHolder {
        View view;
        String id;

        public ProjectRow(View view) {
            super(view);
            this.view = view;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getId() {
            return id;
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

    private void checkAmount(int amount) {
        inProgress(false);

        if (amount == 0) {
            emptyProjectsList.setVisibility(View.VISIBLE);
            projectsList.setVisibility(View.INVISIBLE);
        } else {
            emptyProjectsList.setVisibility(View.INVISIBLE);
            projectsList.setVisibility(View.VISIBLE);
        }
    }

    private void inProgress(boolean loading) {
        if (loading) {
            progressWheel.setVisibility(View.VISIBLE);
            projectsList.setVisibility(View.INVISIBLE);
            emptyProjectsList.setVisibility(View.INVISIBLE);
        } else {
            progressWheel.setVisibility(View.INVISIBLE);
            projectsList.setVisibility(View.VISIBLE);
        }
    }

    private void setAuth() {
        auth = FirebaseAuth.getInstance();
        currentUser = auth.getCurrentUser();
    }

}
