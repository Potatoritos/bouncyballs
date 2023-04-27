import graphics.*;
import graphics.Window;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFWErrorCallback;

import java.awt.*;
import java.nio.ByteBuffer;
import java.util.Vector;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.opengl.GL43C.GL_MAX_FRAMEBUFFER_HEIGHT;
import static org.lwjgl.opengl.GL43C.GL_MAX_FRAMEBUFFER_WIDTH;
import static util.Geometry.*;

public class Game {
    private boolean isRunning;
    private Window window;

    private float fov = (float)Math.toRadians(60);
    private float zNear = 0.01f;
    private float zFar = 100;

    private int rotation = 0;

    private ShaderProgram colorNormals;
    private ShaderProgram textureShader;
    private ShaderProgram sobelShader;
    RenderEntity sphere;
    RenderEntity prism;
    RenderEntity prism2;
    RenderEntity[] entities;
    private Matrix4f projectionMatrix;
    private Camera camera;

    private RenderEntity textureRect;

    private EmptyFbo normalFbo;

    private int colorTextureId;

    public Game() {
        isRunning = true;
    }
    public void run() {
        GLFWErrorCallback.createPrint(System.err).set();
        if (!glfwInit()) {
            throw new IllegalStateException("Unable to initialize GLFW");
        }
        window = new Window();

        colorNormals = ShaderProgram.fromFile("color_normals.glsl");
        textureShader = ShaderProgram.fromFile("texture.glsl");
        sobelShader = ShaderProgram.fromFile("sobel_filter.glsl");

        sphere = new RenderEntity(generateGeodesicPolyhedronMesh(1, new Vector3f(0, 0, 1)));
        sphere.getPosition().x = 1f;
        prism = new RenderEntity(rectangularPrismMesh(new Vector3f(-0.5f, -1f, -0.5f), new Vector3f(1, 2, 1), new Vector3f(1, 0, 0)));
        prism.getPosition().y = 1f;
        prism.getRotation().x = (float)Math.PI/4;

        prism2 = new RenderEntity(rectangularPrismMesh(new Vector3f(-0.5f, -1f, -0.5f), new Vector3f(1, 2, 1), new Vector3f(0, 1, 0)));
        prism2.getPosition().x = 1f;

//        sphere.getPosition().y = -1f;
        entities = new RenderEntity[] {sphere, prism, prism2};
//        entities = new RenderEntity[] {prism};
        projectionMatrix = new Matrix4f()
                .perspective(fov, window.getAspectRatio(), zNear, zFar);

        camera = new Camera();
        camera.getPosition().z = 4;


        normalFbo = new EmptyFbo(window.getWidth(), window.getHeight());

        textureRect = new RenderEntity(texturedRectangle(new Vector2f(0, 0), new Vector2f(1, 1), normalFbo.getColorTexture()));
        textureRect.getPosition().x = -2.5f;
        textureRect.setScale(1f);

        loop();
    }
    private void update() {
        window.update();
        if (window.shouldClose()) {
            isRunning = false;
        }
        rotation++;
        rotation %= 720;

        sphere.getRotation().x = (float)Math.toRadians(rotation);
        prism.getRotation().y = (float)Math.toRadians(rotation);
        prism.getRotation().z = (float)Math.toRadians(rotation)/2;
//        prism.getPosition().y = (float)Math.sin(Math.toRadians(rotation)*2);
        prism.getPosition().z = -10*(float)Math.sin(Math.toRadians(rotation)*2)-10;


        prism2.getRotation().y = (float)Math.toRadians(rotation);
        prism2.getRotation().z = (float)Math.toRadians(rotation)/2;
        prism2.getPosition().y = (float)Math.sin(Math.toRadians(rotation)*2);


//        textureRect.getRotation().x = (float)Math.toRadians(rotation);
//        textureRect.getRotation().z = (float)Math.toRadians(rotation);
//        textureRect.getPosition().y = -(float)Math.sin(Math.toRadians(rotation)*2);


//        camera.getRotation().x = (float)Math.toRadians(rotation);
    }
    private void render() {
        if (window.isResized()) {
            glViewport(0, 0, window.getWidth(), window.getHeight());
            window.setResized(false);

            projectionMatrix = projectionMatrix.identity()
                    .perspective(fov, window.getAspectRatio(), zNear, zFar);

            normalFbo.resize(window.getWidth(), window.getHeight());

        }
        glClearColor(1.0f, 1.0f, 1.0f, 0.4f);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT); // clear the framebuffer
//
        colorNormals.bind();
        normalFbo.bind();
        glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
////        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, GL_TEXTURE_2D, texture.getId(), 0);

        colorNormals.setUniform("projectionMatrix", projectionMatrix);
//        sp.setUniform("color", new Vector3f(0.5f, 0, 0));
        for (RenderEntity entity : entities) {
            colorNormals.setUniform("viewMatrix", camera.getViewMatrix().mul(entity.getWorldMatrix()));
            entity.getMesh().render();
        }

        FrameBufferObject.unbind();

//        for (RenderEntity entity : entities) {
//            colorNormals.setUniform("viewMatrix", camera.getViewMatrix().mul(entity.getWorldMatrix()));
//            entity.getMesh().render();
//        }

        colorNormals.unbind();

        sobelShader.bind();
//        sobelShader.setUniform("normalTexture", 0);
        sobelShader.setUniform("depthTexture", 1);
        sobelShader.setUniform("projectionMatrix", projectionMatrix);
        glActiveTexture(GL_TEXTURE0);
        normalFbo.getColorTexture().bind();
        glActiveTexture(GL_TEXTURE1);
        normalFbo.getDepthTexture().bind();
        for (RenderEntity entity : entities) {
            sobelShader.setUniform("viewMatrix", camera.getViewMatrix().mul(entity.getWorldMatrix()));
//            sobelShader.setUniform("color", entity.getColor());

            entity.getMesh().render();
        }

        sobelShader.unbind();
//
        textureShader.bind();

        textureShader.setUniform("textureSampler", 0);
        textureShader.setUniform("projectionMatrix", projectionMatrix);
        textureShader.setUniform("viewMatrix", camera.getViewMatrix().mul(textureRect.getWorldMatrix()));
        textureRect.getMesh().render();

//        textureShader.setUniform("viewMatrix", camera.getViewMatrix().mul(textureRect2.getWorldMatrix()));
//        textureRect2.getMesh().render();

        textureShader.unbind();

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
