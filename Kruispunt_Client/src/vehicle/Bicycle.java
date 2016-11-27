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
public class Bicycle implements Vehicle {

    private List<TrafficNode> route;
    private List<Vehicle> vehicles;
    private int currentPosition = 0;

    public Bicycle(List<TrafficNode> route, List<Vehicle> vehicles) {
        this.route = route;
        this.vehicles = vehicles;
        route.get(currentPosition).placeVehicle(this);
    }

    @Override
    public void setRoute(List<TrafficNode> route) {
        this.route = route;
    }

    @Override
    public void update() {

        if (currentPosition + 1 >= route.size()) {
            route.get(currentPosition).removeVehicle();
            vehicles.remove(this);
        } else if (route.get(currentPosition + 1).isAvailable()) {
            route.get(currentPosition+1).placeVehicle(this);
            route.get(currentPosition).removeVehicle();
            currentPosition++;
            
        }
    }

    @Override
    public String getType() {
        return "B";
    }

}
