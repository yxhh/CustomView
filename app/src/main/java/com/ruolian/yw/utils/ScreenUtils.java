package com.ruolian.yw.utils;

import android.content.Context;
import android.util.DisplayMetrics;
import android.util.TypedValue;

import com.ruolian.yw.BaseApplication;

public class ScreenUtils {

    private static int screenWidth = -1;
    private static int screenHeight = -1;

    public static int dp2px(int dipValue) {
        float reSize = BaseApplication.getInstance().getResources()
                .getDisplayMetrics().density;
        return (int) ((dipValue * reSize) + 0.5);
    }

    public static int px2dp(int pxValue) {
        float reSize = BaseApplication.getInstance().getResources()
                .getDisplayMetrics().density;
        return (int) ((pxValue / reSize) + 0.5);
    }

    public static float sp2px(int spValue) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, spValue,
                BaseApplication.getInstance().getResources()
                        .getDisplayMetrics());
    }

    public static int px2sp(float pxValue) {
        final float fontScale = BaseApplication.getInstance().getResources().getDisplayMetrics().scaledDensity;
        return (int) (pxValue / fontScale + 0.5f);
    }

    /**
     * 获取屏幕的宽度
     */
    public static int getScreenWith(Context context) {
        if (screenWidth <= 0) {
            DisplayMetrics dm = context.getResources
                    ().getDisplayMetrics();
            screenWidth = dm.widthPixels;
        }

        return screenWidth;
    }

    /****
     * 获取屏幕的高度
     * @param context
     * @return
     */
    public static int getScreenHeight(Context context) {
        if (screenHeight <= 0) {
            DisplayMetrics dm = context.getResources
                    ().getDisplayMetrics();
            screenHeight = dm.heightPixels;
        }
        return screenHeight;
    }

}
