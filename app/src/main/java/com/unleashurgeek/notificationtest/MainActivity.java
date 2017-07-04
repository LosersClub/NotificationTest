package com.unleashurgeek.notificationtest;

import android.app.PendingIntent;
import android.content.ClipData;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.app.RemoteInput;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.Toast;
import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import de.greenrobot.event.EventBus;

import java.util.Arrays;
import java.util.Stack;

/**
 * Created by Kyle on 6/28/2017.
 */
public class MainActivity extends AppCompatActivity {
    public static final String TAG = "MainActivity";
    private  static final String EXTRA_VOICE_REPLY = "extra_voice_reply";
    private AdapterNotificationList adapter;
    private Stack<NotificationWear> notificationsStack = new Stack<>();

    @InjectView(R.id.recyclerView)
    RecyclerView recyclerNotifList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.inject(this);
        recyclerNotifList.setLayoutManager(new LinearLayoutManager(this));
        updateRecycler();
    }

    public void onEvent(NotificationWear notificationWear) {
        notificationsStack.push(notificationWear);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                updateRecycler();
            }
        });
    }

    private void updateRecycler() {
        adapter = new AdapterNotificationList(
                Arrays.asList(notificationsStack.toArray(new NotificationWear[notificationsStack.size()])));
        recyclerNotifList.setAdapter(adapter);
        recyclerNotifList.setItemAnimator(new DefaultItemAnimator());
    }

    @OnClick(R.id.buttonReply) void replyTolastNotification() {
        if (notificationsStack.isEmpty()) {
            Toast.makeText(this, "No Notification", Toast.LENGTH_LONG).show();
            return;
        }

        NotificationWear notificationWear = notificationsStack.pop();
        if (notificationWear == null) {
            Toast.makeText(this, "No Notification", Toast.LENGTH_LONG).show();
            return;
        }

        Intent localIntent = new Intent();
        Bundle localBundle = new Bundle();

        Log.e(TAG, "NOTIFICATION WEAR TEST: " + notificationWear.toString());

        RemoteInput[] remotes = notificationWear.action.getRemoteInputs();
        for (int i = 0; i < remotes.length; i++) {
            getDetailsOfNotification(remotes[i]);
            localBundle.putCharSequence(remotes[i].getResultKey(), "Test Message");
        }
        localIntent.putExtra("android.remoteinput.resultsData", localBundle);
        RemoteInput.addResultsToIntent(remotes, localIntent, localBundle);
        try {
            notificationWear.pendingIntent.send(this.getApplicationContext(), 0, localIntent);
            notificationWear.pendingIntent.send(this.getApplicationContext(), 0, localIntent);
            notificationWear.pendingIntent.send(this.getApplicationContext(), 0, localIntent);
        } catch (PendingIntent.CanceledException e) {
            Log.e(TAG, "replyToLastNotificationException: " + e.getLocalizedMessage());
        }
        updateRecycler();
    }

    private void getDetailsOfNotification(RemoteInput remoteInput) {
        String resultKey = remoteInput.getResultKey();
        String label = remoteInput.getLabel().toString();
        Boolean canFreeForm = remoteInput.getAllowFreeFormInput();
        StringBuilder sb = new StringBuilder();
        sb.append("Result Key: " + resultKey);
        sb.append("\nLabel: " + label);
        sb.append("\ncanFreeForm: " + canFreeForm);
        sb.append("\nPossible Choices: ");
        if (remoteInput.getChoices() != null && remoteInput.getChoices().length > 0) {
            String[] possibleChoices = new String[remoteInput.getChoices().length];
            for (int i = 0; i < remoteInput.getChoices().length; i++) {
                possibleChoices[i] = remoteInput.getChoices()[i].toString();
            }
            sb.append(Arrays.toString(possibleChoices));
        }

        Log.e(TAG, "DETAILS FOR REMOTEINPUT " + remoteInput + ": " + sb.toString());
    }

    @OnClick(R.id.buttonRandomNotif) void sendRandomNotification() {
        Intent intent = new Intent(this, MainActivity.class);
        String[] replyChoices = { "yes", "no" };

        RemoteInput remoteInput = new RemoteInput.Builder(EXTRA_VOICE_REPLY)
                .setLabel("Label").setChoices(replyChoices).build();

        NotificationCompat.Action action = new NotificationCompat.Action.Builder(R.mipmap.ic_launcher,
                "Get Input", PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT))
                .addRemoteInput(remoteInput).build();

        NotificationCompat.WearableExtender wearableExtender = new NotificationCompat.WearableExtender()
                .addAction(action);

        intent.putExtra("our_passed_id", "12345");
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Builder notifcationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher).extend(wearableExtender).setAutoCancel(true)
                .setContentTitle("Random Notification")
                .setContentText("Appears!")
                .setContentIntent(contentIntent);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(888, notifcationBuilder.build());
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }

        if (Settings.Secure.getString(this.getContentResolver(), "enabled_notification_listeners") != null) {
            if (!Settings.Secure.getString(this.getContentResolver(), "enabled_notification_listeners")
                    .contains(getApplicationContext().getPackageName())) {
                getApplicationContext().startActivity(
                        new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS")
                                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
            }
        } else {
            Log.d(TAG, "onResume no Google Play Services");
        }
        displayTextToast();
    }

    private void displayTextToast() {
        String textFromInput = null;
        try {
            textFromInput = getMessageText(getIntent()).toString();
        } catch (Exception e) {}

        if (textFromInput != null && textFromInput.length() > 0) {
            Toast.makeText(this, textFromInput, Toast.LENGTH_LONG).show();
        }
    }

    private CharSequence getMessageText(Intent intent) {
        String reply = "";
        if (intent.getExtras() != null) {
            reply = intent.getExtras().getString(EXTRA_VOICE_REPLY);
        }
        Log.d(TAG, "getMessageText reply " + reply);
        Bundle remoteInput = RemoteInput.getResultsFromIntent(intent);
        if (remoteInput != null) {
            return remoteInput.getCharSequence(EXTRA_VOICE_REPLY);
        }
        return null;
    }

    @Override
    protected void onDestroy() {
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }
}
