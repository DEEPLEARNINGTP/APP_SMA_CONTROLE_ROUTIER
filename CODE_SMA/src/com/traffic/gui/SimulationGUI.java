package com.traffic.gui;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import com.traffic.model.SimulationContext;
import com.traffic.model.VehicleModel;

public class SimulationGUI extends JFrame {
    private RoadPanel panel;
    private String[] lightStates = new String[]{"RED","RED","RED","RED"};
    private static final int STOP_LINE_DISTANCE = 100;
    private static final int INTERSECTION_SIZE = 80;

    public SimulationGUI() {
        setTitle("Smart Traffic Light - JADE");
        setSize(640, 640);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        panel = new RoadPanel();
        add(panel);
        setLocationRelativeTo(null);
    }

    public synchronized void setTrafficLightState(int lane, String state) {
        if(lane >= 0 && lane < 4) {
            lightStates[lane] = state;
            int oppositeLane = (lane + 2) % 4;
            lightStates[oppositeLane] = state;
        }
        SwingUtilities.invokeLater(() -> panel.repaint());
    }

    public String getTrafficLightState(int lane) {
        return lightStates[lane];
    }

    public int getQueueLength(int lane) {
        List<VehicleModel> list = SimulationContext.getInstance().getLane(lane);
        int cnt = 0;
        for(VehicleModel v : list) {
            switch(lane) {
                case 0: if(v.y > 0 && v.y < 280 - STOP_LINE_DISTANCE) cnt++; break;
                case 1: if(v.x < getWidth() && v.x > 280 + STOP_LINE_DISTANCE) cnt++; break;
                case 2: if(v.y < getHeight() && v.y > 280 + STOP_LINE_DISTANCE) cnt++; break;
                case 3: if(v.x > 0 && v.x < 280 - STOP_LINE_DISTANCE) cnt++; break;
            }
        }
        return cnt;
    }

    class RoadPanel extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D)g;
            int w = getWidth(), h = getHeight();
            int roadWidth = 160;
            
            // Fond blanc
            g2.setColor(Color.WHITE);
            g2.fillRect(0, 0, w, h);
            
            // Routes
            g2.setColor(new Color(200, 200, 200));
            g2.fillRect(w/2 - roadWidth/2, 0, roadWidth, h);
            g2.fillRect(0, h/2 - roadWidth/2, w, roadWidth);

            // Intersection
            g2.setColor(new Color(170, 170, 170));
            g2.fillRect(w/2 - INTERSECTION_SIZE/2, h/2 - INTERSECTION_SIZE/2, INTERSECTION_SIZE, INTERSECTION_SIZE);
            g2.setColor(Color.DARK_GRAY);
            g2.setStroke(new BasicStroke(3f));
            g2.drawRect(w/2 - INTERSECTION_SIZE/2, h/2 - INTERSECTION_SIZE/2, INTERSECTION_SIZE, INTERSECTION_SIZE);

            // Lignes d'arrêt
            g2.setColor(new Color(255, 0, 0, 150));
            g2.setStroke(new BasicStroke(3f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10f, new float[]{10f, 5f}, 0f));
            g2.drawLine(w/2 - roadWidth/2, h/2 - STOP_LINE_DISTANCE, w/2 + roadWidth/2, h/2 - STOP_LINE_DISTANCE);
            g2.drawLine(w/2 + STOP_LINE_DISTANCE, h/2 - roadWidth/2, w/2 + STOP_LINE_DISTANCE, h/2 + roadWidth/2);
            g2.drawLine(w/2 - roadWidth/2, h/2 + STOP_LINE_DISTANCE, w/2 + roadWidth/2, h/2 + STOP_LINE_DISTANCE);
            g2.drawLine(w/2 - STOP_LINE_DISTANCE, h/2 - roadWidth/2, w/2 - STOP_LINE_DISTANCE, h/2 + roadWidth/2);

            // Lignes centrales
            g2.setColor(Color.WHITE);
            Stroke oldStroke = g2.getStroke();
            g2.setStroke(new BasicStroke(3f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10f, new float[]{10f, 6f}, 0f));
            g2.drawLine(w/2, 0, w/2, h);
            g2.drawLine(0, h/2, w, h/2);
            g2.setStroke(oldStroke);

            // Feux tricolores
            drawTrafficLight(g2, w/2 - 12, 40, lightStates[0]);
            drawTrafficLight(g2, w - 70, h/2 - 12, lightStates[1]);
            drawTrafficLight(g2, w/2 - 12, h - 70, lightStates[2]);
            drawTrafficLight(g2, 40, h/2 - 12, lightStates[3]);

            // Véhicules
            for(int lane = 0; lane < 4; lane++) {
                List<VehicleModel> vehicles = SimulationContext.getInstance().getLane(lane);
                for(VehicleModel vehicle : vehicles) {
                    drawVehicle(g2, vehicle, lane, w, h);
                }
            }
        }

        private void drawTrafficLight(Graphics2D g2, int x, int y, String state) {
            g2.setColor(Color.BLACK);
            g2.fillRect(x, y, 24, 70);
            
            g2.setColor("RED".equals(state) ? new Color(255, 50, 50) : new Color(80, 0, 0));
            g2.fillOval(x + 4, y + 4, 16, 16);
            
            g2.setColor("YELLOW".equals(state) ? new Color(255, 255, 50) : new Color(80, 80, 0));
            g2.fillOval(x + 4, y + 24, 16, 16);
            
            g2.setColor("GREEN".equals(state) ? new Color(50, 255, 50) : new Color(0, 80, 0));
            g2.fillOval(x + 4, y + 44, 16, 16);
        }

        private void drawVehicle(Graphics2D g2, VehicleModel vehicle, int lane, int w, int h) {
            int vx = (int)vehicle.x;
            int vy = (int)vehicle.y;
            int width = vehicle.width;
            int height = vehicle.height;
            
            switch(lane) {
                case 0: vx = w/2 - 80 + 5; break;
                case 1: vy = h/2 - 80 + 5; break;
                case 2: vx = w/2 + 80 - width - 5; break;
                case 3: vy = h/2 + 80 - height - 5; break;
            }
            
            boolean isStopped = (vehicle.speed == 0 && 
                               ((lane == 0 && vehicle.y < 280 - STOP_LINE_DISTANCE) ||
                                (lane == 1 && vehicle.x > 280 + STOP_LINE_DISTANCE) ||
                                (lane == 2 && vehicle.y > 280 + STOP_LINE_DISTANCE) ||
                                (lane == 3 && vehicle.x < 280 - STOP_LINE_DISTANCE)));
            
            g2.setColor(isStopped ? new Color(255, 100, 100) : new Color(30, 144, 255));
            g2.fillRect(vx, vy, width, height);
            
            g2.setColor(Color.BLACK);
            g2.drawRect(vx, vy, width, height);
            
            g2.setColor(new Color(200, 200, 255, 150));
            if(lane == 0 || lane == 2) {
                g2.fillRect(vx + 2, vy + 2, width - 4, 4);
                g2.fillRect(vx + 2, vy + height - 6, width - 4, 4);
            } else {
                g2.fillRect(vx + 2, vy + 2, 4, height - 4);
                g2.fillRect(vx + width - 6, vy + 2, 4, height - 4);
            }
        }
    }
}