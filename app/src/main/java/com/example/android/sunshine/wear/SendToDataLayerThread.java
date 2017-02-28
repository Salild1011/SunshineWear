package com.example.android.sunshine.wear;

import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

/**
 * Created by Sanjiv on 30-01-2017.
 */

public class SendToDataLayerThread extends Thread {
    private String mPath;
    private String mMessage;
    private GoogleApiClient mGoogleApiClient;

    // Constructor to send a mMessage to the data layer
    public SendToDataLayerThread(String p, String msg, GoogleApiClient client) {
        mPath = p;
        mMessage = msg;
        mGoogleApiClient = client;
    }

    public void run() {
        NodeApi.GetConnectedNodesResult nodes = Wearable.NodeApi.getConnectedNodes(mGoogleApiClient).await();
        for (Node node : nodes.getNodes()) {
            MessageApi.SendMessageResult result = Wearable.MessageApi
                    .sendMessage(mGoogleApiClient, node.getId(), mPath, mMessage.getBytes()).await();

            if (result.getStatus().isSuccess()) {
                Log.v("myTag", "Message: {" + mMessage + "} sent to: " + node.getDisplayName());
            }
            else {
                Log.v("myTag", "ERROR: failed to send Message");
            }
        }
    }
}
