package com.googlecode.gdxquake2.gdxext;

import com.badlogic.gdx.Gdx;

import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Created by haustein on 13.06.16.
 */
public class AsyncLocalStorage {
    /**
     * Creates a new empty file.
     */
    public AsyncFileHandle createFileHandle(final String path) {
        AsyncFileHandle result = new AsyncFileHandle(path);
        result.delete();
        return result;
    }

    /**
     * Retuens an existinhg file
     */
    public void getFileHandle(final String path, final Callback<AsyncFileHandle> callback) {
        Gdx.app.postRunnable(new Runnable() {
            @Override
            public void run() {
                AsyncFileHandle fileHandle = new AsyncFileHandle(path);
                if (!fileHandle.exists()) {
                    callback.onFailure(new FileNotFoundException(path));
                } else if (fileHandle.isDirectory()) {
                    callback.onFailure(new IOException("path is directory: " + path));
                } else {
                    callback.onSuccess(new AsyncFileHandle(path));
                }
            }
        });
    }
}
