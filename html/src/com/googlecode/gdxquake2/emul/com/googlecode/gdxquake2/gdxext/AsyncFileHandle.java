package com.googlecode.gdxquake2.gdxext;

import com.badlogic.gdx.Files;
import com.badlogic.gdx.files.FileHandle;
import com.googlecode.gdxquake2.GdxQuake2;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;

public class AsyncFileHandle extends FileHandle {
    ArrayList<Callback<AsyncFileHandle>> commitListener = new ArrayList<Callback<AsyncFileHandle>>();
    ByteBuffer data;
    AsyncLocalStorage owner;
    String path;

    AsyncFileHandle(AsyncLocalStorage owner, String path) {
        super(path, Files.FileType.Local);
        this.owner = owner;
        this.path = path;
    }

    public boolean delete() {
        // GdxQuake2.tools.log("ignoring delete: " + path);
        return false;
    }

    public String name() {
        int cut = path.lastIndexOf('/');
        return cut == -1 ? path : path.substring(cut + 1);
    }

    public String path() {
        return path;
    }

    /**
     * Called when the file content has actually been stored.
     */
    public void addCommitListener(Callback<AsyncFileHandle> listener) {
        commitListener.add(listener);
    }

    public void writeBuffer(ByteBuffer buffer, boolean append) {
        try {
            OutputStream os = write(append);
            byte[] buf = new byte[Math.min(buffer.capacity(), buffer.capacity())];
            int savePos = buffer.position();
            int saveLimit = buffer.limit();
            buffer.limit(buffer.capacity());
            buffer.position(0);
            while (buffer.position() < buffer.capacity()) {
                int count = Math.min(buf.length, buffer.capacity() - buffer.position());
                buffer.get(buf, 0, count);
                os.write(buf, 0, count);
            }
            buffer.position(savePos);
            buffer.limit(saveLimit);
            os.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
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
            throw new RuntimeException("Append not yet supported for AsyncFileHandle");
        }
        return new ByteArrayOutputStream() {
            public void close() {
                data = ByteBuffer.wrap(this.toByteArray());
                owner.saveFileHandleImpl(AsyncFileHandle.this, new Callback<Void>() {

                    @Override
                    public void onSuccess(Void result) {
                        for (Callback<AsyncFileHandle> callback : commitListener) {
                            callback.onSuccess(AsyncFileHandle.this);
                        }
                    }

                    @Override
                    public void onFailure(Throwable cause) {
                        for (Callback<AsyncFileHandle> callback : commitListener) {
                            callback.onFailure(cause);
                        }
                    }
                });
            }
        };
    }

    public ByteBuffer getData() {
        return data;
    }
}
