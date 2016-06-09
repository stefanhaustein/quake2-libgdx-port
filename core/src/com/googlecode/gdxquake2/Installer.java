package com.googlecode.gdxquake2;

import java.io.IOException;
import java.io.Writer;
import java.nio.ByteBuffer;

import com.badlogic.gdx.Files;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap;

import com.googlecode.gdxquake2.core.tools.AsyncFilesystem;
import com.googlecode.gdxquake2.core.tools.AsyncFilesystem.FileTask;
import com.googlecode.gdxquake2.core.tools.Callback;
import com.googlecode.gdxquake2.core.tools.ImageConverter;
import com.googlecode.gdxquake2.core.tools.PCXConverter;
import com.googlecode.gdxquake2.core.tools.PakFile;
import com.googlecode.gdxquake2.core.tools.TGAConverter;
import com.googlecode.gdxquake2.core.tools.Tools;
import com.googlecode.gdxquake2.core.tools.WALConverter;

public class Installer {
  Preferences prefs;
  Tools tools;
  AsyncFilesystem afs;
  Callback<Void> doneCallback;
  ImageConverter pcxConverter = new PCXConverter();
  ImageConverter tgaConverter = new TGAConverter();
  ImageConverter walConverter = new WALConverter();
  StringBuilder imageSizes;

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
    tools.println(msg);
    doneCallback.onFailure(cause);
  }
  
  public void run() {
    afs.getFile("/models/items/ammo/slugs/medium/tris.md2", 
      new Callback<ByteBuffer>() {
        @Override
        public void onSuccess(ByteBuffer result) {
          unpacked();
        }

        @Override
        public void onFailure(Throwable cause) {
          unpack();
        }
      }
    );
  }


  void unpack() {
    tools.println("Unpacking pak0.pak");
    afs.getFile("Install/Data/baseq2/pak0.pak", new Callback<ByteBuffer>() {

      @Override
      public void onSuccess(ByteBuffer result) {
        new PakFile(result).unpack(tools, 
            new Callback<Void>() {

              @Override
              public void onSuccess(Void result) {
                tools.println("pak0.pak successfully unpacked.");
                // forcing convert here instead of calling unpacked
                // because image sizes may stick in local storage
                convert();
              }

              @Override
              public void onFailure(Throwable cause) {
                error("Error unpacking pak0.pak", cause);
              }
            });
        }

      @Override
      public void onFailure(Throwable cause) {
        error("Error accessing pak0.pak", cause);
      }
    });
  }
  
  void unpacked() {
    if (prefs.getString("imageSizes") != null) {
      converted();
    } else {
      convert();
    }
  }


  void convert() {
    tools.println("Converting Images");
    imageSizes = new StringBuilder();
    afs.processFiles("", processor, new Callback<Void>() {
      @Override
      public void onSuccess(Void result) {
        prefs.putString("imageSizes", imageSizes.toString());
        converted();
      }
      @Override
      public void onFailure(Throwable cause) {
        error("Error processing files", cause);
      }
    });
  }


  void convert(final FileTask task, final ImageConverter converter) {
    afs.getFile(task.fullPath, 
        new Callback<ByteBuffer>() {
          @Override
          public void onSuccess(ByteBuffer result) {
            Pixmap image = converter.convert(result);
            imageSizes.append(task.fullPath + "," + image.getWidth() + "," + image.getHeight() + "\n");
            ByteBuffer png = tools.convertToPng(image);
            
            String path = task.fullPath;
            
            afs.saveFile(path.toLowerCase() + ".png", png, 0, png.limit(), task.readyCallback);
          }
          @Override
          public void onFailure(Throwable cause) {
            task.readyCallback.onFailure(cause);
          }
    });
  }
  
  final Callback<FileTask> processor = new Callback<FileTask>() {
    @Override
    public void onSuccess(FileTask result) {
      ImageConverter converter = null;
      String lowerName = result.fullPath.toLowerCase();
      if (lowerName.endsWith(".pcx")) {
        converter = pcxConverter;
      } else if (lowerName.endsWith(".tga")) {
        converter = tgaConverter;
      } else if (lowerName.endsWith(".wal")) {
        converter = walConverter;
      } else {
        tools.println("Skipping: " + result.fullPath);
        result.readyCallback.onSuccess(null);
        return;
      }
      tools.println("Converting: " + result.fullPath);
      convert(result, converter);
    }

    @Override
    public void onFailure(Throwable cause) {
      tools.println("Error: " + cause);
      cause.printStackTrace();
    }
  };
  
  
  

  void converted() {
    tools.println("All files processed!");
    tools.prparationsDone();
    doneCallback.onSuccess(null);
  }
}
