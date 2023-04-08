import graphics.*;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFWErrorCallback;
import util.Util;

import static algorithm.GeodesicPolyhedra.generateIcosphereMesh;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

public class Game {
    private boolean isRunning;
    private Window window;

    private float fov = (float)Math.toRadians(60);
    private float zNear = 0.01f;
    private float zFar = 1000;

    private int rotation = 0;

    private ShaderProgram sp;
    Mesh mesh;
    private Matrix4f projectionMatrix;
    private Camera camera;

    public Game() {
        isRunning = true;
    }
    public void run() {
        GLFWErrorCallback.createPrint(System.err).set();
        if (!glfwInit()) {
            throw new IllegalStateException("Unable to initialize GLFW");
        }
        window = new Window();
        sp = new ShaderProgram();
        sp.addShader(Shader.fromFile("shaders/fragment.fs"));
        sp.addShader(Shader.fromFile("shaders/vertex.vs"));
        sp.link();
        sp.createUniform("projectionMatrix");
        sp.createUniform("viewMatrix");
        sp.createUniform("color");

//        float[] positions = new float[]{
//                -0.5f, 0.5f, -1.05f,
//                -0.5f, -0.5f, -1.05f,
//                0.5f, -0.5f, -1.05f,
//                0.5f, 0.5f, -1.05f,
//        };
        float[] positions = new float[]{
                -0.5f,  0.5f, 0.0f,
                -0.5f, -0.5f, 0.0f,
                0.5f, -0.5f, 0.0f,
                0.5f,  0.5f, 0.0f,
        };
        float[] colors = new float[]{
                0.5f, 0.0f, 0.0f,
                0.0f, 0.5f, 0.0f,
                0.0f, 0.0f, 0.5f,
                0.0f, 0.5f, 0.5f,
        };
        int[] indices = new int[]{
                0, 1, 3, 3, 1, 2,
        };
//        mesh = new Mesh(positions, indices, colors);

        mesh = generateIcosphereMesh(2);
        projectionMatrix = new Matrix4f()
                .perspective(fov, window.getAspectRatio(), zNear, zFar);

        camera = new Camera();
        camera.getPosition().z = -3;

        loop();
    }
    private void update() {
        window.update();
        if (window.shouldClose()) {
            isRunning = false;
        }
        rotation++;
        rotation %= 360;

        camera.getRotation().x = (float)Math.toRadians(rotation);
    }
    private void render() {
        if (window.isResized()) {
            glViewport(0, 0, window.getWidth(), window.getHeight());
            window.setResized(false);
        }
        // Set the clear color
        glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT); // clear the framebuffer

        sp.bind();
        sp.setUniform("projectionMatrix", projectionMatrix);
        sp.setUniform("viewMatrix", camera.getViewMatrix());
        sp.setUniform("color", new Vector3f(0.5f, 0, 0));
        mesh.render();
        sp.unbind();

        glfwSwapBuffers(window.getHandle()); // swap the color buffers
    }
    private void loop() {
        long previousTime = System.nanoTime(), currentTime;
        long updateDelta = 0, renderDelta = 0;
        long nsPerUpdate = (long)(1e9 / 60), nsPerRender = (long)(1e9 / 60);

        int updateCount = 0, renderCount = 0;
        long previousFPSCalcTime = System.nanoTime();

        while (isRunning) {
            currentTime = System.nanoTime();

            updateDelta += currentTime - previousTime;
            renderDelta += currentTime - previousTime;

            while (updateDelta >= nsPerUpdate) {
                update();
                updateDelta -= nsPerUpdate;
                updateCount++;
            }

            if (renderDelta >= nsPerRender) {
                render();
                renderDelta = 0;
                renderCount++;
            }

            previousTime = currentTime;

            // Calculate performance stats
            if (currentTime - previousFPSCalcTime >= 1e9) {
                System.out.printf("Update FPS: %d | Render FPS: %d \n", updateCount, renderCount);
                previousFPSCalcTime = currentTime;
                updateCount = 0;
                renderCount = 0;
            }
//            LockSupport.parkNanos((long)1e6);

//            try {
//                Thread.sleep(1);
//            } catch (InterruptedException e) {
//                System.out.println("zxczxzxc");
//            }
        }
    }
    public void close() {
        window.delete();
        glfwTerminate();
        glfwSetErrorCallback(null).free();
    }
}
