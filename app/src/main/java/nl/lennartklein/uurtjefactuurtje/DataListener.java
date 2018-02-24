package nl.lennartklein.uurtjefactuurtje;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.wearable.DataClient;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class DataListener extends WearableListenerService {

    private static final String KEY = "nl.lennartklein.uurtjefactuurtje.";
    private static final String PROJECTS_KEY = KEY + "projects";
    private static final String WORK_KEY = KEY + "work";
    private DataClient dataClient;
    private int dataCounter = 0;

    ArrayList<DataMap> projects = new ArrayList<>();

    @Override
    public void onCreate() {
        super.onCreate();

        dataClient = Wearable.getDataClient(this);
    }

    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {
        Log.d("DataLayer", "Phone hears something");
        for (DataEvent event : dataEvents) {
            DataItem item = event.getDataItem();

            if (event.getType() == DataEvent.TYPE_CHANGED) {
                if (item.getUri().getPath().equals("/projects")) {
                    // do something
                }
                if (item.getUri().getPath().equals("/work")) {
                    // do something
                }
            } else if (event.getType() == DataEvent.TYPE_DELETED) {
                Log.d("DataLayer", item.toString());
            }
        }
    }

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        final String message = new String(messageEvent.getData());
        Log.d("DataLayer", "Message path received is: " + messageEvent.getPath());
        Log.d("DataLayer", "Message received is: " + message);

        if (messageEvent.getPath().equals("/message")) {
            tellActivity(message);
        }
        else if (messageEvent.getPath().equals("/request")) {
            fetchProjects();
        }
        else {
            super.onMessageReceived(messageEvent);
        }
    }

    private void fetchProjects() {
        PersistentDatabase.getReference()
                .child("projects")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot projectSnapshot : dataSnapshot.getChildren()) {
                    Project project = projectSnapshot.getValue(Project.class);

                    if (project != null) {
                        projects.add(project.getDataMap());
                        Log.d("DataLayer", "From firebase: " + project.getName());
                    }
                }

                PutDataMapRequest dataMap = PutDataMapRequest.create("/projects");
                dataMap.getDataMap().putDataMapArrayList(PROJECTS_KEY, projects);
                dataMap.getDataMap().putInt("dataCounter", 15);

                PutDataRequest request = dataMap.asPutDataRequest();
                request.setUrgent();
                Task<DataItem> dataItemTask = dataClient.putDataItem(request);

                dataItemTask.addOnSuccessListener(new OnSuccessListener<DataItem>() {
                    @Override
                    public void onSuccess(DataItem dataItem) {
                        Log.d("DataLayer", "Sending projects to wear was successful: " + dataItem);
                    }
                });
                dataItemTask.addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d("DataLayer", String.format("Error sending projects to wear (error code = %s)", e));
                    }
                });

                //sendDataToWearable("projects", projects);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d("DataLayer", "No projects found on Firebase");
            }
        });
    }

    private void sendDataToWearable(final String target, ArrayList<DataMap> data) {
        PutDataMapRequest dataMap = PutDataMapRequest.create("/" + target);
        dataMap.getDataMap().putDataMapArrayList(KEY + target, data);
        dataMap.getDataMap().putInt("dataCounter", dataCounter++);

        PutDataRequest request = dataMap.asPutDataRequest();
        request.setUrgent();
        Task<DataItem> dataItemTask = dataClient.putDataItem(request);

        dataItemTask.addOnSuccessListener(new OnSuccessListener<DataItem>() {
            @Override
            public void onSuccess(DataItem dataItem) {
                Log.d("DataLayer", "Sending projects to wear was successful: " + dataItem);
            }
        });
        dataItemTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d("DataLayer", String.format("Error sending projects to wear (error code = %s)", e));
            }
        });
    }

    private void tellActivity(String message) {
        Intent messageIntent = new Intent();
        messageIntent.setAction(Intent.ACTION_SEND);
        messageIntent.putExtra("message", message);
        LocalBroadcastManager.getInstance(this).sendBroadcast(messageIntent);
    }
}