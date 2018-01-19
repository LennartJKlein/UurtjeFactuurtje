package nl.lennartklein.uurtjefactuurtje;

import android.content.Context;
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
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Objects;

/**
 * See relations of this user
 */
public class RelationsFragment extends Fragment implements View.OnClickListener {

    // Authentication
    private FirebaseAuth auth;
    private FirebaseUser currentUser;

    // UI references
    private Context mContext;
    private ProgressBar progressWheel;
    private RecyclerView relationsList;
    private TextView emptyRelationsList;
    private FloatingActionButton addRelationButton;

    // Database references
    private DatabaseReference db;
    private DatabaseReference dbCompaniesMe;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_relations, container, false);

        setAuth();

        // Set database references
        db = FirebaseDatabase.getInstance().getReference();
        dbCompaniesMe = db.child("companies").child(currentUser.getUid());

        // Set global references
        mContext = getActivity().getApplicationContext();
        progressWheel = view.findViewById(R.id.list_loader);
        relationsList = view.findViewById(R.id.list_relations);
        emptyRelationsList = view.findViewById(R.id.list_relations_empty);
        addRelationButton = view.findViewById(R.id.action_add_relation);

        addRelationButton.setOnClickListener(this);

        initiateRelationsList();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        // Initialise the list of relations
        populateRelationsList();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.action_add_relation:
                openRelationFragment();
                break;
        }
    }

    private void openRelationFragment() {
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        AddRelationFragment dialog = new AddRelationFragment();
        dialog.show(transaction, "AddRelation");
    }

    /**
     * Construct the list of relations
     */
    private void initiateRelationsList() {
        // Construct the list
        RecyclerView.LayoutManager manager = new LinearLayoutManager(mContext);
        relationsList.setLayoutManager(manager);

        // Add divider line
        DividerItemDecoration divider = new DividerItemDecoration(relationsList.getContext(),1);
        relationsList.addItemDecoration(divider);
    }

    /**
     * Fill the list with relations
     */
    private void populateRelationsList() {
        inProgress(true);

        // Create an adapter
        FirebaseRecyclerAdapter<Company, RelationRow> adapter =
                new FirebaseRecyclerAdapter<Company, RelationRow>(
                Company.class,
                R.layout.list_item_relation,
                RelationsFragment.RelationRow.class,
                dbCompaniesMe
        ) {
            @Override
            protected void populateViewHolder(RelationRow row, Company company, final int position) {

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

                // Update amount in list
                checkAmount(relationsList.getAdapter().getItemCount());

            }
        };

        // Set the adapter
        relationsList.setAdapter(adapter);

        // Update amount in list
        checkAmount(relationsList.getAdapter().getItemCount());
    }

    /**
     * View holder for a relation row
     */
    public static class RelationRow extends RecyclerView.ViewHolder {
        View view;
        ImageButton actionDelete;

        public RelationRow(View view) {
            super(view);
            this.view = view;
        }

        public void setName(String name) {
            TextView tvName = view.findViewById(R.id.relation_name);
            tvName.setText(name);
        }

        public void setButton() {
            this.actionDelete = view.findViewById(R.id.action_delete);
        }

    }

    private void checkAmount(int amount) {
        inProgress(false);
        if (amount == 0) {
            emptyRelationsList.setVisibility(View.VISIBLE);
            relationsList.setVisibility(View.INVISIBLE);
        } else {
            emptyRelationsList.setVisibility(View.INVISIBLE);
            relationsList.setVisibility(View.VISIBLE);
        }
    }

    private void inProgress(boolean loading) {
        if (loading) {
            progressWheel.setVisibility(View.VISIBLE);
            relationsList.setVisibility(View.INVISIBLE);
        } else {
            progressWheel.setVisibility(View.INVISIBLE);
            relationsList.setVisibility(View.VISIBLE);
        }
    }

    private void setAuth() {
        auth = FirebaseAuth.getInstance();
        currentUser = auth.getCurrentUser();
    }
}
