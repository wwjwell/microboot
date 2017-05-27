package com.zhuanglide.micrboot.demo;

import com.zhuanglide.micrboot.Server;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Created by wwj on 17/3/6.
 */

public class Main {
    private static volatile boolean running = true;

    public static void main(String[] args) {
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("classpath:api.xml");
        final Server server = context.getBean(Server.class);

        Thread thread = new Thread(new Runnable() {
            public void run() {
                server.start();
            }
        });
        thread.setDaemon(true);
        thread.start();

        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            public void run() {
                synchronized (Main.class) {
                    server.shutdown();
                    running = false;
                    Main.class.notify();
                }
            }
        }));
        synchronized (Main.class) {
            while (running) {
                try {
                    Main.class.wait();
                } catch (Throwable e) {}
            }
        }
    }
}
