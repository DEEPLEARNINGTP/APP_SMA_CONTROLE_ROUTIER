package com.traffic.utils;

import com.traffic.model.SimulationContext;
import com.traffic.model.VehicleModel;
import jade.wrapper.ContainerController;
import jade.wrapper.AgentController;

import java.util.Random;

/**
 * Poisson generator for a single direction. We run one generator per lane so that
 * arrivals can happen concurrently in all directions.
 */
public class PoissonGenerator implements Runnable {
    private double lambda; // arrivals per second for this lane
    private volatile boolean running = true;
    private ContainerController container;
    private Random rng = new Random();
    private int idCounter = 0;
    private int lane;

    public PoissonGenerator(int lane, double lambda, ContainerController container){ this.lane = lane; this.lambda = lambda; this.container = container; }

    private long nextInterarrivalMillis(){
        double u = rng.nextDouble();
        double interval = -Math.log(1.0 - u) / lambda; // seconds
        return Math.max(20, (long)(interval*1000));
    }

    @Override
    public void run(){
        try{
            while(running){
                long sleep = nextInterarrivalMillis();
                Thread.sleep(sleep);
                String vid = "veh-" + lane + "-" + (idCounter++);
                VehicleModel vm;
                // positions tuned to GUI center 280x280 approx (window 560x560)
                switch(lane){
                    case 0: vm = new VehicleModel(vid, 280, -40, 0); break; // from N, move down
                    case 1: vm = new VehicleModel(vid, 600, 280, 1); break; // from E, move left
                    case 2: vm = new VehicleModel(vid, 280, 600, 2); break; // from S, move up
                    default: vm = new VehicleModel(vid, -40, 280, 3); break; // from W, move right
                }
                SimulationContext.getInstance().addVehicle(lane, vm);
                if(container!=null){
                    try{
                        AgentController ac = container.createNewAgent(vm.id, "com.traffic.agents.VehicleAgent", new Object[]{vm});
                        ac.start();
                    }catch(Exception ex){
                        ex.printStackTrace();
                    }
                }
            }
        }catch(InterruptedException e){ Thread.currentThread().interrupt(); }
    }

    public void stop(){ running = false; }
}