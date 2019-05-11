////////////////////////////////////////////////////////////////////////////////
// Title        ProjectWorkFragment
// Parent       ProjectActivity
//
// Date         February 1 2018
// Author       Lennart J Klein  (info@lennartklein.nl)
// Project      UurtjeFactuurtje
// Assignment   App Studio, University of Amsterdam
////////////////////////////////////////////////////////////////////////////////

package nl.lennartklein.uurtjefactuurtje;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
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
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.text.DecimalFormat;

/**
 * A page of all the unpaid work of a project
 */
public class ProjectWorkFragment extends Fragment implements View.OnClickListener {

    // Authentication
    private FirebaseAuth auth;
    private FirebaseUser currentUser;

    // UI references
    private Context mContext;
    private Resources res;
    private ProgressBar progressWheel;
    private RecyclerView workList;
    private TextView emptyWorkList;
    private RelativeLayout listTotals;
    private TextView tvTotalHours;
    private TextView tvTotalPrice;
    private Button newInvoice;
    private Button addWork;

    // Database references
    private DatabaseReference db;
    private DatabaseReference dbUsersMe;
    private DatabaseReference dbWorkMe;

    // Data
    private Project project;
    private User user;
    private double totalHours;
    private double totalPrice;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_project_work, container, false);

        setAuth();

        setReferences();

        // Set UI references
        mContext = getActivity();
        res = mContext.getResources();
        progressWheel = view.findViewById(R.id.list_loader);
        workList = view.findViewById(R.id.list_work);
        emptyWorkList = view.findViewById(R.id.list_work_empty);
        listTotals = view.findViewById(R.id.list_totals);
        tvTotalHours = view.findViewById(R.id.list_totals_hours);
        tvTotalPrice = view.findViewById(R.id.list_totals_price);
        addWork = view.findViewById(R.id.action_add_work);
        addWork.setOnClickListener(this);
        newInvoice = view.findViewById(R.id.action_create_invoice);
        newInvoice.setOnClickListener(this);

        // Get data
        project = (Project) getArguments().getSerializable("PROJECT");
        fetchUser();

        initiateWorkList();

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        // Initialise the list of work
        populateWorkList();
    }

    @Override
    public void onResume() {
        super.onResume();

        // Refresh the list of work
        populateWorkList();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.action_create_invoice:
                ProjectActivity parent = (ProjectActivity) getActivity();
                parent.createNewInvoice();
                newInvoice.setVisibility(View.INVISIBLE);
                break;
            case R.id.action_add_work:
                startWorkActivity();
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

    /**
     * Sets the database references
     */
    private void setReferences() {
        db = PersistentDatabase.getReference();
        dbUsersMe = db.child("users").child(currentUser.getUid());
        dbWorkMe = db.child("work").child(currentUser.getUid());
    }

    /**
     * Get user data from database
     */
    private void fetchUser() {
        dbUsersMe.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                user = dataSnapshot.getValue(User.class);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(mContext,
                        getResources().getString(R.string.error_no_data), Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Construct the list of work
     */
    private void initiateWorkList() {
        // Construct the list
        RecyclerView.LayoutManager manager = new LinearLayoutManager(mContext);
        workList.setLayoutManager(manager);

        inProgress(true);
    }

    /**
     * Fills the list with work
     */
    private void populateWorkList() {
        inProgress(true);

        resetTotals();

        // Create an adapter
        FirebaseRecyclerAdapter<Work, WorkItem> adapter =
                new FirebaseRecyclerAdapter<Work, WorkItem>(
                        Work.class,
                        R.layout.list_item_work,
                        WorkItem.class,
                        dbWorkMe.child(project.getId()).child("unpaid").orderByChild("date")
                ) {
                    @Override
                    protected void populateViewHolder(final WorkItem row, final Work work, int position) {
                        // Fill the row
                        row.setDate(work.getDate());
                        row.setDescription(work.getDescription());
                        row.setHours(work.getHours(), mContext.getResources());
                        row.setPrice(work.getPrice());
                        work.setId(getRef(position).getKey());
                        row.actionEdit.setOnClickListener(new View.OnClickListener(){
                            @Override
                            public void onClick(View view) {
                               startEditActivity(work);
                            }
                        });

                        inProgress(false);

                        totalHours += work.getHours();
                        totalPrice += work.getPrice();

                        // Update amount in list
                        checkAmount(workList.getAdapter().getItemCount());

                        // Set totals on bottom of list
                        setTotals(totalHours, totalPrice);
                    }
                };

        // Set the adapter
        workList.setAdapter(adapter);

        // Update amount in list
        checkAmount(workList.getAdapter().getItemCount());

        inProgress(false);
    }

    private void setTotals(double totalHours, double totalPrice) {
        DecimalFormat decimalFormat = new DecimalFormat("0.00");

        String convertedHours = String.valueOf(decimalFormat.format(totalHours));
        convertedHours = res.getString(R.string.placeholder_hours, convertedHours);
        tvTotalHours.setText(convertedHours);

        String convertedPrice = "€  " + decimalFormat.format(totalPrice);
        tvTotalPrice.setText(convertedPrice);
    }

    private void resetTotals() {
        totalHours = 0;
        totalPrice = 0;
    }

    /**
     * Updates UI based on items in the list
     */
    private void checkAmount(int amount) {
        if (amount == 0) {
            emptyWorkList.setVisibility(View.VISIBLE);
            workList.setVisibility(View.INVISIBLE);
            newInvoice.setVisibility(View.GONE);
            listTotals.setVisibility(View.INVISIBLE);
        } else {
            emptyWorkList.setVisibility(View.INVISIBLE);
            workList.setVisibility(View.VISIBLE);
            newInvoice.setVisibility(View.VISIBLE);
            listTotals.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Show wheel when in progress
     */
    private void inProgress(boolean loading) {
        if (loading) {
            progressWheel.setVisibility(View.VISIBLE);
            workList.setVisibility(View.INVISIBLE);
            emptyWorkList.setVisibility(View.INVISIBLE);
            newInvoice.setVisibility(View.INVISIBLE);
        } else {
            progressWheel.setVisibility(View.INVISIBLE);
            workList.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Open a screen to edit work of this project
     */
    private void startEditActivity(Work work) {
        if (project != null && work != null) {
            Intent editWorkIntent = new Intent(mContext, EditWorkActivity.class);
            Bundle bundle = new Bundle();
            bundle.putSerializable("PROJECT", project);
            bundle.putSerializable("WORK", work);
            editWorkIntent.putExtras(bundle);
            startActivity(editWorkIntent);
        }
    }

    /**
     * Open a dialog fragment to add work to this project
     */
    private void startWorkActivity() {
        Intent addWorkIntent = new Intent(mContext, AddWorkActivity.class);
        addWorkIntent.putExtra("PROJECT_NAME", project.getName());
        startActivity(addWorkIntent);

    }

}
