package com.unleashurgeek.notificationtest;

import android.app.Notification;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.support.v4.app.NotificationCompat;
import de.greenrobot.event.EventBus;

import java.util.List;

/**
 * Created by Kyle on 6/28/2017.
 */
public class NotificationReceiver extends NotificationListenerService {

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        if (!sbn.isOngoing()) {
            EventBus.getDefault().post(extractWearNotification(sbn));
        }
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        super.onNotificationRemoved(sbn);
    }

    private NotificationWear extractWearNotification(StatusBarNotification sbn) {
        NotificationWear notificationWear = new NotificationWear();
        notificationWear.packageName = sbn.getPackageName();

        NotificationCompat.WearableExtender wearableExtender = new NotificationCompat.WearableExtender(
                sbn.getNotification());
        List<NotificationCompat.Action> actions = wearableExtender.getActions();
        for (NotificationCompat.Action act : actions) {
            if (act != null && act.getRemoteInputs() != null && act.getRemoteInputs().length > 0) {
                notificationWear.action = act;
                notificationWear.pendingIntent = act.actionIntent;
                break;
            }
        }
        List<Notification> pages = wearableExtender.getPages();
        notificationWear.pages.addAll(pages);

        notificationWear.bundle = sbn.getNotification().extras;
        notificationWear.tag = sbn.getTag();
        return notificationWear;
    }
}
