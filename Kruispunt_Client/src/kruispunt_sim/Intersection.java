/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kruispunt_sim;

import Nodes.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import org.json.JSONArray;
import org.json.JSONObject;
import vehicle.Car;
import vehicle.Pedestrian;
import vehicle.Vehicle;

/**
 *
 * @author Eden
 */
public class Intersection {

    private TrafficNode[][] intersection;
    private TrafficLight[] trafficlights;
    private final List<List<TrafficNode>> carRoutes = new ArrayList<>();
    private final List<List<TrafficNode>> pedestrianRoutes = new ArrayList<>();
    private final List<List<TrafficNode>> bicycleRoutes = new ArrayList<>();
    private final List<List<TrafficNode>> trainRoutes = new ArrayList<>();
    private final List<Vehicle> vehicles;
    private final Random rand = new Random();
    private ClientSocket connection;

    public Intersection(ClientSocket connection) {
        this.connection = connection;
        vehicles = new ArrayList<>();
        initilizeIntersection();
    }

    public String getColorCode(int x, int y) {
        return intersection[x][y].getColorCode();
    }

    public String getLetterCode(int x, int y) {
        return intersection[x][y].getVehicleLetter();
    }

    public void Update() {
        for (int i = 0; i < vehicles.size(); i++) {
            vehicles.get(i).update();
        }
//        if (15 < rand.nextInt(20) + 1) {
//            int random = rand.nextInt(carRoutes.size());
//            if (carRoutes.get(random).get(0).isAvailable()) {
//                Vehicle vehicle = new Car(carRoutes.get(random), vehicles);
//                vehicles.add(vehicle);
//            }
//        }
        if (4 > rand.nextInt(20) + 1) {
            int random = rand.nextInt(pedestrianRoutes.size());
            if (pedestrianRoutes.get(random).get(0).isAvailable()) {
                Vehicle vehicle = new Pedestrian(pedestrianRoutes.get(random), vehicles);
                vehicles.add(vehicle);
            }
        }
        sendState();
        syncState();
    }

    private void sendState() {
        if (connection != null) {
            JSONObject json = new JSONObject();
            JSONArray state = new JSONArray();
            for (int i = 1; i < 11; i++) {
                JSONObject henk = new JSONObject();
                henk.put("trafficLight", i);
                henk.put("count", trafficlights[i].getCollisionCount());
                state.put(henk);
            }
            json.put("state", state);
            connection.sendString(json.toString());
        }
    }

    private void syncState() {
        if (connection != null) {
            JSONObject state = connection.getState();
            if (state.has("state")) {
                JSONArray lightStates = state.getJSONArray("state");
                for (int i = 0; i < lightStates.length(); i++) {
                    trafficlights[lightStates.getJSONObject(i).getInt("trafficLight")].setLight(lightStates.getJSONObject(i).getString("status"));
                }
            }

        }

    }

