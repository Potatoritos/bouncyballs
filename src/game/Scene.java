package game;

import graphics.Camera;
import org.joml.Matrix4f;

public abstract class Scene {
    protected float fov;
    protected float zNear;
    protected float zFar;
    protected Camera camera;
    protected final Matrix4f projectionMatrix;
    public Scene() {
        fov = (float)Math.PI/3;
        zNear = 0.01f;
        zFar = 100;
        camera = new Camera();
        projectionMatrix = new Matrix4f();
    }
    public void onWindowResize(int width, int height) {
        projectionMatrix.identity().perspective(fov, (float)width/height, zNear, zFar);
    }
    public abstract void update();
    public abstract void render();
    public abstract void delete();
}
