/*
Copyright (C) 2010 Copyright 2010 Google Inc.

This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either version 2
of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.

See the GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
*/
package com.googlecode.gdxquake2.installer;

import java.nio.ByteBuffer;

import com.badlogic.gdx.graphics.Pixmap;
import com.googlecode.gdxquake2.game.common.QuakeImage;

public abstract class ImageConverter {

  protected static class image_t {
    int width, height;
    byte[] pix;
  }

//  private static HashMap<String, Converter> converters = new HashMap<String, Converter>();
//
//  public static Converter get(String name) {
//    int idx = name.lastIndexOf('.');
//    if (idx != -1) {
//      return converters.get(name.substring(idx + 1).toLowerCase());
//    }
//    return null;
//  }


  public abstract Pixmap convert(ByteBuffer raw);

  static Pixmap makeImage(image_t source) {
	Pixmap image = new Pixmap(source.width, source.height, Pixmap.Format.RGBA8888);
	int ofs = 0;
	for (int y = 0; y < source.height; y++) {
      for (int x = 0; x < source.width; x++) {
          int rgba = (((source.pix[ofs] & 255) << 24) |
                  ((source.pix[ofs + 1] & 255) << 16) |
                  ((source.pix[ofs + 2] & 255) << 8)|
                  ((source.pix[ofs + 3] & 255)));
          ofs += 4;
          image.drawPixel(x, y, rgba);
      }
	}

    return image;
  }

  static Pixmap makePalletizedImage(image_t source) {
    Pixmap image = new Pixmap(source.width, source.height, Pixmap.Format.RGBA8888);
    for (int y = 0; y < source.height; ++y) {
      for (int x = 0; x < source.width; ++x) {
        int ofs = source.pix[y * source.width + x];
        if (ofs < 0) {
          ofs += 256;
        }
        int rgba = (ofs == 255) ? 0 : QuakeImage.PALETTE_RGBA[ofs];
        image.drawPixel(x, y, rgba);
      }
    }
    return image;
  }
}
