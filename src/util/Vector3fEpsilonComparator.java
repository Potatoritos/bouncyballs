package util;

import org.joml.Vector3f;

import java.util.Comparator;

import static util.Util.withinEpsilon;

public class Vector3fEpsilonComparator implements Comparator<Vector3f> {
    @Override
    public int compare(Vector3f a, Vector3f b) {
        if (withinEpsilon(a.x, b.x)) {
            if (withinEpsilon(a.y, b.y)) {
                if (withinEpsilon(a.z, b.z)) {
                    return 0;
                }
                return Float.compare(a.z, b.z);
            }
            return Float.compare(a.y, b.y);
        }
        return Float.compare(a.x, b.x);
    }
}
