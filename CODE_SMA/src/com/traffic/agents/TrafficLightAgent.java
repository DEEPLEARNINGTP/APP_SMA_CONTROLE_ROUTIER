package com.traffic.agents;

import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;
import com.traffic.model.SimulationContext;
import com.traffic.gui.SimulationGUI;

public class TrafficLightAgent extends Agent {
    private int lane;    private SimulationGUI gui;
    private String state = "RED";
    private long lastChangeTime;
    
    // Durées configurables (en ms)
    
    private static final int BASE_GREEN_TIME = 10000;  // Augmenter pour des phases vertes plus longues
    private static final int MAX_EXTENSION = 8000;    // Réduire pour limiter l'extension
    private static final int YELLOW_TIME = 3000;      // Ajuster la durée de l'orange
    private static final int MIN_RED_TIME = 5000;     // Garantir un temps de sécurité
    
    protected void setup() {
        Object[] args = getArguments();
        lane = (Integer)args[0];
        gui = SimulationContext.getInstance().getGui();
        lastChangeTime = System.currentTimeMillis();

        addBehaviour(new TickerBehaviour(this, 500) {
            public void onTick() {
                manageLightCycle();
            }
        });
    }

    private void manageLightCycle() {
        long currentTime = System.currentTimeMillis();
        long elapsed = currentTime - lastChangeTime;

        switch(state) {
            case "GREEN":
                handleGreenPhase(elapsed);
                break;
            case "YELLOW":
                if(elapsed >= YELLOW_TIME) {
                    setRed();
                }
                break;
            case "RED":
                if(elapsed >= MIN_RED_TIME && shouldSwitchToGreen()) {
                    setGreen();
                }
                break;
        }
    }

    private void handleGreenPhase(long elapsed) {
        int queueLength = gui.getQueueLength(lane);
        int extension = Math.min(MAX_EXTENSION, queueLength * 1000);
        int totalGreenTime = BASE_GREEN_TIME + extension;

        if(elapsed >= totalGreenTime) {
            setYellow();
        }
    }

    private boolean shouldSwitchToGreen() {
    int perpendicular1 = (lane + 1) % 4;
    int perpendicular2 = (lane + 3) % 4;
    return "RED".equals(gui.getTrafficLightState(perpendicular1)) && 
           "RED".equals(gui.getTrafficLightState(perpendicular2));
}
    private void setGreen() {
        state = "GREEN";
        lastChangeTime = System.currentTimeMillis();
        updateOppositeLight();
    }

    private void setYellow() {
        state = "YELLOW";
        lastChangeTime = System.currentTimeMillis();
        updateOppositeLight();
    }

    private void setRed() {
        state = "RED";
        lastChangeTime = System.currentTimeMillis();
        updateOppositeLight();
    }

    private void updateOppositeLight() {
        int oppositeLane = (lane + 2) % 4;
        gui.setTrafficLightState(oppositeLane, state);
        gui.setTrafficLightState(lane, state);
    }
}