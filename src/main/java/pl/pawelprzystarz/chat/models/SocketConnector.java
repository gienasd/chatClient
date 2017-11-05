package pl.pawelprzystarz.chat.models;

import javafx.application.Platform;
import pl.pawelprzystarz.chat.controllers.MainController;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import javax.websocket.*;

@ClientEndpoint
public class SocketConnector {
    private static SocketConnector ourInstance = new SocketConnector();

    public static SocketConnector getInstance() {
        return ourInstance;
    }

    private Session session;
    private WebSocketContainer container;
    private List<ISocketObserver> observers;

    private SocketConnector() {
        container = ContainerProvider.getWebSocketContainer();
        observers = new ArrayList<>();
    }

    public void connect(){
        URI uri = URI.create("ws://localhost:8080/chat");
        try {
            container.connectToServer(this, uri);
        } catch (DeploymentException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Połączono!");
    }

    @OnOpen
    public void onOpen(Session session){
        this.session = session;
        System.out.println("Połączono");
    }

    public void sendMessage(String message){
        try {
            session.getBasicRemote().sendText(message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @OnMessage
    public void onMessage(Session session, String message){
        observers.forEach(s -> {
            Platform.runLater(() -> s.onMessage(message));
        });
    }

    public void registerObserver(ISocketObserver observer) {
        observers.add(observer);
    }
}
