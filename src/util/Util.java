package util;

import org.joml.Vector3d;
import org.joml.Vector3f;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.io.*;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import org.lwjgl.BufferUtils;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.lwjgl.BufferUtils.createByteBuffer;

public class Util {
    /**
     * Gets the contents of a file
     * @param path the path of the file
     * @return a string containing the file contents in entirety
     */
    public static String getFileSource(String path) {
        try {
            return Files.readString(Paths.get("assets/" + path), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException("Could not get file source: " + path);
        }
    }

    public static Vector3f vector3dTo3f(Vector3d vector) {
        return new Vector3f((float)vector.x, (float)vector.y, (float)vector.z);
    }

    private static ByteBuffer resizeBuffer(ByteBuffer buffer, int newCapacity) {
        ByteBuffer newBuffer = createByteBuffer(newCapacity);
        buffer.flip();
        newBuffer.put(buffer);
        return newBuffer;
    }

//    public static ByteBuffer ioResourceToByteBuffer(String resource, int bufferSize) throws IOException {
//        ByteBuffer buffer;
//        URL url = Thread.currentThread().getContextClassLoader().getResource(resource);
//        if (url == null)
//            throw new IOException("Classpath resource not found: " + resource);
//        File file = new File(url.getFile());
//        if (file.isFile()) {
//            FileInputStream fis = new FileInputStream(file);
//            FileChannel fc = fis.getChannel();
//            buffer = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());
//            fc.close();
//            fis.close();
//        } else {
//            buffer = BufferUtils.createByteBuffer(bufferSize);
//            InputStream source = url.openStream();
//            if (source == null)
//                throw new FileNotFoundException(resource);
//            try {
//                byte[] buf = new byte[8192];
//                while (true) {
//                    int bytes = source.read(buf, 0, buf.length);
//                    if (bytes == -1)
//                        break;
//                    if (buffer.remaining() < bytes)
//                        buffer = resizeBuffer(buffer, Math.max(buffer.capacity() * 2, buffer.capacity() - buffer.remaining() + bytes));
//                    buffer.put(buf, 0, bytes);
//                }
//                buffer.flip();
//            } finally {
//                source.close();
//            }
//        }
//        return buffer;
//    }
//
    public static ByteBuffer ioResourceToByteBuffer(String resource, int bufferSize) throws IOException {
        ByteBuffer buffer;

        Path path = Paths.get(resource);
        if (Files.isReadable(path)) {
            try (SeekableByteChannel fc = Files.newByteChannel(path)) {
                buffer = createByteBuffer((int)fc.size() + 1);
                while (fc.read(buffer) != -1);
            }
        } else {
            try (
                    InputStream source = Util.class.getClassLoader().getResourceAsStream(resource);
                    ReadableByteChannel rbc = Channels.newChannel(source)
            ) {
                buffer = createByteBuffer(bufferSize);

                while (true) {
                    int bytes = rbc.read(buffer);
                    if (bytes == -1)
                        break;
                    if (buffer.remaining() == 0)
                        buffer = resizeBuffer(buffer, buffer.capacity() * 2);
                }
            }
        }

        buffer.flip();
        return buffer;
    }
}
