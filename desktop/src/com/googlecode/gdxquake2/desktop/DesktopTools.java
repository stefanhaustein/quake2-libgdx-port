package com.googlecode.gdxquake2.desktop;

import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Pixmap;
import com.googlecode.gdxquake2.core.tools.AsyncBlobStorage;
import com.googlecode.gdxquake2.core.tools.Callback;
import com.googlecode.gdxquake2.core.tools.NamedBlob;
import com.googlecode.gdxquake2.core.tools.PlatformTools;

import javax.imageio.ImageIO;


public class DesktopTools implements PlatformTools {
	
  DesktopAsyncBlobStore fileSystem = new DesktopAsyncBlobStore("data");
  
  @Override
  public AsyncBlobStorage asyncBlobStorage() {
    return fileSystem;
  }

  @Override
  public Pixmap decodePng(final ByteBuffer data) {
    try {
      InputStream is = new InputStream() {
        int pos = 0;
        @Override
        public int read() throws IOException {
          return pos == data.limit() ? -1 : (data.get(pos++) & 255);
        }
      };
      BufferedImage bufferedImage = ImageIO.read(is);
      int w = bufferedImage.getWidth();
      int h = bufferedImage.getHeight();
      Pixmap pixmap = new Pixmap(w, h, Pixmap.Format.RGBA8888);
      for (int y = 0; y < h; y++) {
        for (int x = 0; x < w; x++) {
          int argb = bufferedImage.getRGB(x, y);
          int rgba = (argb << 8) | (argb >>> 24);
          pixmap.drawPixel(x, y, rgba);
        }
      }
      return pixmap;
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public ByteBuffer encodePng(Pixmap pixmap) {
    int w = pixmap.getWidth();
    int h = pixmap.getHeight();
    BufferedImage bufferedImage = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
    for (int y = 0; y < h; y++) {
      for (int x = 0; x < w; x++) {
        int rgba = pixmap.getPixel(x, y);
        int argb = (rgba >>> 8) | (rgba << 24);
        bufferedImage.setRGB(x, y, argb);
      }
    }
    try {
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      ImageIO.write(bufferedImage, "PNG", baos);
      return ByteBuffer.wrap(baos.toByteArray());
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
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
}
