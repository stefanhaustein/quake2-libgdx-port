package com.googlecode.gdxquake2;


import java.nio.ByteBuffer;

public interface PlatformImage {
    void setArgb(int x, int y, int argb);
    void setArgb(int x, int y, int w, int h, int[] argbArray, int offset, int scanSize);
    ByteBuffer toPng();

    int getWidth();
    int getHeight();

    int getArgb(int x, int y);
}
