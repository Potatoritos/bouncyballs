package graphics;

import game.Colors;
import game.UIButton;
import org.joml.Vector4f;
import org.lwjgl.nanovg.NVGColor;
import org.lwjgl.nanovg.NVGPaint;

import static math.MathUtil.cubicInterpolation;
import static org.lwjgl.nanovg.NanoVG.*;
import static org.lwjgl.nanovg.NanoVGGL3.*;

/**
 * Provides functions for drawing using NanoVG
 */
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

    // functions to help with getting the positions of ui elements
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

    /**
     * Draws an image
     * @param image the image
     * @param x the x-coordinate of the image
     * @param y the y-coordinate of the image
     * @param scale the scale to draw the image at
     */
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

    /**
     * Fills a rectangle
     * @param x the x-coordinate of the rectangle
     * @param y the y-coordinate of the rectangle
     * @param width the width of hte rectangle
     * @param height the height of the rectangle
     */
    public void fillRect(float x, float y, float width, float height) {
        nvgBeginPath(handle);
        nvgRect(handle, x, y, width, height);
        nvgFill(handle);
        nvgClosePath(handle);
    }
    /**
     * Draws the outline of a rectangle
     * @param x the x-coordinate of the rectangle
     * @param y the y-coordinate of the rectangle
     * @param width the width of hte rectangle
     * @param height the height of the rectangle
     */
    public void drawRect(float x, float y, float width, float height) {
        nvgBeginPath(handle);
        nvgRect(handle, x, y, width, height);
        nvgStroke(handle);
        nvgClosePath(handle);
    }
    /**
     * Draws a line
     * @param x1 the x-coordinate of the line's starting position
     * @param y1 the y-coordinate of the line's starting position
     * @param x2 the x-coordinate of the line's ending position
     * @param y2 the y-coordinate of the line's ending position
     */
    public void drawLine(float x1, float y1, float x2, float y2) {
        nvgBeginPath(handle);
        nvgMoveTo(handle, x1, y1);
        nvgLineTo(handle, x2, y2);
        nvgStroke(handle);
        nvgClosePath(handle);
    }

    /**
     * Fills a circle
     * @param x the x-coordinate of the circle
     * @param y the y-coordinate of the circle
     * @param r the radius of the circle
     */
    public void fillCircle(float x, float y, float r) {
        nvgBeginPath(handle);
        nvgCircle(handle, x, y, r);
        nvgFill(handle);
        nvgClosePath(handle);
    }
    public void setStrokeWidth(float width) {
        nvgStrokeWidth(handle, width);
    }

    /**
     * Scales a value to the width
     * @param size the value
     * @return the scaled value
     */
    public float scaledWidthSize(float size) {
        return size * width / 1920;
    }

    /**
     * Scales a value to the height
     * @param size the height
     * @return the scaled value
     */
    public float scaledHeightSize(float size) {
        return size * height / 1080;
    }

    /**
     * Adjusts a value so that it matches with the changing dimensions
     * of a level when the window is resized
     * @param x the value
     * @return the adjusted value
     */
    public float adjustedSceneX(float x) {
        return (x - 1920*0.5f) * height/1080f + width*0.5f;
    }

    /**
     * Indicates that a rendering frame is starting
     * @param width the width of the window
     * @param height the height of the window
     */
    public void beginFrame(int width, int height) {
        this.width = width;
        this.height = height;
        nvgBeginFrame(handle, width, height, 1);
    }

    /**
     * Indicates that a rendering frame is ending
     */
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

    /**
     * Draws text
     * @param x the x-coordinate of the text
     * @param y the y-coordinate of hte text
     * @param text the text to draw
     */
    public void drawText(float x, float y, String text) {
        nvgText(handle, x, y, text);
    }

    /**
     * Renders a UIButton
     * @param button the button
     * @param textX the x-position of the button if it were on a 1920x1080 screen
     */
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
