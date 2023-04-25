import graphics.*;
import graphics.Window;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFWErrorCallback;

import java.awt.*;
import java.nio.ByteBuffer;

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
    private float zFar = 1000;

    private int rotation = 0;

    private ShaderProgram colorNormals;
    private ShaderProgram textureShader;
    private ShaderProgram sobelShader;
    RenderEntity sphere;
    RenderEntity prism;
    RenderEntity[] entities;
    private Matrix4f projectionMatrix;
    private Camera camera;

    private RenderEntity textureRect;
    private RenderEntity textureRect2;

    private FrameBufferObject fbo;
    private Texture depthTexture;
    private Texture colorTexture;

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

        colorNormals = new ShaderProgram();
        colorNormals.addShader(Shader.fromFile("color_normals.frag"));
        colorNormals.addShader(Shader.fromFile("color_normals.vert"));
        colorNormals.link();
        colorNormals.createUniform("projectionMatrix");
        colorNormals.createUniform("viewMatrix");

        textureShader = new ShaderProgram();
        textureShader.addShader(Shader.fromFile("texture.frag"));
        textureShader.addShader(Shader.fromFile("texture.vert"));
        textureShader.link();
        textureShader.createUniform("projectionMatrix");
        textureShader.createUniform("viewMatrix");
        textureShader.createUniform("textureSampler");

        sobelShader = new ShaderProgram();
        sobelShader.addShader(Shader.fromFile("sobel.frag"));
        sobelShader.addShader(Shader.fromFile("sobel.vert"));
        sobelShader.link();
        sobelShader.createUniform("projectionMatrix");
        sobelShader.createUniform("viewMatrix");
        sobelShader.createUniform("sampler");

//        textureShader.createUniform("textureSampler");
        sphere = new RenderEntity(generateGeodesicPolyhedronMesh(3));
        prism = new RenderEntity(rectangularPrismMesh(new Vector3f(-0.5f, -1f, -0.5f), new Vector3f(1, 2, 1)));
        prism.getPosition().y = 1f;
        prism.getRotation().x = (float)Math.PI/4;
//        sphere.getPosition().y = -1f;
        entities = new RenderEntity[] {sphere, prism};
        projectionMatrix = new Matrix4f()
                .perspective(fov, window.getAspectRatio(), zNear, zFar);

        camera = new Camera();
        camera.getPosition().z = 4;

        colorTexture = new Texture();
//        colorTexture.loadImage("assets/image/astolfo_necoarc.png");
        colorTexture.setEmptyImage(window.getWidth(), window.getHeight(), GL_RGBA32F, GL_RGBA, GL_FLOAT);

        depthTexture = new Texture();
        depthTexture.setEmptyImage(window.getWidth(), window.getHeight(), GL_DEPTH_COMPONENT, GL_DEPTH_COMPONENT, GL_UNSIGNED_BYTE);

        fbo = new FrameBufferObject();
        fbo.bind();
        fbo.attachColorTexture(colorTexture);
        fbo.attachDepthTexture(depthTexture);

        int x = glCheckFramebufferStatus(GL_FRAMEBUFFER);
        if (x != GL_FRAMEBUFFER_COMPLETE) {
            System.out.println("WTF");
            System.out.println(x);
        }

        textureRect = new RenderEntity(texturedRectangle(new Vector2f(0, 0), new Vector2f(1, 1), colorTexture));
        textureRect.getPosition().x = -2.5f;
        textureRect.setScale(1f);

        textureRect2 = new RenderEntity(texturedRectangle(new Vector2f(0, 0), new Vector2f(1, 1), depthTexture));
        textureRect2.getPosition().x = -2.52f;
        textureRect2.getPosition().y = -1.5f;
        textureRect2.setScale(1f);
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
        prism.getPosition().y = (float)Math.sin(Math.toRadians(rotation)*2);


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

            FrameBufferObject.unbind();
            colorTexture.delete();
            depthTexture.delete();
            fbo.delete();

            colorTexture = new Texture();
            colorTexture.bind();
            colorTexture.setEmptyImage(window.getWidth(), window.getHeight(), GL_RGBA, GL_RGBA, GL_FLOAT);
            depthTexture = new Texture();
            depthTexture.bind();
            depthTexture.setEmptyImage(window.getWidth(), window.getHeight(), GL_DEPTH_COMPONENT, GL_DEPTH_COMPONENT, GL_UNSIGNED_BYTE);
            Texture.unbind();

            fbo = new FrameBufferObject();
            fbo.bind();
            fbo.attachColorTexture(colorTexture);
            fbo.attachDepthTexture(depthTexture);

            int x = glCheckFramebufferStatus(GL_FRAMEBUFFER);
            if (x != GL_FRAMEBUFFER_COMPLETE) {
                System.out.println("WTF");
                System.out.println(x);
                System.out.println(GL_MAX_FRAMEBUFFER_WIDTH);
                System.out.println(GL_MAX_FRAMEBUFFER_HEIGHT);
                System.out.println(window.getWidth());
                System.out.println(window.getHeight());
            }

        }
        glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT); // clear the framebuffer
//
        colorNormals.bind();
        fbo.bind();
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
        sobelShader.setUniform("sampler", 0);
        sobelShader.setUniform("projectionMatrix", projectionMatrix);
        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, colorTexture.getId());
        for (RenderEntity entity : entities) {
            sobelShader.setUniform("viewMatrix", camera.getViewMatrix().mul(entity.getWorldMatrix()));

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
