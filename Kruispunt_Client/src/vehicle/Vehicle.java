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
    
    public abstract void setRoute(List<TrafficNode> route);
    public abstract void update();
    public abstract String getType();
    
}
