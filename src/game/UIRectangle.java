package game;

public class UIRectangle {
    private float x;
    private float y;
    private float width;
    private float height;
    private float padding;
    public UIRectangle() {

    }
    public UIRectangle(float x, float y, float width, float height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }
    public void setX(float value) {
        x = value;
    }
    public float getX() {
        return x - padding;
    }
    public void setY(float value) {
        y = value;
    }
    public float getY() {
        return y - padding;
    }
    public void setWidth(float value)  {
        width = value;
    }
    public float getWidth() {
        return width + 2*padding;
    }
    public void setHeight(float value) {
        height = value;
    }
    public float getHeight() {
        return height + 2*padding;
    }
    public void setPadding(float value) {
        this.padding = value;
    }
    public float getPadding() {
        return padding;
    }
}
