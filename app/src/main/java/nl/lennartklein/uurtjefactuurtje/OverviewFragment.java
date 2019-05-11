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
import android.support.v4.app.Fragment;
import android.support.v4.content.FileProvider;
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
import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;

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
    private DatabaseReference dbCompaniesMe;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_overview, container, false);

        setAuth();

        // Set database references
        db = PersistentDatabase.getReference();
        dbProjectsMe = db.child("projects").child(currentUser.getUid());
        dbCostsMe = db.child("costs").child(currentUser.getUid());
        dbInvoicesMe = db.child("invoices").child(currentUser.getUid());
        dbCompaniesMe = db.child("companies").child(currentUser.getUid());

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
        new Thread(new Runnable() {
            public void run() {
                initiateProjectsList();
            }
        }).start();
        new Thread(new Runnable() {
            public void run() {
                initiateCostsList();
            }
        }).start();
        new Thread(new Runnable() {
            public void run() {
                initiateInvoicesList();
            }
        }).start();

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
        switch (view.getId()) {
            case R.id.action_add_work:
                startWorkActivity();
                break;
            case R.id.action_add_cost:
                startCostActivity();
                break;
            case R.id.action_add_project:
                startProjectActivity();
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

    /**
     * Opens a dialog fragment to add work
     */
    private void startWorkActivity() {
        Intent addWorkIntent = new Intent(mContext, AddWorkActivity.class);
        startActivity(addWorkIntent);
    }

    /**
     * Opens an activity to add a cost
     */
    private void startCostActivity() {
        Intent addCostIntent = new Intent(mContext, AddCostActivity.class);
        startActivity(addCostIntent);
    }

    /**
     * Opens a dialog fragment to add a project
     */
    private void startProjectActivity() {
        Intent addProjectIntent = new Intent(mContext, AddProjectActivity.class);
        startActivity(addProjectIntent);
    }

    /**
     * Constructs the list of projects
     */
    private void initiateProjectsList() {
        // Construct the list
        RecyclerView.LayoutManager manager = new LinearLayoutManager(mContext,
                LinearLayoutManager.HORIZONTAL, false);
        ((LinearLayoutManager) manager).setReverseLayout(true);
        ((LinearLayoutManager) manager).setStackFromEnd(true);
        projectsList.setLayoutManager(manager);

        inProgress(true);
    }

    /**
     * Fills the list with projects
     */
    private void populateProjectsList() {
        inProgress(true);

        // Create an adapter
        FirebaseRecyclerAdapter<Project, ProjectItem> adapter =
                new FirebaseRecyclerAdapter<Project, ProjectItem>(
                        Project.class,
                        R.layout.list_item_project,
                        ProjectItem.class,
                        dbProjectsMe.orderByChild("lastInvoice")
                ) {
                    @Override
                    protected void populateViewHolder(final ProjectItem row, Project project, int position) {

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

                            // Hide loading symbol when this is the last item
                            if (position == getItemCount() - 1) {
                                inProgress(false);
                            }

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
        DividerItemDecoration divider = new DividerItemDecoration(costsList.getContext(), 1);
        costsList.addItemDecoration(divider);

        inProgress(true);
    }

    /**
     * Fills the list with costs
     */
    private void populateCostsList() {
        inProgress(true);

        // Create an adapter
        FirebaseRecyclerAdapter<Cost, CostItem> adapter =
                new FirebaseRecyclerAdapter<Cost, CostItem>(
                        Cost.class,
                        R.layout.list_item_cost,
                        CostItem.class,
                        dbCostsMe.orderByChild("date")
                ) {
                    @Override
                    protected void populateViewHolder(final CostItem row, final Cost cost, int position) {
                        // Fill the row
                        row.setDate(cost.getDate());
                        row.setDescription(cost.getDescription());
                        row.setCompany(cost.getCompanyName());
                        row.setInvoiceNr(cost.getInvoiceNr());
                        row.setPrice(cost.getPrice());
                        cost.setKey(getRef(position).getKey());

                        // Set click listener
                        row.view.setOnLongClickListener(new View.OnLongClickListener() {
                            @Override
                            public boolean onLongClick(View view) {
                                verifyDeleteCost(cost);
                                return false;
                            }
                        });

                        // Hide loading symbol when this is the last item
                        if (position == getItemCount() - 1) {
                            inProgress(false);
                        }

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
        DividerItemDecoration divider = new DividerItemDecoration(invoicesList.getContext(), 1);
        invoicesList.addItemDecoration(divider);

        inProgress(true);
    }

    /**
     * Fills the list with invoices
     */
    private void populateInvoicesList() {
        inProgress(true);

        // Create an adapter
        FirebaseRecyclerAdapter<Invoice, InvoiceItem> adapter =
                new FirebaseRecyclerAdapter<Invoice, InvoiceItem>(
                        Invoice.class,
                        R.layout.list_item_invoice,
                        InvoiceItem.class,
                        dbInvoicesMe
                ) {

                    @Override
                    protected void populateViewHolder(final InvoiceItem row, final Invoice invoice, final int position) {
                        final String key = getRef(position).getKey();

                        // Fetch company info
                        dbCompaniesMe.child(invoice.getCompanyId()).addListenerForSingleValueEvent(
                                new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        Company thisCompany = dataSnapshot.getValue(Company.class);
                                        if (thisCompany != null) {
                                            invoice.setCompany(thisCompany);
                                            fillInvoiceItem(row, invoice, position, key);
                                        }
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {
                                        fillInvoiceItem(row, invoice, position, key);
                                    }
                                });

                        // Hide loading symbol when this is the last item
                        if (position == getItemCount() - 1) {
                            inProgress(false);
                        }
                    }
                };

        // Set the adapter
        invoicesList.setAdapter(adapter);

        // Update amount in list
        checkAmountInvoices(invoicesList.getAdapter().getItemCount());

        inProgress(false);

    }

    private void fillInvoiceItem(InvoiceItem row, final Invoice invoice, int position, String key) {
        row.setDate(invoice.getDate());
        row.setInvoiceNr(invoice.getInvoiceNr());
        if (invoice.getCompany() != null) {
            row.setCompany(invoice.getCompany().getName());
        } else {
            row.setCompany(getResources().getString(R.string.invoice));
        }
        row.setPrice(invoice.getTotalPrice());
        invoice.setKey(key);

        // Set click listener
        row.view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openFile(invoice);
            }
        });

        // Update amount in list
        checkAmountInvoices(invoicesList.getAdapter().getItemCount());
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
                Uri pdfUri = FileProvider.getUriForFile(mContext, "nl.lennartklein.fileprovider", file);
                target.setDataAndType(pdfUri, "application/pdf");
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
                GenerateInvoicePdf writer = new GenerateInvoicePdf(getActivity(), mContext, invoice);
                writer.createFile();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
            }
        });
    }
}
