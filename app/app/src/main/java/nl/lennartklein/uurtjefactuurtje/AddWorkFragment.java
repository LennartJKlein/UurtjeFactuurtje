package nl.lennartklein.uurtjefactuurtje;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.weiwangcn.betterspinner.library.BetterSpinner;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class AddWorkFragment extends DialogFragment implements View.OnClickListener {

    // Authentication
    private FirebaseAuth auth;
    private FirebaseUser currentUser;

    // Database references
    private DatabaseReference db;
    private DatabaseReference dbProjectsMe;

    // UI references
    private Context mContext;
    private BetterSpinner fieldProject;
    private Button actionInsert;
    private Button actionCancel;

    // Data
    private List<Project> projects;
    private List<String> projectNames;
    private ArrayAdapter<String> projectsAdapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getContext();

        setAuth();

        // Database references
        db = FirebaseDatabase.getInstance().getReference();
        dbProjectsMe = db.child("projects").child(currentUser.getUid());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_work, container, false);

        // Set UI references
        fieldProject = view.findViewById(R.id.field_project);
        actionInsert = view.findViewById(R.id.action_add_work);
        actionCancel = view.findViewById(R.id.action_cancel);

        // Set click listeners
        actionInsert.setOnClickListener(this);
        actionCancel.setOnClickListener(this);

        // Get and set data
        getProjects();

        return view;
    }

    private void setAuth() {
        auth = FirebaseAuth.getInstance();
        currentUser = auth.getCurrentUser();
    }

    private void getProjects() {
        projects = new ArrayList<>();
        projectNames = new ArrayList<>();
        projectsAdapter = new ArrayAdapter<>(mContext, android.R.layout.simple_dropdown_item_1line, projectNames);
        fieldProject.setAdapter(projectsAdapter);

        dbProjectsMe.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                for (DataSnapshot projectSnapshot: dataSnapshot.getChildren()) {
                    Project project = projectSnapshot.getValue(Project.class);

                    if (project != null) {
                        project.setId(projectSnapshot.getKey());

                        projects.add(project);

                        if (project.getName() != null) {
                            projectNames.add(project.getName());
                        }
                    }
                }

                projectsAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d("Data", String.valueOf(databaseError));
            }
        });
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.action_add_work:
                validateFields();
                break;
            case R.id.action_cancel:
                closeFragment();
                break;
        }
    }

    public void validateFields() {

        boolean cancel = false;
        View focusView = null;


        if (cancel) {
            focusView.requestFocus();
        } else {
            insertInDatabase("test", "test", "test");
        }
    }

    public void insertInDatabase(String name, String projectName, String hourPrice) {

        closeFragment();
    }

    public String getDateToday() {
        Calendar c = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        return df.format(c.getTime());
    }

    public void closeFragment() {
        dismiss();
    }
}
