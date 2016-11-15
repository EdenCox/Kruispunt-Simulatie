using Newtonsoft.Json;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace CrossRoad
{
    public class State
    {
        public int trafficLight { get; set; }
        public int count { get; set; }
    }

    public class StateQueue
    {
        public List<State> state { get; set; }
    }

    public class Wrapper
    {
        [JsonProperty("state")]
        public StateQueue StateQueue { get; set; }
    }
}
