package com.googlecode.gdxquake2;

import android.util.Log;

public class AndroidTools implements PlatformTools {

    @Override
    public void log(String s) {
        Log.i("GdxQ2", s);
    }

    @Override
    public void exit(int i) {
        Log.d("GdxQ2", "Ignoring exit() call -- not expected on Android.", new Exception());
    }
}
