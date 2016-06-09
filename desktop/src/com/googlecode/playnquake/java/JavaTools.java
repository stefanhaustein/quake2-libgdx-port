package com.googlecode.playnquake.java;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

import javax.imageio.ImageIO;

import com.badlogic.gdx.graphics.Pixmap;
import com.googlecode.playnquake.core.tools.AsyncFilesystem;
import com.googlecode.playnquake.core.tools.Tools;

public class JavaTools implements Tools {
	
  JavaAsyncFilesystem fileSystem = new JavaAsyncFilesystem("data");
  
  @Override
  public AsyncFilesystem getFileSystem() {
    return fileSystem;
  }

  @Override
  public ByteBuffer convertToPng(Pixmap image) {

    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    try {
      if (1 == 1) throw new IOException("ImageIO.write(javaImage.bufferedImage(), \"PNG\", baos);");
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    byte[] data = baos.toByteArray();
    return ByteBuffer.wrap(data);
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
