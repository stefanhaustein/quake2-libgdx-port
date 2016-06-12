package com.badlogic.gdx.graphics;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Base64Coder;
import com.google.gwt.typedarrays.shared.ArrayBufferView;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.HasArrayBufferView;

public class PixmapIO {
    public static void writePNG(FileHandle fileHandle, Pixmap pixmap) {
        try {
            ArrayBufferView typedArray = ((HasArrayBufferView) pixmap.getPixels()).getTypedArray();
            String base64Url = createPngUrl(pixmap.getWidth(), pixmap.getHeight(), typedArray);

            int cut = base64Url.indexOf(',');
            byte[] decoded = Base64Coder.decode(base64Url.substring(cut + 1));
            OutputStream os = fileHandle.write(false);
            os.write(decoded);
            os.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    static private native String createPngUrl(int width, int height, ArrayBufferView rgba) /*-{
        var canvas = $doc.createElement("canvas");
        canvas.width = width;
        canvas.height = height;
        var ctx = canvas.getContext("2d");
        var imageData = ctx.getImageData(0, 0, width, height);
        imageData.data.set(rgba);
        return canvas.toDataURL();
    }-*/;
}
