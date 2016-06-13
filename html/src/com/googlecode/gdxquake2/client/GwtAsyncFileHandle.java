package com.googlecode.gdxquake2.client;

import com.badlogic.gdx.files.FileHandle;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;

/**
 *
 */
public class GwtAsyncFileHandle extends FileHandle {

    private ByteBuffer data;

    public GwtAsyncFileHandle(String name) {
        this(name, null);
    }

    // Absolute is the default and no other type seems to match better.
    public GwtAsyncFileHandle(String name, ByteBuffer data) {
        super(name);
        this.data = data;
    }

    public InputStream read() {
        return new InputStream() {
            int pos = 0;

            @Override
            public int read() throws IOException {
                return pos >= data.capacity() ? -1 : (data.get(pos++) & 255);
            }

            public int read(byte[] buf, int ofs, int len) {
                if (len <= 0) {
                    return 0;
                }
                int count = Math.min(data.capacity() - pos, len);
                if (count <= 0) {
                    return -1;
                }
                int saveLimit = data.limit();
                int savePos = data.position();
                data.limit(data.capacity());
                data.position(pos);
                data.get(buf, ofs, count);
                data.position(savePos);
                data.limit(saveLimit);
                pos += count;
                return count;
            }
        };
    }

    public OutputStream write(final boolean append) {
        if (append) {
            throw new RuntimeException("Append not yet supported for GwtAsyncFileHandle");
        }
        return new ByteArrayOutputStream() {
            public void close() {
                data = ByteBuffer.wrap(this.toByteArray());
            }
        };
    }

    public ByteBuffer getData() {
        return data;
    }
}
