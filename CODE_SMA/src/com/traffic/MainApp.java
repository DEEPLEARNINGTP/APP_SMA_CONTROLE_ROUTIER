package com.traffic;

import com.traffic.gui.SimulationGUI;
import com.traffic.model.SimulationContext;
import com.traffic.utils.PoissonGenerator;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.ContainerController;
import jade.wrapper.AgentController;

public class MainApp {
    public static void main(String[] args){
        try{
            // start GUI
            SimulationGUI gui = new SimulationGUI();
            SimulationContext.getInstance().setGui(gui);
            gui.setVisible(true);

            // start JADE runtime inside the same JVM
            Runtime rt = Runtime.instance();
            Profile p = new ProfileImpl();
            ContainerController main = rt.createMainContainer(p);
            SimulationContext.getInstance().setContainerController(main);

            // start the RMA (Remote Monitoring Agent) so you can see agents in the platform
            try{
                AgentController rma = main.createNewAgent("rma", "jade.tools.rma.rma", new Object[]{});
                rma.start();
            }catch(Exception e){ e.printStackTrace(); }

            // create four traffic light agents (one per approach)
            // Dans MainApp.java, remplacez la création des feux par :
            AgentController tlVertical = main.createNewAgent("TL-Vertical", "com.traffic.agents.TrafficLightAgent", new Object[]{0});
            AgentController tlHorizontal = main.createNewAgent("TL-Horizontal", "com.traffic.agents.TrafficLightAgent", new Object[]{1});
            tlVertical.start();
            tlHorizontal.start();

            // start one Poisson generator per lane to allow simultaneous arrivals
            double lambda = 0.15; // arrivals per second per lane
            PoissonGenerator genN = new PoissonGenerator(0, lambda, main);
            PoissonGenerator genE = new PoissonGenerator(1, lambda, main);
            PoissonGenerator genS = new PoissonGenerator(2, lambda, main);
            PoissonGenerator genW = new PoissonGenerator(3, lambda, main);

            Thread tn = new Thread(genN); tn.setDaemon(true); tn.start();
            Thread te = new Thread(genE); te.setDaemon(true); te.start();
            Thread ts = new Thread(genS); ts.setDaemon(true); ts.start();
            Thread tw = new Thread(genW); tw.setDaemon(true); tw.start();

        }catch(Exception e){ e.printStackTrace(); }
    }
}