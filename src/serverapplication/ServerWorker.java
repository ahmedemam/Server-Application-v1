package serverapplication;

import com.mysql.fabric.Server;

import java.io.*;
import java.net.Socket;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

public class ServerWorker extends Thread {

    private final Socket clientSocket;
    private ServerController serverController;
    private OutputStream outputStream;
    private DatabaseManager databaseManager = null;
    private String databaseName = "Game";
    private String databasePath = "jdbc:mysql://localhost:3306";
    private String databaseUsername = "root";
    private String databaseUserpassword = "011421264482014Hardworker";
    private LinkedHashMap<String, String> serverWorkerData = null;
    private ArrayList<ServerWorker> serverWorkers=null;
    // private int PLAYER_ID=0;

    public ServerWorker(ServerController serverController, Socket clientSocket) {
        this.clientSocket = clientSocket;
        this.serverController = serverController;
        serverWorkerData = new LinkedHashMap<>();
    }

    private void handleClientSocket() throws IOException, InterruptedException, SQLException {
        InputStream inputStream = clientSocket.getInputStream();
        outputStream = this.clientSocket.getOutputStream();
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        String clientMessage = "";
        while ((clientMessage = bufferedReader.readLine()) != null) {
            System.out.println(clientMessage);
            String[] inputTokens = clientMessage.split(" ");
            if (inputTokens.length > 0) {
                if ("INSERT".equals(inputTokens[0]) && inputTokens.length > 1) {
                    // INSERT PLAYER PlayerName PlayerEmail PlayerPassword
                    insertOperationHandler(outputStream, inputTokens);
                } else if ("DELETE".equals(inputTokens[0]) && inputTokens.length > 1) {
                    // DELETE PLAYER ID
                    deleteOperationHandler(outputStream, inputTokens);
                } else if ("SIGNOUT".equals(inputTokens[0]) && inputTokens.length > 1) {
                    // SIGNOUT
                    signOutOperationHandler(outputStream,inputTokens);
                } else if ("LOGIN".equals(inputTokens[0]) && inputTokens.length > 1) {
                    // LOGIN USEREMAIL PASSWORD
                    loginOperationHandler(outputStream, inputTokens);
                } else if("UPDATE".equals(inputTokens[0]) && inputTokens.length > 1){
                    updateOperationHandler(outputStream,inputTokens);
                }else if("INVITATION".equals(inputTokens[0]) && inputTokens.length >2){
                    // INVITATION GAME REQUEST S.ID R.ID
                    // INVITATION GAME RESPONSE S.ID R.ID ACCEPTANCE_STATE
                    invitationOperationHandler(outputStream,inputTokens);
                }else if("GAME".equals(inputTokens[0])){
                    gameOperationHandler(outputStream,inputTokens);
                }
                else if ("CHAT".equals(inputTokens[0]) && inputTokens.length > 1){
                    chatOperationHandler(outputStream,inputTokens);
                }
                else{
                    //
                    sendMessage("INVALID TOKEN");
                }
            }
        }
        clientSocket.close();
    }

    private void chatOperationHandler(OutputStream outputStream, String[] inputTokens) throws IOException {
        // Chat Message
        // CHAT S_ID R_ID S_MESSAGE
        ArrayList<ServerWorker> serverWorkers=serverController.getServerWorkers();
        if (inputTokens[0].equals("CHAT") && inputTokens.length==4){
            String senderId=inputTokens[1];
            String recieverId=inputTokens[2];
            String senderMessage=inputTokens[3];
            String chatToken="CHAT "+senderId+" "+recieverId+" "+senderMessage;
            for (ServerWorker worker: serverWorkers){
                if (worker.getWorkerId().equals(recieverId)){
                    worker.sendMessage(chatToken);
                }
            }
        }
    }

    private void gameOperationHandler(OutputStream outputStream, String[] inputTokens) throws IOException {
        // GAME REQUEST - INVITATION
        // GAME PLAY
        // GAME SAVED
        // GAME PAUSED
        ArrayList<ServerWorker> serverWorkers=serverController.getServerWorkers();
        if("GAME".equals(inputTokens[0]) && "PLAY".equals(inputTokens[1]) && inputTokens.length==5){
            // GAME PLAY S_ID R_ID X Y CHAR
            String senderId=inputTokens[2];
            String recieverId=inputTokens[3];
            String xPoint=inputTokens[4];
            String yPoint=inputTokens[5];
            String senderChar=inputTokens[6];
            String takenSending="GAME PLAY "+senderId+" "+recieverId+" "+xPoint+" "+yPoint+" "+senderChar;
            for (ServerWorker worker: serverWorkers){
                if(worker.getWorkerId().equals(recieverId)){
                    System.out.println(takenSending);
                    worker.sendMessage(takenSending);
                }
            }
        }
        else if("GAME".equals(inputTokens[0]) && "PAUSED".equals(inputTokens[1])){
            // GAME PAUSED
            // NOT IMPLEMENTED YET
        }
        else if("GAME".equals(inputTokens[0]) && "SAVED".equals(inputTokens[1])){
            // GAME SAVED
            // NOT IMPLEMENTED YET
        }
    }

