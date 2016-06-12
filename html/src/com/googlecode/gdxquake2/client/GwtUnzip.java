package com.googlecode.gdxquake2.client;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.BufferUtils;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.typedarrays.shared.Uint8Array;
import com.googlecode.gdxquake2.core.tools.Callback;
import com.googlecode.gdxquake2.core.tools.NamedBlob;

import java.nio.ByteBuffer;
import java.nio.HasArrayBufferView;


public class GwtUnzip implements Runnable {

    private final String url;
    private final Callback<NamedBlob> dataCallback;
    private final Callback<Void> doneCallback;
    private JsArray zipEntries;
    private int zipIndex;

    // Hack to avoid JSNI ultra-long parameter specification for
    private String currentName;
    private ByteBuffer currentBuffer;

    GwtUnzip(String url, Callback<NamedBlob> dataCallback, Callback<Void> doneCallback) {
        this.url = url;
        this.dataCallback = dataCallback;
        this.doneCallback = doneCallback;
    }

    public void run() {
        if (zipEntries == null) {
            request(url);
        } else if (zipIndex < zipEntries.length()) {
            processEntry(zipEntries.get(zipIndex++));
        } else {
            doneCallback.onSuccess(null);
        }
    }

    private void postSelf() {
        Gdx.app.postRunnable(this);
    }

    private native void processEntry(JavaScriptObject zipEntry) /*-{
      var zip = $wnd.zip;
      var self = this;

      var fileName = zipEntry.filename;
      if (zipEntry.directory) {
        self.@com.googlecode.gdxquake2.client.GwtUnzip::postSelf()();
        return;
      }

      var ArrayWriter = function(data) {
        this.data = data;
        this.pos = 0;
      };
      ArrayWriter.prototype.init = function(callback) {
        callback();
      };
      ArrayWriter.prototype.writeUint8Array = function(array, callback) {
        this.data.set(array, this.pos);
        this.pos += array.length;
        callback();
      };
      ArrayWriter.prototype.getData = function(callback) {
        callback(this.data);
      }

      var array = self.@com.googlecode.gdxquake2.client.GwtUnzip::createBuffer(I)(zipEntry.uncompressedSize);

      zipEntry.getData(new ArrayWriter(array),
        function(array) {
          self.@com.googlecode.gdxquake2.client.GwtUnzip::currentName = fileName;
          self.@com.googlecode.gdxquake2.client.GwtUnzip::entryAvailable()();
        },
        function(current, total) {
        }
      );
    }-*/;

    private Uint8Array createBuffer(int size) {
        currentBuffer = BufferUtils.newByteBuffer(size);
        return (Uint8Array) ((HasArrayBufferView) currentBuffer).getTypedArray();
    }

    private void entryAvailable() {
        dataCallback.onSuccess(new NamedBlob(currentName, currentBuffer));
        currentName = null;
        currentBuffer = null;
        postSelf();
    }

    private native void request(String url) /*-{
      var zip = $wnd.zip;
      var self = this;
      zip.createReader(new zip.HttpReader(url), function(reader) {
        reader.getEntries(function(zipEntries) {
          self.@com.googlecode.gdxquake2.client.GwtUnzip::zipEntries = zipEntries;
          self.@com.googlecode.gdxquake2.client.GwtUnzip::postSelf()();
        }); // getEntries
      }, function(msg) {
        // TODO: done error callback
        $wnd.console.log("Creating a ZIP reader failed: " + msg);
      });
    }-*/;
}
