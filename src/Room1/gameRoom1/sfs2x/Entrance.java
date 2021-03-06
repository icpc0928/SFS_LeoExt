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
import com.smartfoxserver.v2.entities.data.SFSObject;
import com.smartfoxserver.v2.extensions.SFSExtension;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Date;
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

        System.out.println("Debug01: " + gameID);

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
    public long getBaseResult(long lineBet){
        HashMap<String, Object> resp = null;

        int runCount = 0;
        //取得bankProps內的屬性(剛剛設的)
        double rate = Double.parseDouble(bankProps.getProperty("WinRate")) * 100;
        long bank = Long.parseLong(bankProps.getProperty("Bank"));
        long totalWin = 0;
        long totalBet = logic.getTotalBet(lineBet);

        prob = Integer.parseInt(getZone().getProperty("GameProb" + gameID).toString());

        round++;
        seqRound++;

        log.info("[" + zoneName + "]" + "[" + roomName + "]" + "=========== " + grpID + "-" + round + " ===========");

        //算出可出的獎
        if(gameMode == 0)
        {
            do
            {
                resp = logic.getBaseResult(lineBet, rate, (double)prob - probRange, freeLight);
                totalWin = (long) resp.get("TotalWin");
                runCount++;

            }while((bank < totalWin && totalWin != 0) || (bank < 0 && totalWin != 0));

            log.info("[" + zoneName + "]" + "[" + roomName + "]" + "bet:" + lineBet + ", rate:" + rate + ", prob:" + prob + ", useProb:" + (int) resp.get("UseProb") + ", runCount:" + runCount);
        }

        ISFSObject respObj = new SFSObject();
        respObj.putClass("resp", resp);
        log.info("[" + zoneName + "]" + "[" + roomName + "]" + "Base resp:" + respObj.toJson());

        freeSpinWin = 0;

        grid = (int[]) resp.get("Grid");
        freeLight = (int[]) resp.get("FreeLight");

        freeSpinCount = (int) resp.get("FreeSpinCount");
        superSpinCount = (int) resp.get("SuperSpinCount");

        log.info("[" + zoneName + "]" + "[" + roomName + "]" + "baseWin:" + totalWin);


        //出免費遊戲
        int freeMode = (int) resp.get("FreeMode");
        if(freeMode != 0)
        {
            reSpinBet = lineBet;
            freeLight = new int[maxReel];

            if(freeMode == GameState.STATE_FREE_GAME.getId())
                gameState = GameState.STATE_FREE_GAME;
            else if(freeMode == GameState.STATE_SUPER_GAME.getId())
                gameState = GameState.STATE_SUPER_GAME;
        }

        //將局需求 存將初始贏分
        seqOpenWin = totalWin;

        //存Bank
        setBank(gameMode, totalBet, totalWin);
        saveBankProps(zoneName, roomName);

        return totalWin;
    }

    //算出免費遊戲結果
    public long getFreeResult(long lineBet){

        HashMap<String, Object> resp = null;
        int runCount = 0;

        double rate = Double.parseDouble(bankProps.getProperty("WinRate")) * 100;
        long bank = Long.parseLong(bankProps.getProperty("Bank"));

        long totalWin = 0;

        prob = Integer.parseInt(getZone().getProperty("GameProb" + gameID).toString());

        round++;
        seqRound++;

        //中免費遊戲
        if(freeSpinCount > 0)
        {
            log.info("[" + zoneName + "]" + "[" + roomName + "]" + "=========== " + grpID + "-" + round + " ===========");

            //算出可出的獎
            if(gameMode == 0)
            {
                do
                {
                    resp = logic.getFreeResult(lineBet, rate, (double)prob - probRange, freeLight);
                    totalWin = (long) resp.get("TotalWin");
                    runCount++;

                }while((bank < totalWin && totalWin != 0) || (bank < 0 && totalWin != 0));

            }

            ISFSObject respObj = new SFSObject();
            respObj.putClass("resp", resp);
            log.info("[" + zoneName + "]" + "[" + roomName + "]" + "bet:" + lineBet + ", rate:" + rate + ", prob:" + prob + ", runCount:" + runCount);
            log.info("[" + zoneName + "]" + "[" + roomName + "]" + "Free resp:" + respObj.toJson());


            grid = (int[]) resp.get("Grid");
            freeLight = (int[]) resp.get("FreeLight");

            superSpinCount = (int) resp.get("SuperSpinCount");

            freeToFree = (int) resp.get("FreeSpinCount");
            freeSpinCount += freeToFree;
            freeSpinCount--;
            freeSpinWin += totalWin;

            log.info("[" + zoneName + "]" + "[" + roomName + "]" + "freeWin:" + totalWin);
            log.info("[" + zoneName + "]" + "[" + roomName + "]" + "freeTotalWin:" + freeSpinWin);

            //存Bank
            setBank(gameMode, 0, totalWin);
            saveBankProps(zoneName, roomName);

            int freeMode = (int) resp.get("FreeMode");
            if(freeMode != 0)
            {
                if(freeMode == GameState.STATE_SUPER_GAME.getId())
                {
                    freeSpinCount = 0;
                    freeLight = new int[maxReel];
                    gameState = GameState.STATE_SUPER_GAME;
                }
            }
            //免費遊戲 結束
            else if(freeSpinCount <= 0)
                gameState = GameState.STATE_BASE_GAME;

            log.info("[" + zoneName + "]" + "[" + roomName + "]" + "freeSpinCount:" + freeSpinCount);
        }

        return totalWin;

    }

    //取特殊獎
    public long getSuperResult(long lineBet)
    {
        HashMap<String, Object> resp = null;
        int runCount = 0;

        double rate = Double.parseDouble(bankProps.getProperty("WinRate")) * 100;
        long bank = Long.parseLong(bankProps.getProperty("Bank"));

        long totalWin = 0;

        prob = Integer.parseInt(getZone().getProperty("GameProb" + gameID).toString());

        round++;
        seqRound++;

        //中免費遊戲
        if(superSpinCount > 0)
        {
            log.info("[" + zoneName + "]" + "[" + roomName + "]" + "=========== " + grpID + "-" + round + " ===========");

            //算出可出的獎
            if(gameMode == 0)
            {
                do
                {
                    resp = logic.getSuperResult(lineBet, rate, (double)prob - probRange);
                    totalWin = (long) resp.get("TotalWin");
                    runCount++;
                }while((bank < totalWin && totalWin != 0) || (bank < 0 && totalWin != 0));

            }

            ISFSObject respObj = new SFSObject();
            respObj.putClass("resp", resp);
            log.info("[" + zoneName + "]" + "[" + roomName + "]" + "bet:" + lineBet + ", rate:" + rate + ", prob:" + prob + ", runCount:" + runCount);
            log.info("[" + zoneName + "]" + "[" + roomName + "]" + "super resp:" + respObj.toJson());

            grid = (int[]) resp.get("Grid");

            superToSuper = (int) resp.get("SuperSpinCount");
            superSpinCount += superToSuper;
            superSpinCount--;
            freeSpinWin += totalWin;

            superTimes = (int) resp.get("Times");

            log.info("[" + zoneName + "]" + "[" + roomName + "]" + "superWin:" + totalWin);
            log.info("[" + zoneName + "]" + "[" + roomName + "]" + "freeTotalWin:" + freeSpinWin);

            //存Bank
            setBank(gameMode, 0, totalWin);
            saveBankProps(zoneName, roomName);

            //免費遊戲 結束
            if(superSpinCount <= 0)
                gameState = GameState.STATE_BASE_GAME;

            log.info("[" + zoneName + "]" + "[" + roomName + "]" + "superSpinCount:" + superSpinCount);
        }

        return totalWin;
    }

    //////////////////////////////////////////////////////
    //功能
    //////////////////////////////////////////////////////

    //初始化圖示
    public void initGrid()
    {
        grid = logic.initGrid((double)prob);
    }

    //結算成績
    public ISFSObject setGameBet(User player, int mode, long userPoint, long bet, int gameType)
    {
        int memberID = Integer.parseInt(player.getProperty("MemberID").toString());

        ISFSObject data = new SFSObject();
        data.putInt("memberID", memberID);
        data.putInt("gameID", gameID);
        data.putUtfString("grpID", grpID);
        data.putInt("round", round);
        data.putLong("totalBetPoint", bet);

        log.info( "[" + zoneName + "]" + "[" + roomName + "]" + player.getName() + ", " + data.toJson());

        //押注
        ISFSObject resp = betClass.setGameBet(zoneName, roomName, Integer.valueOf(player.getProperty("LoginState").toString()), memberID, "", gameID, round, "0", grpID, userPoint, bet, gameType);

        return resp;
    }

    //結算成績
    public ISFSObject setGameResult(User player, int mode, long bet, long win, long userPoint, String code, long newCurrentPoints, int[] grid, int gameType) throws JSONException
    {
        int memberID = Integer.parseInt(player.getProperty("MemberID").toString());

        ISFSObject data = new SFSObject();
        data.putInt("memberID", memberID);
        data.putInt("gameID", gameID);
        data.putUtfString("grpID", grpID);
        data.putInt("round", round);
        data.putUtfString("sequenceID", sequenceNumber);
        data.putLong("totalBetPoint", bet);
        data.putLong("win", win);
        data.putLong("userPoint", userPoint);
        data.putUtfString("code", code);
        data.putLong("newCurrentPoints", newCurrentPoints);

        log.info( "[" + zoneName + "]" + "[" + roomName + "]" + player.getName() + ", " + data.toJson());

        Date time = new Date();

        //json
        JSONObject json = new JSONObject();
        json.put("mode", String.valueOf(mode));
        json.put("bet", String.valueOf((double)bet / (double)100));
        json.put("lines", String.valueOf(maxLine));
        json.put("win", String.valueOf((double)win / (double)100));
        json.put("time", time);
        json.put("wheel", String.valueOf(maxGrid));
        json.put("reel", String.valueOf(maxReel));
        json.put("grid", grid);
//		json.put("winLinesDetail", gridWinPos);

        log.info("[" + zoneName + "]" + "[" + roomName + "]" + "setGameResult," + player.getName() + "," + bet + "," + win + "," + userPoint + "," + json.toString());

        //賽果 api版 額外送帶局數局號ㄝ, pp版 不變
        ISFSObject resp = new SFSObject();

        resp = recordClass.setGameResult(zoneName, roomName, Integer.valueOf(player.getProperty("LoginState").toString()), memberID, 0, gameID, getGameRoom().getId(), round, sequenceNumber, grpID, bet, 0, win, userPoint, json.toString(), player.getSession().getAddress(), sequenceNumber, gameType, Integer.parseInt(channelID));

        return resp;
    }

    //結束將局 pp版傳送將號  api版傳送將局資訊
    public ISFSObject setGameComplete(User player, long betPoints, long winPoints, int gameType)
    {
        ISFSObject resp = new SFSObject();
        resp.putInt("Ret", 99);

        int memberID = Integer.parseInt(player.getProperty("MemberID").toString());

        if(getZone().getProperty("DBMode").equals("PP"))
        {
            log.info("[" + zoneName + "]" + "[" + roomName + "]" + "setGameComplete," + sequenceNumber);

            resp.putInt("Ret", recordClass.GameComplete(zoneName, roomName, Integer.valueOf(player.getProperty("LoginState").toString()), memberID, sequenceNumber));
        }
        else if (getZone().getProperty("DBMode").equals("Api"))
        {
            log.info("[" + zoneName + "]" + "[" + roomName + "]" + "setGameComplete," + sequenceNumber);

            resp = recordClass.setGameResultApi(zoneName, roomName, memberID, Integer.valueOf(player.getProperty("LoginState").toString()), gameID, sequenceNumber, grpID + "-" + round, seqRound, betPoints, winPoints, Integer.parseInt(channelID), 0, gameType);

            //將局數歸零
            seqRound = 0;
        }

        return resp;
    }

    //取歷史紀錄
    public ISFSObject getGameHistory(User player, int day, int page, int quantity)
    {
        ISFSObject data = getRecord.getGameRecord(zoneName, roomName, Integer.valueOf(player.getProperty("LoginState").toString()), Integer.parseInt(player.getProperty("MemberID").toString()), gameID, day, page, quantity);
        log.info( "[" + zoneName + "]" + "[" + roomName + "]" + "getGameHistory," + data.toJson());
        return data;
    }

    //取歷史詳情
    public ISFSObject getHistoryDetail(User player, String gameNumber)
    {
        ISFSObject data = new SFSObject();

//		ISFSObject data = getRecordDetail.getGameRecordDetail(zoneName, roomName, Integer.valueOf(player.getProperty("LoginState").toString()), Integer.parseInt(player.getProperty("MemberID").toString()), gameNumber);
//		log.info( "[" + zoneName + "]" + "[" + roomName + "]" + "getHistoryDetail," + data.toJson());

        return data;
    }







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
