package com.googlecode.gdxquake2.core.tools;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Pixmap;

import java.nio.ByteBuffer;

public interface PlatformTools {
  AsyncBlobStorage asyncBlobStorage();

  Sound decodeWav(ByteBuffer data);
  Pixmap decodePng(ByteBuffer data);
  ByteBuffer encodePng(Pixmap pixmap);

  void unzip(final String url, Callback<NamedBlob> dataCallback, Callback<Void> readyCallback);

  void println(String text);
  float intBitsToFloat(int i);
  int floatToIntBits(float f);
  void exit(int i);

}
