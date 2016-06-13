package com.googlecode.gdxquake2.installer;

import java.nio.ByteBuffer;

import com.badlogic.gdx.Gdx;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.PixmapIO;
import com.googlecode.gdxquake2.gdxext.AsyncFileHandle;
import com.googlecode.gdxquake2.gdxext.Callback;
import com.googlecode.gdxquake2.GdxQuake2;
import com.googlecode.gdxquake2.gdxext.UnZip;
import com.googlecode.gdxquake2.gdxext.ZipEntry;

public class Installer implements Runnable {
  Callback<Void> doneCallback;
  ImageConverter pcxConverter = new PCXConverter();
  ImageConverter tgaConverter = new TGAConverter();
  ImageConverter walConverter = new WALConverter();
  String url;
  boolean failed = false;
  int pending = 0;

  public Installer(String url, Callback<Void> doneCallback) {
    this.doneCallback = doneCallback;
    this.url = url;
  }

  void error(String msg, Throwable cause) {
    failed = true;
    GdxQuake2.showInitStatus(msg);
    doneCallback.onFailure(cause);
  }

  Callback await() {
    pending++;
    return new Callback() {
      @Override
      public void onSuccess(Object result) {
          System.out.println("Success: " + result + " pending: " + pending);
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
        await());
    Gdx.app.postRunnable(unZip);
  }


  void processFile(String path, ByteBuffer data) {
    path = path.toLowerCase();
    if (path.endsWith(".pak")) {
      GdxQuake2.showInitStatus("Unpacking: " + path);
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
          GdxQuake2.showInitStatus("Skipping: " + path);
        } else {
          GdxQuake2.showInitStatus("Extracting: " + path);
          AsyncFileHandle fileHandle = GdxQuake2.asyncLocalStorage.createFileHandle(GdxQuake2.PATH_PREFIX + path);
          fileHandle.addCommitListener((Callback<AsyncFileHandle>) await());
          fileHandle.writeBuffer(data, false);
        }
        return;
      }
      GdxQuake2.showInitStatus("Converting: " + path);
      convert(path, converter, data);
    }
  }


  void unpack(String path, ByteBuffer data) {
    GdxQuake2.showInitStatus("Unpacking pak file");
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
    Gdx.app.postRunnable(pakFile);
  }


  void convert(String path, final ImageConverter converter, ByteBuffer data) {
    Pixmap image = converter.convert(data);
    GdxQuake2.imageSizes.putInteger(path, image.getWidth() * 10000 + image.getHeight());
    AsyncFileHandle fileHandle = GdxQuake2.asyncLocalStorage.createFileHandle(GdxQuake2.PATH_PREFIX + path + ".png");
    fileHandle.addCommitListener((Callback<AsyncFileHandle>) await());
    PixmapIO.writePNG(fileHandle, image);
  }

}
