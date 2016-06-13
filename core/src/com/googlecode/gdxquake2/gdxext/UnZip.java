package com.googlecode.gdxquake2.gdxext;

import com.badlogic.gdx.Gdx;

import java.io.*;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.Enumeration;
import java.util.zip.ZipFile;

/**
 * Created by haustein on 13.06.16.
 */
public class UnZip implements Runnable {
  final String url;
  final Callback<ZipEntry> dataCallback;
  final Callback<Void> readyCallback;
  Enumeration<? extends java.util.zip.ZipEntry> entries;
  InputStream is;
  OutputStream os;
  byte[] buf = new byte[65536];
  File tmpFile;
  ZipFile zipFile;

  public UnZip(String url, Callback<ZipEntry> dataCallback, Callback<Void> readyCallback) {
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
        java.util.zip.ZipEntry zipEntry = entries.nextElement();
        byte[] data = new byte[(int) zipEntry.getSize()];
        DataInputStream dis = new DataInputStream(zipFile.getInputStream(zipEntry));
        dis.readFully(data);
        dis.close();
        dataCallback.onSuccess(new ZipEntry(zipEntry.getName(), ByteBuffer.wrap(data)));
      }
      Gdx.app.postRunnable(this);
    } catch (IOException e) {
      readyCallback.onFailure(e);
    }
  }
}
