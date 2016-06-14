package com.googlecode.gdxquake2.gdxext;

import com.badlogic.gdx.Files;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandleStream;

import java.io.*;
import java.nio.ByteBuffer;
import java.util.ArrayList;

/**
 * A file handle that provides commit notifications.
 * Use AsyncLocalStorage to create and access files.
 */
public class AsyncFileHandle extends FileHandleStream {
    ArrayList<Callback<AsyncFileHandle>> commitListener = new ArrayList<Callback<AsyncFileHandle>>();
    private File file;

    /**
     * Package local -- use AsyncLocalStorage for instantiation.
     */
    AsyncFileHandle(String path, boolean create) throws IOException {
        super(path);
        this.file = new File(".asyncLocalStorage", path);
        if (create) {
            file.delete();
        } else {
            if (!file.exists()) {
                throw new FileNotFoundException(path);
            } else if (file.isDirectory()) {
                throw new IOException("Regular file expected, got a directory: " + path);
            }
        }
    }

    /**
     * Called when the file content has actually been stored.
     */
    public void addCommitListener(Callback<AsyncFileHandle> listener) {
        commitListener.add(listener);
    }

    public boolean exists() {
        return file.exists();
    }

    public void writeBuffer(ByteBuffer buffer, boolean append) {
        try {
            OutputStream os = write(append);
            if (buffer.hasArray()) {
                os.write(buffer.array(), buffer.arrayOffset(), buffer.capacity());
            } else {
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
            }
            os.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public OutputStream write(boolean append) {
        try {
            file.getParentFile().mkdirs();
            return new FileOutputStream(file, append) {
                private boolean closed;
                public void close() throws IOException {
                    super.close();
                    if (!closed) {
                        closed = true;
                        for (final Callback<AsyncFileHandle> callback : commitListener) {
                            Gdx.app.postRunnable(new Runnable() {
                                @Override
                                public void run() {
                                    callback.onSuccess(AsyncFileHandle.this);
                                }
                            });
                        }
                    }
                }
            };
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public InputStream read() {
       try {
           return new FileInputStream(file);
       } catch (IOException e) {
           throw new RuntimeException(e);
       }
    }

}
