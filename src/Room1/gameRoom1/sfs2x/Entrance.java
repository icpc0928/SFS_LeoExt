package Room1.gameRoom1.sfs2x;

import Room1.gameRoom1Logic.sfs2x.GameState;
import Room1.gameRoom1Logic.sfs2x.Room1Logic;
import com.basedb.sfs2x.GameBet;
import com.basedb.sfs2x.GameRecord;
import com.basedb.sfs2x.GetGameRecord;
import com.basedb.sfs2x.GetGameRecordDetail;
import com.helper.sfs2x.Helper;
import com.log.sfs2x.GameLog;
import com.notify.sfs2x.Notify;
import com.smartfoxserver.v2.SmartFoxServer;
import com.smartfoxserver.v2.core.SFSEventType;
import com.smartfoxserver.v2.entities.Room;
import com.smartfoxserver.v2.entities.User;
import com.smartfoxserver.v2.entities.Zone;
import com.smartfoxserver.v2.entities.data.ISFSObject;
import com.smartfoxserver.v2.extensions.SFSExtension;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class Entrance extends SFSExtension {


        int gameMode;
        int gameID;
        String gameGroup;

        SmartFoxServer sfs;
        Room1Logic logic;

        GameLog log;
        Helper helper = new Helper();
        Notify notify = new Notify();

        Properties bankProps = new Properties();

        Map<Integer, User> userMap = new HashMap<>();

        //遊戲參數
        int maxIdleTime;					//閒置時間
        int maxReel;						//軸
        int maxGrid;						//格
        int maxLine;						//線
        int betTimes;						//注
        int[] payTable;						//賠付表
        HashMap<String, Integer> gameGrid;	//圖標表

        int[] grid;
        int[] gridWin;
        int[] betRange;
        int[] levelWinPoint;

        int[] freeLight;

        GameState gameState = GameState.STATE_BASE_GAME;
        String sequenceNumber = "";
        int seqRound = 0;
        int round = 0;
        int maxPlayer;
        int runCount = 0;
        int userIdle = 0;

        int prob = 96;
        float probRange = (float) 0.5;

        int freeSpinCount = 0;
        int freeToFree = 0;

        int superSpinCount = 0;
        int superToSuper = 0;

        int superTimes = 0;

        long userPoint = 0;
        long freeSpinWin = 0;
        long seqOpenWin = 0;
        long reSpinBet = 0;

        String zoneName;
        String roomName;
        String roomID;
        String channelID;
        String gameName;
        String grpID;

        GameBet betClass = new GameBet();
        GameRecord recordClass = new GameRecord();
        GetGameRecord getRecord = new GetGameRecord();
        GetGameRecordDetail getRecordDetail = new GetGameRecordDetail();

    @Override
    public void init() {
        System.out.println("gameRoom1.init()");

        this.addRequestHandler("Table", Table.class);
        this.addRequestHandler("Bet", Bet.class);
        this.addRequestHandler("FreeSpin", FreeSpin.class);
        this.addRequestHandler("SuperSpin", SuperSpin.class);
        this.addRequestHandler("GetGameHistory", GetGameHistory.class);
        this.addRequestHandler("GetHistoryDetail", GetHistoryDetail.class);

        this.addEventHandler(SFSEventType.USER_JOIN_ROOM, UserJoinRoom.class);
        this.addEventHandler(SFSEventType.USER_LEAVE_ROOM, UserLeaveRoom.class);
        this.addEventHandler(SFSEventType.USER_DISCONNECT, UserLeaveRoom.class);

        gameMode = Integer.parseInt(getGameRoom().getProperties().get("GameMode").toString());  // 0  在Game1Config.properties
        gameID = Integer.parseInt(getGameRoom().getProperties().get("GameID").toString());      // 1
        gameGroup = getGameRoom().getProperties().get("GameGroup").toString();                  //game1
        gameName = getGameRoom().getProperties().get("GameName").toString();                    //OneLine
        maxIdleTime = Integer.parseInt(getGameRoom().getProperties().get("IdleTime").toString());//300  //閒置時間
        betRange = getBetRange(getGameRoom().getProperties().get("BetRange").toString());       //將設定檔陣列格式的字串轉 陣列
        levelWinPoint = getBetRange(getGameRoom().getProperties().get("LevelWinPoint").toString());//大巨超巨獎倍數[10,30,50]

        logic = new Room1Logic(gameID);
        maxReel = logic.getMaxReel();
        maxGrid = logic.getMaxGrid();
        maxLine = logic.getMaxLine();
        betTimes = logic.getBetTimes();
        payTable = logic.getPayTable();
        gameGrid = logic.getGameGrid();

        grid = new int[maxReel];
        gridWin = new int[maxReel];
        freeLight = new int[maxReel];

        maxPlayer = getGameRoom().getMaxUsers();

        //寫入Bank的屬性 之後要寫到 bank資料夾的
        bankProps.setProperty("TotalBet", "0");
        bankProps.setProperty("TotalWin", "0");
        bankProps.setProperty("WinRate", "0");
        bankProps.setProperty("Bank", String.valueOf(logic.getTotalBet(betRange[9]) * 100));
        bankProps.setProperty("Balance", "0");
        bankProps.setProperty("Count", "0");
        bankProps.setProperty("Prob", String.valueOf(prob));

        prob = Integer.parseInt(getZone().getProperty("GameProb" + gameID).toString());
        System.out.println("prob: " + prob);
        roomName = getGameRoom().getName();
        zoneName = getZone().getName();
        roomID = getGameRoom().getName().substring(getGameRoom().getName().length() - 3);   //猜OneLine
        System.out.println("roomID: " + roomID);

        getGameRoom().setProperty("isPlaying", "0");

        if(!getZone().getProperty("ServerMode").equals("local"))
            gameMode = 0;
        if(getZone().getProperty("ServerMode").equals("demo") && getZone().getName().equals("H5Demo"))
            gameMode = 100;
        if(getZone().getName().equals("LeoTest"))
            gameMode = 0;

        log = new GameLog(zoneName, gameName, roomName);


        //讀取Bank
        loadBankProps(zoneName, roomName);
        trace("[" + zoneName + "]" + "[" + roomName + "]" + gameName + " init, maxPlayer:" + maxPlayer + ", prob:" + prob + ", gameMode:" + gameMode +", config:" + getGameRoom().getProperties().toString());
    }


    //算主遊戲結果










    //////////////////////////////////////////////////////
    //工具
    //////////////////////////////////////////////////////

    public int[] getBetRange(String betRange){
        return helper.getBetRange(betRange);
    }

    //取房間
    public Room getGameRoom(){return this.getParentRoom();}

    //取zone
    public Zone getZone(){return this.getParentZone();}

    //傳給客端的點數
    public String reSetPointToClient(long point){return helper.setPointLongToStr(point);}

    //產生局號
    public String getGrpID(){return helper.randomGrpID(gameID, roomID);}

    //讀取bank
    public void loadBankProps(String zoneName, String roomName)
    {
        try
        {
            bankProps = helper.loadBankProps(channelID, zoneName, gameName, roomName);
            log.info("[" + zoneName + "]" + "[" + roomName + "]" + "bankProps:" + bankProps);

            if(Long.parseLong(bankProps.getProperty("Bank")) < 0)
            {
                bankProps.setProperty("TotalBet", "0");
                bankProps.setProperty("TotalWin", "0");
                bankProps.setProperty("WinRate", "0");
                bankProps.setProperty("Bank", String.valueOf(logic.getTotalBet(betRange[9]) * 100));
                bankProps.setProperty("Balance", "0");
                bankProps.setProperty("Count", "0");
                bankProps.setProperty("Prob", String.valueOf(prob));

                saveBankProps(zoneName, roomName);

                log.info("[" + zoneName + "]" + "[" + roomName + "]" + "reset bankProps:" + bankProps);
            }

        }
        catch (IOException e)
        {
            log.info("[" + zoneName + "]" + "[" + roomName + "]" + "loadProps is error," + e);
        }
    }

    //儲存bank
    public void saveBankProps(String zoneName, String roomName)
    {
        try
        {
            helper.saveBankProps(channelID, zoneName, gameName, roomName, bankProps);
            log.info("[" + zoneName + "]" + "[" + roomName + "]" + "bankProps:" + bankProps);
        }
        catch (IOException e)
        {
            log.error("[" + zoneName + "]" + "[" + roomName + "]" + "saveProps is error," + e);
        }
    }

    //設置bank
    public void setBank(int mode, long totalBet, long totalWin)
    {
        if(mode != 0)
            return;

        //局數到達上限||prob更動  重置bank
        if(totalBet != 0 && (Integer.valueOf(bankProps.getProperty("Count")) > Integer.parseInt(getGameRoom().getProperties().get("BankGrpReSet").toString())))
        {
            bankProps.setProperty("TotalBet", "0");
            bankProps.setProperty("TotalWin", "0");
            bankProps.setProperty("WinRate", "0");
            bankProps.setProperty("Bank", String.valueOf(logic.getTotalBet(betRange[9]) * 100));
            bankProps.setProperty("Count", "0");
        }

        if(totalBet != 0)
        {
            bankProps.setProperty("TotalBet", String.valueOf(Long.parseLong(bankProps.getProperty("TotalBet")) + totalBet));
            bankProps.setProperty("Bank", String.valueOf(Long.parseLong(bankProps.getProperty("Bank")) + (totalBet * prob / 100) - totalWin));
            bankProps.setProperty("Balance", String.valueOf(Long.parseLong(bankProps.getProperty("Balance")) + (totalBet * prob / 100) - totalWin));
        }
        else
        {
            bankProps.setProperty("Bank", String.valueOf(Long.parseLong(bankProps.getProperty("Bank")) - totalWin));
            bankProps.setProperty("Balance", String.valueOf(Long.parseLong(bankProps.getProperty("Balance")) - totalWin));
        }

        bankProps.setProperty("Count", String.valueOf(Integer.parseInt(bankProps.getProperty("Count")) + 1));
        bankProps.setProperty("TotalWin", String.valueOf(Long.parseLong(bankProps.getProperty("TotalWin")) + totalWin));
        bankProps.setProperty("WinRate", String.valueOf(Double.parseDouble(bankProps.getProperty("TotalWin")) / Double.parseDouble(bankProps.getProperty("TotalBet"))));
        bankProps.setProperty("Prob", String.valueOf(prob));
    }

    //擴展內部溝通 目前只做補點用
    public Object handleInternalMessage(String action, Object params)
    {
        ISFSObject res = (ISFSObject) params;

        log.info("[" + zoneName + "]" + "[" + roomName + "]" + action + "Params" + res.toJson());

        if(action.equals("resetPoint"))
            reSetPoint(helper.setPointStrToLong(res.getUtfString("Points").toString()));

        return params;
    }

    //重置房間點數
    public void reSetPoint(long points)
    {
        userPoint = points;

        log.info( "[" + zoneName + "]" + "[" + roomName + "]" + "ResetPoints : " + userPoint);
    }

    //大獎廣播 風控用
    public void bigWinBroadcast(User player, int levelWin, long totalBet, long totalWin)
    {
        if(!getZone().getProperty("BroadcastMode").equals("0") || !zoneName.equals("H5Game"))
            return;

        String message =
                "\r\n-seqNumber : " + sequenceNumber +
                        "\r\n-game : " + roomName +
                        "\r\n-player : " + player.getName() +
                        "\r\n-levelWin : " + levelWin +
                        "\r\n-bet : " + reSetPointToClient(totalBet) +
                        "\r\n-win : " + reSetPointToClient(totalWin) +
                        "\r\n-bank : " + reSetPointToClient(Long.parseLong(bankProps.getProperty("Bank"))) +
                        "\r\n-winRate : " + bankProps.getProperty("WinRate").toString();

        notify.bigWinNotify(message);
    }

    //取大獎等級
    public int getLevelWin(long totalBet, long totalWin)
    {
        int levelWin = 0;

        for(int i = levelWinPoint.length; i >= 1; i--)
        {
            if(totalWin >= totalBet * levelWinPoint[i - 1])
            {
                levelWin = i;
                break;
            }
        }

        return levelWin;
    }





}
