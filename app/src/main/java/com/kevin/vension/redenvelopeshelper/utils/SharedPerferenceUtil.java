package com.kevin.vension.redenvelopeshelper.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.kevin.vension.redenvelopeshelper.R;

public class SharedPerferenceUtil {
    private static SharedPreferences sharedPerences = null;
    private static SharedPreferences.Editor editor = null;
    private static SharedPerferenceUtil instance = null;


    protected SharedPerferenceUtil() {
    }

    public static SharedPerferenceUtil getInstance(Context context) {
        if (instance == null && context != null) {
            synchronized (SharedPerferenceUtil.class) {
                instance = new SharedPerferenceUtil();
                initShared(context, context.getString(R.string.app_name));
            }
        }
        return instance;
    }

    private static void initShared(Context ctx, String sharedname) {
        sharedPerences = ctx.getSharedPreferences(sharedname, Context.MODE_PRIVATE);
        editor = sharedPerences.edit();
    }

    public void putLong(String key, long value) {
        editor.putLong(key, value).commit();
    }

    public void putString(String key, String value) {
        editor.putString(key, value).commit();
    }

    public void putFloat(String key, float value) {
        editor.putFloat(key, value).commit();
    }

    public void putBoolean(String key, boolean value) {
        editor.putBoolean(key, value).commit();
    }

    public void putInteger(String key, int value) {
        editor.putInt(key, value).commit();
    }

    public long getLong(String key, long defValue) {
        return sharedPerences.getLong(key, defValue);
    }

    public String getString(String key, String defValue) {
        return sharedPerences.getString(key, defValue);
    }

    public float getFloat(String key, float defValue) {
        return sharedPerences.getFloat(key, defValue);
    }

    public boolean getBoolean(String key, boolean defValue) {
        return sharedPerences.getBoolean(key, defValue);
    }

    public int getInteger(String key, int defValue) {
        return sharedPerences.getInt(key, defValue);
    }

    public void clear() {
        editor.clear();
        editor.commit();
    }
}

