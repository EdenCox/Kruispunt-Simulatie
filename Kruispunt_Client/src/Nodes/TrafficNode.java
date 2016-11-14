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
public interface  TrafficNode {
       
    public abstract void placeVehicle(Vehicle vehicle);
    public abstract boolean hasVehicle();
    public abstract void removeVehicle();
    public abstract Vehicle getVehicle();
    public abstract String getColorCode();
    public abstract String getVehicleLetter();
    public abstract boolean isAvailable();
       
}
