package com.example.mdp_android;

import java.util.ArrayList;

public class Map {
    // Singleton pattern
    private static Map map;
    public static Map getInstance(){
        if (map == null){
            map = new Map();
        }
        return map;
    }

    private Map(){}

    private final ArrayList<Obstacle> obstacles = new ArrayList<>();

    public ArrayList<Obstacle> getObstacles(){
        return this.obstacles;
    }

    public Obstacle addObstacle(){
        int number = this.obstacles.size() + 1;
        Obstacle newObstacle = new Obstacle(number);
        this.obstacles.add(newObstacle);
        return newObstacle;
    }

    public void removeObstacle(Obstacle obstacle){
        int indexToRemove = obstacle.getNumber() - 1;
        this.obstacles.remove(indexToRemove);
        for (int i = indexToRemove; i < this.obstacles.size(); i++){
            this.obstacles.get(i).setNumber(i + 1);
        }
    }

    public ICoordinate findObstacle(int x, int y){
        for (ICoordinate obstacle: this.obstacles){
            if (obstacle.containsCoordinate(x, y)){
                return obstacle;
            }
        }
        return null;
    }

    public boolean isOccupied(int x, int y, Obstacle obstacle){
        for (Obstacle o: this.obstacles){
            if (o.containsCoordinate(x, y) && o != obstacle){
                return true;
            }
        }
        return false;
    }
}
