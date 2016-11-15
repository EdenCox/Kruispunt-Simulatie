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
public class BicycleLane implements TrafficNode{

    private Vehicle vehicle;
    
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
        return "-fx-background-color: #FFFFFF;";
    }

    @Override
    public String getVehicleLetter() {
        if (vehicle == null) {
            return "";
        } else{
            return vehicle.getType();
        }
    }

    @Override
    public boolean isAvailable() {
        return !hasVehicle();
    }
    
}
