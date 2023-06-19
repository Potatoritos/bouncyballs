package game;

import shape.Line2;

public class UIButton {
    public final Line2 geometry;
    private boolean isHoveredOver;
    private boolean isClicked;
    private String text;
    private String secondaryText;
    public UIButton() {
        geometry = new Line2();
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
        isHoveredOver = geometry.isPointInRectangle(input.mousePosition);
        if (isHoveredOver) {
            System.out.println("edfwefwef");
        }
        isClicked = isHoveredOver && input.isSelectLevelPressed();
    }
}
