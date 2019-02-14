package serverapplication;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.shape.Rectangle;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class Controller implements Initializable {
    private ServerController serverController=null;
    private boolean serverState=false;
    private boolean restartServer=false;
    @FXML private Button startServer;
    @FXML private Button stopServer;
    @FXML private Button showPlayers;
    @FXML private Rectangle stateRectangle;


    public void initialize(URL url, ResourceBundle resourceBundle){
        startServer.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if(!serverState && !restartServer){
                    serverController=new ServerController(4444);
                    serverController.start();
                    stateRectangle.setStyle("-fx-fill: chartreuse;");
                    serverState=true;
                }
                if(!serverState && restartServer){
                    serverController.restartConnection();
                }
            }
        });

        stopServer.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if(serverState){
                    // serverController=null;
                    serverState=false;
                    stateRectangle.setStyle("-fx-fill: tomato;");
                    serverController.stopConnection();
                    restartServer=true;
                }
            }
        });

        /// CLOSE CONNECTION PROBLEM
    }
}
