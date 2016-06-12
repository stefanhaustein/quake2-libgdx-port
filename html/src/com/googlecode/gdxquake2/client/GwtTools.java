package com.googlecode.gdxquake2.client;

import com.badlogic.gdx.Gdx;
import com.googlecode.gdxquake2.GdxQuake2;
import com.googlecode.gdxquake2.core.tools.AsyncBlobStorage;
import com.googlecode.gdxquake2.core.tools.Callback;
import com.googlecode.gdxquake2.core.tools.NamedBlob;
import com.googlecode.gdxquake2.core.tools.PlatformTools;

public class GwtTools implements PlatformTools {
  GwtAsyncBlobStorage asyncBlobStorage = new GwtAsyncBlobStorage();

  @Override
  public AsyncBlobStorage asyncBlobStorage() {
    return asyncBlobStorage;
  }

  @Override
  public void unzip(String url, Callback<NamedBlob> dataCallback, Callback<Void> readyCallback) {
    Gdx.app.postRunnable(new GwtUnzip(url, dataCallback, readyCallback));
  }

  @Override
  public native void log(String s) /*-{
    $wnd.console.log(s);
  }-*/;

  @Override
  public void exit(int i) {
    GdxQuake2.tools.log("exit(" + i + ") requested.");
  }
}
