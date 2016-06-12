package com.googlecode.gdxquake2.android;

import com.badlogic.gdx.Gdx;
import com.googlecode.gdxquake2.GdxQuake2;
import com.googlecode.gdxquake2.core.tools.AsyncBlobStorage;
import com.googlecode.gdxquake2.core.tools.Callback;
import com.googlecode.gdxquake2.core.tools.NamedBlob;
import com.googlecode.gdxquake2.core.tools.PlatformTools;

import java.io.*;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;


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
    int downloaded = 0;

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
            GdxQuake2.showInitStatus("Server connected");
          } else {
            int count = is.read(buf);
            if (count > 0) {
              os.write(buf, 0, count);
              downloaded += count;
              GdxQuake2.showInitStatus(downloaded / 1024 / 1024 + " (of 40) MB downloaded.");
            } else {
              os.close();
              is.close();
              GdxQuake2.showInitStatus("Donwnload complete, unpacking.");
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

  @Override
  public AsyncBlobStorage asyncBlobStorage() {
    return fileSystem;
  }

  @Override
  public void unzip(final String url, final Callback<NamedBlob> dataCallback, final Callback<Void> readyCallback) {
    Gdx.app.postRunnable(new Unzipper(url, dataCallback, readyCallback));
  }

  @Override
  public void log(String s) {
    System.out.println(s);
  }

  @Override
  public void exit(int i) {
    System.exit(i);
  }
}
