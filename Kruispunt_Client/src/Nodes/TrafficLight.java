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

    Vehicle vehicle;
    Light light = Light.Green;
    
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
        switch(light){
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
        } else{
            return vehicle.getType();
        }
    }
    
    @Override
    public boolean isAvailable() {
        return !hasVehicle() && light != Light.Red;
    }
    
    public void setLight(Light light){
        this.light = light;
    }
    
    public Light getLight(){
        return light;
    }
    
    
}
