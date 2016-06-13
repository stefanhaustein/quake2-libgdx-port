package com.googlecode.gdxquake2.gdxext;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.HasArrayBufferView;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.BufferUtils;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.typedarrays.shared.ArrayBuffer;
import com.google.gwt.typedarrays.shared.ArrayBufferView;
import com.google.gwt.typedarrays.shared.TypedArrays;
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
      ByteBuffer data = fileHandle.data;
      if (!(data instanceof HasArrayBufferView)) {
        ByteBuffer b2 = BufferUtils.newByteBuffer(data.capacity());
        b2.put(data);
        data = b2;
      }
      ArrayBufferView arrayBuffer = ((HasArrayBufferView) data).getTypedArray();
      saveFileImpl(fileHandle.path(), arrayBuffer, new Runnable() {
        @Override
        public void run() {
          GdxQuake2.tools.log("Actually seem to have managed to save file: " + fileHandle.path());
          readyCallback.onSuccess(null);
        }
      });
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

  private native void saveFileImpl(String path, ArrayBufferView data, Runnable callback) /*-{
    var db = this.@com.googlecode.gdxquake2.gdxext.AsyncLocalStorage::db;
    var trans = db.transaction(["blobs"], "readwrite");
    trans.oncomplete = function() {
      callback.@java.lang.Runnable::run()();
    }
    trans.onerror = function() {
      $wnd.console.log("Transaction error");
    }
    var objectStore = trans.objectStore("blobs");
    objectStore.put(data, path);
  }-*/;


  public void getFileHandle(String path, Callback<AsyncFileHandle> callback) {

  }
}
