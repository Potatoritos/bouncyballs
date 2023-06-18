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

    private long nvg;
    private int font;

    public Game() {
        isRunning = true;
    }
    public void run() {
        GLFWErrorCallback.createPrint(System.err).set();
        if (!glfwInit()) {
            throw new IllegalStateException("Unable to initialize GLFW");
        }
        window = new Window();
        nvg = nvgCreate(NVG_ANTIALIAS | NVG_STENCIL_STROKES);
        font = nvgCreateFont(nvg, "montserrat", "assets/fonts/Montserrat-Bold.otf");

        gameScene = new GameScene(window.getWidth(), window.getHeight());

        loop();
    }
    private void update() {
        window.update();
        if (window.shouldClose()) {
            isRunning = false;
        }

        gameScene.update(window.input);
    }
    private void render() {
        if (window.isResized()) {
            glViewport(0, 0, window.getWidth(), window.getHeight());
            window.setResized(false);

            gameScene.handleWindowResize(window.getWidth(), window.getHeight());

        }
        FrameBufferObject.unbind();

        gameScene.render();

        nvgBeginFrame(nvg, window.getWidth(), window.getHeight(), 1);
        gameScene.nvgRender(nvg);
        nvgEndFrame(nvg);

        glfwSwapBuffers(window.getHandle()); // swap the color buffers
    }
    private void loop() {
        long previousTime = System.nanoTime(), currentTime;
        long updateDelta = 0, renderDelta = 0;
        long nsPerUpdate = (long)(1e9 / 144), nsPerRender = (long)(1e9 / 144);

        int updateCount = 0, renderCount = 0;
        long previousFPSCalcTime = System.nanoTime();

//        long updateTime = 0;
//        int updateTimeCounter = 0;

        while (isRunning) {
            currentTime = System.nanoTime();

            updateDelta += currentTime - previousTime;
            renderDelta += currentTime - previousTime;

            while (updateDelta >= nsPerUpdate) {
                long time = System.nanoTime();
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
