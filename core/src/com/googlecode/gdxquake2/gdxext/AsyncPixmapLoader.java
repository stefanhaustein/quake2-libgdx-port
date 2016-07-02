package com.googlecode.gdxquake2.gdxext;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap;

/**
 * @author Stefan Haustein
 */
public class AsyncPixmapLoader {
    public static void loadPixmap(final FileHandle fileHandle, final Callback<Pixmap> callback) {
        Gdx.app.postRunnable(new Runnable() {
            @Override
            public void run() {
                callback.onSuccess(new Pixmap(fileHandle));
            }
        });
    }
}
