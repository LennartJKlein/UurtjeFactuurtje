package nl.lennartklein.uurtjefactuurtje;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.Wearable;

import java.util.List;
import java.util.concurrent.ExecutionException;

public class SendThread extends Thread {
    String path;
    String message;
    Context context;
    Activity activity;
    Handler handler;

    //constructor
    SendThread(Context context, Activity activity, String p, String msg) {
        path = p;
        message = msg;
        this.context = context;
        this.activity = activity;

        // message handler for the send thread
        handler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                Bundle bundle = msg.getData();
                Log.d("DataLayer", bundle.getString("logthis"));
                return true;
            }
        });
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
                    //sendMessage("SendThread: message send to " + node.getDisplayName());
                    Log.d("DataLayer", "SendThread: message send to " + node.getDisplayName());

                } catch (ExecutionException exception) {
                    //sendMessage("SendThread: message failed to" + node.getDisplayName());
                    Log.d("DataLayer", "Send Task failed: " + exception);

                } catch (InterruptedException exception) {
                    Log.d("DataLayer", "Send Interrupt occurred: " + exception);
                }
            }
        } catch (ExecutionException exception) {
            //sendMessage("Node Task failed: " + exception);
            Log.d("DataLayer", "Node Task failed: " + exception);
        } catch (InterruptedException exception) {
            Log.d("DataLayer", "Node Interrupt occurred: " + exception);
        }

    }

    // method to create up a bundle to send to a handler via the thread below.
    public void sendMessage(String text) {
        Bundle b = new Bundle();
        b.putString("logthis", text);
        Message msg = handler.obtainMessage();
        msg.setData(b);
        msg.arg1 = 1;
        msg.what = 1;
        handler.sendMessage(msg);
    }
}