package com.googlecode.gdxquake2.gdxext;

import com.badlogic.gdx.Gdx;

import java.io.IOException;

/**
 * Created by haustein on 13.06.16.
 */
public class AsyncLocalStorage {
    /**
     * Creates a new empty file.
     */
    public AsyncFileHandle createFileHandle(final String path) {
        try {
            AsyncFileHandle result = new AsyncFileHandle(path, true);
            return result;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Retuens an existinhg file
     */
    public void getFileHandle(final String path, final Callback<AsyncFileHandle> callback) {
        Gdx.app.postRunnable(new Runnable() {
            @Override
            public void run() {
                try {
                    AsyncFileHandle fileHandle = new AsyncFileHandle(path, false);
                    callback.onSuccess(fileHandle);
                } catch (IOException e) {
                    callback.onFailure(e);
                }
            }
        });
    }
}
