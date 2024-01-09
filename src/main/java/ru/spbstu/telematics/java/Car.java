package ru.spbstu.telematics.java;

public class Car {
    private final Direction direction;

    public Car(Direction direction) {
        this.direction = direction;
    }

    public Direction getDirection() {
        return this.direction;
    }

    public String toString() {
        return "Car: " + this.direction;
    }
}