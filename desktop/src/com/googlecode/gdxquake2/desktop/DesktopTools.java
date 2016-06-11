package com.googlecode.gdxquake2.desktop;

import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import com.badlogic.gdx.Files;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.backends.lwjgl.audio.OpenALAudio;
import com.badlogic.gdx.backends.lwjgl.audio.Wav;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap;
import com.googlecode.gdxquake2.core.tools.AsyncBlobStorage;
import com.googlecode.gdxquake2.core.tools.Callback;
import com.googlecode.gdxquake2.core.tools.NamedBlob;
import com.googlecode.gdxquake2.core.tools.PlatformTools;

import javax.imageio.ImageIO;


public class DesktopTools implements PlatformTools {

  class Unzipper implements Runnable {
    final String url;
    final Callback<NamedBlob> dataCallback;
    final Callback<Void> readyCallback;
    Enumeration<? extends ZipEntry> entries;
    InputStream is;
    OutputStream os;
    byte[] buf = new byte[65536];
    File tmpFile;
    ZipFile zipFile;

    Unzipper(String url, Callback<NamedBlob> dataCallback, Callback<Void> readyCallback) {
      this.url = url;
      this.dataCallback = dataCallback;
      this.readyCallback = readyCallback;
    }

    @Override
    public void run() {
      try {
        if (entries == null) {
          if (tmpFile == null) {
            is = new URL(url).openConnection().getInputStream();
            tmpFile = File.createTempFile("tmp", ".zip");
            os = new FileOutputStream(tmpFile);
          } else {
            int count = is.read(buf);
            if (count > 0) {
              os.write(buf, 0, count);
            } else {
              zipFile = new ZipFile(tmpFile);
              entries = zipFile.entries();
            }
          }
        } else if (!entries.hasMoreElements()) {
          zipFile.close();
          tmpFile.delete();
          readyCallback.onSuccess(null);
          return;
        } else {
          ZipEntry zipEntry = entries.nextElement();
          byte[] data = new byte[(int) zipEntry.getSize()];
          DataInputStream dis = new DataInputStream(zipFile.getInputStream(zipEntry));
          dis.readFully(data);
          dis.close();
          dataCallback.onSuccess(new NamedBlob(zipEntry.getName(), ByteBuffer.wrap(data)));
        }
        Gdx.app.postRunnable(this);
      } catch (IOException e) {
        readyCallback.onFailure(e);
      }
    }
  }

  DesktopAsyncBlobStore fileSystem = new DesktopAsyncBlobStore();
  int wavCount = 0;

  @Override
  public AsyncBlobStorage asyncBlobStorage() {
    return fileSystem;
  }

  @Override
  public Sound decodeWav(ByteBuffer data) {
    try {
      FileHandle tmp = Gdx.files.local("tmp-" + (wavCount++) + ".wav");
      OutputStream os = tmp.write(false);
      for (int i = 0; i < data.capacity(); i++) {
        os.write(data.get(i));
      }
      os.close();
      Sound result = Gdx.audio.newSound(tmp);
      tmp.delete();
      return result;
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
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
    Gdx.app.postRunnable(new Unzipper(url, dataCallback, readyCallback));
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
