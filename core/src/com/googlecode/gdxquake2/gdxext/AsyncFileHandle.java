package com.googlecode.gdxquake2.gdxext;

import com.badlogic.gdx.Files;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;

/**
 * A file handle that provides commit notifications.
 */
public class AsyncFileHandle extends FileHandle {
    ArrayList<Callback<AsyncFileHandle>> commitListener = new ArrayList<Callback<AsyncFileHandle>>();

    /**
     * Package local -- use AsyncLocalStorage for instantiation.
     */
    AsyncFileHandle(String path) {
        super(path, Files.FileType.Local);
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
            if (file().getParentFile() != null) {
                file().getParentFile().mkdirs();
            }
            return new FileOutputStream(file(), append) {
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

}
