package com.unleashurgeek.notificationtest;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

/**
 * Created by Kyle on 6/28/2017.
 */
public class AdapterNotificationList extends RecyclerView.Adapter<AdapterNotificationList.ViewHolder> {
    private List<NotificationWear> notifications;

    public AdapterNotificationList(List<NotificationWear> notificationWears) {
        this.notifications = notificationWears;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemLayoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_notif_list, null);
        ViewHolder viewHolder = new ViewHolder(itemLayoutView);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.textViewTitle.setText(notifications.get(position).packageName);
    }

    @Override
    public int getItemCount() {
        return notifications.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView textViewTitle;

        public ViewHolder(View itemLayoutView) {
            super(itemLayoutView);
            textViewTitle = (TextView) itemLayoutView.findViewById(R.id.notif_app_title);
        }
    }
}