    private void invitationOperationHandler(OutputStream outputStream, String[] inputTokens) throws IOException {
        // INVITATION REQUEST S.ID R.ID
        // INVITATION RESPONSE S.ID R.ID ACCEPTANCE_STATE
        ArrayList<ServerWorker> serverWorkers=serverController.getServerWorkers();
        if(inputTokens[0].equals("INVITATION") && inputTokens[1].equals("REQUEST") && inputTokens.length == 5){
            String senderID=inputTokens[2];
            String receiverID=inputTokens[3];
            String reciverName=inputTokens[4];
            String invitationRequest="INVITATION REQUEST "+senderID+" "+receiverID+" "+reciverName;
            System.out.println(invitationRequest);
            // search and send
            for (ServerWorker worker: serverWorkers){
                if(worker.getWorkerId().equals(receiverID)){
                    System.out.println(worker.getWorkerId() + " "+worker.getWorkerName()+" :"+receiverID);
                    worker.sendMessage(invitationRequest);
                }
            }
        }
        else if(inputTokens[0].equals("INVITATION")&&inputTokens[1].equals("RESPONSE") && inputTokens.length == 6){
            String senderId=inputTokens[2];
            String receiverId=inputTokens[3];
            String invitaionState=inputTokens[5];
            String recieverName=inputTokens[4];
            String invitationResponse="INVITATION RESPONSE "+senderId+" "+receiverId+" "+recieverName+" "+invitaionState;
            for (ServerWorker worker: serverWorkers){
                if(worker.getWorkerId().equals(receiverId)){
                    System.out.println(worker.getWorkerId() + " "+worker.getWorkerName()+" : "+senderId+" "+receiverId);
                    worker.sendMessage(invitationResponse);
                }
            }
        }
    }

    private void signOutOperationHandler(OutputStream outputStream, String[] inputTokens) throws SQLException, IOException {
        String table="";
        databaseManager=new DatabaseManager(databasePath, databaseName, databaseUsername, databaseUserpassword);
        ArrayList<ServerWorker> serverWorkers=serverController.getServerWorkers();
        // get update data and close connection
        // close socket connection
        // ............................
        // SIGNOUT PLAYER Data
        // ............................
        if(inputTokens[0].equals("SIGNOUT") && inputTokens[1].equals("PLAYER") && inputTokens.length==3){
            String [] playerData=inputTokens[2].split(",");
            LinkedHashMap<String,String> playerDataUpdated=new LinkedHashMap<>();
            String [] tempKeyValue;
            int playerIdValue=0;
            for (int i=0;i<playerData.length;i++){
                tempKeyValue=playerData[i].split(":");
                if(tempKeyValue[0].equals("PlayerId")){
                    playerIdValue=Integer.parseInt(tempKeyValue[1]);
                }
                else{
                    if(tempKeyValue[0].equals("PlayerOnlineStatus")){
                        // System.out.println(tempKeyValue[0]+" "+tempKeyValue[1]);
                        playerDataUpdated.put(tempKeyValue[0],"Offline");
                    }
                }
            }
            // SignOut Query Built
            String offlinePlayer="OFFLINE PLAYER "+playerIdValue+" "+playerDataUpdated.get("PlayerName");
            // update data to database
            databaseManager.updateDatabase("Player", playerIdValue, "PlayerId", true, playerDataUpdated);
            // remove worker form server workers
            serverController.removeServerWorker(this);
            // send message to all
            for (ServerWorker worker:serverWorkers){
                if(!worker.getWorkerId().equals(playerIdValue + "")){
                    worker.sendMessage(offlinePlayer);
                }
            }
            clientSocket.close();
        }

    }

    private void sendMessage(String messageWorker) throws IOException {
        messageWorker += '\n';
        outputStream.write(messageWorker.getBytes());
    }

    @Override
    public void run() {
        try {
            handleClientSocket();
        } catch (IOException | InterruptedException | SQLException exception) {
            System.err.println("> exception: ServerWorker.run(): " + exception.getMessage());
            exception.printStackTrace();
        }
    }

