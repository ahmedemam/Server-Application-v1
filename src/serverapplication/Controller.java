package serverapplication;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.shape.Rectangle;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.ResourceBundle;

public class Controller implements Initializable {
    private ServerController serverController=null;
    private boolean serverState=false;
    private boolean restartServer=false;
    @FXML private Button startServer;
    @FXML private Button stopServer;
    @FXML private Button showPlayers;
    @FXML private Rectangle stateRectangle;
    @FXML private ListView<String> playersList;
    private DatabaseManager databaseManager = null;
    private String databaseName = "Game";
    private String databasePath = "jdbc:mysql://localhost:3306";
    private String databaseUsername = "root";
    private String databaseUserpassword = "011421264482014Hardworker";
    private LinkedHashMap<String,LinkedHashMap<String,String>> dataSelection;
    private LinkedHashMap<String,String> conditionData;




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

        showPlayers.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                databaseManager=new DatabaseManager(databasePath, databaseName, databaseUsername, databaseUserpassword);
                String tableName="Player";
                try {
                    dataSelection=databaseManager.selectTable(tableName, null);
                    if(dataSelection!=null){
                        playersList.getItems().clear();
                        int length=dataSelection.size();
                        int counter=1;
                        LinkedHashMap<String,String> dataRows = null;
                        for (Map.Entry<String, LinkedHashMap<String,String>> entry : dataSelection.entrySet()) {
                            String key = entry.getKey();
                            LinkedHashMap<String,String> valueRow=entry.getValue();
                            String [] rowArray= valueRow.values().toArray(new String[0]);
                            String row=counter+" - ";
                            for (int i=0;i<4;i++){
                                row+=rowArray[i]+" ";
                            }
                            playersList.getItems().add(row);
                            counter++;
                        }
                    }
                    else{
                        System.out.println("NO DATA RESULT NULL");
                    }
                } catch (SQLException exception) {
                    System.err.println("> exception: "+exception.getMessage());
                }
            }
        });
        /// CLOSE CONNECTION PROBLEM
    }
}
