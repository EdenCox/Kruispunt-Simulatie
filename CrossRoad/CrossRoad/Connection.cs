using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Net.Sockets;
using System.Net;
using System.Threading;
using System.Diagnostics;
using System.Text.RegularExpressions;
using System.Security.Cryptography;
using Newtonsoft.Json;

namespace CrossRoad
{
    class Connection
    {
        private TcpListener server;
        private TcpClient client;
        private Thread acceptThread;
        private Thread listenThread;
        private int port;
        private bool handshakeDone = false;
        private bool running = false;
        private string clientIP = "";
        private bool stateChange = false;
        private List<State> queue = new List<State>();
        
        static private string guid = "258EAFA5-E914-47DA-95CA-C5AB0DC85B11";

        public void createHTTPListener(int port) {
            this.port = port;
            this.handshakeDone = false;
            server = new TcpListener(IPAddress.Any, port);
            server.Start();
            acceptThread = new Thread(new ThreadStart(acceptClient));
            try {
                acceptThread.Start();
            }
            catch (Exception e) {
                Debug.Write("Debug: Accept client exception. MSG:" + e.ToString());
            }
            running = true;

            listenThread = new Thread(new ThreadStart(listenClient));
            listenThread.Start();
        }

        private void acceptClient() {
            byte[] buffer = new byte[1024];
            try
            {
                client = server.AcceptTcpClient();
                clientIP = ((IPEndPoint)client.Client.RemoteEndPoint).Address.ToString();
                Debug.Write("Debug: Client has connected. " + clientIP);

                int i = client.Client.Receive(buffer);
                string headerResponse = (System.Text.Encoding.UTF8.GetString(buffer)).Substring(0, i);
                Debug.Write("Debug: HeaderResponse: " + headerResponse);

                string key = headerResponse.Replace("ey:", "`")
                            .Split('`')[1]
                            .Replace("\r", "").Split('\n')[0]
                            .Trim();

                // key should now equal dGhlIHNhbXBsZSBub25jZQ==
                var acceptKey = AcceptKey(ref key);

                var newLine = "\r\n";

                var response = "HTTP/1.1 101 Switching Protocols" + newLine
                     + "Upgrade: websocket" + newLine
                     + "Connection: Upgrade" + newLine
                     + "Sec-WebSocket-Accept: " + acceptKey + newLine + newLine
                     //+ "Sec-WebSocket-Protocol: chat, superchat" + newLine
                     //+ "Sec-WebSocket-Version: 13" + newLine
                     ;

                // which one should I use? none of them fires the onopen method
                client.Client.Send(System.Text.Encoding.UTF8.GetBytes(response));
                Debug.Write("Debug: Sending handshake");
                this.handshakeDone = true;
            }
            catch (Exception e) {
                Debug.WriteLine(e);
            }
        }

        private void listenClient() {
            //wait untill a client has connected
            while (client == null && running) ;
            //wait until usefull data has been recieved
            while (!client.GetStream().DataAvailable && running) ;

            while (running) {
                if (handshakeDone) {

                    byte[] buffer = new byte[client.Available];
                    if (buffer.Length == 0)
                        continue; //connection is closed notify mainclass
                    
                    client.GetStream().Read(buffer, 0, buffer.Length);
                    //List<byte[]> decodedMsg = new List<byte[]>();
                    //decodedMsg = decodeWebsocketFrame(buffer);
                    //try {
                    //    decodedMsg = decodeWebsocketFrame(buffer);
                    //}

                    //catch(Exception e) { Debug.Write(e); }
                    string msg = GetDecodedData(buffer, buffer.Length);

                    /*string testByte = "";
                    foreach (byte[] b in decodedMsg) {
                        testByte += System.Text.Encoding.UTF8.GetString(b);
                    }
                    */
                    Debug.WriteLine(System.Environment.NewLine + "Debug: MSG: " + msg + System.Environment.NewLine);
                    
                    
                    if (msg != "" && msg != "\u0003�no reason given")
                    {
                        lock (queue)
                        {

                            queue = JsonConvert.DeserializeObject<StateQueue>(msg).state;
                            //queue.Add((jsonObject)JsonConvert.DeserializeObject(testByte));
                            this.stateChange = true;
                        }
                    }
                    
                }
            }
        }

