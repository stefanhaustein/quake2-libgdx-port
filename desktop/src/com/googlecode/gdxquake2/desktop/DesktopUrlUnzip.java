import java.io.*;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;


public class DesktopUrlUnzip implements UrlUnzip {


    public void unzip(String url, Callback<Entry> callback) {
        Runnable runnable = new Runnable() {
            public final void run() {
                try {
                    InputStream is = new URL(url).openConnection().getInputStream();
                    File f = File.createTempFile("tmp", ".zip");
                    OutputStream os = new FileOutputStream(f);
                    byte[] buf = new byte[65536];
                    while (true) {
                        int count = is.read(buf);
                        if (count <= 0) {
                         break;
                        }
                        os.write(buf, 0, count);
                    }

                    ZipFile zipFile = new ZipFile(f);
                    Enumeration<? extends ZipEntry> entries = zipFile.entries();
                    while (entries.hasMoreElements()) {
                        ZipEntry zipEntry = entries.nextElement();
                        if (zipEntry == null) {
                            break;
                        }
                        byte[] data = new byte[(int) zipEntry.getSize()];
                        DataInputStream dis = new DataInputStream(zipFile.getInputStream(zipEntry));
                        dis.readFully(data);
                        dis.close();
                        callback.success(new Entry(zipEntry.getName(), ByteBuffer.wrap(data)));
                    }
                    zipFile = null;
                    f.delete();
                } catch (IOException e) {
                    callback.error(e);
                }

            }
        };
        runnable.run();
//        new Thread(runnable).start();
    }
}
