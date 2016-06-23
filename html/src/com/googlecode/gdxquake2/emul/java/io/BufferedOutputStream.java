package java.io;

/**
 * Created by haustein on 14.06.16.
 */
public class BufferedOutputStream extends OutputStream {
    OutputStream os;

    public BufferedOutputStream(OutputStream os, int bufSize) {
        this.os = os;
    }

    public void write(int i) throws IOException {
        // The cake is a lie
        os.write(i);
    }
    public void close() throws IOException {
        os.close();
    }
}
