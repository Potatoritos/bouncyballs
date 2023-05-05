import game.Game;

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

        Game game = new Game();
        game.run();
        game.close();
    }
}
