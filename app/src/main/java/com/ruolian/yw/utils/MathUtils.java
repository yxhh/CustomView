package com.ruolian.yw.utils;

/**
 * @author yangwang
 * @date 18-6-15 14:16
 * @company Beijing QiaoData Management Co.
 * @projectName CustomView
 * @packageName com.ruolian.yw.utils
 */
public class MathUtils {
    public static float sin(float angle) {
        return ((int) (Math.sin(angle * 3.141592653 / 180) * 1000 + 5)) / 1000f;
    }

    public static float cos(float angle) {
        return ((int) (Math.cos(angle * 3.141592653 / 180) * 1000 + 5)) / 1000f;
    }
}
