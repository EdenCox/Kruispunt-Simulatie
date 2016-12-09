/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kruispunt_sim;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.logging.Logger;
import javax.websocket.DeploymentException;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import org.json.*;

/**
 * This Class acts as a websocket client for connection purposes.
 *
 * @author Eden
 */

public class ClientSocket extends WebSocketClient{

    private static final Logger LOGGER = Logger.getLogger(ClientSocket.class.getName());
    //private Session session;
    private JSONObject lastState;

    /**
     * *
     * Constructor of the websocket class
     *
     * @param ip string representation of the server ip
     * @param port port connection
     * @throws IOException
     * @throws javax.websocket.DeploymentException
     * @throws java.net.URISyntaxException
     * @throws java.net.MalformedURLException
     */
    public ClientSocket(String ip, String port) throws URISyntaxException  {
        super(new URI("ws://" + ip + ":" + port));
        connectToWebSocket();
        lastState = new JSONObject();
    }

    /**
     * *
     * Tells the socket what to do when a connection has opened
     *
     * @param session
     * @throws IOException
     */
    @Override
    public void onOpen(ServerHandshake handshake){
        
    }
    
    /***
     * Tells the socket what to do when the connection has closed.
     * @throws IOException 
     */

    @Override
    public void onClose( int code, String reason, boolean remote ) {
        
    }
    
    /***
     * Tells the socket what to do when it has received a message
     * @param message 
     */
    @Override
    public void onMessage(String message) {
        lastState = new JSONObject(message);
    }


    @Override
    public void onError(Exception ex) {
        System.err.println(ex.toString());
    }

    /***
     * This method makes a valid websocket connection with a valid host
     * @param ip
     * @param port
     * @throws IOException
     * @throws DeploymentException
     * @throws URISyntaxException 
     */
    private void connectToWebSocket(){
        super.connect();
    }

    /***
     * method used to send a message to the server
     * @param text 
     */
    public void sendString(String text) {
         super.send(text);
    }

    /***
     * This method returns the last message gotten from the server
     * @return 
     */
    public JSONObject getState() {
        return lastState;
    }

    /***
     * This method closes the websocket connections 
     */
    public void closeConnection() {
        super.close();
    }
}
