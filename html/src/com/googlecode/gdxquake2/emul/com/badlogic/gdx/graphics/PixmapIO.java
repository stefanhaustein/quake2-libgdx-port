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
            if (base64Url.length() < 2048) {
                GdxQuake2.tools.log(base64Url);
            }
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
