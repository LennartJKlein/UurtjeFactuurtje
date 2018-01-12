package nl.lennartklein.uurtjefactuurtje;

import android.content.Context;
import android.content.Intent;
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
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.github.clans.fab.FloatingActionButton;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.json.JSONArray;

import java.util.ArrayList;

/**
 * An overview of projects and recent costs
 */
public class OverviewFragment extends Fragment {

    // Global references
    private Context mContext;
    private RecyclerView projectsList;

    private DatabaseReference db;
    private DatabaseReference db_projects;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_overview, container, false);

        // Set global references
        mContext = getActivity().getApplicationContext();
        projectsList = view.findViewById(R.id.list_projects);
        db = FirebaseDatabase.getInstance().getReference();
        db_projects = db.child("projects");

        initiateProjectsList();

        // Floating action button
        initiateActionButton(view);

        return view;
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

        // Initialise the list of projects
        populateProjectsList();
    }

    /**
     * Make menu of floating action button clickable
     */
    private void initiateActionButton(View view) {
        FloatingActionButton add_work = view.findViewById(R.id.action_add_work);
        add_work.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(mContext, "Scherm voor toevoegen werk", Toast.LENGTH_SHORT).show();
            }
        });

        FloatingActionButton add_cost = view.findViewById(R.id.action_add_cost);
        add_cost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(mContext, "Scherm voor toevoegen kosten", Toast.LENGTH_SHORT).show();
            }
        });

        FloatingActionButton add_project = view.findViewById(R.id.action_add_project);
        add_project.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(mContext, "Scherm voor toevoegen project", Toast.LENGTH_SHORT).show();
            }
        });
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
    }

    /**
     * Fill the list with projects
     */
    private void populateProjectsList() {
        // Create an adapter
        FirebaseRecyclerAdapter<Project, ProjectViewHolder> adapter = new FirebaseRecyclerAdapter<Project, ProjectViewHolder>(
                Project.class,
                R.layout.list_item_project,
                ProjectViewHolder.class,
                db_projects
        ) {
            @Override
            protected void populateViewHolder(ProjectViewHolder viewHolder, Project model, int position) {
                viewHolder.setName(model.getName());
                viewHolder.setCompany(model.getCompany_name());
                String format = getResources().getString(R.string.placeholder_date_invoice);
                String format_last_invoice = String.format(format, model.getLast_invoice());
                viewHolder.setInvoice(format_last_invoice);

                viewHolder.model_view.setOnClickListener(new View.OnClickListener(){
                    @Override
                    public void onClick(View view) {
                        startActivity(new Intent(mContext, ProjectActivity.class));
                    }
                });
            }

        };

        // Set the adapter
        projectsList.setAdapter(adapter);
    }

    /**
     * View holder for a Project
     */
    public static class ProjectViewHolder extends RecyclerView.ViewHolder {

        View model_view;

        public ProjectViewHolder(View itemView) {
            super(itemView);
            model_view = itemView;
        }

        public void setName(String name) {
            TextView tvName = model_view.findViewById(R.id.project_name);
            tvName.setText(name);
        }

        public void setCompany(String company_name) {
            TextView tvCompany = model_view.findViewById(R.id.project_company);
            tvCompany.setText(company_name);
        }

        public void setInvoice(String last_invoice) {
            TextView tvInvoice = model_view.findViewById(R.id.project_last_invoice);
            tvInvoice.setText(last_invoice);
        }
    }

}
