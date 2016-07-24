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
package com.googlecode.gdxquake2.game.gdxadapter;


import java.nio.ByteBuffer;


import com.googlecode.gdxquake2.gdxext.AsyncFileHandle;
import com.googlecode.gdxquake2.GdxQuake2;
import com.googlecode.gdxquake2.game.common.ResourceLoader;
import com.googlecode.gdxquake2.gdxext.Callback;

public class ResourceLoaderImpl implements ResourceLoader.Impl {

  int missing = 0;

  int delay;

  public boolean pump() {
    return missing > 0;
  }

  public void reset() {
  }
  
  public void loadResourceAsync(final String rawPath, final ResourceLoader.Callback callback) {
    missing++;
    
    final String path = rawPath.toLowerCase();
    GdxQuake2.asyncLocalStorage.getFileHandle(path, new Callback<AsyncFileHandle>() {

      @Override
      public void onSuccess(AsyncFileHandle result) {
        missing--;
        callback.onSuccess(ByteBuffer.wrap(result.readBytes()));
      }

      @Override
      public void onFailure(Throwable cause) {
        missing--;
        System.err.println("ResourceLoader.onFailure: " + cause);
      }});
    
  }

}
