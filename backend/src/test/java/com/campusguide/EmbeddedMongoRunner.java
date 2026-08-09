package com.campusguide;

public class EmbeddedMongoRunner {
    public static void main(String[] args) throws Exception {
        System.out.println(">>> Starting standalone Embedded MongoDB server...");
        EmbeddedMongoInitializer initializer = new EmbeddedMongoInitializer();
        try {
            initializer.initialize(null);
            System.out.println(">>> Standalone Embedded MongoDB started. Keep this process running.");
            // Keep the main thread alive indefinitely
            while (true) {
                Thread.sleep(10000);
            }
        } catch (Exception e) {
            System.err.println(">>> Standalone Embedded MongoDB failed to run: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
