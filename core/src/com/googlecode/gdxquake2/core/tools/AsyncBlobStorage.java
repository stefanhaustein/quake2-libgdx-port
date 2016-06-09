package com.googlecode.gdxquake2.core.tools;

import java.nio.ByteBuffer;

/**
 * Special-purpose async filesystem interface for unpacking 
 * @author haustein
 */
public interface AsyncBlobStorage {

  void saveFile(String path, ByteBuffer data, Callback<String> readyCallback);

  class FileTask {
    public String fullPath;
    public Callback<Void> readyCallback;
  }
  
  void getFile(String filename, Callback<ByteBuffer> callback);



}
