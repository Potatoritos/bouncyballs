package graphics;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import util.Deletable;

public class ShadowMap implements Deletable {
//    public final DepthMapFbo depthMap;
    public final DepthMapFbo depthMap;
    public final Matrix4f lightProjection;
    public final Matrix4f lightView;
    public final Matrix4f lightSpaceMatrix;
    private int width;
    private int height;
    public ShadowMap(int width, int height, float radius) {
        this.width = width;
        this.height = height;
        depthMap = new DepthMapFbo(width, height);
        float nearPlane = 1.0f, farPlane = 7.5f;
        lightProjection = new Matrix4f().ortho(-radius, radius, -radius, radius, nearPlane, farPlane);
        lightView = new Matrix4f().lookAt(
                new Vector3f(0, 0, 4),
                new Vector3f(0, 0, 0),
                new Vector3f(0, 1, 0)
        );
        lightSpaceMatrix = new Matrix4f(lightProjection).mul(lightView);
    }
    public int getWidth() {
        return width;
    }
    public int getHeight() {
        return height;
    }
    public void delete() {
        depthMap.delete();
    }
}
