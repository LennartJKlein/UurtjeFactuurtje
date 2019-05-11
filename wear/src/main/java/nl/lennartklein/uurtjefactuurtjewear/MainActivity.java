package nl.lennartklein.uurtjefactuurtjewear;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.LinearSnapHelper;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SnapHelper;
import android.support.wear.widget.WearableRecyclerView;
import android.support.wearable.activity.WearableActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.wearable.DataClient;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

import java.util.ArrayList;

public class MainActivity extends WearableActivity {

    // UI references
    private Context mContext;
    private WearableRecyclerView projectsList;
    private TextView emptyProjectsList;

    // Data
    private static final String KEY = "nl.lennartklein.uurtjefactuurtje.";
    private static final String PROJECTS_KEY = KEY + "projects";
    private ArrayList<Project> projects;
    private ProjectsAdapter projectsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Set UI references
        mContext = this;
        projectsList = findViewById(R.id.list_projects);
        emptyProjectsList = findViewById(R.id.list_projects_empty);

        // Populate list
        initiateProjectsList();
        populateProjectsList();

        // Enables Always-on
        setAmbientEnabled();
    }

    /**
     * Constructs the list of projects
     */
    private void initiateProjectsList() {
        // Construct the list
        RecyclerView.LayoutManager manager = new LinearLayoutManager(mContext,
                LinearLayoutManager.VERTICAL, false);
        projectsList.setLayoutManager(manager);

        SnapHelper snapper = new LinearSnapHelper();
        snapper.attachToRecyclerView(projectsList);
    }

    /**
     * Fills the list with projects
     */
    private void populateProjectsList() {

        projects = new ArrayList<>();
        projectsAdapter = new ProjectsAdapter(projects);
        projectsList.setAdapter(projectsAdapter);
        Project dummy = new Project("fdf", "Website vernieuwen in Wordpress", "dfdf", "Haze Timmerwerken",
                "2018-07-01", "2018-01-01",  16.00);
        projects.add(dummy);
        dummy = new Project("fdf", "Hosting koeching.nl", "dfdf", "Koeching",
                "2018-07-01", "2018-01-01",  16.00);
        projects.add(dummy);
        dummy = new Project("fdf", "Ontwerp logo", "dfdf", "Excelsior Cothen",
                "2018-07-01", "2018-01-01",  16.00);
        projects.add(dummy);

        projectsAdapter.notifyDataSetChanged();

        // Update amount in list
        checkAmountProjects(projectsList.getAdapter().getItemCount());
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

    public class ProjectsAdapter extends RecyclerView.Adapter<ProjectItem> {

        private ArrayList<Project> projects;

        public ProjectsAdapter(ArrayList<Project> projects) {
            this.projects = projects;
        }

        @Override
        public ProjectItem onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.list_item_project, parent, false);

            return new ProjectItem(itemView);
        }

        @Override
        public void onBindViewHolder(ProjectItem item, int position) {
            Project project = projects.get(position);
            item.setName(project.getName());
            item.setCompany(project.getCompanyName());

            // Set click listener
            item.view.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View view) {
                    populateProjectsList();
                }
            });
        }

        @Override
        public int getItemCount() {
            return projects.size();
        }
    }
}
