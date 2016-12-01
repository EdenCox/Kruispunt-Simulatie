﻿using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Data;
using System.Diagnostics;
using System.Drawing;
using System.Linq;
using System.Text;
using System.Threading;
using System.Threading.Tasks;
using System.Windows.Forms;
using Newtonsoft.Json;
using Newtonsoft.Json.Linq;

namespace CrossRoad
{
    public partial class Form1 : Form
    {
        private List<Road> roads;
        private List<Tuple<int, List<int>>> collisionGraph;
        private Connection connection;
        private bool running = false;
        private Thread mainThread;
        private List<State> stateQueue;
        private DateTime previousTimestamp;
        private bool firstRun = true;

        private int maxServerPulseTime = 500; //2fps server heartbeat
        private int maxGreenTime = 3000;//30000 // in het echt 30 sec
        private int maxOrangeTime = 3500;//10000 // in het echt 3.5 sec 
        private int maxClearingTime = 2000;//ontruiminstijd 1 a 2 sec
        private int maxWaitingTime = 120000;

        private int currentClearingTime = 0;
        private int pulseTime = 0;
        private int lastIndex = 0;

        public Form1()
        {
            InitializeComponent();
            collisionGraph = new List<Tuple<int, List<int>>>();
            populateCollisionGraph();
            connection = new Connection();
        }

        public void mainLoop() {
            while (running) {
                if (connection.stateChanged()){
                    stateQueue = connection.getStateQueue();
                    foreach (State s in stateQueue) {
                        foreach (Road r in roads) {
                            if (s.trafficLight == r.trafficLight) {
                                r.count = s.count;
                                break;
                            }
                        }
                    }//end foreach
                }//end if     

                int difference = 0;
                int heartBeatTimer = 0;
                if (firstRun) 
                    previousTimestamp = DateTime.Now;  
                else{
                    difference = ((TimeSpan)(DateTime.Now - previousTimestamp)).Milliseconds;
                    heartBeatTimer += difference;
                    previousTimestamp = DateTime.Now;
                }

                firstRun = false;
                bool safeToCross = true;
                bool holdPulse = false;

                foreach (Road r in roads){
                    if (r.status == Status.green)
                    {
                        r.milliSec += difference;
                        safeToCross = false;
                        if (r.milliSec >= this.maxGreenTime)
                        {
                            r.status = Status.orange;
                            r.milliSec = 0;
                            r.changed = true;
                            holdPulse = true;
                        }
                    }
                    else if (r.status == Status.orange) {
                        r.milliSec += difference;
                        safeToCross = false;
                        if (r.milliSec >= this.maxOrangeTime) {
                            r.status = Status.red;
                            r.changed = true;
                            r.milliSec = 0;
                            safeToCross = true;
                            holdPulse = true;
                        }
                    }
                    /*
*/
                }//end red and orange

                //wait until the crossroad has been cleared
                if (safeToCross && currentClearingTime < maxClearingTime) {
                    currentClearingTime += difference;
                }

                //begin green light
                else if (safeToCross) {
                    currentClearingTime = 0;//reset the clearing time
                    bool priority = false;
                    foreach(Road r in roads)
                    {
                        if (r.trafficLight == 45 && r.count > 0 || r.trafficLight == 46 && r.count > 0) {//train has priority
                            priority = true;
                            r.status = Status.green;
                            r.changed = true;
                            r.milliSec = 0;

                            List<int> collision = getCollisionTuple(r.trafficLight).Item2;
                            giveNonCollisionGreenLight(collision);
                            safeToCross = false;
                            holdPulse = true;
                            break;
                        }
                        if (r.trafficLight == 42 && r.count > 0) {//bus  has priority
                            priority = true;
                            r.status = Status.green;
                            r.changed = true;
                            r.milliSec = 0;

                            List<int> collision = getCollisionTuple(r.trafficLight).Item2;
                            giveNonCollisionGreenLight(collision);
                            safeToCross = false;
                            holdPulse = true;
                            break;
                        }
                    }

                    if (!priority) {
                        for (int i = 0; i < roads.Count; i++) {
                            int mod = (lastIndex + i) % roads.Count;//give each road an equal change of green
                            if (roads.ElementAt(mod).count > 0)
                            {
                                //if (roads.ElementAt((i)).count > 0)
                                //{
                                //List<int> collision = getCollisionTuple(roads.ElementAt((i)).trafficLight).Item2;
                                roads.ElementAt(mod).status = Status.green;
                                roads.ElementAt(mod).changed = true;
                                roads.ElementAt(mod).milliSec = 0;

                                List<int> collision = getCollisionTuple(roads.ElementAt((mod)).trafficLight).Item2;
                                giveNonCollisionGreenLight(collision);
                                Debug.Write("mod" + mod);
                                lastIndex = (mod + 1) % roads.Count;//prevent out of index
                                safeToCross = false;
                                holdPulse = true;
                                break;
                            }
                        }
                    }
                }//end green light

                //a simple server heartbeat to be compatible for some of the simulations
                if (!holdPulse && heartBeatTimer > maxServerPulseTime)
                {
                    serverPulse();
                    pulseTime = 0;
                }
                else if (holdPulse) {
                    pulseTime = 0; // prevent pulse in next cycle to prevent flooding client with data
                }

                //write all changes to Client
                writeToClient();
            }
        }

