package com.googlecode.gdxquake2.desktop;

import java.io.*;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import com.badlogic.gdx.Gdx;
import com.googlecode.gdxquake2.core.tools.AsyncBlobStorage;
import com.googlecode.gdxquake2.core.tools.Callback;
import com.googlecode.gdxquake2.core.tools.NamedBlob;
import com.googlecode.gdxquake2.core.tools.PlatformTools;



public class DesktopTools implements PlatformTools {

  class Unzipper implements Runnable {
    final String url;
    final Callback<NamedBlob> dataCallback;
    final Callback<Void> readyCallback;
    Enumeration<? extends ZipEntry> entries;
    InputStream is;
    OutputStream os;
    byte[] buf = new byte[65536];
    File tmpFile;
    ZipFile zipFile;

    Unzipper(String url, Callback<NamedBlob> dataCallback, Callback<Void> readyCallback) {
      this.url = url;
      this.dataCallback = dataCallback;
      this.readyCallback = readyCallback;
    }

    @Override
    public void run() {
      try {
        if (entries == null) {
          if (tmpFile == null) {
            is = new URL(url).openConnection().getInputStream();
            tmpFile = File.createTempFile("tmp", ".zip");
            os = new FileOutputStream(tmpFile);
          } else {
            int count = is.read(buf);
            if (count > 0) {
              os.write(buf, 0, count);
            } else {
              zipFile = new ZipFile(tmpFile);
              entries = zipFile.entries();
            }
          }
        } else if (!entries.hasMoreElements()) {
          zipFile.close();
          tmpFile.delete();
          readyCallback.onSuccess(null);
          return;
        } else {
          ZipEntry zipEntry = entries.nextElement();
          byte[] data = new byte[(int) zipEntry.getSize()];
          DataInputStream dis = new DataInputStream(zipFile.getInputStream(zipEntry));
          dis.readFully(data);
          dis.close();
          dataCallback.onSuccess(new NamedBlob(zipEntry.getName(), ByteBuffer.wrap(data)));
        }
        Gdx.app.postRunnable(this);
      } catch (IOException e) {
        readyCallback.onFailure(e);
      }
    }
  }

  DesktopAsyncBlobStore fileSystem = new DesktopAsyncBlobStore();
  int wavCount = 0;

  @Override
  public AsyncBlobStorage asyncBlobStorage() {
    return fileSystem;
  }

  @Override
  public void unzip(final String url, final Callback<NamedBlob> dataCallback, final Callback<Void> readyCallback) {
    Gdx.app.postRunnable(new Unzipper(url, dataCallback, readyCallback));
  }

  @Override
  public void exit(int i) {
    System.exit(i);
  }
}
