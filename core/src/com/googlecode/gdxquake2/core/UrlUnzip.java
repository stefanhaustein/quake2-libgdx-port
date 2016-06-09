import java.nio.ByteBuffer;

/**
 * Created by haustein on 09.06.16.
 */
public interface UrlUnzip {
    class Entry {
        public final String name;
        public final ByteBuffer data;

        public Entry (String name, ByteBuffer data) {
            this.name = name;
            this.data = data;
        }
    }

}
