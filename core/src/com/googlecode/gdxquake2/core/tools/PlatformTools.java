package com.googlecode.gdxquake2.core.tools;

import com.googlecode.gdxquake2.PlatformImage;

import java.nio.ByteBuffer;

public interface PlatformTools {
  AsyncBlobStorage getFileSystem();

  PlatformImage createImage(int width, int height);
  PlatformImage decodePng(ByteBuffer result);

  void unzip(final String url, Callback<NamedBlob> dataCallback, Callback<Void> readyCallback);

  void println(String text);
  float intBitsToFloat(int i);
  int floatToIntBits(float f);
  void prparationsDone();
  void exit(int i);

}
