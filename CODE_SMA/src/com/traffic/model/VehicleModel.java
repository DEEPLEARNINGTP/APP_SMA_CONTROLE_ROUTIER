package com.traffic.model;

public class VehicleModel {
    public double x,y; // pixel coords
    public int lane; // 0..3
    public boolean active = true;
    public int width = 28, height = 16;
    public String id;
    public int turn = 0;
    public boolean passedIntersection = false;
    public double speed = 0;
    public double maxSpeed = 3.0;
    public double acceleration = 0.1;
    public double brakingDistance = 50;
    public double followingDistance = 30;
    public boolean isVertical;

    public VehicleModel(String id, double x, double y, int lane){ 
        this.id=id; this.x=x; this.y=y; this.lane=lane; 
    }
}