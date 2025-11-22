package hu.nomindz.devkit.data;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public interface DataSerializer<T> {
    public void serialize(T data, OutputStream out) throws IOException;
    public T deserialize(InputStream in) throws IOException;
}
