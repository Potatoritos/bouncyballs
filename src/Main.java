import game.Game;
import geometry.Cylinder;
import geometry.Line3;
import org.joml.Vector3d;

import static geometry.Geometry.intersectionLineCylinder;

public class Main {
    public static void main(String[] args) throws InterruptedException {

//        long prevTime = System.nanoTime(), curTime;
//        long maxDelta = 0;
//        long avgDelta = 0;
//        for (int i = 0; i < 1000; i++) {
//            curTime = System.nanoTime();
//            long delta = curTime - prevTime;
//            maxDelta = Math.max(maxDelta, delta);
//            avgDelta += delta;
//            prevTime = System.nanoTime();
//            Thread.sleep(1);
//        }
//        System.out.printf("avg: %f\nmax: %f\n", (double)avgDelta/1e9/1000, (double)maxDelta/1e9);

        Line3 line = new Line3(new Vector3d(1,2,3), new Vector3d(-3,-2,-1));
        Cylinder cylinder = new Cylinder(new Vector3d(0,0,0), new Vector3d(0,10,0), 2);
        Vector3d result = new Vector3d();
        System.out.println(intersectionLineCylinder(line, cylinder, result));
        System.out.println(result);
//        Game game = new Game();
//        game.run();
//        game.close();
    }
}
