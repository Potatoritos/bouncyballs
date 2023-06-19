package game;

import graphics.NanoVGContext;
import graphics.PerspectiveCamera;
import util.Deletable;

/**
 * A class used to both update and render everything in a scene
 */
public abstract class Scene implements Deletable {
    protected final PerspectiveCamera camera;
    public Scene() {
        camera = new PerspectiveCamera();
        camera.setFov((float)Math.PI/3);
        camera.setZNear(0.01f);
        camera.setZFar(100f);
        camera.setAspectRatio(16f/9);
        camera.updateProjectionMatrix();
    }

    /**
     * Update structures that use window width/height
     */
    public void handleWindowResize(int width, int height) {
        camera.setAspectRatio((float)width/height);
        camera.updateProjectionMatrix();
    }

    public abstract void update(InputState inputMap);

    public abstract void render();

    /**
     * Draw things using NanoVG
     */
    public abstract void nvgRender(NanoVGContext nvg);
    public abstract void delete();
}
