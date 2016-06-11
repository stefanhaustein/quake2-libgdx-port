package com.googlecode.gdxquake2;

import java.io.IOException;
import java.io.Writer;
import java.nio.ByteBuffer;

import com.badlogic.gdx.Files;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;

import com.badlogic.gdx.graphics.Pixmap;
import com.googlecode.gdxquake2.core.tools.*;
import com.googlecode.gdxquake2.core.converter.ImageConverter;
import com.googlecode.gdxquake2.core.converter.PCXConverter;
import com.googlecode.gdxquake2.core.converter.TGAConverter;
import com.googlecode.gdxquake2.core.converter.WALConverter;

public class Installer {
  Callback<Void> doneCallback;
  ImageConverter pcxConverter = new PCXConverter();
  ImageConverter tgaConverter = new TGAConverter();
  ImageConverter walConverter = new WALConverter();
  boolean failed = false;
  int pending = 0;

  public Installer(Callback<Void> doneCallback) {
    this.doneCallback = doneCallback;

    try {
      FileHandle marker = Gdx.files.getFileHandle("libgdx-local-marker.txt", Files.FileType.Local);
      Writer writer = marker.writer(false, "utf-8");
      writer.write("Hello World\n");
      writer.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  void error(String msg, Throwable cause) {
    failed = true;
    GdxQuake2.tools.println(msg);
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
    GdxQuake2.tools.unzip("http://commondatastorage.googleapis.com/quake2demo/q2-314-demo-x86.exe",
        new Callback<NamedBlob>() {
           @Override
           public void onSuccess(NamedBlob result) {
             processFile(result.name, result.data);
           }
           @Override
           public void onFailure(Throwable cause) {
           }
        },
        await());
  }


  void processFile(String path, ByteBuffer data) {
    path = path.toLowerCase();
    if (path.endsWith(".pak")) {
      GdxQuake2.tools.println("Unpacking: " + path);
      unpack(path, data);
    } else if (path.endsWith(".wav")) {
      GdxQuake2.tools().asyncBlobStorage().saveFile(path, data, await());
    } else {
      ImageConverter converter = null;
      if (path.endsWith(".pcx")) {
        converter = pcxConverter;
      } else if (path.endsWith(".tga")) {
        converter = tgaConverter;
      } else if (path.endsWith(".wal")) {
        converter = walConverter;
      } else {
        // tools.println("Skipping: " + path);
        return;
      }
      GdxQuake2.tools.println("Converting: " + path);
      convert(path, converter, data);
    }
  }


  void unpack(String path, ByteBuffer data) {
    GdxQuake2.tools.println("Unpacking pak file");
    final String prefix = path.substring(0, path.lastIndexOf("/") + 1);

    new PakFile(data).unpack(GdxQuake2.tools,
        new Callback<NamedBlob>() {
          @Override
          public void onSuccess(NamedBlob result) {
            processFile(prefix + result.name, result.data);
          }

          @Override
          public void onFailure(Throwable cause) {
            error("Error unpacking pak file", cause);
          }
      }, await());
  }


  void convert(String path, final ImageConverter converter, ByteBuffer data) {
    Pixmap image = converter.convert(data);
    GdxQuake2.imageSizes.putInteger(path, image.getWidth() * 10000 + image.getHeight());
    ByteBuffer png = GdxQuake2.tools.encodePng(image);
    GdxQuake2.tools().asyncBlobStorage().saveFile(path + ".png", png, await());
  }

}
