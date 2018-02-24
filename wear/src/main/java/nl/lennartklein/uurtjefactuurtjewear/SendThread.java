package nl.lennartklein.uurtjefactuurtjewear;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.Wearable;

import java.util.List;
import java.util.concurrent.ExecutionException;

class SendThread extends Thread {
    String path;
    String message;
    Context context;
    Activity activity;

    //constructor
    SendThread(Context context, Activity activity, String p, String msg) {
        path = p;
        message = msg;
        this.context = context;
        this.activity = activity;
    }

    public void run() {
        Task<List<Node>> nodeListTask =
                Wearable.getNodeClient(context).getConnectedNodes();
        try {
            List<Node> nodes = Tasks.await(nodeListTask);
            for (Node node : nodes) {
                Task<Integer> sendMessageTask =
                        Wearable.getMessageClient(activity).sendMessage(node.getId(), path, message.getBytes());
                try {
                    Integer result = Tasks.await(sendMessageTask);
                    Log.d("DataLayer", "SendThread: message send to " + node.getDisplayName());

                } catch (ExecutionException exception) {
                    Log.d("DataLayer", "Task failed: " + exception);

                } catch (InterruptedException exception) {
                    Log.d("DataLayer", "Interrupt occurred: " + exception);
                }
            }
        } catch (ExecutionException exception) {
            Log.d("DataLayer", "Task failed: " + exception);
        } catch (InterruptedException exception) {
            Log.d("DataLayer", "Interrupt occurred: " + exception);
        }
    }
}