package graphics;

import util.Deletable;
import util.Util;

import static org.lwjgl.opengl.GL20.*;

public class Shader implements Deletable {
    private int id;

    public Shader(String source, int type) {
        id = glCreateShader(type);
        if (id == 0) {
            throw new RuntimeException("Error creating shader!");
        }

        glShaderSource(id, source);
        glCompileShader(id);

        if (glGetShaderi(id, GL_COMPILE_STATUS) == 0) {
            throw new RuntimeException("Error compiling shader: " + glGetShaderInfoLog(id, 1024));
        }
    }

    public static Shader fromFile(String path) {
        int type;
        if (path.endsWith(".frag")) {
            type = GL_FRAGMENT_SHADER;
        } else if (path.endsWith(".vert")) {
            type = GL_VERTEX_SHADER;
        } else {
            throw new RuntimeException("Could not determine shader type");
        }

        return new Shader(Util.getFileSource("shaders/" + path), type);

    }

    public int getId() {
        return id;
    }
    public void delete() {
        glDeleteShader(id);
    }
}
