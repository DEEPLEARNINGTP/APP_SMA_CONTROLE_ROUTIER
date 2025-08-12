package com.traffic.agents;

import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;
import com.traffic.model.VehicleModel;
import com.traffic.model.SimulationContext;
import com.traffic.gui.SimulationGUI;
import java.util.List;

public class VehicleAgent extends Agent {
    private VehicleModel model;
    private SimulationGUI gui;
    private static final int STOP_DISTANCE = 100;
    private static final int INTERSECTION_SIZE = 80;
    private static final int STOPPING_GAP = 15; // Distance minimale entre véhicules à l'arrêt

    protected void setup() {
        Object[] args = getArguments();
        if(args != null && args.length > 0 && args[0] instanceof VehicleModel) {
            model = (VehicleModel)args[0];
        } else { 
            doDelete(); 
            return; 
        }
        gui = SimulationContext.getInstance().getGui();

        addBehaviour(new TickerBehaviour(this, 60) {
            public void onTick() {
                if(model == null) { 
                    doDelete(); 
                    return; 
                }
                
                String tl = gui.getTrafficLightState(model.lane);
                boolean canGo = "GREEN".equals(tl) || ("YELLOW".equals(tl) && model.passedIntersection);
                
                adjustSpeed(canGo);
                moveVehicle();
                checkOffScreen();
                
                gui.repaint();
            }

            private void adjustSpeed(boolean canGo) {
                List<VehicleModel> laneVehicles = SimulationContext.getInstance().getLane(model.lane);
                int myIndex = laneVehicles.indexOf(model);
                
                if(myIndex > 0) {
                    VehicleModel front = laneVehicles.get(myIndex - 1);
                    double distance = calculateDistance(front);
                    
                    if(front.speed == 0) {
                        // Véhicule devant à l'arrêt
                        if(distance < model.followingDistance + STOPPING_GAP) {
                            model.speed = Math.max(0, model.speed - model.acceleration);
                            
                            if(distance < STOPPING_GAP) {
                                model.speed = 0;
                                maintainStoppingGap(front);
                            }
                            return;
                        }
                    } else if(distance < model.followingDistance) {
                        // Véhicule devant en mouvement
                        model.speed = Math.max(0, model.speed - model.acceleration);
                        return;
                    }
                }
                
                if(!canGo && isApproachingIntersection()) {
                    double stopPosition = getStopPosition();
                    double currentPos = getCurrentPosition();
                    double distanceToStop = Math.abs(currentPos - stopPosition);
                    
                    if(distanceToStop < model.brakingDistance) {
                        model.speed = Math.max(0, model.speed - model.acceleration);
                        
                        if(distanceToStop < STOPPING_GAP) {
                            model.speed = 0;
                            setExactStopPosition(stopPosition);
                        }
                        return;
                    }
                }
                
                model.speed = Math.min(model.maxSpeed, model.speed + model.acceleration);
            }

            private void maintainStoppingGap(VehicleModel front) {
                switch(model.lane) {
                    case 0: // Nord
                        model.y = Math.min(model.y, front.y + front.height + STOPPING_GAP);
                        break;
                    case 1: // Est
                        model.x = Math.max(model.x, front.x - model.width - STOPPING_GAP);
                        break;
                    case 2: // Sud
                        model.y = Math.max(model.y, front.y - model.height - STOPPING_GAP);
                        break;
                    case 3: // Ouest
                        model.x = Math.min(model.x, front.x + front.width + STOPPING_GAP);
                        break;
                }
            }

            private double calculateDistance(VehicleModel front) {
                switch(model.lane) {
                    case 0: return front.y - (model.y + model.height);
                    case 1: return model.x - (front.x + front.width);
                    case 2: return model.y - (front.y + front.height);
                    default: return front.x - (model.x + model.width);
                }
            }

            private boolean isApproachingIntersection() {
                switch(model.lane) {
                    case 0: return model.y < 280 && model.y > 280 - STOP_DISTANCE - 50;
                    case 1: return model.x > 280 && model.x < 280 + STOP_DISTANCE + 50;
                    case 2: return model.y > 280 && model.y < 280 + STOP_DISTANCE + 50;
                    default: return model.x < 280 && model.x > 280 - STOP_DISTANCE - 50;
                }
            }

            private double getCurrentPosition() {
                switch(model.lane) {
                    case 0: return model.y;
                    case 1: return model.x;
                    case 2: return model.y;
                    default: return model.x;
                }
            }

            private double getStopPosition() {
                switch(model.lane) {
                    case 0: return 280 - STOP_DISTANCE - model.height;
                    case 1: return 280 + STOP_DISTANCE + model.width;
                    case 2: return 280 + STOP_DISTANCE + model.height;
                    default: return 280 - STOP_DISTANCE - model.width;
                }
            }

            private void setExactStopPosition(double position) {
                switch(model.lane) {
                    case 0: model.y = position; break;
                    case 1: model.x = position; break;
                    case 2: model.y = position; break;
                    case 3: model.x = position; break;
                }
            }

            private void moveVehicle() {
                switch(model.lane) {
                    case 0: model.y += model.speed; break;
                    case 1: model.x -= model.speed; break;
                    case 2: model.y -= model.speed; break;
                    default: model.x += model.speed; break;
                }
                
                if(!model.passedIntersection) {
                    switch(model.lane) {
                        case 0: if(model.y > 280 + INTERSECTION_SIZE/2) model.passedIntersection = true; break;
                        case 1: if(model.x < 280 - INTERSECTION_SIZE/2) model.passedIntersection = true; break;
                        case 2: if(model.y < 280 - INTERSECTION_SIZE/2) model.passedIntersection = true; break;
                        default: if(model.x > 280 + INTERSECTION_SIZE/2) model.passedIntersection = true; break;
                    }
                }
            }

            private void checkOffScreen() {
                if(model.x < -200 || model.x > 900 || model.y < -200 || model.y > 900) {
                    SimulationContext.getInstance().removeVehicle(model.lane, model);
                    doDelete();
                }
            }
        });
    }
}