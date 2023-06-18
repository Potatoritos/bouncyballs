package game;

import org.joml.Vector4f;

public class Colors {
    public static Vector4f hexRGBA(int hexCode) {
        return new Vector4f(((hexCode>>24) & 0xFF)/255f, ((hexCode>>16) & 0xFF)/255f, ((hexCode>>8) & 0xFF)/255f, (hexCode & 0xFF)/255f);
    }
    public static final Vector4f tile = hexRGBA(0xeef0f2ff);
    public static final Vector4f background = hexRGBA(0x8ea2adff);
    public static final Vector4f backgroundDarker = hexRGBA(0x2b3134ff);
    public static final Vector4f red = hexRGBA(0xff5b5bff);
    public static final Vector4f blue = hexRGBA(0x8884ffff);
    public static final Vector4f green = hexRGBA(0x7ac756ff);
    public static final Vector4f black = hexRGBA(0x000000ff);
    public static final Vector4f pink = hexRGBA(0xe784ffff);
    public static final Vector4f[] base = {red, blue, green};
}
