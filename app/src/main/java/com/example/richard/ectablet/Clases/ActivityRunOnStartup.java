package com.example.richard.ectablet.Clases;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.example.richard.ectablet.Activity.LoginVerificationActivity;

public class ActivityRunOnStartup extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
            Intent i = new Intent(context, LoginVerificationActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(i);
        }
    }
}