        private void writeToClient() {
            List<Road> changedRoads = new List<Road>();
            //old legacy code, needs to be revamped

            foreach (Road r in roads) {
                if (r.changed) {
                    //todo add to JSON
                    changedRoads.Add(r);
                    r.changed = false;
                    break;
                }
            }//end legacy code
            
            if(changedRoads.Count != 0) {
                serverPulse();
            }
        }

        private void serverPulse() {
            string msg = "{'state':[";
            for (int i = 0; i < roads.Count; i++)
            {
                //for (int i = 0; i < changedRoads.Count; i++) {
                msg += "{'trafficLight':" + roads.ElementAt(i).trafficLight + ",";
                msg += "'status': " + "\"" + roads.ElementAt(i).status.ToString().ToLower() + "\"" + "}";
                msg += (i != roads.Count - 1) ? "," : "";

            }
            msg += "]}";
            connection.writeToClient(msg);
        }

        private Tuple<int, List<int>> getCollisionTuple(int trafficID) {
            for (int i = 0; i < collisionGraph.Count; i++){
                if (collisionGraph.ElementAt(i).Item1 == trafficID){
                    return collisionGraph.ElementAt(i);
                }
            }
            return null;
        }

        //potential todo: also add modifier to this function so it cycles
        private void giveNonCollisionGreenLight(List<int> collision) {
            for(int rI = 0; rI < roads.Count(); rI++) {
                bool inCollision = false;
                foreach (int i in collision) {
                    if(roads.ElementAt(rI).trafficLight == i){
                        inCollision = true;
                        break;
                    }   
                }
                if (!inCollision && roads.ElementAt(rI).count > 0) {
                    roads.ElementAt(rI).status = Status.green;
                    roads.ElementAt(rI).changed = true;
                    roads.ElementAt(rI).milliSec = 0;
                    collision.AddRange(getCollisionTuple(roads.ElementAt(rI).trafficLight).Item2); //update roads collisiongraph
                    collision.AddRange(bikePedestrianLane(rI));
                }
            }
        }

        private List<int> bikePedestrianLane(int index) {
            int light = roads.ElementAt(index).trafficLight;
            int mod = -1;
            switch (light) {
                case 21:
                    mod = index + 1;
                    break;
                case 31:
                    mod = index + 1;
                    break;
                case 25:
                    mod = index + 1;
                    break;
                case 35:
                    mod = index + 1;
                    break;
                case 27:
                    mod = index + 1;
                    break;
                case 37:
                    mod = index + 1;
                    break;
                case 22:
                    mod = index - 1;
                    break;
                case 32:
                    mod = index - 1;
                    break;
                case 26:
                    mod = index - 1;
                    break;
                case 36:
                    mod = index - 1;
                    break;
                case 28:
                    mod = index - 1;
                    break;
                case 38:
                    mod = index - 1;
                    break;

            }
            if (mod > -1){
                roads.ElementAt(mod).status = Status.green;
                roads.ElementAt(mod).changed = true;
                roads.ElementAt(mod).milliSec = 0;
                return getCollisionTuple(roads.ElementAt(mod).trafficLight).Item2;
            }
            return new List<int>();
            
            //21 22  31 32
            //25 26  35 36
            //27 28  37 38
        }

        private void populateRoads() {
            int carLanes = 10;
            int bikeLanes = 28;
            int pedestrianLane = 38;

            for(int i =1; i <= carLanes; i++){
                roads.Add(new Road(i, Status.red, 0));
            }
            
            for(int i = 21; i <= bikeLanes; i++){
                roads.Add(new Road(i, Status.red, 0));
            }

            for (int i = 31; i <= pedestrianLane; i++){
                roads.Add(new Road(i, Status.red, 0));
            }

            //buslane
            roads.Add(new Road(42, Status.red, 0));

            //train
            roads.Add(new Road(45, Status.red, 0));
            roads.Add(new Road(46, Status.red, 0));
            
        }

