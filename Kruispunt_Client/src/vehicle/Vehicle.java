/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package vehicle;

import Nodes.TrafficNode;
import java.util.List;

/**
 *
 * @author Eden
 */
public interface Vehicle {
    /***
     * This method lets you set the route of the vehicle
     * @param route route is a list of traffic nodes the vehicle will traverse
     */
    public abstract void setRoute(List<TrafficNode> route);
    /***
     * This method updates the vehicle logic for 1 tick
     */
    public abstract void update();
    
    /***
     * This method returns a String representation of the current vehicle.
     * @return
     */
    public abstract String getType();
    
}
