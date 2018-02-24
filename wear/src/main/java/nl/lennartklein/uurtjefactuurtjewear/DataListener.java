package nl.lennartklein.uurtjefactuurtjewear;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

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

import java.util.ArrayList;

public class DataListener extends WearableListenerService {

    private static final String KEY = "nl.lennartklein.uurtjefactuurtje.";
    private static final String PROJECTS_KEY = KEY + "projects";
    private static final String WORK_KEY = KEY + "work";
    private DataClient dataClient;
    private int dataCounter = 0;

    @Override
    public void onCreate() {
        super.onCreate();

        dataClient = Wearable.getDataClient(this);
    }

    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {
        Log.d("DataLayer", "Wear hears something");
        for (DataEvent event : dataEvents) {
            DataItem item = event.getDataItem();

            if (event.getType() == DataEvent.TYPE_CHANGED) {
                Log.d("DataLayer", item.getUri().getPath());
                switch (item.getUri().getPath()) {
                    case "/projects":
                        DataMap dataMap = DataMapItem.fromDataItem(item).getDataMap();

                        Intent messageIntent = new Intent();
                        messageIntent.setAction(Intent.ACTION_SEND);
                        messageIntent.putExtra("message", "projects");
                        messageIntent.putExtra("projects", dataMap.getDataMapArrayList(PROJECTS_KEY));
                        LocalBroadcastManager.getInstance(this).sendBroadcast(messageIntent);
                        break;

                    case "/work":
                        break;
                }
            } else if (event.getType() == DataEvent.TYPE_DELETED) {
                Log.d("DataLayer", item.toString());
            }
        }
    }

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        final String message = new String(messageEvent.getData());
        Log.v("DataLayer", "Message path received on wear is: " + messageEvent.getPath());
        Log.v("DataLayer", "Message received on wear is: " + message);

        if (messageEvent.getPath().equals("/message")) {
            tellActivity("message", message);
        }
        else if (messageEvent.getPath().equals("/request")) {
            // do something
        }
        else {
            super.onMessageReceived(messageEvent);
        }
    }

    private void sendDataToPhone(final String target, ArrayList<DataMap> data) {
        PutDataMapRequest dataMap = PutDataMapRequest.create("/" + target);
        dataMap.getDataMap().putDataMapArrayList(KEY + target, data);
        dataMap.getDataMap().putInt("dataCounter", dataCounter++);

        PutDataRequest request = dataMap.asPutDataRequest();
        request.setUrgent();
        Task<DataItem> dataItemTask = dataClient.putDataItem(request);

        dataItemTask.addOnSuccessListener(new OnSuccessListener<DataItem>() {
            @Override
            public void onSuccess(DataItem dataItem) {
                Log.d("DataLayer", "Sending "+ target +" was successful: " + dataItem);
            }
        });
        dataItemTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d("DataLayer", String.format("Error sending "+ target +" (error code = %s)", e));
            }
        });
    }

    private void tellActivity(String action, String message) {
        Intent messageIntent = new Intent();
        messageIntent.setAction(Intent.ACTION_SEND);
        messageIntent.putExtra(action, message);
        LocalBroadcastManager.getInstance(this).sendBroadcast(messageIntent);
    }
}