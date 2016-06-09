package com.googlecode.gdxquake2.core.tools;

import java.nio.ByteBuffer;

public class NamedBlob {
    public final String name;
    public final ByteBuffer data;
    public NamedBlob(String name, ByteBuffer data) {
        this.name = name;
        this.data = data;
    }

}
