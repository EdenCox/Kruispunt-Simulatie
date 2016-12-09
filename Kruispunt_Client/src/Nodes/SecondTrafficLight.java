/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Nodes;

/**
 * A different trafficlight made with the purpose to be able to cross it while it's yellow.
 * @author Eden
 */
public class SecondTrafficLight extends TrafficLight {
    
    @Override
    public boolean isAvailable() {
        return !hasVehicle() && light != Light.Red;
    }
    
}
