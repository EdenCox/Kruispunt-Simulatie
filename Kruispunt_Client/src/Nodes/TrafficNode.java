/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Nodes;

import vehicle.Vehicle;


/***
 * The TrafficNode Class is an interface that represents 1 square of the Traffic Simulation grid.
 * @author Eden
 */
public interface  TrafficNode {
    
    /***
     * This method places a vehicle at the node.
     * @param vehicle vehicle to be placed.
     */
    public abstract void placeVehicle(Vehicle vehicle);
    /***
     * This method checks if the node has a vehicle.
     * @return returns true if there is a vehicle.
     */
    public abstract boolean hasVehicle();
    /***
     * This method removes the current vehicle from the node.
     */
    public abstract void removeVehicle();
    /***
     * This method returns the current vehicle of the node.
     * Warning may return null if the node doesn't have any vehicles.
     * @return return vehicle placed at the node.
     */
    public abstract Vehicle getVehicle();
    /***
     * This method returns a string hexadecimal representation color of the node.
     * @return 
     */
    public abstract String getColorCode();
    /***
     * This method returns a String representation of the current vehicle.
     * @return
     */
    public abstract String getVehicleLetter();
    /***
     * This method checks if the node is available or not.
     * @return returns true if the node is available.
     */
    public abstract boolean isAvailable();
       
}