    private void initilizeIntersection() {
        intersection = new TrafficNode[23][26];
        trafficlights = new TrafficLight[46];
        carRoutes.forEach((trafficNodes) -> {
            trafficNodes = new ArrayList<>();
        });
        for (int i = 0; i < trafficlights.length; i++) {
            trafficlights[i] = new TrafficLight();
        }

        for (int x = 0; x < intersection.length; x++) {
            for (int y = 0; y < intersection[0].length; y++) {
                intersection[x][y] = new Empty();
                //System.out.println(intersection[x][y].getColorCode());
            }
        }

        //All Road Nodes
        for (int i = 0; i < 14; i++) {
            intersection[9][i] = new RoadNode();
            intersection[11][i] = new RoadNode();
        }
        for (int i = 0; i < intersection[0].length; i++) {
            intersection[10][i] = new RoadNode();
            intersection[13][i] = new RoadNode();
        }
        for (int i = 10; i < intersection[0].length; i++) {
            intersection[12][i] = new RoadNode();
        }

        for (TrafficNode[] intersection1 : intersection) {
            intersection1[10] = new RoadNode();
        }
        for (int i = 9; i < intersection.length; i++) {
            intersection[i][11] = new RoadNode();
        }
        for (TrafficNode[] intersection1 : intersection) {
            intersection1[12] = new RoadNode();
        }
        for (int i = 0; i < 14; i++) {
            intersection[i][13] = new RoadNode();
        }
        for (TrafficNode[] intersection1 : intersection) {
            intersection1[14] = new RoadNode();
        }
        
        //All BicycleLane Nodes
        for (int i = 0; i < intersection[0].length; i++) {
            intersection[17][i] = new BicycleLane();
        }
        for (int i = 0; i < 19; i++) {
            intersection[5][i] = new BicycleLane();
        }
        for (int i = 18; i < intersection[0].length; i++) {
            intersection[6][i] = new BicycleLane();
        }
        
        for (TrafficNode[] intersection1 : intersection) {
            intersection1[6] = new BicycleLane();
        }
        
        //All Sidewalk Nodes
        for (int i = 0; i < intersection[0].length; i++) {
            intersection[15][i] = new Sidewalk();
        }
        for (int i = 0; i < 17; i++) {
            intersection[7][i] = new Sidewalk();
        }
        for (int i = 16; i < intersection[0].length; i++) {
            intersection[8][i] = new Sidewalk();
        }
        
        for (TrafficNode[] intersection1 : intersection) {
            intersection1[8] = new Sidewalk();
        }
        
        //All TrainTrack Nodes
        for (TrafficNode[] intersection1 : intersection) {
            intersection1[20] = new TrainTrack();
        }

        //Stoplights 1 t/m 10 and 42
        intersection[18][10] = trafficlights[42];
        intersection[18][11] = trafficlights[1];
        intersection[18][12] = trafficlights[2];
        intersection[13][21] = trafficlights[3];
        intersection[12][21] = trafficlights[4];
        intersection[4][14] = trafficlights[5];
        intersection[4][13] = trafficlights[6];
        intersection[4][12] = trafficlights[7];
        intersection[9][4] = trafficlights[8];
        intersection[10][4] = trafficlights[9];
        intersection[11][4] = trafficlights[10];
        
         //Stoplights 21 t/m 28
        intersection[15][13] =  trafficlights[21];
        intersection[15][15] =  trafficlights[22];
        intersection[15][21] =  trafficlights[23];
        intersection[8][19] =  trafficlights[24];
        intersection[7][11] =  trafficlights[25];
        intersection[7][9] =  trafficlights[26];
        intersection[12][8] =  trafficlights[27];
        intersection[14][8] =  trafficlights[28];
        
        //Stoplights 31 t/m 38
        intersection[17][13] =  trafficlights[31];
        intersection[17][15] =  trafficlights[32];
        intersection[17][21] =  trafficlights[33];
        intersection[6][19] =  trafficlights[34];
        intersection[5][11] =  trafficlights[35];
        intersection[5][9] =  trafficlights[36];
        intersection[12][6] =  trafficlights[37];
        intersection[14][6] =  trafficlights[38];
        
        

        //Stoplight nodes 1 t/m 10 and 42
        trafficlights[42].setCollisionNodes(new TrafficNode[]{intersection[19][10], intersection[20][10], intersection[21][10], intersection[22][10]});
        trafficlights[1].setCollisionNodes(new TrafficNode[]{intersection[19][11], intersection[20][11], intersection[21][11], intersection[22][11]});
        trafficlights[2].setCollisionNodes(new TrafficNode[]{intersection[19][12], intersection[20][12], intersection[21][12], intersection[22][12]});
        trafficlights[3].setCollisionNodes(new TrafficNode[]{intersection[13][22], intersection[13][23], intersection[13][24], intersection[13][25]});
        trafficlights[4].setCollisionNodes(new TrafficNode[]{intersection[12][22], intersection[12][23], intersection[12][24], intersection[12][25]});
        trafficlights[5].setCollisionNodes(new TrafficNode[]{ intersection[3][14], intersection[2][14], intersection[1][14], intersection[0][14]});
        trafficlights[6].setCollisionNodes(new TrafficNode[]{intersection[3][13], intersection[2][13], intersection[1][13], intersection[0][13]});
        trafficlights[7].setCollisionNodes(new TrafficNode[]{intersection[3][12], intersection[2][12], intersection[1][12], intersection[0][12]});
        trafficlights[8].setCollisionNodes(new TrafficNode[]{intersection[9][3], intersection[9][2], intersection[9][1], intersection[9][0]});
        trafficlights[9].setCollisionNodes(new TrafficNode[]{intersection[10][3], intersection[10][2], intersection[10][1], intersection[10][0]});
        trafficlights[10].setCollisionNodes(new TrafficNode[]{intersection[11][3], intersection[11][2], intersection[11][1], intersection[11][0]});

        //route 1
        carRoutes.add(Arrays.asList(intersection[22][11], intersection[21][11], intersection[20][11], intersection[19][11],
                intersection[18][11], intersection[17][11], intersection[16][11], intersection[15][11], intersection[14][11], intersection[13][11],
                intersection[13][10], intersection[13][9], intersection[13][8], intersection[13][7], intersection[13][6], intersection[13][5],
                intersection[13][4], intersection[13][3], intersection[13][2], intersection[13][1], intersection[13][0]));

        //route 2
        carRoutes.add(Arrays.asList(intersection[22][12], intersection[21][12], intersection[20][12], intersection[19][12], intersection[18][12],
                intersection[17][12], intersection[16][12], intersection[15][12], intersection[14][12], intersection[13][12], intersection[12][12],
                intersection[11][12], intersection[10][12], intersection[9][12], intersection[9][11], intersection[9][10], intersection[8][10],
                intersection[7][10], intersection[6][10], intersection[5][10], intersection[4][10], intersection[3][10], intersection[2][10],
                intersection[1][10], intersection[0][10]));

        //route 3.1
        carRoutes.add(Arrays.asList(intersection[13][25], intersection[13][24], intersection[13][23], intersection[13][22], intersection[13][21],
                intersection[13][20], intersection[13][19], intersection[13][18], intersection[13][17], intersection[13][16], intersection[13][15],
                intersection[13][14], intersection[14][14], intersection[15][14], intersection[16][14], intersection[17][14], intersection[18][14],
                intersection[19][14], intersection[20][14], intersection[21][14], intersection[22][14]));

        //route 3.2
        carRoutes.add(Arrays.asList(intersection[13][25], intersection[13][24], intersection[13][23], intersection[13][22], intersection[13][21],
                intersection[13][20], intersection[13][19], intersection[13][18], intersection[13][17], intersection[13][16], intersection[13][15],
                intersection[13][14], intersection[13][13], intersection[13][12], intersection[13][11], intersection[13][10], intersection[13][9],
                intersection[13][8], intersection[13][7], intersection[13][6], intersection[13][5], intersection[13][4], intersection[13][3],
                intersection[13][2], intersection[13][1], intersection[13][0]));

        //route 4
        carRoutes.add(Arrays.asList(intersection[12][25], intersection[12][24], intersection[12][23], intersection[12][22], intersection[12][21],
                intersection[12][20], intersection[12][19], intersection[12][18], intersection[12][17], intersection[12][16], intersection[12][15],
                intersection[12][14], intersection[12][13], intersection[12][12], intersection[12][11], intersection[12][10], intersection[11][10],
                intersection[10][10], intersection[9][10], intersection[8][10], intersection[7][10], intersection[6][10], intersection[5][10],
                intersection[4][10], intersection[3][10], intersection[2][10], intersection[1][10], intersection[0][10]));

        //route 5
        carRoutes.add(Arrays.asList(intersection[0][14], intersection[1][14], intersection[2][14], intersection[3][14], intersection[4][14],
                intersection[5][14], intersection[6][14], intersection[7][14], intersection[8][14], intersection[9][14], intersection[10][14], intersection[10][15],
                intersection[10][16], intersection[10][17], intersection[10][18], intersection[10][19], intersection[10][20], intersection[10][21],
                intersection[10][22], intersection[10][23], intersection[10][24], intersection[10][25]));

        //route 6
        carRoutes.add(Arrays.asList(intersection[0][13], intersection[1][13], intersection[2][13], intersection[3][13], intersection[4][13],
                intersection[5][13], intersection[6][13], intersection[7][13], intersection[8][13], intersection[9][13], intersection[10][13], intersection[11][13],
                intersection[11][14], intersection[12][14], intersection[13][14], intersection[14][14], intersection[15][14], intersection[16][14],
                intersection[17][14], intersection[18][14], intersection[19][14], intersection[20][14], intersection[21][14], intersection[22][14]));

        //route 7
        carRoutes.add(Arrays.asList(intersection[0][12], intersection[1][12], intersection[2][12], intersection[3][12], intersection[4][12],
                intersection[5][12], intersection[6][12], intersection[7][12], intersection[8][12], intersection[9][12], intersection[10][12], intersection[11][12],
                intersection[12][12], intersection[13][12], intersection[13][11], intersection[13][10], intersection[13][9], intersection[13][8],
                intersection[13][7], intersection[13][6], intersection[13][5], intersection[13][4], intersection[13][3], intersection[13][2], intersection[13][1], intersection[13][0]));

        //route 8
        carRoutes.add(Arrays.asList(intersection[9][0], intersection[9][1], intersection[9][2], intersection[9][3], intersection[9][4], intersection[9][5],
                intersection[9][6], intersection[9][7], intersection[9][8], intersection[9][9], intersection[9][10],
                intersection[8][10], intersection[7][10], intersection[6][10], intersection[5][10], intersection[4][10], intersection[3][10],
                intersection[2][10], intersection[1][10], intersection[0][10]));

        //route 9
        carRoutes.add(Arrays.asList(intersection[10][0], intersection[10][1], intersection[10][2], intersection[10][3], intersection[10][4], intersection[10][5],
                intersection[10][6], intersection[10][7], intersection[10][8], intersection[10][9], intersection[10][10],
                intersection[10][11], intersection[10][12], intersection[10][13], intersection[10][14], intersection[10][15], intersection[10][16],
                intersection[10][17], intersection[10][18], intersection[10][19], intersection[10][20], intersection[10][21],
                intersection[10][22], intersection[10][23], intersection[10][24], intersection[10][25]));

        //route 10
        carRoutes.add(Arrays.asList(intersection[11][0], intersection[11][1], intersection[11][2], intersection[11][3], intersection[11][4], intersection[11][5],
                intersection[11][6], intersection[11][7], intersection[11][8], intersection[11][9], intersection[11][10],
                intersection[11][11], intersection[11][12], intersection[11][13], intersection[11][14], intersection[12][14], intersection[13][14], intersection[14][14],
                intersection[15][14], intersection[16][14], intersection[17][14], intersection[18][14], intersection[19][14],
                intersection[20][14], intersection[21][14], intersection[22][14]));
        
        //route 23
        pedestrianRoutes.add(Arrays.asList(intersection[15][25], intersection[15][24], intersection[15][23], intersection[15][22], intersection[15][21], intersection[15][20],
                intersection[15][19], intersection[15][18], intersection[15][17], intersection[15][16], intersection[15][15],intersection[15][14], intersection[15][13],
                intersection[15][12], intersection[15][11], intersection[15][10], intersection[15][9],intersection[15][8],intersection[15][7],
                intersection[15][6],intersection[15][5], intersection[15][4], intersection[15][3], intersection[15][2], intersection[15][1], intersection[15][0]));
        
        //route 24
        pedestrianRoutes.add(Arrays.asList(intersection[7][0], intersection[7][1], intersection[7][2], intersection[7][3], intersection[7][4], intersection[7][5],
                intersection[7][6], intersection[7][7], intersection[7][8], intersection[7][9], intersection[7][10],intersection[7][11], intersection[7][12],
                intersection[7][13], intersection[7][14], intersection[7][15], intersection[7][16], intersection[8][16], intersection[8][17],intersection[8][18],
                intersection[8][19], intersection[8][20], intersection[8][21], intersection[8][22], intersection[8][23], intersection[8][24], intersection[8][25]));
        
         //route 28
        pedestrianRoutes.add(Arrays.asList(intersection[22][8], intersection[21][8], intersection[20][8], intersection[19][8], intersection[18][8], intersection[17][8]
        , intersection[16][8], intersection[15][8], intersection[14][8], intersection[13][8], intersection[12][8], intersection[11][8], intersection[10][8]
        , intersection[9][8], intersection[8][8], intersection[7][8], intersection[6][8], intersection[5][8], intersection[4][8], intersection[3][8]
        , intersection[2][8], intersection[1][8], intersection[0][8]));
        
        //todo add train route and bicycle route. update logics
                
        //route 42
//        carRoutes.add(Arrays.asList(intersection[22][10], intersection[21][10], intersection[20][10], intersection[19][10], intersection[18][10],
//                intersection[17][10], intersection[16][10], intersection[15][10], intersection[14][10], intersection[13][10], intersection[12][10],
//                intersection[11][10], intersection[10][10], intersection[9][10], intersection[8][10], intersection[7][10], intersection[6][10],
//                intersection[5][10], intersection[4][10], intersection[3][10], intersection[2][10], intersection[1][10], intersection[0][10]));
    }
}
