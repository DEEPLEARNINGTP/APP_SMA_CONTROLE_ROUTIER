package com.traffic.model;

import jade.wrapper.ContainerController;
import com.traffic.gui.SimulationGUI;
import com.traffic.model.VehicleModel;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class SimulationContext {
    private static SimulationContext instance = new SimulationContext();
    private SimulationGUI gui;
    private ContainerController containerController;

    // four lanes: 0=NORTH,1=EAST,2=SOUTH,3=WEST
    private List<VehicleModel> north = new CopyOnWriteArrayList<>();
    private List<VehicleModel> east = new CopyOnWriteArrayList<>();
    private List<VehicleModel> south = new CopyOnWriteArrayList<>();
    private List<VehicleModel> west = new CopyOnWriteArrayList<>();

    private SimulationContext() {}
    public static SimulationContext getInstance(){ return instance; }

    public void setGui(SimulationGUI gui){ this.gui = gui; }
    public SimulationGUI getGui(){ return gui; }

    public void setContainerController(ContainerController c){ this.containerController = c; }
    public ContainerController getContainerController(){ return containerController; }

    public List<VehicleModel> getLane(int lane){
        switch(lane){
            case 0: return north;
            case 1: return east;
            case 2: return south;
            default: return west;
        }
    }

    public void addVehicle(int lane, VehicleModel v){ getLane(lane).add(v); if(gui!=null) gui.repaint(); }
    public void removeVehicle(int lane, VehicleModel v){ getLane(lane).remove(v); if(gui!=null) gui.repaint(); }
    
    
}