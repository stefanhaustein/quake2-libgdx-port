package com.googlecode.gdxquake2.core.tools;

import java.nio.ByteBuffer;

import com.badlogic.gdx.graphics.Pixmap;

public interface Tools {
  AsyncFilesystem getFileSystem();
  ByteBuffer convertToPng(Pixmap image);
  void println(String text);
  float intBitsToFloat(int i);
  int floatToIntBits(float f);
  void prparationsDone();
  void exit(int i);
}
