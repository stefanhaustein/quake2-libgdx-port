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

import com.badlogic.gdx.Gdx;
import com.googlecode.gdxquake2.gdxext.Callback;
import com.googlecode.gdxquake2.gdxext.ZipEntry;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;


public class PakFile implements Runnable {
  static final int SIZE = 64;
  static final int NAME_SIZE = 56;
  private static final int MAX_FILES_IN_PACK = 4096;
  private static final int IDPAKHEADER = 
      (('K' << 24) + ('C' << 16) + ('A' << 8) + 'P');

  private ByteBuffer packhandle;
  int numpackfiles;
  private Callback<ZipEntry> dataCallback;
  private Callback<Void> readyCallback;
  private int index;
  
  public PakFile(ByteBuffer packhandle, Callback<ZipEntry> dataCallback, Callback<Void> readyCallback) {
    this.packhandle = packhandle;
    this.packhandle.order(ByteOrder.LITTLE_ENDIAN);

    int ident = packhandle.getInt();
    int dirofs = packhandle.getInt();
    int dirlen = packhandle.getInt();

    if (ident != IDPAKHEADER) {
       throw new RuntimeException("Data is not a packfile. ident: " + Integer.toHexString(ident) + " expected: " + Integer.toHexString(IDPAKHEADER));
    }
  
    numpackfiles = dirlen / SIZE;

    if (numpackfiles > MAX_FILES_IN_PACK) {
        throw new RuntimeException("This pakfile has " + numpackfiles + " files");
    }

    packhandle.position(dirofs);
    this.dataCallback = dataCallback;
    this.readyCallback = readyCallback;
  }
  

  public void run() {
    if (numpackfiles == 0) {
      readyCallback.onSuccess(null);
      return;
    }
    numpackfiles--;

    int savedLimit = packhandle.limit();
    byte[] tmpText = new byte[NAME_SIZE];
    packhandle.get(tmpText);

    int cut = 0;
    while (cut < tmpText.length && tmpText[cut] > ' ') {
      cut++;
    }

    final String name = new String(tmpText, 0, cut).toLowerCase();

    int filepos = packhandle.getInt();
    int filelen = packhandle.getInt();

    int savedPos = packhandle.position();

    packhandle.position(filepos);
    packhandle.limit(filelen + filepos);
    final ByteBuffer slice = packhandle.slice();
    packhandle.limit(savedLimit);
    packhandle.position(savedPos);

    Gdx.app.postRunnable(new Runnable() {
      @Override
      public void run() {
        dataCallback.onSuccess(new ZipEntry(name, slice));
      }
    });

    Gdx.app.postRunnable(this);
  }
}
