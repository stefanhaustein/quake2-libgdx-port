package com.googlecode.gdxquake2.desktop;

import com.googlecode.gdxquake2.PlatformImage;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

public class DesktopImage implements PlatformImage {
    final BufferedImage bufferedImage;

    DesktopImage(int width, int height) {
        bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
    }

    @Override
    public void setArgb(int x, int y, int argb) {
        bufferedImage.setRGB(x, y, argb);
    }

    @Override
    public void setArgb(int x, int y, int w, int h, int[] argbArray, int offset, int scanSize) {
        bufferedImage.setRGB(x, y, w, h, argbArray, offset, scanSize);
    }

    @Override
    public ByteBuffer toPng() {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(bufferedImage, "PNG", baos);
            return ByteBuffer.wrap(baos.toByteArray());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public int getWidth() {
        return bufferedImage.getWidth();
    }

    @Override
    public int getHeight() {
        return bufferedImage.getHeight();
    }
}
