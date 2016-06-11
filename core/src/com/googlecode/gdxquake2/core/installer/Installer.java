package com.googlecode.gdxquake2.core.installer;

import java.io.IOException;
import java.io.Writer;
import java.nio.ByteBuffer;

import com.badlogic.gdx.Files;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;

import com.badlogic.gdx.graphics.Pixmap;
import com.googlecode.gdxquake2.GdxQuake2;
import com.googlecode.gdxquake2.core.tools.*;
import com.googlecode.gdxquake2.core.converter.ImageConverter;
import com.googlecode.gdxquake2.core.converter.PCXConverter;
import com.googlecode.gdxquake2.core.converter.TGAConverter;
import com.googlecode.gdxquake2.core.converter.WALConverter;

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
    GdxQuake2.tools.unzip(url,
        new Callback<NamedBlob>() {
           @Override
           public void onSuccess(NamedBlob result) {
             processFile(result.name, result.data);
           }
           @Override
           public void onFailure(Throwable cause) {
             error("Unzip failed", cause);
           }
        },
        await());
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
        GdxQuake2.tools.asyncBlobStorage().saveFile(path, data, await());
        return;
      }
      GdxQuake2.showInitStatus("Converting: " + path);
      convert(path, converter, data);
    }
  }


  void unpack(String path, ByteBuffer data) {
    GdxQuake2.showInitStatus("Unpacking pak file");
    Callback<NamedBlob> extractedCallback = new Callback<NamedBlob>() {
      @Override
      public void onSuccess(NamedBlob result) {
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
    ByteBuffer png = GdxQuake2.tools.encodePng(image);
    GdxQuake2.tools.asyncBlobStorage().saveFile(path + ".png", png, await());
  }

}
