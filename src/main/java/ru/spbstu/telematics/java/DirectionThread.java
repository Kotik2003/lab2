package ru.spbstu.telematics.java;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class DirectionThread extends Thread {

    private final Queue<Car> carsOnDirection = new ArrayDeque<>();
    private final Lock carRideLock = new ReentrantLock(true);
    private final Lock carAddLock = new ReentrantLock(true);
    private static final Lock mainLock = new ReentrantLock(true);
    private final Direction direction;
    private final Condition condition;

    public Direction getDirection() {
        return direction;
    }

    public DirectionThread(Direction direction) {
        this.direction = direction;
        condition = carAddLock.newCondition();
    }

    public void addCar(Car car) {
        carAddLock.lock();
        try {
            carsOnDirection.add(car);
            System.out.println("Added " + car + " to " + direction + " direction");
            condition.signal();
        } finally {
            carAddLock.unlock();
        }

    }


    public boolean isIntersect(Direction direction) {
        switch (getDirection()) {
            case SN -> {
                return direction == Direction.WE || direction == Direction.ES || direction == Direction.SN;
            }
            case NS -> {
                return direction == Direction.WE || direction == Direction.ES;
            }
            case WE -> {
                return direction == Direction.NS || direction == Direction.ES || direction == Direction.SN;
            }
            case ES -> {
                return direction == Direction.SN || direction == Direction.WE;
            }
        }
        throw new IllegalArgumentException();
    }

    public Lock getCarRideLock() {
        return carRideLock;
    }

    @Override
    public void run() {
        while (true) {
            carAddLock.lock();
            try {
                if (carsOnDirection.isEmpty()) {
                    try {
                        condition.await();
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            } finally {
                carAddLock.unlock();
            }


            Car car = carsOnDirection.poll();
            if (car.getDirection() == null) {
                break;
            }

            mainLock.lock();
            try {
                for (var entry : Main.getDirectionThreadMap().entrySet()) {
                    if (isIntersect(entry.getKey())) {
                        System.out.println("Locked direction " + entry.getKey() + " by " + car);
                        entry.getValue().getCarRideLock().lock();
                    }
                }
                carRideLock.lock();
            } finally {
                mainLock.unlock();
            }
            try {
                Thread.sleep(300 + ThreadLocalRandom.current().nextInt(500));
                System.out.println(car + " has ridden away");
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } finally {
                carRideLock.unlock();
                for (var entry : Main.getDirectionThreadMap().entrySet()) {
                    if (isIntersect(entry.getKey())) {
                        entry.getValue().getCarRideLock().unlock();
                        System.out.println("Unlocked direction " + entry.getKey() + " by " + car);
                    }
                }
            }

        }
    }
}