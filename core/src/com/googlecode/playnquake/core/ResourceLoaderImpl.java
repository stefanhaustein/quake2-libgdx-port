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
package com.googlecode.playnquake.core;


import java.io.IOException;
import java.nio.ByteBuffer;


import com.googlecode.gdxquake2.GdxQuake2;
import com.googlecode.playnquake.core.common.ResourceLoader;
import com.googlecode.playnquake.core.tools.Callback;

public class ResourceLoaderImpl implements ResourceLoader.Impl {

  int missing = 0;
  
  public boolean pump() {
    return missing > 0;
  }

  public void reset() {
  }
  
  public void loadResourceAsync(final String rawPath, final ResourceLoader.Callback callback) {
    missing++;
    
    final String path = rawPath.toLowerCase();
    System.out.println("Requesting resource: " + path);
    
    GdxQuake2.tools().getFileSystem().getFile(path, new Callback<ByteBuffer>() {

      @Override
      public void onSuccess(ByteBuffer result) {
        missing--;
        callback.onSuccess(result);
      }

      @Override
      public void onFailure(Throwable cause) {
        missing--;
        System.err.println("ResourceLoader.onFailure: " + cause);
      }});
    
  }

}