    private void insertOperationHandler(OutputStream outputStream, String[] inputTokens) throws SQLException, IOException {
        databaseManager = new DatabaseManager(databasePath, databaseName, databaseUsername, databaseUserpassword);
        LinkedHashMap<String, String> tableData = new LinkedHashMap<>();
        String tableName = "";
        if ("INSERT".equals(inputTokens[0])  && inputTokens.length > 1) {
            if ("PLAYER".equals(inputTokens[1]) && inputTokens.length == 5) {
                tableName = "Player";
                String playerName = inputTokens[2];
                String playerEmail = inputTokens[3];
                String playerPassword = inputTokens[4];
                tableData.put("PlayerName", playerName);
                tableData.put("PlayerEmail", playerEmail);
                tableData.put("PlayerPassword", playerPassword);
                tableData.put("PlayerOnlineStatus", "Offline");
                tableData.put("PlayerScore", "0");
                if (databaseManager.insertDatabase(tableName, MIN_PRIORITY, true, tableData)) {
                    // SEND SUCCESSFULLY INSERTED
                    sendMessage("SUCCESS INSERT");
                } else {
                    // FAILED INSERTED
                    sendMessage("FAILED INSERT");
                }
            } else if ("GAME".equals(inputTokens[1]) && inputTokens.length == 8) {
                /////////////////////////////////////////////////////////////////////////////////////////////////////////////
                ////////////////// ..................................................... ////////////////////////////////////
                ////////////////// DATA TYPES PROBLEM - HANDELDE BY CHANGING IN DATABASE ////////////////////////////////////
                ////////////////// PARSE DATA TO SOLVE THIS PROBLEM                      ////////////////////////////////////
                ////////////////// ..................................................... ////////////////////////////////////
                /////////////////////////////////////////////////////////////////////////////////////////////////////////////
                tableName = "Game";
                String player1Id = inputTokens[2];
                String player2Id = inputTokens[3];
                String player1Symbol = inputTokens[4];
                String player2Symbol = inputTokens[5];
                String gameStatus = inputTokens[6];
                String gameBoardId = inputTokens[7];
                tableData.put("GamePlayer1Id", player1Id);
                tableData.put("GamePlayer2Id", player2Id);
                tableData.put("GamePlayer1Symbol", player1Symbol);
                tableData.put("GamePlayer2Symbol", player2Symbol);
                tableData.put("GameStatus", gameStatus);
                tableData.put("GameBoardId", gameBoardId);
                if (databaseManager.insertDatabase(tableName, MIN_PRIORITY, true, tableData)) {
                    // SEND SUCCESSFULLY INSERTED
                    sendMessage("SUCCESS INSERT");
                } else {
                    // FAILED INSERTED
                    sendMessage("FAILED INSERT");
                }
            } else if ("GAMEBOARD".equals(inputTokens[1]) && inputTokens.length == 3) {
                /////////////////////////////////////////////////////////////////////////////////////////////////////////////
                //////////////////                                                       ////////////////////////////////////
                ////////////////// CONFLICT USAGE PROBLEM: INSERTED IN WHAT CASE ??????? ////////////////////////////////////
                //////////////////                                                       ////////////////////////////////////
                /////////////////////////////////////////////////////////////////////////////////////////////////////////////
                tableName = "GameBoard";
                String GameBoardArray = inputTokens[2];
                tableData.put("GameBoardArray", GameBoardArray);
                if (databaseManager.insertDatabase(tableName, MIN_PRIORITY, true, tableData)) {
                    // SEND SUCCESSFULLY INSERTED
                    sendMessage("SUCCESS INSERT");
                } else {
                    // FAILED INSERTED
                    sendMessage("FAILED INSERT");
                }
            } else if ("CHAT".equals(inputTokens[1]) && inputTokens.length == 4) {
                /////////////////////////////////////////////////////////////////////////////////////////////////////////////
                //////////////////                                                       ////////////////////////////////////
                ////////////////// CHAT COVERSATION ARRAY STORED AS [.......]            ////////////////////////////////////
                //////////////////                                                       ////////////////////////////////////
                /////////////////////////////////////////////////////////////////////////////////////////////////////////////

                tableName = "Chat";
                String chatGameId = inputTokens[3];
                String chatConversation = inputTokens[4];
                if (databaseManager.insertDatabase(tableName, MIN_PRIORITY, true, tableData)) {
                    // SEND SUCCESSFULLY INSERTED
                    sendMessage("SUCCESS INSERT");
                } else {
                    // FAILED INSERTED
                    sendMessage("FAILED INSERT");
                }
            }
        } else {
            sendMessage("INVALID TOKENS");
        }
        databaseManager.closeConnection();
    }

