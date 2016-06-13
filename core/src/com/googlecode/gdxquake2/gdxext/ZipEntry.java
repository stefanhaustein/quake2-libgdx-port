package com.googlecode.gdxquake2.gdxext;

import java.nio.ByteBuffer;

public class ZipEntry {
    public final String name;
    public final ByteBuffer data;
    public ZipEntry(String name, ByteBuffer data) {
        this.name = name;
        this.data = data;
    }

}
