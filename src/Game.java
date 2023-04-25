import graphics.*;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFWErrorCallback;

import java.nio.ByteBuffer;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL30.*;
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
    RenderEntity sphere;
    RenderEntity prism;
    RenderEntity[] entities;
    private Matrix4f projectionMatrix;
    private Camera camera;

    private RenderEntity textureRect;

    private int fbo;
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
//        textureShader.createUniform("textureSampler");
        sphere = new RenderEntity(generateGeodesicPolyhedronMesh(4));
        prism = new RenderEntity(rectangularPrismMesh(new Vector3f(-0.5f, -1f, -0.5f), new Vector3f(1, 2, 1)));
        prism.getPosition().y = 1f;
        prism.getRotation().x = (float)Math.PI/4;
//        sphere.getPosition().y = -1f;
        entities = new RenderEntity[] {sphere, prism};
        projectionMatrix = new Matrix4f()
                .perspective(fov, window.getAspectRatio(), zNear, zFar);

        camera = new Camera();
        camera.getPosition().z = 4;

//        fbo = glGenFramebuffers();
//         glBindFramebuffer(GL_FRAMEBUFFER, fbo);
//
//        colorTextureId = glGenTextures();
//        glBindTexture(GL_TEXTURE_2D, colorTextureId);
//
//        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGB, 800, 600, 0, GL_RGB, GL_UNSIGNED_BYTE, (ByteBuffer)null);
//
//        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
//        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
//
//        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, colorTextureId, 0);
//
//        int x = glCheckFramebufferStatus(GL_FRAMEBUFFER);
//        if (x != GL_FRAMEBUFFER_COMPLETE) {
//            System.out.println("==========");
//            System.out.println(x);
//        }

//        fbo = glGenFramebuffers();

//        glBindFramebuffer(GL_FRAMEBUFFER, fbo);
//        depthTexture = new Texture();
//        texture.setEmptyImage(window.getWidth(), window.getHeight(), GL_DEPTH_COMPONENT);
//        depthTexture.setEmptyImage(window.getWidth(), window.getHeight(), GL_DEPTH_COMPONENT);
//        colorTexture.setEmptyImage(window.getWidth(), window.getHeight(), GL_RGB);


//        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, colorTexture.getId(), 0);
//        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, GL_TEXTURE_2D, depthTexture.getId(), 0);


//        int x = glCheckFramebufferStatus(GL_FRAMEBUFFER);
//        if (x != GL_FRAMEBUFFER_COMPLETE) {
//            System.out.println("WTF");
//            System.out.println(x);
//        }
        colorTexture = new Texture();
        colorTexture.loadImage("assets/image/astolfo_necoarc.png");

        textureRect = new RenderEntity(texturedRectangle(new Vector2f(0, 0), new Vector2f(1, 1), colorTexture));
        textureRect.getPosition().x = -2f;
        textureRect.setScale(1.5f);
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


        textureRect.getRotation().x = (float)Math.toRadians(rotation);
        textureRect.getRotation().z = (float)Math.toRadians(rotation);
        textureRect.getPosition().y = -(float)Math.sin(Math.toRadians(rotation)*2);


//        camera.getRotation().x = (float)Math.toRadians(rotation);
    }
    private void render() {
        if (window.isResized()) {
            glViewport(0, 0, window.getWidth(), window.getHeight());
            window.setResized(false);

            projectionMatrix = new Matrix4f()
                    .perspective(fov, window.getAspectRatio(), zNear, zFar);
        }
        // Set the clear color
        glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT); // clear the framebuffer
//
        colorNormals.bind();
//        glBindFramebuffer(GL_FRAMEBUFFER, fbo);
////        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, GL_TEXTURE_2D, texture.getId(), 0);

        colorNormals.setUniform("projectionMatrix", projectionMatrix);
//        sp.setUniform("color", new Vector3f(0.5f, 0, 0));
//        for (RenderEntity entity : entities) {
//            colorNormals.setUniform("viewMatrix", camera.getViewMatrix().mul(entity.getWorldMatrix()));
//            entity.getMesh().render();
//        }

//        glBindFramebuffer(GL_FRAMEBUFFER, 0);

        for (RenderEntity entity : entities) {
            colorNormals.setUniform("viewMatrix", camera.getViewMatrix().mul(entity.getWorldMatrix()));
            entity.getMesh().render();
        }

        colorNormals.unbind();
//
        textureShader.bind();

        textureShader.setUniform("projectionMatrix", projectionMatrix);
        textureShader.setUniform("viewMatrix", camera.getViewMatrix().mul(textureRect.getWorldMatrix()));
//        textureShader.setUniform("textureSampler", 0);

        textureRect.getMesh().render();

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
