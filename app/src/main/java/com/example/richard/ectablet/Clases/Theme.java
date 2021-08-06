package com.example.richard.ectablet.Clases;

import android.app.Activity;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;

import com.example.richard.ectablet.R;

public class Theme {
    private static int sTheme;
    public final static int THEME_DAY = 0;
    public final static int THEME_NIGHT = 1;

    public static void changeToThemeDay(Activity activity, int theme)
    {

    }
    /** Set the theme of the activity, according to the configuration. */
    public static void onActivityCreateSetTheme(Activity activity)
    {
        switch (sTheme)
        {
            default:
            case THEME_DAY:
                activity.setTheme(R.style.AppTheme);
                break;
            case THEME_NIGHT:
                activity.setTheme(R.style.AppTheme);
                break;
        }
    }
}
