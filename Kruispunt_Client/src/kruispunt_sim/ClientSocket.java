/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kruispunt_sim;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.ZonedDateTime;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.websocket.ClientEndpoint;
import javax.websocket.ContainerProvider;
import javax.websocket.DeploymentException;
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.WebSocketContainer;
import javax.websocket.OnError;

import org.json.*;

/**
 * This Class acts as a websocket client for connection purposes.
 *
 * @author Eden
 */
@ClientEndpoint
public class ClientSocket {

    private static final Logger LOGGER = Logger.getLogger(ClientSocket.class.getName());
    private Session session;
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
    public ClientSocket(String ip, String port) throws IOException, DeploymentException, URISyntaxException {
        connectToWebSocket(ip, port);
        lastState = new JSONObject();
    }

    /**
     * *
     * Tells the socket what to do when a connection has opened
     *
     * @param session
     * @throws IOException
     */
    @OnOpen
    public void onOpen(Session session) throws IOException {
        this.session = session;
    }
    
    /***
     * Tells the socket what to do when the connection has closed.
     * @throws IOException 
     */
    @OnClose
    public void onClose() throws IOException {
        session.close();
    }
    
    /***
     * Tells the socket what to do when it has received a message
     * @param message 
     */

    @OnMessage
    public void onMessage(String message) {
        System.out.println(ZonedDateTime.now() + " WebSocket message Received!");
        System.out.println(message);
        lastState = new JSONObject(message);
    }

    @OnError
    public void onError(Throwable t) {
        System.err.println(t.toString());
    }

    /***
     * This method makes a valid websocket connection with a valid host
     * @param ip
     * @param port
     * @throws IOException
     * @throws DeploymentException
     * @throws URISyntaxException 
     */
    private void connectToWebSocket(String ip, String port) throws IOException, DeploymentException, URISyntaxException {
        WebSocketContainer container = ContainerProvider.getWebSocketContainer();
        String uriCheck = "ws://" + ip + ":" + port;
        URI uri = new URI(uriCheck);
        container.connectToServer(this, uri);

        //LOGGER.log(Level.SEVERE, null, ex);
        //System.exit(-1);
    }

    /***
     * method used to send a message to the server
     * @param text 
     */
    public void sendString(String text) {
        try {
            session.getBasicRemote().sendText(text);
            //System.out.println(text + " has been send");
        } catch (IOException ex) {
            Logger.getLogger(ClientSocket.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /***
     * This method returns the last message gotten from the servr
     * @return 
     */
    public JSONObject getState() {
        return lastState;
    }

    /***
     * This method closes the websocket connections
     * @throws IOException 
     */
    public void closeConnection() throws IOException {
        session.close();
    }
}
