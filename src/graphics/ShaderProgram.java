package graphics;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import util.Deletable;
import util.Util;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static org.lwjgl.opengl.GL20.*;

public class ShaderProgram implements Deletable {
    private final int id;
    private final ArrayList<Shader> shaders;
    private final HashMap<String, Integer> uniforms;

    public ShaderProgram() {
        id = glCreateProgram();
        if (id == 0) {
            throw new RuntimeException("Could not create shader program!");
        }
        shaders = new ArrayList<>();
        uniforms = new HashMap<>();
    }

    /**
     * Loads a shader program from a file
     * Supports multiple shaders in a single file
     * @param path the path of the file
     * @return the shader program
     */
    public static ShaderProgram fromFile(String path) {
        ShaderProgram program = new ShaderProgram();
        String source = Util.getFileSource("shaders/" + path);

        StringBuilder lines = new StringBuilder();
        int shaderType = 0;
        String[] split = source.split("\n");
        ArrayList<String> uniformNames = new ArrayList<>();

        for (int i = 0; i < split.length; i++) {
            String line = split[i];

            if (!line.startsWith("///")) {
                lines.append(line + "\n");
            }
            if (line.startsWith("/// ") || i == split.length-1) {
                if (shaderType != 0) {
                    program.addShader(new Shader(lines.toString(), shaderType));
                    lines.setLength(0);
                }
                if (i != split.length-1) {
                    String typeName = line.substring(4);
                    switch (typeName) {
                        case "Vertex" -> shaderType = GL_VERTEX_SHADER;
                        case "Fragment" -> shaderType = GL_FRAGMENT_SHADER;
                        default -> {
                            throw new RuntimeException(
                                    String.format("Could not determine shader type: '%s' on line %d\n", typeName, i));
                        }
                    }
                }
            }
            if (line.startsWith("uniform")) {
                int bracket = line.lastIndexOf('[');
                uniformNames.add(line.substring(
                        line.lastIndexOf(' ') + 1,
                        bracket == -1 ? line.length()-1 : bracket)
                );
            }
        }

        program.link();
        for (String uniformName : uniformNames) {
            program.createUniform(uniformName);
        }

        return program;
    }
    public void addShader(Shader shader) {
        glAttachShader(id, shader.getId());
        shaders.add(shader);
    }

    public void link() {
        glLinkProgram(id);
        if (glGetProgrami(id, GL_LINK_STATUS) == 0) {
            throw new RuntimeException("Error linking shader: " + glGetProgramInfoLog(id, 1024));
        }
        for (Shader shader : shaders) {
            glDetachShader(id, shader.getId());
        }
        shaders.clear();

        glValidateProgram(id);
        if (glGetProgrami(id, GL_VALIDATE_STATUS) == 0) {
            System.err.println("Warning validating shader: " + glGetProgramInfoLog(id, 1024));
        }
    }
    // The following functions are for setting uniforms (shader global variables)
    public void setUniformMatrix4fv(String name, FloatBuffer buffer) {
        glUniformMatrix4fv(uniforms.get(name), false, buffer);
    }
    public void setUniform3fv(String name, FloatBuffer buffer) {
        glUniform3fv(uniforms.get(name), buffer);
    }
    public void setUniform4fv(String name, FloatBuffer buffer) {
        glUniform4fv(uniforms.get(name), buffer);
    }
    public void setUniform1fv(String name, FloatBuffer buffer) {
        glUniform1fv(uniforms.get(name), buffer);
    }
    public void setUniform(String name, ArrayList<Matrix4f> values) {
        FloatBuffer buffer = MemoryUtil.memAllocFloat(16*values.size());
        for (int i = 0; i < values.size(); i++) {
            values.get(i).get(i*16, buffer);
        }
        buffer.flip();
        glUniformMatrix4fv(uniforms.get(name), false, buffer);
        MemoryUtil.memFree(buffer);
    }
    public void setUniform(String name, Matrix4f value) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            FloatBuffer buffer = stack.mallocFloat(16);
            value.get(buffer);
            glUniformMatrix4fv(uniforms.get(name), false, buffer);
        }
    }
    public void setUniform(String name, Vector4f value) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            FloatBuffer buffer = stack.mallocFloat(4);
            value.get(buffer);
            glUniform4fv(uniforms.get(name), buffer);
        }
    }
    public void setUniform(String name, Vector3f value) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            FloatBuffer buffer = stack.mallocFloat(3);
            value.get(buffer);
            glUniform3fv(uniforms.get(name), buffer);
        }
    }
    public void setUniform(String name, Vector2f value) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            FloatBuffer buffer = stack.mallocFloat(2);
            value.get(buffer);
            glUniform3fv(uniforms.get(name), buffer);
        }
    }
    public void setUniform(String name, int value) {
        glUniform1i(uniforms.get(name), value);
    }
    public void setUniform(String name, float value) {
        glUniform1f(uniforms.get(name), value);
    }
    public void createUniform(String name) {
        int location = glGetUniformLocation(id, name);
        if (location < 0) {
            throw new RuntimeException("Could not find uniform: " + name);
        }
        uniforms.put(name, location);
    }

    public void bind() {
        glUseProgram(id);
    }
    public void unbind() {
        glUseProgram(0);
    }
    public void delete() {
        unbind();
        for (Shader shader : shaders) {
            shader.delete();
        }
        glDeleteProgram(id);
    }
}
