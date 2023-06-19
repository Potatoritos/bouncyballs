package graphics;

import static org.lwjgl.nanovg.NanoVG.NVG_IMAGE_GENERATE_MIPMAPS;
import static org.lwjgl.nanovg.NanoVG.nvgCreateImage;

/**
 * Represents an image used by NanoVG
 */
public class NanoVGImage {
    private int handle;
    private int width;
    private int height;
    public NanoVGImage(long nvg, String path, int width, int height) {
        handle = nvgCreateImage(nvg, path, NVG_IMAGE_GENERATE_MIPMAPS);
        this.width = width;
        this.height = height;
    }
    public int getHandle() {
        return handle;
    }
    public int getWidth() {
        return width;
    }
    public int getHeight() {
        return height;
    }
}