        private void populateCollisionGraph() {
            collisionGraph.Add(Tuple.Create(1, new List<int>() { 3, 7, 21, 22, 27, 28, 31, 32, 37, 38, 42 }));
            collisionGraph.Add(Tuple.Create(2, new List<int>() { 3, 4, 7, 8, 9, 10, 21, 22, 26, 31, 32, 36, 42 }));
            collisionGraph.Add(Tuple.Create(3, new List<int>() { 1, 2, 6, 7, 10, 21, 22, 27, 28, 32, 37, 38, 42, 45, 46 }));
            collisionGraph.Add(Tuple.Create(4, new List<int>() { 2, 6, 7, 8, 9, 25, 26, 36, 42, 45, 46 }));
            collisionGraph.Add(Tuple.Create(5, new List<int>() { 9, 25, 35, 36, 45, 46 }));
            collisionGraph.Add(Tuple.Create(6, new List<int>() { 3, 4, 9, 10, 22, 25, 26, 32, 35, 36}));
            collisionGraph.Add(Tuple.Create(7, new List<int>() { 1, 2, 3, 4, 9, 10, 25, 26, 27, 28, 35, 36, 37, 38, 42}));
            collisionGraph.Add(Tuple.Create(8, new List<int>() { 2, 4, 25, 26, 27, 28, 35, 36, 37, 38, 42 }));
            collisionGraph.Add(Tuple.Create(9, new List<int>() { 2, 4, 5, 6, 27, 28, 37, 42, 45, 46 }));
            collisionGraph.Add(Tuple.Create(10, new List<int>() { 2, 3, 6, 7, 21, 22, 27, 28, 31, 32 ,37, 38 ,42 }));
            
            //bikeLane
            collisionGraph.Add(Tuple.Create(21, new List<int>() { 1, 2, 42}));
            collisionGraph.Add(Tuple.Create(22, new List<int>() { 3, 6, 10 }));
            collisionGraph.Add(Tuple.Create(23, new List<int>() { 45, 46 }));
            collisionGraph.Add(Tuple.Create(24, new List<int>() { 45, 46 }));
            collisionGraph.Add(Tuple.Create(25, new List<int>() { 5, 6, 7}));
            collisionGraph.Add(Tuple.Create(26, new List<int>() { 2, 4, 8, 42}));
            collisionGraph.Add(Tuple.Create(27, new List<int>() { 8, 9, 10 }));
            collisionGraph.Add(Tuple.Create(28, new List<int>() { 1, 3, 7 }));
            //footpath
            collisionGraph.Add(Tuple.Create(31, new List<int>() { 1, 2, 42 }));
            collisionGraph.Add(Tuple.Create(32, new List<int>() { 3, 6, 10 }));
            collisionGraph.Add(Tuple.Create(33, new List<int>() { 45, 46 }));
            collisionGraph.Add(Tuple.Create(34, new List<int>() { 45, 46 }));
            collisionGraph.Add(Tuple.Create(35, new List<int>() { 5, 6, 7 }));
            collisionGraph.Add(Tuple.Create(36, new List<int>() { 2, 4, 8, 42 }));
            collisionGraph.Add(Tuple.Create(37, new List<int>() { 8, 9, 10 }));
            collisionGraph.Add(Tuple.Create(38, new List<int>() { 1, 3, 7 }));
            //busLane
            collisionGraph.Add(Tuple.Create(42, new List<int>() { 1, 2, 3, 7, 8, 9, 10, 21, 22, 25, 26, 31, 32, 26, 35, 36  }));

            //Train
            collisionGraph.Add(Tuple.Create(45, new List<int>() { 3, 4, 5, 9, 23, 24, 33, 34, 46 }));
            collisionGraph.Add(Tuple.Create(46, new List<int>() { 3, 4, 5, 9, 23, 24, 33, 34, 45 }));

        }

        private void buttonStartListener_Click(object sender, EventArgs e)
        {
            buttonStartListener.Enabled = false;
            buttonStopListener.Enabled = true;
            roads = new List<Road>();
            stateQueue = new List<State>();
            populateRoads();
            int port = int.Parse(this.textBoxPort.Text.Trim());
            connection.createHTTPListener(port);
            running = true;
            mainThread = new Thread(new ThreadStart(mainLoop));
            mainThread.Start();
            
        }

        private void buttonStopListener_Click(object sender, EventArgs e)
        {
            buttonStartListener.Enabled = true;
            buttonStopListener.Enabled = false;

            connection.stopListener();

            running = false;
            mainThread.Abort();
        }
    }
}
