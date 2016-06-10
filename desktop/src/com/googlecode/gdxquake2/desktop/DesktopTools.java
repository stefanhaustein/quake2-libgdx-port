package com.googlecode.gdxquake2.desktop;

import java.io.*;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import com.badlogic.gdx.Gdx;
import com.googlecode.gdxquake2.PlatformImage;
import com.googlecode.gdxquake2.core.tools.AsyncBlobStorage;
import com.googlecode.gdxquake2.core.tools.Callback;
import com.googlecode.gdxquake2.core.tools.NamedBlob;
import com.googlecode.gdxquake2.core.tools.PlatformTools;


public class DesktopTools implements PlatformTools {
	
  DesktopAsyncBlobStore fileSystem = new DesktopAsyncBlobStore("data");
  
  @Override
  public AsyncBlobStorage getFileSystem() {
    return fileSystem;
  }

  @Override
  public PlatformImage createImage(int width, int height) {
    return new DesktopImage(width, height);
  }

  @Override
  public void unzip(final String url, final Callback<NamedBlob> dataCallback, final Callback<Void> readyCallback) {
    Runnable runnable = new Runnable() {
      public final void run() {
        try {
          InputStream is = new URL(url).openConnection().getInputStream();
          File f = File.createTempFile("tmp", ".zip");
          OutputStream os = new FileOutputStream(f);
          byte[] buf = new byte[65536];
          while (true) {
            int count = is.read(buf);
            if (count <= 0) {
              break;
            }
            os.write(buf, 0, count);
          }

          ZipFile zipFile = new ZipFile(f);
          Enumeration<? extends ZipEntry> entries = zipFile.entries();
          while (entries.hasMoreElements()) {
            ZipEntry zipEntry = entries.nextElement();
            if (zipEntry == null) {
              break;
            }
            byte[] data = new byte[(int) zipEntry.getSize()];
            DataInputStream dis = new DataInputStream(zipFile.getInputStream(zipEntry));
            dis.readFully(data);
            dis.close();
            dataCallback.onSuccess(new NamedBlob(zipEntry.getName(), ByteBuffer.wrap(data)));
          }
          zipFile = null;
          f.delete();
        } catch (IOException e) {
          readyCallback.onFailure(e);
          return;
        }
        readyCallback.onSuccess(null);
      }
    };
    Gdx.app.postRunnable(runnable);
//    runnable.run();
//        new Thread(runnable).start();
  }

  @Override
  public void println(String text) {
    System.out.println(text);
  }

  @Override
  public float intBitsToFloat(int i) {
    return Float.intBitsToFloat(i);
  }

  @Override
  public int floatToIntBits(float f) {
    return Float.floatToIntBits(f);
  }

  @Override
  public void exit(int i) {
    System.exit(i);
  }

  @Override
  public void prparationsDone() {
    // TODO Auto-generated method stub
    
  }



}
