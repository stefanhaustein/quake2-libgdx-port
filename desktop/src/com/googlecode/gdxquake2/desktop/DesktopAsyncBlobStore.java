package com.googlecode.gdxquake2.desktop;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import javax.swing.*;


import com.badlogic.gdx.Gdx;
import com.googlecode.gdxquake2.core.tools.AsyncBlobStorage;
import com.googlecode.gdxquake2.core.tools.Callback;

public class DesktopAsyncBlobStore implements AsyncBlobStorage {
  File root;
  public DesktopAsyncBlobStore(String rootPath) {
    this.root = new File(rootPath);
  }
  
  @Override
  public void getFile(String filename, final Callback<ByteBuffer> callback) {
    final File file = new File(root, filename);
    Runnable runnable = new Runnable() {
      public void run() {
        byte[] data = new byte[(int) file.length()];
        try {
          System.out.println("file: " + file + " size: " + file.length());

          new DataInputStream(new FileInputStream(file)).readFully(data);
          ByteBuffer buf = ByteBuffer.wrap(data);
          buf.order(ByteOrder.LITTLE_ENDIAN);
          callback.onSuccess(buf);

        } catch (IOException e) {
          callback.onFailure(e);
        }
      }
    };
    Gdx.app.postRunnable(runnable);
  }



  @Override
  public void saveFile(final String filename, ByteBuffer data, final Callback<String> callback) {
    final File file = new File(root, filename);
    
    final byte[] array = new byte[data.limit()];
    int pos = data.position();
    data.position(0);
    data.get(array);
    data.position(pos);
    file.getParentFile().mkdirs();

    Runnable runnable = new Runnable() {
      @Override
      public void run() {
        try {
          FileOutputStream fos = new FileOutputStream(file);
          fos.write(array);
          fos.close();
          callback.onSuccess(filename);
        } catch (IOException e) {
          callback.onFailure(e);
        }
      }
    };
    Gdx.app.postRunnable(runnable);
  }

}
