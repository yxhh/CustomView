package com.ruolian.yw;

import android.app.Application;

/**
 * @author yangwang
 * @date 18-6-15 14:20
 * @company Beijing QiaoData Management Co.
 * @projectName CustomView
 * @packageName PACKAGE_NAME
 */
public class BaseApplication extends Application {

    private static BaseApplication APP;


    protected static boolean debug;

    public static BaseApplication getInstance() {
        return APP;
    }

    public void onCreate() {
        super.onCreate();
        APP = this;
    }

    public static boolean isDebug() {
        return debug;
    }

}