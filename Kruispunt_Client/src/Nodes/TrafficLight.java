/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Nodes;

import vehicle.Vehicle;

/**
 *
 * @author Eden
 */
public class TrafficLight implements TrafficNode {

    private Vehicle vehicle;
    private Light light = Light.Green;
    private TrafficNode[] collisionNodes;

    @Override
    public void placeVehicle(Vehicle vehicle) {
        this.vehicle = vehicle;
    }

    @Override
    public boolean hasVehicle() {
        return (vehicle != null);
    }

    @Override
    public void removeVehicle() {
        vehicle = null;
    }

    @Override
    public Vehicle getVehicle() {
        return vehicle;
    }

    @Override
    public String getColorCode() {
        String colorCode = "-fx-background-color: #FFFFFF;";
        switch (light) {
            case Green:
                colorCode = "-fx-background-color: #0E9100;";
                break;
            case Yellow:
                colorCode = "-fx-background-color: #FCC511;";
                break;
            case Red:
                colorCode = "-fx-background-color: #F70000;";
                break;
        }
        return colorCode;
    }

    @Override
    public String getVehicleLetter() {
        if (vehicle == null) {
            return "";
        } else {
            return vehicle.getType();
        }
    }

    @Override
    public boolean isAvailable() {
        return !hasVehicle() && light == Light.Green;
    }

    public void setLight(Light light) {
        this.light = light;
    }
    
    public void setLight(String light) {
        switch(light) {
            case "green":
                this.light = Light.Green;
                break;
            case "orange":
                this.light = Light.Yellow;
                break;
            case "red":
                this.light = Light.Red;
                break;
            default:
                System.out.println("Received: "+ light + " as light status?");
                break;
        }
    }

    public Light getLight() {
        return light;
    }

    public void setCollisionNodes(TrafficNode[] collisionNodes) {
        this.collisionNodes = collisionNodes;
    }

    public int getCollisionCount() {
        if (collisionNodes != null) {
            for (int i = 0; i < collisionNodes.length; i++) {
                if (!collisionNodes[i].hasVehicle()) {
                    return i;
                }
            }
            return collisionNodes.length;
        }
        return 0;
    }
    

}
