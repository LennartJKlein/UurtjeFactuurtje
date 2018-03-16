////////////////////////////////////////////////////////////////////////////////
// Title        OverviewFragment
// Parent       MainActivity
//
// Date         February 1 2018
// Author       Lennart J Klein  (info@lennartklein.nl)
// Project      UurtjeFactuurtje
// Assignment   App Studio, University of Amsterdam
////////////////////////////////////////////////////////////////////////////////

package nl.lennartklein.uurtjefactuurtje;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
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
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.firebase.ui.database.SnapshotParser;
import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.io.File;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import static android.content.ContentValues.TAG;
import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

/**
 * An overview of projects and recent costs
 */
public class OverviewFragment extends Fragment implements View.OnClickListener {

    // Authentication
    private FirebaseAuth auth;
    private FirebaseUser currentUser;

    // UI references
    private Context mContext;
    private Resources res;
    private ProgressBar progressWheel;
    private FloatingActionMenu floatingMenu;
    private RecyclerView projectsList;
    private TextView emptyProjectsList;
    private RecyclerView costsList;
    private TextView emptyCostsList;
    private RecyclerView invoicesList;
    private TextView emptyInvoicesList;
    private TextView currentDate;

    // Database references
    private DatabaseReference db;
    private DatabaseReference dbProjectsMe;
    private DatabaseReference dbCostsMe;
    private DatabaseReference dbInvoicesMe;
    private Query queryProjects;
    private Query queryCosts;
    private Query queryInvoices;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_overview, container, false);

        setAuth();

        setReferences();

        // Set UI references
        mContext = getActivity().getApplicationContext();
        res = mContext.getResources();
        progressWheel = view.findViewById(R.id.list_loader);
        projectsList = view.findViewById(R.id.list_projects);
        emptyProjectsList = view.findViewById(R.id.list_projects_empty);
        costsList = view.findViewById(R.id.list_costs);
        emptyCostsList = view.findViewById(R.id.list_costs_empty);
        invoicesList = view.findViewById(R.id.list_invoices);
        emptyInvoicesList = view.findViewById(R.id.list_invoices_empty);
        floatingMenu = view.findViewById(R.id.action_add_data);
        currentDate = view.findViewById(R.id.current_date);
        FloatingActionButton addWork = view.findViewById(R.id.action_add_work);
        FloatingActionButton addCost = view.findViewById(R.id.action_add_cost);
        FloatingActionButton addProject = view.findViewById(R.id.action_add_project);

        // Populate lists
        initiateProjectsList();
        initiateCostsList();
        initiateInvoicesList();

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
        populateInvoicesList();
    }

    @Override
    public void onResume() {
        super.onResume();

        populateProjectsList();
        populateCostsList();
        populateInvoicesList();
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
     * Sets the FireBase authentication and current user
     */
    private void setAuth() {
        auth = FirebaseAuth.getInstance();
        currentUser = auth.getCurrentUser();
    }

    private void setReferences() {
        db = PersistentDatabase.getReference();
        dbProjectsMe = db.child("projects").child(currentUser.getUid());
        dbCostsMe = db.child("costs").child(currentUser.getUid());
        dbInvoicesMe = db.child("invoices").child(currentUser.getUid());
        queryProjects = dbProjectsMe.orderByChild("lastActivity");
        queryCosts = dbCostsMe;
        queryInvoices = dbInvoicesMe;
    }

    /**
     * Opens a dialog fragment to add work
     */
    private void openWorkFragment() {
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        AddWorkFragment dialog = new AddWorkFragment();
        dialog.show(transaction, "AddWork");
    }

    /**
     * Opens a dialog fragment to add a cost
     */
    private void openCostFragment() {
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        AddCostFragment dialog = new AddCostFragment();
        dialog.show(transaction, "AddCost");
    }

    /**
     * Opens a dialog fragment to add a project
     */
    private void openProjectFragment() {
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        AddProjectFragment dialog = new AddProjectFragment();
        dialog.show(transaction, "AddProject");
    }

    /**
     * Constructs the list of projects
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
     * Fills the list with projects
     */
    private void populateProjectsList() {
        inProgress(true);

        FirebaseRecyclerOptions<Project> options =
                new FirebaseRecyclerOptions.Builder<Project>()
                        .setQuery(queryProjects, Project.class)
                        .build();

        FirebaseRecyclerAdapter adapter =
                new FirebaseRecyclerAdapter<Project, ProjectItem>(options) {

                    @Override
                    public ProjectItem onCreateViewHolder(ViewGroup parent, int viewType) {
                        View view = LayoutInflater.from(parent.getContext())
                                .inflate(R.layout.list_item_project, parent, false);
                        return new ProjectItem(view);
                    }

                    @Override
                    protected void onBindViewHolder(final ProjectItem row, int position, Project project) {
                        if (project.getStatus() == 1) {

                            // Fill the row
                            row.setName(project.getName());
                            row.setCompany(project.getCompanyName());

                            String invoiceDate = ((invoiceDate = project.getLastInvoice()) != null) ? invoiceDate : "-";
                            String format = getResources().getString(R.string.placeholder_date_invoice);
                            row.setInvoice(invoiceDate, format);
                            row.setKey(getRef(position).getKey());

                            // Set click listener
                            row.view.setOnClickListener(new View.OnClickListener() {
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
                    }
                };

        // Set the adapter
        projectsList.setAdapter(adapter);

        inProgress(false);

        // Update amount in list
        checkAmountProjects(projectsList.getAdapter().getItemCount());
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
     * Fills the list with costs
     */
    private void populateCostsList() {
        inProgress(true);

        FirebaseRecyclerOptions<Cost> options =
                new FirebaseRecyclerOptions.Builder<Cost>()
                        .setQuery(queryCosts, Cost.class)
                        .build();

        FirebaseRecyclerAdapter adapter =
                new FirebaseRecyclerAdapter<Cost, CostItem>(options) {

                    @Override
                    public CostItem onCreateViewHolder(ViewGroup parent, int viewType) {
                        View view = LayoutInflater.from(parent.getContext())
                                .inflate(R.layout.list_item_cost, parent, false);
                        return new CostItem(view);
                    }

                    @Override
                    protected void onBindViewHolder(final CostItem row, int position, final Cost cost) {
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
     * Construct the list of invoices
     */
    private void initiateInvoicesList() {
        // Construct the list
        RecyclerView.LayoutManager manager = new LinearLayoutManager(mContext,
                LinearLayoutManager.VERTICAL, true);
        invoicesList.setLayoutManager(manager);

        // Add divider line
        DividerItemDecoration divider = new DividerItemDecoration(invoicesList.getContext(),1);
        invoicesList.addItemDecoration(divider);

        inProgress(true);
    }

    /**
     * Fills the list with invoices
     */
    private void populateInvoicesList() {
        inProgress(true);

        FirebaseRecyclerOptions<Invoice> options =
                new FirebaseRecyclerOptions.Builder<Invoice>()
                        .setQuery(queryInvoices, new SnapshotParser<Invoice>(){
                            @NonNull
                            @Override
                            public Invoice parseSnapshot(@NonNull DataSnapshot snapshot) {
                                Invoice subInvoice = null;
                                for (DataSnapshot invoiceSnapshot: snapshot.getChildren()) {
                                    subInvoice = invoiceSnapshot.getValue(Invoice.class);
                                }
                                if (subInvoice != null) {
                                    return subInvoice;
                                }
                                return snapshot.getValue(Invoice.class);
                            }
                        })
                        .build();

        FirebaseRecyclerAdapter adapter =
                new FirebaseRecyclerAdapter<Invoice, InvoiceItem>(options) {

                    @Override
                    public InvoiceItem onCreateViewHolder(ViewGroup parent, int viewType) {
                        View view = LayoutInflater.from(parent.getContext())
                                .inflate(R.layout.list_item_invoice, parent, false);
                        return new InvoiceItem(view);
                    }

                    @Override
                    protected void onBindViewHolder(final InvoiceItem row, int position, final Invoice invoice) {
                        row.setDate(invoice.getDate());
                        row.setInvoiceNr(invoice.getInvoiceNr());
                        row.setPrice(invoice.getTotalPrice());
                        invoice.setKey(getRef(position).getKey());

                        // Set click listener
                        row.view.setOnClickListener(new View.OnClickListener(){
                            @Override
                            public void onClick(View view) {
                                openFile(invoice);
                            }
                        });

                        inProgress(false);

                        // Update amount in list
                        checkAmountInvoices(invoicesList.getAdapter().getItemCount());
                    }
                };

        // Set the adapter
        invoicesList.setAdapter(adapter);

        // Update amount in list
        checkAmountInvoices(invoicesList.getAdapter().getItemCount());

        inProgress(false);

    }

    /**
     * Updates UI based on items in the list
     */
    private void checkAmountProjects(int amount) {
        if (amount == 0) {
            emptyProjectsList.setVisibility(View.VISIBLE);
            projectsList.setVisibility(View.INVISIBLE);
        } else {
            emptyProjectsList.setVisibility(View.INVISIBLE);
            projectsList.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Updates UI based on items in the list
     */
    private void checkAmountCosts(int amount) {
        if (amount == 0) {
            emptyCostsList.setVisibility(View.VISIBLE);
            costsList.setVisibility(View.INVISIBLE);
        } else {
            emptyCostsList.setVisibility(View.INVISIBLE);
            costsList.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Updates UI based on items in the list
     */
    private void checkAmountInvoices(int amount) {
        if (amount == 0) {
            emptyInvoicesList.setVisibility(View.VISIBLE);
            invoicesList.setVisibility(View.INVISIBLE);
        } else {
            emptyInvoicesList.setVisibility(View.INVISIBLE);
            invoicesList.setVisibility(View.VISIBLE);
        }
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

    /**
     * Open an invoice document
     */
    private void openFile(Invoice invoice) {
        String filepath = invoice.getFilePath();

        if (filepath == null) {
            // No file generated previously
            fetchDataAndBuild(invoice);

        } else {
            File file = new File(filepath);
            if (!file.exists()) {
                // Document doesn't exist on device
                fetchDataAndBuild(invoice);

            } else {
                // Open file at filepath
                Intent target = new Intent(Intent.ACTION_VIEW);
                target.setDataAndType(Uri.fromFile(new File(filepath)), "application/pdf");
                target.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                Intent intent = Intent.createChooser(target, res.getString(R.string.title_chooser));
                intent.addFlags(FLAG_ACTIVITY_NEW_TASK);

                try {
                    mContext.startActivity(intent);
                } catch (ActivityNotFoundException e) {
                    Toast.makeText(mContext, res.getString(R.string.error_no_reader),
                            Toast.LENGTH_LONG).show();
                }
            }
        }
    }


    /**
     * Fetches all data of this invoice
     */
    public void fetchDataAndBuild(final Invoice invoice) {
        db.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot data) {
                // Get Id's for this invoice
                String userId = invoice.getUserId();
                String companyId = invoice.getCompanyId();
                String projectId = invoice.getProjectId();

                // Fetch project
                Project project = data.child("projects").child(userId).child(projectId)
                        .getValue(Project.class);
                invoice.setProject(project);

                // Fetch user
                User user = data.child("users").child(userId)
                        .getValue(User.class);
                invoice.setUser(user);

                // Fetch receiver company
                Company company = data.child("companies").child(userId).child(companyId)
                        .getValue(Company.class);
                invoice.setCompany(company);

                // Fetch work
                DataSnapshot dataWorks = data.child("work").child(userId).child(projectId).child("paid");
                for (DataSnapshot workSnapshot : dataWorks.getChildren()) {
                    Work work = workSnapshot.getValue(Work.class);
                    if (work != null) {
                        if (work.getInvoiceId().equals(invoice.getKey())) {
                            // Add this work to the invoice
                            invoice.addWork(work);
                        }
                    }
                }

                // Write PDF file
                GeneratePdf writer = new GeneratePdf(getActivity(), mContext, invoice);
                writer.createFile();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
            }
        });
    }
}
