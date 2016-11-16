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

        private int maxGreenTime = 30000;
        private int maxOrangeTime = 10000;
        private int maxWaitingTime = 120000;
        private int lastIndex = 0;

        public Form1()
        {
            InitializeComponent();
            roads = new List<Road>();
            stateQueue = new List<State>();
            collisionGraph = new List<Tuple<int, List<int>>>();
            populateRoads();
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
                if (firstRun) 
                    previousTimestamp = DateTime.Now;  
                else{
                    difference = ((TimeSpan)(DateTime.Now - previousTimestamp)).Milliseconds;
                    previousTimestamp = DateTime.Now;
                }
                firstRun = false;
                bool safeToCross = true;
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
                        }
                    }
                    else if (r.status == Status.orange) {
                        r.milliSec += difference;
                        safeToCross = false;
                        if (r.milliSec >= this.maxOrangeTime) {
                            r.status = Status.red;
                            r.changed = true;
                            r.milliSec = 0;
                            safeToCross = false;
                        }
                    }
                    /*
*/
                }//end red and orange

                //todo check if train is waiting, it has priority

                if (safeToCross) {
                    for (int i = 0; i < roads.Count; i++) { 
                        if (roads.ElementAt((i)).count > 0)
                        {
                            //if (roads.ElementAt((lastIndex + i) % roads.Count).count > 0) {
//                            Tuple<int,List<int>> collision = getCollisionTuple((lastIndex + i) % roads.Count);
                            Tuple<int, List<int>> collision = getCollisionTuple(roads.ElementAt((i)).trafficLight);

                            giveNonCollisionGreenLight(collision);
                            lastIndex = lastIndex + i + 1 % roads.Count;
                            break;
                        }
                    }
                    safeToCross = false;
                }//end green light

                //write all changes to Client
                writeToClient();
            }
        }

        private void writeToClient() {
            List<Road> changedRoads = new List<Road>();
            
            foreach (Road r in roads) {
                if (r.changed) {
                    //todo add to JSON
                    changedRoads.Add(r);
                    r.changed = false;
                }
            }
            if(changedRoads.Count != 0) {
                string msg = "{'state':[";
                for (int i = 0; i < changedRoads.Count; i++) {
                    msg += "{'trafficLight':" + changedRoads.ElementAt(i).trafficLight + ",";
                    msg += "'status': " + changedRoads.ElementAt(i).status.ToString().ToLower() + "}";
                    msg += (i != changedRoads.Count -1) ? "," : "";
                    
                }
                msg += "]}";
                connection.writeToClient(msg);
            }
            /*
            string msg = "{'state':[";
            for (int i = 0; i < roads.Count; i++)
            {
                msg += "{'trafficLight: '" + roads.ElementAt(i).trafficLight + ",";
                msg += "'status': " + roads.ElementAt(i).status.ToString().ToLower() + "}";
                msg += (i != roads.Count - 1) ? "," : "";
            }
            msg += "]}";
            connection.writeToClient(msg);
            */
        }

        private Tuple<int, List<int>> getCollisionTuple(int trafficID) {
            for (int i = 0; i < collisionGraph.Count; i++){
                if (collisionGraph.ElementAt(i).Item1 == trafficID){
                    return collisionGraph.ElementAt(i);
                }
            }
            return null;
        }

        private void giveNonCollisionGreenLight(Tuple<int, List<int>> collision) {
            foreach (Road r in roads) {
                bool inCollision = false;
                foreach (int i in collision.Item2) {
                    if(r.trafficLight == i){
                        inCollision = true;
                        break;
                    }   
                }
                if (!inCollision && r.count > 0) {
                    r.status = Status.green;
                    r.changed = true;
                    r.milliSec = 0;
                }
            }
        }

        private void populateRoads() {
            int carLanes = 10;
            int bikeLanes = 28;
            int pedestrianLane = 38;

            for(int i =1; i <= carLanes; i++){
                roads.Add(new Road(i, Status.red, 0));
            }
            /*
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
            */
        }

        private void populateCollisionGraph() {
            collisionGraph.Add(Tuple.Create(1, new List<int>() { 3, 7, 21, 28, 31, 38, 42 }));
            collisionGraph.Add(Tuple.Create(2, new List<int>() { 3, 4, 7, 8, 9, 10, 21, 25, 31, 35, 42 }));
            collisionGraph.Add(Tuple.Create(3, new List<int>() { 1, 2, 6, 7, 10, 22, 28, 32, 38, 42, 45 }));
            collisionGraph.Add(Tuple.Create(4, new List<int>() { 2, 6, 7, 8, 9, 26, 36, 42, 45 }));
            collisionGraph.Add(Tuple.Create(5, new List<int>() { 9, 25, 35, 45}));
            collisionGraph.Add(Tuple.Create(6, new List<int>() { 3, 4, 9, 10, 22, 25, 32, 35}));
            collisionGraph.Add(Tuple.Create(7, new List<int>() { 1, 2, 3, 4, 9, 10, 25, 28, 35, 38, 42}));
            collisionGraph.Add(Tuple.Create(8, new List<int>() { 2, 4, 26, 27, 36, 37, 42 }));
            collisionGraph.Add(Tuple.Create(9, new List<int>() { 2, 4, 5, 6, 27, 37, 42, 45 }));
            collisionGraph.Add(Tuple.Create(10, new List<int>() { 2, 3, 6, 7, 22, 27, 32 ,37 ,42 }));
            /*
            //bikeLane
            collisionGraph.Add(Tuple.Create(21, new List<int>() { 1, 2, 42}));
            collisionGraph.Add(Tuple.Create(22, new List<int>() { 3, 6, 10 }));
            collisionGraph.Add(Tuple.Create(23, new List<int>() { 45 }));
            collisionGraph.Add(Tuple.Create(24, new List<int>() { 45 }));
            collisionGraph.Add(Tuple.Create(25, new List<int>() { 5, 6, 7}));
            collisionGraph.Add(Tuple.Create(26, new List<int>() { 2, 4, 8, 42}));
            collisionGraph.Add(Tuple.Create(27, new List<int>() { 8, 9, 10 }));
            collisionGraph.Add(Tuple.Create(28, new List<int>() { 1, 3, 7 }));
            //footpath
            collisionGraph.Add(Tuple.Create(31, new List<int>() { 1, 2, 42 }));
            collisionGraph.Add(Tuple.Create(32, new List<int>() { 3, 6, 10 }));
            collisionGraph.Add(Tuple.Create(33, new List<int>() { 45 }));
            collisionGraph.Add(Tuple.Create(34, new List<int>() { 45 }));
            collisionGraph.Add(Tuple.Create(35, new List<int>() { 5, 6, 7 }));
            collisionGraph.Add(Tuple.Create(36, new List<int>() { 2, 4, 8, 42 }));
            collisionGraph.Add(Tuple.Create(37, new List<int>() { 8, 9, 10 }));
            collisionGraph.Add(Tuple.Create(38, new List<int>() { 1, 3, 7 }));
            //busLane
            collisionGraph.Add(Tuple.Create(42, new List<int>() { 1, 3, 7, 8, 9, 10, 21, 26, 31, 26  }));

            //Train
            collisionGraph.Add(Tuple.Create(45, new List<int>() { 3, 4, 5, 10, 23, 24, 33, 34 }));
            */
        }

        private void buttonStartListener_Click(object sender, EventArgs e)
        {
            buttonStartListener.Enabled = false;
            buttonStopListener.Enabled = true;
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
