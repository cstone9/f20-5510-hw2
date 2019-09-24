import edu.vt.ece.bench.ThreadId;
import edu.vt.ece.locks.Bakery;
import edu.vt.ece.locks.LBakery;
import edu.vt.ece.locks.Lock;
import edu.vt.ece.locks.TreePeterson;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.stream.IntStream;

import static java.time.Duration.ofMillis;
import static java.time.Duration.ofSeconds;
import static org.junit.jupiter.api.Assertions.*;


class SubmissionTest1 {

    @Test
    void TestLBakery_NoMoreThanL() throws InterruptedException {
        int l = 4, n = 10;
        ExecutorService executorService = getExecutorService(l);
        Lock lBakery = new LBakery(l, n);
        IntStream.range(0, l)
                .forEach(user -> executorService.execute(lBakery::lock));
        executorService.shutdown();

        Thread t = new Thread(lBakery::lock);
        t.start();
        Thread.sleep(1000);
        assertTrue(t.isAlive());
        t.interrupt();
    }

    @Test
    void TestLBakery_Atleast1() {
        int l = 1, n = 10;
        Lock lBakery = new LBakery(l, n);
        assertTimeout(ofMillis(10), lBakery::lock);
    }

    @Test
    void TestLBakery_AtleastL() {
        int l = 4, n = 10;
        ExecutorService executorService = getExecutorService(l);
        Lock lBakery = new LBakery(l, n);
        IntStream.range(0, l - 1)
                .forEach(user -> executorService.execute(lBakery::lock));
        executorService.shutdown();

        assertTimeout(ofMillis(10), lBakery::lock);
    }

    @Test
    void TestBinaryTreePeterson_BoundedTimeLocking() {
        int n = 8;
        ExecutorService executorService = getExecutorService(n);
        Lock tPeterson = new TreePeterson(n);
        IntStream.range(0, n-1)
                .forEach(user -> executorService.execute(() -> {
                    tPeterson.lock();
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    tPeterson.unlock();
                }));
        executorService.shutdown();

        assertTimeout(ofSeconds(1), tPeterson::lock);
        tPeterson.unlock();
    }

    @Test
    void TestBinaryTreePeterson_NoMoreThan1() throws InterruptedException {
        int n = 8;
        Lock tPeterson = new TreePeterson(n);
        tPeterson.lock();

        Thread t = new Thread(tPeterson::lock);
        t.start();
        Thread.sleep(1000);
        assertTrue(t.isAlive());

        tPeterson.unlock();
        Thread.sleep(100);
        assertFalse(t.isAlive());
    }

    @Test
    void TestBakery_BoundedTimeLocking() {
        int n = 8;
        ExecutorService executorService = getExecutorService(n);
        Lock bakery = new Bakery(n);
        IntStream.range(0, n-1)
                .forEach(user -> executorService.execute(() -> {
                    bakery.lock();
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    bakery.unlock();
                }));
        executorService.shutdown();

        assertTimeout(ofSeconds(1), bakery::lock);
        bakery.unlock();
    }

    @Test
    void TestBakery_NoMoreThan1() throws InterruptedException {
        int n = 8;
        Lock bakery = new Bakery(n);
        bakery.lock();

        UnitTestThread t = new UnitTestThread(bakery::lock);
        t.start();
        Thread.sleep(1000);
        assertTrue(t.isAlive());

        bakery.unlock();
        Thread.sleep(100);
        assertFalse(t.isAlive());
    }

    private ExecutorService getExecutorService(int n) {
        return Executors.newFixedThreadPool(n, new UnitTestThreadFactory());
    }

    static class UnitTestThread extends Thread implements ThreadId {

        private static int ID_GEN = 0;

        public static void reset() {
            ID_GEN = 0;
        }

        private int id;

        UnitTestThread(Runnable runnable) {
            super(runnable);
            id = ID_GEN++;
        }

        @Override
        public int getThreadId() {
            return id;
        }


    }

    static class UnitTestThreadFactory implements ThreadFactory {

        UnitTestThreadFactory() {
            UnitTestThread.reset();
        }

        @Override
        public Thread newThread(Runnable r) {
            return new UnitTestThread(r);
        }
    }
}

