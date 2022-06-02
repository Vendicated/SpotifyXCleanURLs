package dev.vendicated.xposed.spotify;

import android.content.ClipData;
import android.net.Uri;
import android.util.Log;

import de.robv.android.xposed.*;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class SpotifyCleanUrls implements IXposedHookLoadPackage {
    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        if (!lpparam.packageName.equals("com.spotify.music")) return;

        XposedBridge.hookMethod(Uri.Builder.class.getDeclaredMethod("appendQueryParameter", String.class, String.class), new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) {
                var key = (String) param.args[0];
                if ("si".equals(key) || key.startsWith("utm_"))
                    param.setResult(param.thisObject);
            }
        });

        // For some reason ^ does not catch it so shrug
        XposedBridge.hookMethod(ClipData.class.getDeclaredMethod("newPlainText", CharSequence.class, CharSequence.class), new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                var text = (String) param.args[1];
                if (text.startsWith("https://open")) {
                    param.args[1] = text.replaceFirst("\\?(si|utm_.+?)=.+", "");
                }
            }
        });
    }
}
