package game;

import graphics.*;
import graphics.Window;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFWErrorCallback;
import util.Util;

import static geometry.MeshGeometry.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL30.*;
import static geometry.Geometry.*;

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
    private ShaderProgram lightShader;
    private ShaderProgram outlineShader;
    RenderEntity sphere;
    RenderEntity prism;
    RenderEntity prism2;
    RenderEntity[] entities;
    private Matrix4f projectionMatrix;
    private PerspectiveCamera camera;

    private RenderEntity textureRect;

    private EmptyFbo normalFbo;

    private int colorTextureId;

    private double rotationX;
    private double rotationY;
    private float expandFactor;

    private double velX;
    private double velY;

    private LevelScene levelScene;
    private InputState inputMap;

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
        lightShader = ShaderProgram.fromFile("light.glsl");
        outlineShader = ShaderProgram.fromFile("outline.glsl");

        sphere = new RenderEntity(generateGeodesicPolyhedronMesh(2, new Vector3f(0, 0, 1)));
        sphere.getPosition().x = 1f;
        sphere.getPosition().z = 0.5f;
        prism = new RenderEntity(rectangularPrismMesh(new Vector3f(-0.5f, -4f, -0.5f), new Vector3f(1, 1, 1), new Vector3f(1, 0, 0)));
        prism.getPosition().x = -1f;
        prism.getPosition().z = 0.5f;
//        prism.getRotation().x = (float)Math.PI/4;
//        prism.getRotation().y = (float)Math.toRadians(60)/2;

        prism2 = new RenderEntity(rectangularPrismMesh(new Vector3f(-4f, -4f, -0.5f), new Vector3f(8, 8, 1), new Vector3f(0, 1, 0)));
//        prism2.getPosition().x = 1f;
        prism2.getPosition().z = -0.5f;

//        sphere.getPosition().y = -1f;
//        entities = new RenderEntity[] {sphere, prism, prism2};
        entities = new RenderEntity[] {prism, prism2, sphere};
        projectionMatrix = new Matrix4f()
                .perspective(fov, window.getAspectRatio(), zNear, zFar);

        camera = new PerspectiveCamera();
        camera.position.z = 20;

        normalFbo = new EmptyFbo(window.getWidth(), window.getHeight());

        textureRect = new RenderEntity(texturedRectangle(new Vector2f(0, 0), new Vector2f(1, 1), normalFbo.getColorTexture()));
        textureRect.getPosition().x = -2.5f;
