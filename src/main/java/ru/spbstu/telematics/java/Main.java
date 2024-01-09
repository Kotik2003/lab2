package ru.spbstu.telematics.java;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public class Main {
    private static final Map<Direction, DirectionThread> directionThreadMap = new HashMap<>();

    public static Map<Direction, DirectionThread> getDirectionThreadMap() {
        return directionThreadMap;
    }

    public static void main(String[] args) {
        List<Thread> threads = new ArrayList<>();
        for (Direction direction : Direction.values()) {
            getDirectionThreadMap().put(direction, new DirectionThread(direction));
            threads.add(new Thread(() -> {
                int n = 8 + ThreadLocalRandom.current().nextInt(5);
                System.out.println("Cars for " + direction + " direction: " + n);
                for (int i = 0; i < n; i++) {
                    Car car = new Car(direction);
                    System.out.println("New " + car);
                    directionThreadMap.get(direction).addCar(car);
                    try {
                        Thread.sleep(100 + ThreadLocalRandom.current().nextInt(500));
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
                directionThreadMap.get(direction).addCar(new Car(null));
            }));
        }


        for (Thread thread : threads) {
            thread.start();
        }

        for (DirectionThread thread : directionThreadMap.values()) {
            thread.start();
        }

    }
}
