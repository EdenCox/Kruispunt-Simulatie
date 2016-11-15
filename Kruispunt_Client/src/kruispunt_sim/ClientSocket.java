/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kruispunt_sim;

import java.io.IOException;
import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.websocket.ClientEndpoint;
import javax.websocket.ContainerProvider;
import javax.websocket.DeploymentException;
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.WebSocketContainer;
import org.json.*;

/**
 *
 * @author Eden
 */
@ClientEndpoint
public class ClientSocket {

    private static final Logger LOGGER = Logger.getLogger(ClientSocket.class.getName());
    private Session session;
    JSONObject jsonString;

    public ClientSocket(String ip, String port) {
        connectToWebSocket(ip, port);
        jsonString = new JSONObject();

    }

    @OnOpen
    public void onOpen(Session session) {
        this.session = session;
    }

    @OnClose
    public void onClose() throws IOException {
        session.close();
    }

    @OnMessage
    public void onMessage(String message) {
        System.out.println("WebSocket message Received!");
        System.out.println(message);
//        JSONObject json = new JSONObject(message);
//        System.out.println(message);
//        System.out.println(json.get("Lol"));
    }

    private void connectToWebSocket(String ip, String port) {
        WebSocketContainer container = ContainerProvider.getWebSocketContainer();
        try {
            URI uri = URI.create("ws://"+ip+":"+port);
            //URI uri = URI.create("ws://echo.websocket.org");
            container.connectToServer(this, uri);
        } catch (DeploymentException | IOException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
            System.exit(-1);
        }
        JSONObject json = new JSONObject();
        json.append("Test", "Henk");
        sendString("{'state':[{ 'trafficLight': 1, 'count' : 10}]}");
    }

    public void sendString(String text) {
        try {
            session.getBasicRemote().sendText(text);
            System.out.println(text + " has been send");
        } catch (IOException ex) {
            Logger.getLogger(ClientSocket.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
