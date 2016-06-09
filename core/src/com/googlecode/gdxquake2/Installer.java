package com.googlecode.gdxquake2;

import java.io.IOException;
import java.io.Writer;
import java.nio.ByteBuffer;

import com.badlogic.gdx.Files;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.files.FileHandle;

import com.googlecode.gdxquake2.core.tools.*;
import com.googlecode.gdxquake2.core.converter.ImageConverter;
import com.googlecode.gdxquake2.core.converter.PCXConverter;
import com.googlecode.gdxquake2.core.converter.TGAConverter;
import com.googlecode.gdxquake2.core.converter.WALConverter;

public class Installer {
  Preferences prefs;
  Tools tools;
  AsyncBlobStorage afs;
  Callback<Void> doneCallback;
  ImageConverter pcxConverter = new PCXConverter();
  ImageConverter tgaConverter = new TGAConverter();
  ImageConverter walConverter = new WALConverter();
  StringBuilder imageSizes = new StringBuilder();
  boolean failed = false;
  int pending = 0;

  public Installer(Tools tools, Preferences prefs, Callback<Void> doneCallback) {
    this.tools = tools;
    this.prefs = prefs;
    this.afs = tools.getFileSystem();
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
    tools.println(msg);
    doneCallback.onFailure(cause);
  }

  Callback await() {
    pending++;
    return new Callback() {
      @Override
      public void onSuccess(Object result) {
          pending--;
          if (pending == 0 && !failed) {
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
    tools.unzip("http://commondatastorage.googleapis.com/quake2demo/q2-314-demo-x86.exe",
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
      tools.println("Unpacking: " + path);
      unpack(data);
    } else if (path.endsWith(".wav")) {
      afs.saveFile(path, data, await());
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
      tools.println("Converting: " + path);
      convert(path, converter, data);
    }
  }


  void unpack(ByteBuffer data) {
    tools.println("Unpacking pak file");
    new PakFile(data).unpack(tools,
        new Callback<NamedBlob>() {
          @Override
          public void onSuccess(NamedBlob result) {
            processFile(result.name, result.data);
          }

          @Override
          public void onFailure(Throwable cause) {
            error("Error unpacking pak file", cause);
          }
      }, await());
  }


  void convert(String path, final ImageConverter converter, ByteBuffer data) {
    PlatformImage image = converter.convert(data);
    imageSizes.append(path + "," + image.getWidth() + "," + image.getHeight() + "\n");
    ByteBuffer png = image.toPng();
    afs.saveFile(path + ".png", png, await());
  }

}
