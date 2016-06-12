package com.googlecode.gdxquake2.client;

import com.badlogic.gdx.Gdx;
import com.googlecode.gdxquake2.core.tools.AsyncBlobStorage;
import com.googlecode.gdxquake2.core.tools.Callback;
import com.googlecode.gdxquake2.core.tools.NamedBlob;
import com.googlecode.gdxquake2.core.tools.PlatformTools;
import com.google.gwt.typedarrays.shared.Float32Array;
import com.google.gwt.typedarrays.shared.Float64Array;
import com.google.gwt.typedarrays.shared.Int32Array;
import com.google.gwt.typedarrays.shared.Int8Array;
import com.google.gwt.typedarrays.shared.TypedArrays;

public class GwtTools implements PlatformTools {
  static Int8Array wba = TypedArrays.createInt8Array(8);
  static Int32Array wia = TypedArrays.createInt32Array(wba.buffer(), 0, 2);
  static Float32Array wfa = TypedArrays.createFloat32Array(wba.buffer(), 0, 2);
  static Float64Array wda = TypedArrays.createFloat64Array(wba.buffer(), 0, 1);
  
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
  public void exit(int i) {
    Gdx.app.log("GwtTools", "exit(" + i + ") requested.");
  }
}
