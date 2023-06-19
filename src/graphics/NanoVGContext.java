package graphics;

import game.Colors;
import game.UIButton;
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

    private int montserratBold;
    private int montserrat;

    private final NVGColor nvgColor1;
    private final NVGColor nvgColor2;

    private int width;
    private int height;
    public NanoVGContext() {
        handle = nvgCreate(NVG_ANTIALIAS | NVG_STENCIL_STROKES);

        escapeImage = new NanoVGImage(handle, "assets/images/escape.png", 83, 68);
        mouse1Image = new NanoVGImage(handle, "assets/images/mouse1.png", 83, 68);
        mousewheelImage = new NanoVGImage(handle, "assets/images/mousewheel.png", 83, 104);

        montserratBold = nvgCreateFont(handle, "montserrat_bold", "assets/fonts/Montserrat-Bold.otf");
        montserrat = nvgCreateFont(handle, "montserrat", "assets/fonts/Montserrat-Medium.otf");

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
            nvgRect(handle, x, y, scaledWidthSize(image.getWidth())*scale, scaledWidthSize(image.getHeight())*scale);
            nvgImagePattern(handle, x, y, scaledWidthSize(image.getWidth()*scale), scaledWidthSize(image.getHeight()*scale), 0, image.getHandle(), 1, imagePaint);
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
    public void drawRect(float x, float y, float width, float height) {
        nvgBeginPath(handle);
        nvgRect(handle, x, y, width, height);
        nvgStroke(handle);
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
    public float scaledWidthSize(float size) {
        return size * width / 1920;
    }
    public float scaledHeightSize(float size) {
        return size * height / 1080;
    }
    public float adjustedSceneX(float x) {
        return (x - 1920*0.5f) * height/1080f + width*0.5f;
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
        nvgFontSize(handle, size);
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
    public void renderButton(UIButton button, float textX) {
        float hoverPadding = 5;
        if (button.isHoveredOver()) {
            setFillColor(Colors.backgroundDarker);
            fillRect((float)button.geometry.position.x-hoverPadding, (float)button.geometry.position.y-hoverPadding, (float)button.geometry.displacement.x+2*hoverPadding, (float)button.geometry.displacement.y+2*hoverPadding);
            setFillColor(Colors.tile);
            setFontFace("montserrat_bold");
            setFontSize(scaledHeightSize(50));
            drawText(adjustedSceneX(textX + 20), (float)button.geometry.y1() + scaledHeightSize(60), button.getText());

            setFontFace("montserrat");
            setFontSize(scaledHeightSize(90));
            drawText(adjustedSceneX(textX + 20), (float)button.geometry.y2() - scaledHeightSize(20), button.getSecondaryText());
        } else {
            setStrokeColor(Colors.black);
            setStrokeWidth(scaledHeightSize(4));
            drawRect((float)button.geometry.position.x, (float)button.geometry.position.y, (float)button.geometry.displacement.x, (float)button.geometry.displacement.y);
            setFillColor(Colors.backgroundDarker);
            setFontFace("montserrat_bold");
            setFontSize(scaledHeightSize(50));
            drawText(adjustedSceneX(textX + 20), (float)button.geometry.y1() + scaledHeightSize(60), button.getText());

            setFontFace("montserrat");
            setFontSize(scaledHeightSize(90));
            drawText(adjustedSceneX(textX + 20), (float)button.geometry.y2() - scaledHeightSize(20), button.getSecondaryText());
        }
    }
}
