package graphics;
import org.lwjgl.BufferUtils;
import org.lwjgl.system.MemoryStack;
import util.Deletable;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.stb.STBImage.*;

public class Texture implements Deletable {
    private final int id;
    public Texture() {
        id = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, id);

        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);

        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_R, GL_REPEAT);

        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_BASE_LEVEL, 0);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAX_LEVEL, 1);
        glBindTexture(GL_TEXTURE_2D, 0);
    }
    public void bind() {
        glBindTexture(GL_TEXTURE_2D, id);
    }
    public static void unbind() {
        glBindTexture(GL_TEXTURE_2D, 0);
    }

    /**
     * Sets the texture to an empty image of specified properties
     * @param width the width of the image
     * @param height the height of the image
     * @param internalFormat the internal format of the image
     * @param format the format of the image
     * @param type the type of the image
     */
    public void setEmptyImage(int width, int height, int internalFormat, int format, int type) {
        glTexImage2D(GL_TEXTURE_2D, 0, internalFormat, width, height, 0, format, type, 0);
    }

    /**
     * Loads an image into this texture
     * (Stopped working and I don't know why)
     * @param path the path of the file to load from
     */
    public void loadImage(String path) {
        ByteBuffer image;
        int width, height;
        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer w = stack.mallocInt(1);
            IntBuffer h = stack.mallocInt(1);
            IntBuffer comp = stack.mallocInt(1);

            stbi_set_flip_vertically_on_load(true);

            image = stbi_load(path, w, h, comp, 4);
            if (image == null) {
                throw new RuntimeException("Could not load texture from image with path '" + path + "'!" + stbi_failure_reason());
            }

            width = w.get();
            height = h.get();
            System.out.printf("wtf %s %s\n", width, height);
        }

        glPixelStorei(GL_UNPACK_ALIGNMENT, 1);

        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, image);
        glGenerateMipmap(GL_TEXTURE_2D);
        stbi_image_free(image);
    }
    public int getId() {
        return id;
    }
    public void delete() {
        glDeleteTextures(id);
    }

}
