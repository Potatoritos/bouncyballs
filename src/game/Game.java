package game;

import graphics.*;
import graphics.Window;
import org.lwjgl.glfw.GLFWErrorCallback;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.nanovg.NanoVG.*;
import static org.lwjgl.nanovg.NanoVGGL3.*;
import static org.lwjgl.opengl.GL30.*;

public class Game {
    private boolean isRunning;
    private Window window;

    private GameScene gameScene;

    private NanoVGContext nvg;
    private long nsPerUpdate;
    private long nsPerRender;

    public Game() {
        isRunning = true;
    }
    public void setUpdateFps(int fps) {
        nsPerUpdate = (long)(1e9 / fps);
    }
    public void setRenderFps(int fps) {
        nsPerRender = (long)(1e9 / fps);
    }
    public void run() {
        GLFWErrorCallback.createPrint(System.err).set();
        if (!glfwInit()) {
            throw new IllegalStateException("Unable to initialize GLFW");
        }
        window = new Window();
        nvg = new NanoVGContext();

        gameScene = new GameScene(window.getWidth(), window.getHeight());

        loop();
    }
    private void update() {
        window.update();
        if (window.shouldClose()) {
            isRunning = false;
            return;
        }

        gameScene.update(window.input);

        if (gameScene.hasRequestedExit()) {
            isRunning = false;
            return;
        }
        setRenderFps(gameScene.getRequestedFpsCap());
        setUpdateFps((int)Math.round(144 * gameScene.getRequestedGameSpeed()));
    }
    private void render() {
        if (window.isResized()) {
            glViewport(0, 0, window.getWidth(), window.getHeight());
            window.setResized(false);

            gameScene.handleWindowResize(window.getWidth(), window.getHeight());

        }
        FrameBufferObject.unbind();

        gameScene.render();

        nvg.beginFrame(window.getWidth(), window.getHeight());
//        nvgScale(nvg, (float)1920/window.getWidth(), (float)1920/window.getWidth());
        gameScene.nvgRender(nvg);
        nvg.endFrame();

        glfwSwapBuffers(window.getHandle()); // swap the color buffers
    }
    private void loop() {
        long previousTime = System.nanoTime(), currentTime;
        long updateDelta = 0, renderDelta = 0;
        setUpdateFps(144);
        setRenderFps(144);

        int updateCount = 0, renderCount = 0;
        long previousFPSCalcTime = System.nanoTime();

//        long updateTime = 0;
//        int updateTimeCounter = 0;

        while (isRunning) {
            currentTime = System.nanoTime();

            updateDelta += currentTime - previousTime;
            renderDelta += currentTime - previousTime;

            while (updateDelta >= nsPerUpdate) {
//                long time = System.nanoTime();
                update();
//                updateTime += System.nanoTime() - time;
//                updateTimeCounter++;

                updateDelta -= nsPerUpdate;
                updateCount++;
            }

//            if (updateTimeCounter >= 60) {
//                System.out.printf("avg update time: %fms\n", 1.0*updateTime/updateTimeCounter / 1e6);
//                updateTimeCounter = 0;
//                updateTime = 0;
//            }

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
        }
    }
    public void close() {
        gameScene.delete();
        window.delete();
        glfwTerminate();
        glfwSetErrorCallback(null).free();
    }
}
