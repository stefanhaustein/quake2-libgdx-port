package com.googlecode.gdxquake2.gdxext;

import com.badlogic.gdx.Gdx;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.nio.ByteBuffer;
import java.util.Enumeration;
import java.util.zip.ZipFile;


public class UnZip implements Runnable {
  final String url;
  final Callback<ZipEntry> dataCallback;
  final Callback<Void> readyCallback;
  final ProgressTracker progressTracker;
  Enumeration<? extends java.util.zip.ZipEntry> entries;
  InputStream is;
  OutputStream os;
  byte[] buf = new byte[65536];
  File tmpFile;
  ZipFile zipFile;
  int zipFileSize;
  int downloaded;

  public UnZip(String url, Callback<ZipEntry> dataCallback, Callback<Void> readyCallback, ProgressTracker progressTracker) {
    this.url = url;
    this.dataCallback = dataCallback;
    this.readyCallback = readyCallback;
    this.progressTracker = progressTracker;
  }

  @Override
  public void run() {
    try {
      if (entries == null) {
        if (tmpFile == null) {
          progressTracker.action = "Connecting";
          progressTracker.file = url;
          progressTracker.callback.run();
          URLConnection connection = new URL(url).openConnection();
          zipFileSize = connection.getContentLength();
          downloaded = 0;
          is = connection.getInputStream();
          tmpFile = File.createTempFile("tmp", ".zip");
          os = new FileOutputStream(tmpFile);
        } else {
          progressTracker.action = "Downloading " + (zipFileSize < 0 ? (downloaded + " bytes") : (((100L * downloaded) / zipFileSize) + "%"));
          progressTracker.callback.run();
          int count = is.read(buf);
          if (count > 0) {
            downloaded += count;
            os.write(buf, 0, count);
          } else {
            zipFile = new ZipFile(tmpFile);
            entries = zipFile.entries();
            progressTracker.total += zipFile.size();
          }
        }
      } else if (!entries.hasMoreElements()) {
        zipFile.close();
        tmpFile.delete();
        readyCallback.onSuccess(null);
        return;
      } else {
        java.util.zip.ZipEntry zipEntry = entries.nextElement();
        progressTracker.action = "Unpacking";
        progressTracker.file = zipEntry.getName();
        progressTracker.callback.run();
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
