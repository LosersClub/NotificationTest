package com.unleashurgeek.notificationtest;

import android.app.Notification;
import android.app.PendingIntent;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;

import java.util.ArrayList;

/**
 * Created by Kyle on 6/28/2017.
 */
public class NotificationWear {
    public String packageName = "";
    public PendingIntent pendingIntent;
    public ArrayList<Notification> pages = new ArrayList<>();
    public Bundle bundle;
    public String tag = "";
    public NotificationCompat.Action action;

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(packageName + "\n");
        sb.append(" Pending Intent: " + pendingIntent.toString() + "\n");
        sb.append( "\npages: ");
        for (Notification n : pages) {
            sb.append(n.toString() + ",");
        }
        sb.append("\nBundle: " + bundle.toString());
        if (tag != null) {
            sb.append("\ntag: " + tag.toString());
        }
        return sb.toString();
    }
}