//        textureRect.setScale(1f);

        levelScene = new LevelScene();
        levelScene.loadLevel(Level.fromFile("level0.txt"));
        inputMap = new InputState();

        loop();
    }
    private void update() {
        window.update();
        if (window.shouldClose()) {
            isRunning = false;
        }

//        rotation++;
//        rotation %= 720;

        expandFactor = 0.03f;
//        expandFactor = (float)Math.sin(Math.toRadians(rotation)*2)*0.5f+0.5f;

//        prism.getRotation().y = (float)Math.toRadians(rotation);
//        prism.getPosition().x = 1;//(float)Math.sin(Math.toRadians(rotation)*2);
//        prism.getRotation().z = (float)Math.toRadians(rotation)/2;

        int minWindowDimension = Math.min(window.getHeight(), window.getWidth());
        double mouseX = (window.getMouseX() - (window.getWidth() - minWindowDimension)/2.0)/minWindowDimension;
        double mouseY = (window.getMouseY() - (window.getHeight() - minWindowDimension)/2.0)/minWindowDimension;
        inputMap.getMousePosition().set(mouseX, mouseY);

        levelScene.update(inputMap);

        rotationX = (Util.cutMaxMin(mouseY, 0, 1)-0.5) * Math.PI / 3;
        rotationY = (Util.cutMaxMin(mouseX, 0, 1)-0.5) * Math.PI / 3;

        velX += Math.sin(rotationY)*0.02;
        velY += -Math.sin(rotationX)*0.02;

        velX = Util.cutMaxMin(velX, -0.2, 0.2);
        velY = Util.cutMaxMin(velY, -0.2, 0.2);

//        System.out.printf("a %f, %f\n", velX, velY);

        sphere.getPosition().x += velX;
        sphere.getPosition().y += velY;

        sphere.getPosition().x = Util.cutMaxMin(sphere.getPosition().x, -5, 5);
        sphere.getPosition().y = Util.cutMaxMin(sphere.getPosition().y, -5, 5);

//        System.out.printf("a %f, %f\n", Math.toDegrees(rotationX), Math.toDegrees(rotationY));
    }
    private void render() {
        if (window.isResized()) {
            glViewport(0, 0, window.getWidth(), window.getHeight());
            window.setResized(false);

            projectionMatrix = projectionMatrix.identity()
                    .perspective(fov, window.getAspectRatio(), zNear, zFar);

            normalFbo.resize(window.getWidth(), window.getHeight());

            levelScene.handleWindowResize(window.getWidth(), window.getHeight());

        }
        FrameBufferObject.unbind();
        /*
        glClearColor(1.0f, 1.0f, 1.0f, 1f);
        glEnable(GL_STENCIL_TEST);
        glStencilOp(GL_KEEP, GL_KEEP, GL_REPLACE);
        glStencilMask(0xFF);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT | GL_STENCIL_BUFFER_BIT); // clear the framebuffer
        */



//
//        colorNormals.bind();
//        normalFbo.bind();
//        glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
//        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
//////        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, GL_TEXTURE_2D, texture.getId(), 0);
//
//        colorNormals.setUniform("projectionMatrix", projectionMatrix);
////        sp.setUniform("color", new Vector3f(0.5f, 0, 0));
//        for (RenderEntity entity : entities) {
//            colorNormals.setUniform("viewMatrix", camera.getViewMatrix().mul(entity.getWorldMatrix()));
//            entity.getMesh().render();
//        }
//
//        FrameBufferObject.unbind();

//        for (RenderEntity entity : entities) {
//            colorNormals.setUniform("viewMatrix", camera.getViewMatrix().mul(entity.getWorldMatrix()));
//            entity.getMesh().render();
//        }

//        colorNormals.unbind();

//        sobelShader.bind();
//        sobelShader.setUniform("normalTexture", 0);
//        sobelShader.setUniform("depthTexture", 1);
//        sobelShader.setUniform("projectionMatrix", projectionMatrix);

//        glDisable(GL_DEPTH_TEST);

//        lightShader.bind();
//        lightShader.setUniform("projectionMatrix", projectionMatrix);

//        glActiveTexture(GL_TEXTURE0);
//        normalFbo.getColorTexture().bind();
//        glActiveTexture(GL_TEXTURE1);
//        normalFbo.getDepthTexture().bind();

        /*
        glStencilFunc(GL_ALWAYS, 1, 0xFF);
        glStencilMask(0xFF);

        colorNormals.bind();
        colorNormals.setUniform("projectionMatrix", projectionMatrix);
        for (RenderEntity entity : entities) {
            colorNormals.setUniform("viewMatrix", camera.getViewMatrix().mul(entity.getWorldMatrix((float)rotationX, (float)rotationY)));
            entity.getMesh().render();
        }
        colorNormals.unbind();

        glStencilFunc(GL_NOTEQUAL, 1, 0xFF);
        glStencilMask(0x00);
//        glDisable(GL_DEPTH_TEST);

        outlineShader.bind();
        outlineShader.setUniform("projectionMatrix", projectionMatrix);
        for (RenderEntity entity : entities) {
            outlineShader.setUniform("expand", expandFactor);
            outlineShader.setUniform("viewMatrix", camera.getViewMatrix().mul(entity.getWorldMatrix((float)rotationX, (float)rotationY)));
            entity.getMesh().render();
        }
//        glEnable(GL_DEPTH_TEST);

        outlineShader.unbind();
        glDisable(GL_STENCIL_TEST);
//
//        textureShader.bind();
//
//        textureShader.setUniform("textureSampler", 0);
//        textureShader.setUniform("projectionMatrix", projectionMatrix);
//        textureShader.setUniform("viewMatrix", camera.getViewMatrix().mul(textureRect.getWorldMatrix()));
//        textureRect.getMesh().render();

//        textureShader.setUniform("viewMatrix", camera.getViewMatrix().mul(textureRect2.getWorldMatrix()));
//        textureRect2.getMesh().render();

//        textureShader.unbind();

         */

        levelScene.render();

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
//                updateCount++;
            }

            if (renderDelta >= nsPerRender) {
                render();
                renderDelta = 0;
//                renderCount++;
            }

            previousTime = currentTime;

            // Calculate performance stats
//            if (currentTime - previousFPSCalcTime >= 1e9) {
//                System.out.printf("Update FPS: %d | Render FPS: %d \n", updateCount, renderCount);
//                previousFPSCalcTime = currentTime;
//                updateCount = 0;
//                renderCount = 0;
//            }
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
