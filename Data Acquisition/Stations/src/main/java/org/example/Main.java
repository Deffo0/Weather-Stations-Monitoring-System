package org.example;

public class Main {
    public static void main(String[] args) {
        System.out.println(args[0] + " " + args[1] + " " + args[2]);
        WeatherStationProducer produce = new WeatherStationProducer(args[0], args[1], args[2]);
//        WeatherStationProducer produce = new WeatherStationProducer("1","50.6","63.7");
        produce.produce();
    }
}