package com.badlogic.gdx.files;

import java.io.InputStream;

/**
 * Created by haustein on 14.06.16.
 */
public class PlatformHelper {
    static boolean hasResource(Class cls, String name) {
        return false;
    }

    static InputStream getResourceAsStream(Class cls, String name) {
        return null;
    }
}
