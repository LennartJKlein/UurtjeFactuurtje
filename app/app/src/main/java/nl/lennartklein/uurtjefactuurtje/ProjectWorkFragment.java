package nl.lennartklein.uurtjefactuurtje;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
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
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import static android.content.ContentValues.TAG;

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
    private Button newInvoice;

    // Database references
    private DatabaseReference db;
    private DatabaseReference dbUsersMe;
    private DatabaseReference dbInvoicesMe;
    private DatabaseReference dbCompaniesMe;
    private DatabaseReference dbWorkMe;

    // Data
    private Project project;
    private User user;

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
        dbCompaniesMe = db.child("companies").child(currentUser.getUid());
        dbWorkMe = db.child("work").child(currentUser.getUid());
    }

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
     * Fill the list with work
     */
    private void populateWorkList() {
        inProgress(true);

        // Create an adapter
        FirebaseRecyclerAdapter<Work, ProjectWorkFragment.WorkRow> adapter =
                new FirebaseRecyclerAdapter<Work, ProjectWorkFragment.WorkRow>(
                        Work.class,
                        R.layout.list_item_work,
                        ProjectWorkFragment.WorkRow.class,
                        dbWorkMe.child(project.getId()).child("unpaid")
                ) {
                    @Override
                    protected void populateViewHolder(final ProjectWorkFragment.WorkRow row, Work work, int position) {
                        // Fill the row
                        row.setDate(work.getDate());
                        row.setDescription(work.getDescription());
                        row.setHours(work.getHours(), mContext.getResources());
                        row.setPrice(work.getPrice());

                        inProgress(false);

                        // Update amount in list
                        checkAmount(workList.getAdapter().getItemCount());
                    }
                };

        // Set the adapter
        workList.setAdapter(adapter);

        // Update amount in list
        checkAmount(workList.getAdapter().getItemCount());

        inProgress(false);
    }

    /**
     * View holder for a project row
     */
    public static class WorkRow extends RecyclerView.ViewHolder {
        View view;

        public WorkRow(View view) {
            super(view);
            this.view = view;
        }

        public void setDate(String date) {
            TextView tvDate = view.findViewById(R.id.work_date);

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
            TextView tvDescription = view.findViewById(R.id.work_description);
            tvDescription.setText(description);
        }

        public void setHours(double hours, Resources res) {
            TextView tvHours = view.findViewById(R.id.work_hours);
            if (hours > 0) {
                String convertedHours = String.valueOf(hours);
                convertedHours = res.getString(R.string.placeholder_hours, convertedHours);
                tvHours.setText(convertedHours);
            } else {
                tvHours.setVisibility(View.GONE);
            }
        }

        public void setPrice(double price) {
            TextView tvPrice = view.findViewById(R.id.work_price);
            DecimalFormat currency = new DecimalFormat("0.00");
            String convertedPrice = "€  " + currency.format(price);
            tvPrice.setText(convertedPrice);
        }

    }

    private void checkAmount(int amount) {
        if (amount == 0) {
            emptyWorkList.setVisibility(View.VISIBLE);
            workList.setVisibility(View.INVISIBLE);
            newInvoice.setVisibility(View.GONE);
        } else {
            emptyWorkList.setVisibility(View.INVISIBLE);
            workList.setVisibility(View.VISIBLE);
            newInvoice.setVisibility(View.VISIBLE);
        }
    }

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

}