    private void deleteOperationHandler(OutputStream outputStream, String[] inputTokens) throws SQLException, IOException {
        String tableName = "";
        databaseManager = new DatabaseManager(databasePath, databaseName, databaseUsername, databaseUserpassword);
        if ("PLAYER".equals(inputTokens[1])) {
            tableName = "Player";
            int playerId = Integer.parseInt(inputTokens[2]);
            if (databaseManager.deleteDatabase(tableName, playerId, "PlayerId", true)) {
                sendMessage("SUCCESS DELETE");
            } else {
                sendMessage("FAILED DELETE");
            }
        } else {
            sendMessage("WEIRD TOKENS");
        }
        databaseManager.closeConnection();
    }

    private void loginOperationHandler(OutputStream outputStream, String[] inputTokens) throws SQLException, IOException {
        // check data ? getData,SendSuscce,saveWorkerDat : sendFailed
        databaseManager = new DatabaseManager(databasePath, databaseName, databaseUsername, databaseUserpassword);
        LinkedHashMap<String, String> workerData = new LinkedHashMap<>();
        LinkedHashMap<String, String> conditionData = new LinkedHashMap<>();

        String tableName = "Player";
        if (inputTokens.length == 3) {
            String userEmail = inputTokens[1];
            String userPassword = inputTokens[2];
            conditionData.put("PlayerEmail", userEmail);
            conditionData.put("PlayerPassword", userPassword);
            workerData = databaseManager.selectOneDatabase(tableName, conditionData);
            if (workerData == null) {
                sendMessage("FAILED LOGIN");
            } else {
                int playerId = 0;
                // sendMessage("SUCCESS LOGIN");
                String playerDataSend="SUCCESS LOGIN ";
                for (Map.Entry<String, String> entry : workerData.entrySet()) {
                    String key = entry.getKey();
                    String value = entry.getValue();
                    if(key.equals("PlayerId")){
                        playerId=Integer.parseInt(value);
                        System.out.println("PlayerId: "+playerId);
                    }
                    if(key.equals("PlayerOnlineStatus")){
                        value="Online";
                    }
                    serverWorkerData.put(key, value);
                    // sender to client
                    playerDataSend+=key+":"+value+",";
                }
                // sendMessage("ONLINE "+this.getWorkerName());
                sendMessage(playerDataSend);
                // Update database with these data
                databaseManager.updateDatabase(tableName, playerId, "PlayerId", true, serverWorkerData);
                // Send Login User Status to All Users Online
                ArrayList<ServerWorker> serverWorkers=serverController.getServerWorkers();
                System.out.println("Send All users to online one");
                for(ServerWorker serverWorker : serverWorkers){
                    if(serverWorker.getWorkerId()!=null){ // server worker name != null
                        if(!this.getWorkerId().equals(serverWorker.getWorkerId())) { //
                            String currentUserMessage = "ONLINE " + serverWorker.getWorkerId() +" " +serverWorker.getWorkerName()+" "+serverWorker.getWorkerScore();
                            sendMessage(currentUserMessage);
                        }
                    }
                }
                // send others online users current user's status
                String messageWorker = "ONLINE " + this.getWorkerId() +" " +this.getWorkerName()+" "+this.getWorkerScore();
                for(ServerWorker serverWorker : serverWorkers){
                    if(!this.getWorkerId().equals(serverWorker.getWorkerId())) {
                        serverWorker.sendMessage(messageWorker);
                    }
                }
            }
        } else {
            sendMessage("WIRED TOKENDS");
        }
        databaseManager.closeConnection();
    }

    public LinkedHashMap<String, String> getWorkerData() {
        return this.serverWorkerData;
    }

    public void setWorkerData(LinkedHashMap<String, String> workerData) {
        this.serverWorkerData = workerData;
    }


    private void updateOperationHandler(OutputStream outputStream, String[] inputTokens) {
        String tableName="";
        databaseManager=new DatabaseManager(databasePath, databaseName, databaseUsername, databaseUserpassword);
    }

    private String getWorkerName(){
       return this.serverWorkerData.get("PlayerName");
    }

    private String getWorkerId(){
        return this.serverWorkerData.get("PlayerId");
    }

    private String getWorkerScore(){
       return this.serverWorkerData.get("PlayerScore");
    }

}




///////////////////////////////////////////////
// CLIENT
// . client signout
// . client invitation
// . client chat
// . client save game
// . client resume game
// . client re-gui
// . client ai-game
// . client facebook
///////////////////////////////////////////////
//////////// test all server operations
//////////// test all client operations
///////////////////////////////////////////////
// Invitation - class message


