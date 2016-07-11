package com.googlecode.gdxquake2.installer;

import java.nio.ByteBuffer;

import com.badlogic.gdx.Gdx;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.PixmapIO;
import com.googlecode.gdxquake2.gdxext.*;
import com.googlecode.gdxquake2.GdxQuake2;

public class Installer implements Runnable {
  Callback<Void> doneCallback;
  ImageConverter pcxConverter = new PCXConverter();
  ImageConverter tgaConverter = new TGAConverter();
  ImageConverter walConverter = new WALConverter();
  String url;
  boolean failed = false;
  int pending;
  final ProgressTracker progressTracker;

  public Installer(String url, Callback<Void> doneCallback, ProgressTracker progressTracker) {
    this.doneCallback = doneCallback;
    this.progressTracker = progressTracker;
    this.url = url;
  }

  void error(String msg, Throwable cause) {
    failed = true;
    doneCallback.onFailure(cause);
  }

  Callback await() {
    pending++;
    return new Callback() {
      @Override
      public void onSuccess(Object result) {
        pending--;
        if (pending == 0 && !failed) {
          GdxQuake2.imageSizes.flush();
          doneCallback.onSuccess(null);
        }
      }

      @Override
      public void onFailure(Throwable cause) {
        failed = true;
        doneCallback.onFailure(cause);
      }
    };
  }


  public void run() {
    progressTracker.action = "Downloading";
    progressTracker.file = url;

    UnZip unZip = new UnZip(url,
        new Callback<ZipEntry>() {
           @Override
           public void onSuccess(ZipEntry result) {
             processFile(result.name, result.data);
           }
           @Override
           public void onFailure(Throwable cause) {
             error("Unzip failed", cause);
           }
        },
        await(), progressTracker);
    Gdx.app.postRunnable(unZip);
  }


  void processFile(String path, ByteBuffer data) {
    path = path.toLowerCase();
    progressTracker.processed++;
    progressTracker.file = path;
    if (path.endsWith(".pak")) {
      progressTracker.action = "Unpacking PAK File";
      progressTracker.callback.run();
      unpack(path, data);
    } else {
      ImageConverter converter = null;
      if (path.endsWith(".pcx")) {
        converter = pcxConverter;
      } else if (path.endsWith(".tga")) {
        converter = tgaConverter;
      } else if (path.endsWith(".wal")) {
        converter = walConverter;
      } else {
        if (path.startsWith("install/data/docs/") || path.endsWith(".exe") || path.endsWith(".dll") ||
                path.equals("install/cdrom.spd")) {
          progressTracker.action = "Skipping";
        } else {
          progressTracker.action = "Storing";
          AsyncFileHandle fileHandle = GdxQuake2.asyncLocalStorage.createFileHandle(path);
          fileHandle.addCommitListener((Callback<AsyncFileHandle>) await());
          fileHandle.writeBuffer(data, false);
        }
        progressTracker.callback.run();
        return;
      }
      progressTracker.action = "Converting Image";
      progressTracker.callback.run();
      convert(path, converter, data);
    }
  }


  void unpack(String path, ByteBuffer data) {
    Callback<ZipEntry> extractedCallback = new Callback<ZipEntry>() {
      @Override
      public void onSuccess(ZipEntry result) {
        processFile(result.name, result.data);
      }

      @Override
      public void onFailure(Throwable cause) {
        error("Error unpacking pak file", cause);
      }
    };

    PakFile pakFile = new PakFile(data, extractedCallback, await());
    progressTracker.total += pakFile.numpackfiles;
    Gdx.app.postRunnable(pakFile);
  }


  void convert(String path, final ImageConverter converter, ByteBuffer data) {
    Pixmap image = converter.convert(data);
    GdxQuake2.imageSizes.putInteger(path, image.getWidth() * 10000 + image.getHeight());
    AsyncFileHandle fileHandle = GdxQuake2.asyncLocalStorage.createFileHandle(path + ".png");
    fileHandle.addCommitListener((Callback<AsyncFileHandle>) await());
    PixmapIO.writePNG(fileHandle, image);
  }

}
