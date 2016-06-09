package com.googlecode.gdxquake2.core.tools;

import java.nio.ByteBuffer;

/**
 * Special-purpose async filesystem interface for unpacking 
 * @author haustein
 */
public interface AsyncFilesystem {

  class FileTask {
    public String fullPath;
    public Callback<Void> readyCallback;
  }
  
  void getFile(String filename, Callback<ByteBuffer> callback);
  
  void saveFile(String filename, ByteBuffer data, int offet, int len, Callback<Void> callback);

  /**
   * Processes all files in the given directory recursively and invoke processCallback on them.
   * When all files have been processed, readyCallback is called.
   */
  void processFiles(String dirName, Callback<FileTask> processCallback, Callback<Void> readyCallback);

}
