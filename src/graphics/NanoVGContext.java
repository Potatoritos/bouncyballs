package graphics;

import org.joml.Vector4f;
import org.lwjgl.nanovg.NVGColor;
import org.lwjgl.nanovg.NVGPaint;

import static math.MathUtil.cubicInterpolation;
import static org.lwjgl.nanovg.NanoVG.*;
import static org.lwjgl.nanovg.NanoVGGL3.*;

public class NanoVGContext {
    private long handle;

    public final NanoVGImage escapeImage;
    public final NanoVGImage mouse1Image;
    public final NanoVGImage mousewheelImage;

    private int montserratFont;

    private final NVGColor nvgColor1;
    private final NVGColor nvgColor2;

    private int width;
    private int height;
    public NanoVGContext() {
        handle = nvgCreate(NVG_ANTIALIAS | NVG_STENCIL_STROKES);

        escapeImage = new NanoVGImage(handle, "assets/images/escape.png", 83, 68);
        mouse1Image = new NanoVGImage(handle, "assets/images/mouse1.png", 83, 68);
        mousewheelImage = new NanoVGImage(handle, "assets/images/mousewheel.png", 83, 104);

        montserratFont = nvgCreateFont(handle, "montserrat", "assets/fonts/Montserrat-Bold.otf");

        nvgColor1 = NVGColor.create();
        nvgColor2 = NVGColor.create();
    }
    public float getWidth() {
        return width;
    }
    public float getHeight() {
        return height;
    }
    public float bottom() {
        return height*0.93f;
    }
    public float left() {
        return width*0.05f;
    }
    public float right() {
        return width*0.95f;
    }
    public float top() {
        return height*0.07f;
    }

    public void drawImage(NanoVGImage image, float x, float y, float scale) {
        try (NVGPaint imagePaint = NVGPaint.calloc()) {
            nvgBeginPath(handle);
            nvgRect(handle, x, y, adjustedSize(image.getWidth())*scale, adjustedSize(image.getHeight())*scale);
            nvgImagePattern(handle, x, y, adjustedSize(image.getWidth()*scale), adjustedSize(image.getHeight()*scale), 0, image.getHandle(), 1, imagePaint);
            nvgFillPaint(handle, imagePaint);
            nvgFill(handle);
            nvgClosePath(handle);
        }
    }
    public void fillRect(float x, float y, float width, float height) {
        nvgBeginPath(handle);
        nvgRect(handle, x, y, width, height);
        nvgFill(handle);
        nvgClosePath(handle);
    }
    public void drawLine(float x1, float y1, float x2, float y2) {
        nvgBeginPath(handle);
        nvgMoveTo(handle, x1, y1);
        nvgLineTo(handle, x2, y2);
        nvgStroke(handle);
        nvgClosePath(handle);
    }
    public void fillCircle(float x, float y, float r) {
        nvgBeginPath(handle);
        nvgCircle(handle, x, y, r);
        nvgFill(handle);
        nvgClosePath(handle);
    }
    public void setStrokeWidth(float width) {
        nvgStrokeWidth(handle, width);
    }
    public float adjustedSize(float size) {
        return size * width / 1920;
    }

    public void beginFrame(int width, int height) {
        this.width = width;
        this.height = height;
        nvgBeginFrame(handle, width, height, 1);
    }
    public void endFrame() {
        nvgEndFrame(handle);
    }
    public void setFontFace(String name) {
        nvgFontFace(handle, name);
    }
    public void setFontSize(float size) {
        nvgFontSize(handle, adjustedSize(size));
    }
    public void setTextAlign(int alignment) {
        nvgTextAlign(handle, alignment);
    }
    public void setFillColor(Vector4f color) {
        nvgFillColor(handle, nvgRGBAf(color.x, color.y, color.z, color.w, nvgColor1));
    }
    public void setStrokeColor(Vector4f color) {
        nvgStrokeColor(handle, nvgRGBAf(color.x, color.y, color.z, color.w, nvgColor2));
    }
    public void drawText(float x, float y, String text) {
        nvgText(handle, x, y, text);
    }
}
