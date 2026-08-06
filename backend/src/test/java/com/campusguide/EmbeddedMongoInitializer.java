package com.campusguide;

import de.flapdoodle.embed.mongo.MongodExecutable;
import de.flapdoodle.embed.mongo.MongodProcess;
import de.flapdoodle.embed.mongo.MongodStarter;
import de.flapdoodle.embed.mongo.config.MongodConfig;
import de.flapdoodle.embed.mongo.config.Net;
import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.process.runtime.Network;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;

public class EmbeddedMongoInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    private static MongodProcess mongodProcess;
    private static MongodExecutable mongodExecutable;

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        synchronized (EmbeddedMongoInitializer.class) {
            if (mongodProcess == null) {
                try {
                    System.out.println(">>> Starting Embedded MongoDB for tests on port 27017...");
                    MongodConfig mongodConfig = MongodConfig.builder()
                            .version(Version.Main.PRODUCTION)
                            .net(new Net(27017, Network.localhostIsIPv6()))
                            .build();

                    MongodStarter starter = MongodStarter.getDefaultInstance();
                    mongodExecutable = starter.prepare(mongodConfig);
                    mongodProcess = mongodExecutable.start();
                    System.out.println(">>> Embedded MongoDB started successfully!");

                    Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                        System.out.println(">>> Stopping Embedded MongoDB...");
                        if (mongodProcess != null) {
                            mongodProcess.stop();
                        }
                        if (mongodExecutable != null) {
                            mongodExecutable.stop();
                        }
                        System.out.println(">>> Embedded MongoDB stopped.");
                    }));
                } catch (Exception e) {
                    System.err.println(">>> Failed to start Embedded MongoDB: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }
    }
}
