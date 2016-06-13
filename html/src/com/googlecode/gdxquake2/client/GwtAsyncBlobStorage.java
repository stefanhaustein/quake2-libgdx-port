package com.googlecode.gdxquake2.client;

import java.nio.ByteBuffer;

import com.badlogic.gdx.Gdx;
import com.google.gwt.core.client.JavaScriptObject;
import com.googlecode.gdxquake2.gdxext.Callback;

public class GwtAsyncBlobStorage implements AsyncBlobStorage {

  JavaScriptObject db;

  public GwtAsyncBlobStorage() {
    init();
  }

  native private void init() /*-{
    var indexedDb = $wnd.indexedDB || $wnd.webkitIdexedDB || $wnd.msIndexedDB;
    var request = indexedDb.open("AsynBlobStorage", 1);
    request.onerror = function (evt) {
      $wnd.console.log("Database error ", evt);
    }
    request.onsuccess = function(evt) {
      var db = evt.target.result;
      $wnd.console.log("Database success ", db, evt);
      this.@com.googlecode.gdxquake2.client.GwtAsyncBlobStorage::db = db;
    }
    request.onupgradeneeded = function(evt) {
      var db = evt.target.result;
      $wnd.console.log("Database upgrade needed", db, evt);
      this.@com.googlecode.gdxquake2.client.GwtAsyncBlobStorage::db = db;
      db.createObjectStore("blobs");
    }
  }-*/;

  @Override
  public void saveFile(final String path, final ByteBuffer data, final Callback<String> readyCallback) {
    if (db == null) {
      Gdx.app.postRunnable(new Runnable() {
        @Override
        public void run() {
          saveFile(path, data, readyCallback);
        }
      });
    } else {
      saveFileImpl(path, data, readyCallback);
    }
  }

  private native void saveFileImpl(String path, ByteBuffer data, Callback<String> readyCallback) /*-{
    var db = this.@com.googlecode.gdxquake2.client.GwtAsyncBlobStorage::db;
    $wnd.console.log("Should save a file here");
  }-*/;


  @Override
  public void getFile(String filename, Callback<ByteBuffer> callback) {

  }
}
