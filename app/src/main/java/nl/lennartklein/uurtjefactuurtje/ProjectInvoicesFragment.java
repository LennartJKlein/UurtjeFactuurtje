////////////////////////////////////////////////////////////////////////////////
// Title        ProjectInvoicesFragment
// Parent       ProjectActivity
//
// Date         February 1 2018
// Author       Lennart J Klein  (info@lennartklein.nl)
// Project      UurtjeFactuurtje
// Assignment   App Studio, University of Amsterdam
////////////////////////////////////////////////////////////////////////////////

package nl.lennartklein.uurtjefactuurtje;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.io.File;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import static android.content.ContentValues.TAG;

/**
 * A page with all the invoices of a project
 */
public class ProjectInvoicesFragment extends Fragment implements View.OnClickListener {

    // Authentication
    private FirebaseAuth auth;
    private FirebaseUser currentUser;

    // UI references
    private Context mContext;
    private Resources res;
    private ProgressBar progressWheel;
    private RecyclerView invoicesList;
    private TextView emptyInvoicesList;
    private Button newInvoice;

    // Database references
    private DatabaseReference db;
    private DatabaseReference dbUsersMe;
    private DatabaseReference dbInvoicesMe;
    private DatabaseReference dbInvoicesThis;
    private DatabaseReference dbWorkThis;

    // Data
    private Project project;
    private User user;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_project_invoices, container, false);

        setAuth();

        project = (Project) getArguments().getSerializable("PROJECT");

        setReferences();

        fetchUser();

        // Set UI references
        mContext = getActivity();
        res = mContext.getResources();
        progressWheel = view.findViewById(R.id.list_loader);
        invoicesList = view.findViewById(R.id.list_invoices);
        emptyInvoicesList = view.findViewById(R.id.list_invoices_empty);
        newInvoice = view.findViewById(R.id.action_create_invoice);
        newInvoice.setOnClickListener(this);

        initiateInvoicesList();

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        // Initialise the list of invoices
        populateInvoicesList();
    }

    @Override
    public void onResume() {
        super.onResume();

        // Refresh the list of invoices
        populateInvoicesList();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.action_create_invoice:
                ProjectActivity parent = (ProjectActivity) getActivity();
                parent.createNewInvoice();
                break;
        }
    }

    private void setAuth() {
        auth = FirebaseAuth.getInstance();
        currentUser = auth.getCurrentUser();
    }

    private void setReferences() {
        db = PersistentDatabase.getReference();
        dbUsersMe = db.child("users").child(currentUser.getUid());
        dbInvoicesMe = db.child("invoices").child(currentUser.getUid());
        dbInvoicesThis = dbInvoicesMe.child(project.getId());
        dbWorkThis = db.child("work").child(currentUser.getUid()).child(project.getId());
    }

    private void fetchUser() {
        dbUsersMe.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                user = dataSnapshot.getValue(User.class);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(mContext, getResources().getString(R.string.error_no_data),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Construct the list of work
     */
    private void initiateInvoicesList() {
        // Construct the list
        RecyclerView.LayoutManager manager = new LinearLayoutManager(mContext);
        invoicesList.setLayoutManager(manager);

        inProgress(true);
    }

    /**
     * Fill the list with work
     */
    private void populateInvoicesList() {
        inProgress(true);

        // Create an adapter
        FirebaseRecyclerAdapter<Invoice, ProjectInvoicesFragment.InvoiceRow> adapter =
                new FirebaseRecyclerAdapter<Invoice, ProjectInvoicesFragment.InvoiceRow>(
                        Invoice.class,
                        R.layout.list_item_invoice,
                        ProjectInvoicesFragment.InvoiceRow.class,
                        dbInvoicesThis
                ) {
                    @Override
                    protected void populateViewHolder(final ProjectInvoicesFragment.InvoiceRow row,
                                                      final Invoice invoice, int position) {
                        // Fill the row
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
                        checkAmount(invoicesList.getAdapter().getItemCount());
                    }
                };

        // Set the adapter
        invoicesList.setAdapter(adapter);

        // Update amount in list
        checkAmount(invoicesList.getAdapter().getItemCount());

        inProgress(false);
    }

    /**
     * View holder for a project row
     */
    public static class InvoiceRow extends RecyclerView.ViewHolder {
        View view;

        public InvoiceRow(View view) {
            super(view);
            this.view = view;
        }

        public void setDate(String date) {
            TextView tvDate = view.findViewById(R.id.invoice_date);

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

        public void setInvoiceNr(String invoiceNr) {
            TextView tvInvoiceNr = view.findViewById(R.id.invoice_nr);
            tvInvoiceNr.setText("#" + invoiceNr);
        }

        public void setPrice(double price) {
            TextView tvPrice = view.findViewById(R.id.invoice_price);
            DecimalFormat currency = new DecimalFormat("0.00");
            String convertedPrice = "€  " + currency.format(price);
            tvPrice.setText(convertedPrice);
        }

    }

    private void checkAmount(int amount) {
        if (amount == 0) {
            emptyInvoicesList.setVisibility(View.VISIBLE);
            invoicesList.setVisibility(View.INVISIBLE);
            newInvoice.setVisibility(View.GONE);
        } else {
            emptyInvoicesList.setVisibility(View.INVISIBLE);
            invoicesList.setVisibility(View.VISIBLE);
            newInvoice.setVisibility(View.VISIBLE);
        }
    }

    private void inProgress(boolean loading) {
        if (loading) {
            progressWheel.setVisibility(View.VISIBLE);
            invoicesList.setVisibility(View.INVISIBLE);
            emptyInvoicesList.setVisibility(View.INVISIBLE);
            newInvoice.setVisibility(View.INVISIBLE);
        } else {
            progressWheel.setVisibility(View.INVISIBLE);
            invoicesList.setVisibility(View.VISIBLE);
        }
    }

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

                try {
                    mContext.startActivity(intent);
                } catch (ActivityNotFoundException e) {
                    Toast.makeText(mContext, res.getString(R.string.error_no_reader),
                            Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    private void fetchDataAndBuild(final Invoice invoice) {
        db.addValueEventListener(new ValueEventListener() {
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
                GeneratePdf writer = new GeneratePdf(mContext, invoice);
                writer.createFile();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
            }
        });
    }
}
