package com.googlecode.gdxquake2.gdxext;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.utils.Base64Coder;
import com.google.gwt.dom.client.ImageElement;
import com.googlecode.gdxquake2.GdxQuake2;
import com.googlecode.gdxquake2.gdxext.Callback;

/**
 * @author Stefan Haustein
 */
public class AsyncPixmapLoader {

    public static void loadPixmap(final FileHandle fileHandle, final Callback<Pixmap> callback) {
        String url = "data:image/" + fileHandle.extension() + ";base64," + new String(Base64Coder.encode(fileHandle.readBytes()));

        GdxQuake2.tools.log(url);

        createImage(url, new Callback<ImageElement>() {
            @Override
            public void onSuccess(ImageElement imageElement) {
                Pixmap pixmap = new Pixmap(imageElement.getWidth(), imageElement.getHeight(), Pixmap.Format.RGBA8888);
                pixmap.getCanvasElement().getContext2d().drawImage(imageElement, 0, 0);
                callback.onSuccess(pixmap);
            }

            @Override
            public void onFailure(Throwable cause) {
                callback.onFailure(cause);
            }
        });
    }

    private static native void createImage(String url, Callback<ImageElement> callback) /*-{
        var img = $doc.createElement("img");
        img.onload = function() {
          callback.@com.googlecode.gdxquake2.gdxext.Callback::onSuccess(Ljava/lang/Object;)(img);
        }
        img.src = url;
    }-*/;

}
