package com.googlecode.gdxquake2.client;

import java.nio.ByteBuffer;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Pixmap;
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
  public Sound decodeWav(ByteBuffer data) {
    throw new RuntimeException("NYI");
  }

  @Override
  public Pixmap decodePng(ByteBuffer data) {
    throw new RuntimeException("NYI");
  }

  @Override
  public ByteBuffer encodePng(Pixmap pixmap) {
/*    CanvasElement canvasElement = (CanvasElement) ((HtmlImage) image).imageElement();
    String data = canvasElement.toDataURL(null);
    int cut = data.indexOf(',');
    String decoded = atob(data.substring(cut + 1));
    ByteBuffer buf = ByteBuffer.allocate(decoded.length());
    for (int i = 0; i < decoded.length(); i++) {
      buf.put(i, (byte) decoded.charAt(i));
    }
    return buf;*/
    throw new RuntimeException("NYI");
  }

  @Override
  public void unzip(String url, Callback<NamedBlob> dataCallback, Callback<Void> readyCallback) {

  }

  @Override
  public native void println(String text) /*-{
    $wnd.console.log(text);
    var log = $doc.getElementById("log");
    if (log) {
      log.textContent += text +"\n"
      $doc.getElementById("log-bottom").scrollIntoView();
    }
  }-*/;

  @Override
  public final int floatToIntBits(float f) {
      wfa.set(0, f);
      return wia.get(0);
  }

  @Override
  public final float intBitsToFloat(int i) {
      wia.set(0, i);
      return wfa.get(0);
  }

  @Override
  public void exit(int i) {
    println("exit(" + i + ") requested.");
  }
}
