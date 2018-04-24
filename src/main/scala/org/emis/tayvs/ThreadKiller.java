package org.emis.tayvs;

public class ThreadKiller {

    public static void main(String[] args) throws Exception {
        Thread th = new Thread(() -> {
            try {
                Thread.sleep(10000000);
            } catch (Exception ignored) {}
        }, "hello");
        th.start();
        th.interrupt();
        System.out.println("thread created");
        Thread.sleep(1000000);

    }

}
