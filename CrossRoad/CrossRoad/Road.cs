using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace CrossRoad
{
    class Road
    {
        public int trafficLight{ get; set; }
        public Status status { get; set;
        }
        public int count { get; set; }
        public bool changed { get; set; }
        public int milliSec { get; set; }

        public Road(int trafficLight, Status status, int count) {
            this.trafficLight = trafficLight;
            this.status = status;
            this.count = count;
            this.changed = false;
        }

        
    }
}
