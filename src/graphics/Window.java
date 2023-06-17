package graphics;

import game.InputState;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;
import org.lwjgl.system.MemoryStack;

import java.nio.IntBuffer;
import java.util.HashSet;

import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL11.GL_DEPTH_BUFFER_BIT;
import static org.lwjgl.opengl.GL13C.GL_MULTISAMPLE;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.system.MemoryUtil.NULL;

public class Window {
    private long handle;

    private int width;
    private int height;
    public final InputState input;
    private boolean resized;
    public Window() {
        input = new InputState();
        width = 600;
        height = 480;
        resized = false;
        glfwDefaultWindowHints(); // optional, the current window hints are already the default
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE); // the window will stay hidden after creation
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE); // the window will be resizable
        glfwWindowHint(GLFW_SAMPLES, 4);

        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3);
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);

        handle = glfwCreateWindow(width, height, "Tilt", NULL, NULL);
        if (handle == NULL) {
            throw new RuntimeException("Failed to create the GLFW window");
        }

        // Get the thread stack and push a new frame
        try (MemoryStack stack = stackPush()) {
            IntBuffer pWidth = stack.mallocInt(1); // int*
            IntBuffer pHeight = stack.mallocInt(1); // int*

            // Get the window size passed to glfwCreateWindow
            glfwGetWindowSize(handle, pWidth, pHeight);

            // Get the resolution of the primary monitor
            GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());

            // Center the window
            glfwSetWindowPos(
                    handle,
                    (vidmode.width() - pWidth.get(0)) / 2,
                    (vidmode.height() - pHeight.get(0)) / 2
            );
        } // the stack frame is popped automatically

        // Make the OpenGL context current
        glfwMakeContextCurrent(handle);

        GL.createCapabilities();

        glEnable(GL_DEPTH_TEST);
        glEnable(GL_MULTISAMPLE);

        // Enable v-sync
        glfwSwapInterval(1);

        // Make the window visible
        glfwShowWindow(handle);

        glfwSetFramebufferSizeCallback(handle, (window, width, height) -> {
            this.width = width;
            this.height = height;
            this.resized = true;
        });

        glfwSetCursorPosCallback(handle, (window, x, y) -> {
            int minDimension = Math.min(height, width);
            input.mousePosition.x = (x - (width - minDimension)/2.0) / minDimension;
            input.mousePosition.y = (y - (height - minDimension)/2.0) / minDimension;
        });

        glfwSetKeyCallback(handle, (window, key, scanCode, action, mods) -> {
            if (action == GLFW_PRESS) {
                input.addPressedKey(key);
            }
//            System.out.printf("key=%s, scanCode=%s, action=%s, mods=%d\n", key, scanCode, action, mods);
        });
    }
    public boolean shouldClose() {
        return glfwWindowShouldClose(handle);
    }
    public void update() {
        input.clearPressedKeys();
        glfwPollEvents();
    }
    public long getHandle() {
        return handle;
    }
    public void delete() {
        // Free the window callbacks and destroy the window
        glfwFreeCallbacks(handle);
        glfwDestroyWindow(handle);
    }
    public boolean isResized() {
        return resized;
    }
    public void setResized(boolean value) {
        resized = value;
    }
    public int getWidth() {
        return width;
    }
    public int getHeight() {
        return height;
    }
    public float getAspectRatio() {
        return (float)width / height;
    }
}
