package com.googlecode.gdxquake2.gdxext;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.HasArrayBufferView;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.BufferUtils;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.typedarrays.shared.ArrayBufferView;
import com.google.gwt.typedarrays.shared.Int8Array;
import com.google.gwt.typedarrays.shared.Uint8Array;
import com.googlecode.gdxquake2.GdxQuake2;

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

  public void getFileHandle(final String path, final Callback<AsyncFileHandle> callback) {
    GdxQuake2.tools.log("getFileHandle: " + path);
    if (db != null) {
      GdxQuake2.tools.log("db != null, calling impl");
      getFileHandleImpl(path, new Callback<Int8Array>() {
        @Override
        public void onSuccess(Int8Array result) {
          GdxQuake2.tools.log("getFileHandle.onSuccess for " + path);
          // TODO(haustein): Add a way to wrap an ArrayBuffer w/o copy.
          ByteBuffer buffer = BufferUtils.newByteBuffer(result.length()); // ByteBuffer.wrap(result.buffer()); // BufferUtils.newByteBuffer(result.byteLength());

          // :-(
          for (int i = 0; i < result.length(); i++) {
            buffer.put(i, result.get(i));
          }

          AsyncFileHandle fileHandle = new AsyncFileHandle(AsyncLocalStorage.this, path);
          fileHandle.data = buffer;
          callback.onSuccess(fileHandle);
        }

        @Override
        public void onFailure(Throwable cause) {
          GdxQuake2.tools.log("getFileHandle.onFailure for " + path);
          // Throwable will be null from JS
          callback.onFailure(new IOException("IDB read error"));
        }
      });
    } else if (error) {
      GdxQuake2.tools.log("db error");
      callback.onFailure(new IOException("Database intialization failure"));
    } else {
      GdxQuake2.tools.log("db == null, posting for later");
      Gdx.app.postRunnable(new Runnable() {
        @Override
        public void run() {
          getFileHandle(path, callback);
        }
      });
    }
  }

  public native void getFileHandleImpl(String path, Callback<Int8Array> callback) /*-{
    $wnd.console.log("getFileHandleImpl enter " + path);
    var db = this.@com.googlecode.gdxquake2.gdxext.AsyncLocalStorage::db;
    var trans = db.transaction(["blobs"], "readonly");
    var objectStore = trans.objectStore("blobs");
    var request = objectStore.get(path);
    $wnd.console.log("getFileHandleImpl get() was called " + path);
    trans.oncomplete = function(event) {
       var result = request.result;
       $wnd.console.log("getFileHandleImpl callback success for " + path + " result: ", result);
       callback.@com.googlecode.gdxquake2.gdxext.Callback::onSuccess(Ljava/lang/Object;)(result);
    }
    trans.onerror = function() {
       $wnd.console.log("getFileHandleImpl transaction error success for " + path);
    }
  }-*/;
}
