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
    private float radius;
    private float nearPlane;
    private float farPlane;
    public final Vector3f lastSourcePosition;
    public ShadowMap(int width, int height, float radius, float nearPlane, float farPlane) {
        this.width = width;
        this.height = height;
        this.radius = radius;
        this.nearPlane = nearPlane;
        this.farPlane = farPlane;
        depthMap = new DepthMapFbo(width, height);
        lightProjection = new Matrix4f();
        lightView = new Matrix4f();
        lightSpaceMatrix = new Matrix4f();
        lastSourcePosition = new Vector3f();
        setSourcePosition(new Vector3f(0, 0, 4));
        updateLightSpaceMatrix();
    }
    public void setRadius(float value) {
        radius = value;
    }
    public void setNearPlane(float value) {
        nearPlane = value;
    }
    public void setFarPlane(float value) {
        farPlane = value;
    }
    public void setSourcePosition(Vector3f position) {
        this.lastSourcePosition.set(position);
        lightView.setLookAt(
                position,
                new Vector3f(0, 0, 0),
                new Vector3f(0, 1, 0)
        );
    }
    public void updateLightSpaceMatrix() {
        lightProjection.setOrtho(-radius, radius, -radius, radius, nearPlane, farPlane);
        lightSpaceMatrix.set(lightProjection).mul(lightView);
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
