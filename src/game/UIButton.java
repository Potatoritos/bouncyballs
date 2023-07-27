package game;

import shape.Line2d;

/**
 * Represents a button (seen in the main menu)
 */
public class UIButton {
    public final Line2d geometry;
    private boolean isHoveredOver;
    private boolean isClicked;
    private String text;
    private String secondaryText;
    public UIButton() {
        geometry = new Line2d();
        text = "";
        secondaryText = "";
    }
    public void setText(String text) {
        this.text = text;
    }
    public String getText() {
        return text;
    }
    public void setSecondaryText(String text) {
        secondaryText = text;
    }
    public String getSecondaryText() {
        return secondaryText;
    }
    public boolean isHoveredOver() {
        return isHoveredOver;
    }
    public boolean isClicked() {
        return isClicked;
    }
    public void update(InputState input) {
        isHoveredOver = geometry.isPointInRectangle(input.windowMousePosition);
        isClicked = isHoveredOver && input.isSelectLevelPressed();
    }
}
