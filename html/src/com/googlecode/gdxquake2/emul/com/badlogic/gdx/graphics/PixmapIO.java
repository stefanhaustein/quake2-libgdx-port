package com.badlogic.gdx.graphics;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Base64Coder;
import com.googlecode.gdxquake2.GdxQuake2;

import java.io.IOException;
import java.io.OutputStream;

public class PixmapIO {

    public static void writePNG(FileHandle fileHandle, Pixmap pixmap) {
        try {
            String base64Url = pixmap.canvas.toDataUrl();
            int cut = base64Url.indexOf(',');
            byte[] decoded = Base64Coder.decode(base64Url.substring(cut + 1));
            OutputStream os = fileHandle.write(false);
            os.write(decoded);
            os.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
