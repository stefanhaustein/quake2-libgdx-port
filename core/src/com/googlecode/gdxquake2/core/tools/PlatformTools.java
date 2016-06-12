package com.googlecode.gdxquake2.core.tools;

public interface PlatformTools {
  AsyncBlobStorage asyncBlobStorage();

  void unzip(final String url, Callback<NamedBlob> dataCallback, Callback<Void> readyCallback);
  void exit(int i);

}
