package com.googlecode.gdxquake2.core.tools;

import com.googlecode.gdxquake2.PlatformImage;

public interface Tools {
  AsyncBlobStorage getFileSystem();

  PlatformImage createImage(int width, int height);

  void unzip(final String url, Callback<NamedBlob> dataCallback, Callback<Void> readyCallback);

  void println(String text);
  float intBitsToFloat(int i);
  int floatToIntBits(float f);
  void prparationsDone();
  void exit(int i);
}