        public static string GetDecodedData(byte[] buffer, int length)
        {
            byte b = buffer[1];
            int dataLength = 0;
            int totalLength = 0;
            int keyIndex = 0;

            if (b - 128 <= 125)
            {
                dataLength = b - 128;
                keyIndex = 2;
                totalLength = dataLength + 6;
            }

            if (b - 128 == 126)
            {
                dataLength = BitConverter.ToInt16(new byte[] { buffer[3], buffer[2] }, 0);
                keyIndex = 4;
                totalLength = dataLength + 8;
            }

            if (b - 128 == 127)
            {
                dataLength = (int)BitConverter.ToInt64(new byte[] { buffer[9], buffer[8], buffer[7], buffer[6], buffer[5], buffer[4], buffer[3], buffer[2] }, 0);
                keyIndex = 10;
                totalLength = dataLength + 14;
            }

            if (totalLength > length)
                throw new Exception("The buffer length is small than the data length");

            byte[] key = new byte[] { buffer[keyIndex], buffer[keyIndex + 1], buffer[keyIndex + 2], buffer[keyIndex + 3] };

            int dataIndex = keyIndex + 4;
            int count = 0;
            for (int i = dataIndex; i < totalLength; i++)
            {
                buffer[i] = (byte)(buffer[i] ^ key[count % 4]);
                count++;
            }
            return System.Text.Encoding.UTF8.GetString(buffer, dataIndex, dataLength);
            //return Encoding.ASCII.GetString(buffer, dataIndex, dataLength);
        }

        private List<byte[]> decodeWebsocketFrame(Byte[] bytes)
        {
            List<Byte[]> ret = new List<Byte[]>();
            int offset = 0;
            while (offset + 6 < bytes.Length)
            {
                // format: 0==ascii/binary 1=length-0x80, byte 2,3,4,5=key, 6+len=message, repeat with offset for next...
                int len = bytes[offset + 1] - 0x80;

                if (len <= 125)
                {

                    //String data = Encoding.UTF8.GetString(bytes);
                    //Debug.Log("len=" + len + "bytes[" + bytes.Length + "]=" + ByteArrayToString(bytes) + " data[" + data.Length + "]=" + data);
                    Debug.Write("len=" + len + " offset=" + offset);
                    Byte[] key = new Byte[] { bytes[offset + 2], bytes[offset + 3], bytes[offset + 4], bytes[offset + 5] };
                    Byte[] decoded = new Byte[len];
                    for (int i = 0; i < len; i++)
                    {
                        int realPos = offset + 6 + i;
                        decoded[i] = (Byte)(bytes[realPos] ^ key[i % 4]);
                    }
                    offset += 6 + len;
                    ret.Add(decoded);
                }
                else
                {
                    int a = bytes[offset + 2];
                    int b = bytes[offset + 3];
                    len = (a << 8) + b;
                    //Debug.Log("Length of ws: " + len);

                    Byte[] key = new Byte[] { bytes[offset + 4], bytes[offset + 5], bytes[offset + 6], bytes[offset + 7] };
                    Byte[] decoded = new Byte[len];
                    for (int i = 0; i < len; i++)
                    {
                        int realPos = offset + 8 + i;
                        decoded[i] = (Byte)(bytes[realPos] ^ key[i % 4]);
                    }

                    offset += 8 + len;
                    ret.Add(decoded);
                }
            }
            return ret;
        }

        public List<State> getStateQueue() {
            this.stateChange = false;
            return this.queue;
        }

        public bool stateChanged() {
            return this.stateChange;
        }

        public void writeToClient(string message) {
            if(client != null) {
                byte[] msg = Encoding.UTF8.GetBytes(message);
                Debug.Write(Environment.NewLine + "Debug: " + message + Environment.NewLine);
                client.Client.Send(WebSocketHelper.EncodeMessageToSend(message));
            }
        }

        public void stopListener() {
            running = false;
            listenThread.Abort();
            if (client != null) {
                acceptThread.Abort();
                client.Close();
                client = null;
            }

            server.Stop();
            server = null;
        }

        public static T[] SubArray<T>(T[] data, int index, int length)
        {
            T[] result = new T[length];
            Array.Copy(data, index, result, 0, length);
            return result;
        }

        private static string AcceptKey(ref string key)
        {
            string longKey = key + guid;
            byte[] hashBytes = ComputeHash(longKey);
            return Convert.ToBase64String(hashBytes);
        }

        static SHA1 sha1 = SHA1CryptoServiceProvider.Create();
        private static byte[] ComputeHash(string str)
        {
            return sha1.ComputeHash(System.Text.Encoding.ASCII.GetBytes(str));
        }

    }
}
