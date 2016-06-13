package com.googlecode.gdxquake2.gdxext;

import java.io.IOException;
import java.nio.ByteBuffer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.google.gwt.core.client.JavaScriptObject;
import com.googlecode.gdxquake2.GdxQuake2;
import com.googlecode.gdxquake2.gdxext.*;

public class AsyncLocalStorage {

  JavaScriptObject db;
  boolean error = false;

  public AsyncLocalStorage() {
    init();
  }

  native private void init() /*-{
    var indexedDb = $wnd.indexedDB || $wnd.webkitIdexedDB || $wnd.msIndexedDB;
    var request = indexedDb.open("AsyncLocalStorage", 1);
    var self = this;
    request.onerror = function (evt) {
      $wnd.console.log("Database error ", evt);
      self.@com.googlecode.gdxquake2.gdxext.AsyncLocalStorage::error = true;
    }
    request.onsuccess = function(evt) {
      var db = evt.target.result;
      $wnd.console.log("Database success ", db, evt);
      self.@com.googlecode.gdxquake2.gdxext.AsyncLocalStorage::db = db;
    }
    request.onupgradeneeded = function(evt) {
      var db = evt.target.result;
      $wnd.console.log("Database upgrade needed", db, evt);
      self.@com.googlecode.gdxquake2.gdxext.AsyncLocalStorage::db = db;
      db.createObjectStore("blobs");
    }
  }-*/;

  public AsyncFileHandle createFileHandle(final String path) {
    AsyncFileHandle result = new AsyncFileHandle(this, path);
    result.delete();
    return result;
  }

  void saveFileHandleImpl(final AsyncFileHandle fileHandle, final Callback<Void> readyCallback) {
    GdxQuake2.tools.log("saveFileHandle: " + fileHandle.path);
    if (db != null) {
      saveFileImpl(fileHandle.path(), fileHandle.data, readyCallback);
    } else if (error) {
      readyCallback.onFailure(new IOException("Database intialization failure"));
    } else {
      Gdx.app.postRunnable(new Runnable() {
          @Override
          public void run() {
            saveFileHandleImpl(fileHandle, readyCallback);
          }
        });
    }
  }

  private native void saveFileImpl(String path, ByteBuffer data, Callback<Void> readyCallback) /*-{
    var db = this.@com.googlecode.gdxquake2.gdxext.AsyncLocalStorage::db;
    $wnd.console.log("Should save a file here: " + path);
  }-*/;


  public void getFileHandle(String path, Callback<AsyncFileHandle> callback) {

  }
}
