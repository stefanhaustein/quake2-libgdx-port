package com.googlecode.gdxquake2.client;

import com.googlecode.gdxquake2.GdxQuake2;
import com.googlecode.gdxquake2.PlatformTools;

public class GwtTools implements PlatformTools {

  @Override
  public native void log(String s) /*-{
    $wnd.console.log(s);
  }-*/;

  @Override
  public void exit(int i) {
    GdxQuake2.tools.log("exit(" + i + ") requested.");
  }
}
