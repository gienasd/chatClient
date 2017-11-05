package pl.pawelprzystarz.chat.controllers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import pl.pawelprzystarz.chat.Utils;
import pl.pawelprzystarz.chat.models.ISocketObserver;
import pl.pawelprzystarz.chat.models.MessageModel;
import pl.pawelprzystarz.chat.models.SocketConnector;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;


public class MainController implements Initializable, ISocketObserver {

    public static final Gson GSON = new GsonBuilder().create();

    @FXML
    Button buttonSend;

    @FXML
    TextArea textMessages;

    @FXML
    TextField textWriteMessage;

    private List<String> commandList;
    private int index;

    private SocketConnector socketConnector = SocketConnector.getInstance();

    public void initialize(URL location, ResourceBundle resources) {
        commandList = new ArrayList<>();
        clickEnterOnWriteMessage();
        clickButtonSend();

        textMessages.setWrapText(true);

        socketConnector.connect();
        socketConnector.registerObserver(this);
    }

    private void clickButtonSend() {
        buttonSend.setOnMouseClicked(event -> {
                sendAndClear();
        });
    }

    private void clickEnterOnWriteMessage() {
        textWriteMessage.setOnKeyPressed(event -> {
                if (event.getCode() == KeyCode.ENTER) {
                        sendAndClear();
                }else if(event.getCode() == KeyCode.UP){
                    parseUpKey();
                }
        });
    }

    private void parseUpKey() {
        textWriteMessage.setText(commandList.get(index));
        if(index - 1 < 0){
            index = commandList.size() - 1;
        }else {
            index--;
        }
    }

    private void sendAndClear() {
        if(!textWriteMessage.getText().isEmpty()) {
            commandList.add(textWriteMessage.getText());
            index = commandList.size() - 1;
            sendMessagePacket(textWriteMessage.getText());
            textWriteMessage.clear();
        }else {
            Utils.createSimpleDialog("Empty message", "", "Can't send empty message!");
        }
    }

    private void sendMessagePacket(String message){
        MessageModel messageModel = new MessageModel();
        messageModel.setContext(message);
        messageModel.setMessageType(MessageModel.MessageType.MESSAGE);

        socketConnector.sendMessage(GSON.toJson(messageModel));
    }

    public void onMessage(String s) {
        MessageModel messageModel = GSON.fromJson(s, MessageModel.class);

        switch (messageModel.getMessageType()){
            case MESSAGE:{
                textMessages.appendText(messageModel.getContext());
                break;
            }
            case OPEN_DIALOG:{
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setHeaderText("");
                alert.setTitle("Serwer");
                alert.setContentText(messageModel.getContext());
                alert.show();
                break;
            }
            case CLOSE_WINDOW:{
                Platform.exit();
                break;
            }
        }
    }
}