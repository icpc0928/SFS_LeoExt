package Login.com.leologin.sfs2x;

import com.smartfoxserver.v2.SmartFoxServer;
import com.smartfoxserver.v2.api.CreateRoomSettings;
import com.smartfoxserver.v2.entities.Room;
import com.smartfoxserver.v2.entities.User;
import com.smartfoxserver.v2.entities.data.ISFSObject;
import com.smartfoxserver.v2.entities.data.SFSObject;
import com.smartfoxserver.v2.entities.managers.BanMode;
import com.smartfoxserver.v2.exceptions.SFSCreateRoomException;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class Control {

    Entrance gameExt = null;
    Properties gameConfig = new Properties();
    String zoneName = "";

    List<String> affectedUser = new ArrayList<String>();

    Object updatePointResp = null;

    public void setGameExt(String zoneName, Entrance ext)
    {
        gameExt = (Entrance) ext;

        this.zoneName = zoneName;

        loadGameConfig(zoneName);
    }

    public String control(String action, Object params) throws JSONException
    {
        String[] key = getKeyList(action);
        JSONObject json = new JSONObject((String)params);

        if(action.equals("/updateUserPoints"))
            gameExt.controlLog.info("[" + zoneName + "]Action:" + action + ", Params:" + json);
        else
            gameExt.controlLog.warn("[" + zoneName + "]Action:" + action + ", Params:" + json);

        //指令有錯
        if(key == null)
            return respErrorMsg(101, action);

        //參數有誤
        for (int i = 0; i < key.length; i++)
        {
            if(json.isNull(key[i]))
                return respErrorMsg(100, action);

            int errorCode = judgeValue(key[i], json);
            if(errorCode != 0)
                return respErrorMsg(errorCode, action);
        }

        //執行指令
        return respErrorMsg(toDo(action, json), action);
    }

    public String[] getKeyList(String action)
    {
        String[] key = null;

        switch(action)
        {
            //開啟Zone
            case "/startZone":
                key = new String[] {"zone", "adminAccount", "reason"};
                break;

            //關閉Zone
            case "/stopZone":
                key = new String[] {"zone", "adminAccount", "reason"};
                break;

            //開啟遊戲
            case "/startGame":
                key = new String[] {"zone", "adminAccount", "reason", "gameID"};
                break;

            //關閉遊戲
            case "/stopGame":
                key = new String[] {"zone", "adminAccount", "reason", "gameID"};
                break;

            //更新遊戲
            case "/updateGame":
                key = new String[] {"zone", "adminAccount", "reason", "gameID"};
                break;

            //踢Zone的人
            case "/kickZoneUser":
                key = new String[] {"zone", "adminAccount", "reason"};
                break;

            //踢遊戲的人
            case "/kickGameUser":
                key = new String[] {"zone", "adminAccount", "reason", "gameID", "msg"};
                break;

            //踢渠道的人
            case "/kickChannelUser":
                key = new String[] {"zone", "adminAccount", "reason", "channelID"};
                break;

            //踢渠道遊戲的人
            case "/kickChannelGameUser":
                key = new String[] {"zone", "adminAccount", "reason", "channelID", "gameID"};
                break;

            //踢單人(帳號)
            case "/kickAccount":
                key = new String[] {"zone", "adminAccount", "reason", "account"};
                break;

            //踢單人(玩家唯一碼)
            case "/kickUser":
                key = new String[] {"zone", "adminAccount", "reason", "memberID"};
                break;

            //黑名單 單人(帳號)
            case "/banUserAccount":
                key = new String[] {"zone", "adminAccount", "reason", "account", "time"};
                break;

            //更新機率
            case "/updateGameProb":
                key = new String[] {"zone", "adminAccount", "reason", "gameID", "prob"};
                break;

            //Zone公告
            case "/broadcastZone":
                key = new String[] {"zone", "adminAccount", "reason", "message"};
                break;

            //遊戲公告
            case "/broadcastGame":
                key = new String[] {"zone", "adminAccount", "reason", "gameID", "message"};
                break;

            case "/updateUserPoints":
                key = new String[] {"zone", "adminAccount", "account", "memberID", "points"};
                break;

            default:
                break;
        }

        return key;
    }

    public int judgeValue(String value, JSONObject json) throws JSONException
    {
        switch(value)
        {
            case "gameID":
                if(!gameConfig.containsKey("GameProb" + json.getString("gameID")))
                    return 300;
                break;
            case "prob":
                int prob = (int) Float.parseFloat(json.getString("prob"));
                if(prob > 120)
                    return 301;
                if(prob < 80)
                    return 302;
                break;
            case "memberID":
                if(json.getString("memberID").equals(""))
                    return 303;
                break;
            case "account":
                if(json.getString("account").equals(""))
                    return 304;
                break;
            case "message":
                if(json.getString("message").equals(""))
                    return 305;
                break;
            case "channelID":
                if(json.getString("channelID").equals(""))
                    return 308;
                break;
        }

        return 0;
    }

    public int toDo(String action, JSONObject json) throws NumberFormatException, JSONException
    {
        int result = 0;
        List<Room> roomList = new ArrayList<Room>();
        List<User> userList = new ArrayList<User>();

        switch(action)
        {
            //重開遊戲伺服器
            case "/reStartServer":
                break;

            //開啟Zone
            case "/startZone":
                gameExt.getZone().setActive(true);
                break;

            //關閉Zone
            case "/stopZone":
                gameExt.getZone().setActive(false);
                userList = (List<User>) gameExt.getZone().getUserList();
                for(int i = 0; i < userList.size(); i++)
                    kickUser(userList.get(i));
                break;

            //開啟遊戲
            case "/startGame":
                gameExt.getZone().getRoomByName("GameLobby" + json.getString("gameID")).setActive(true);

                roomList = gameExt.getZone().getRoomListFromGroup("game" + json.getString("gameID"));
                for(int i = 0; i < roomList.size(); i++)
                    roomList.get(i).setActive(true);
                break;

            //關閉遊戲
            case "/stopGame":
                gameExt.getZone().getRoomByName("GameLobby" + json.getString("gameID")).setActive(false);

                roomList = gameExt.getZone().getRoomListFromGroup("game" + json.getString("gameID"));
                for(int i = 0; i < roomList.size(); i++)
                {
                    roomList.get(i).setActive(false);
                    userList = roomList.get(i).getUserList();

                    String reason = (String) json.get("reason");

                    for(int j = 0; j < userList.size(); j++)
                    {
                        if(!reason.equals(""))
                            sendMsg(userList.get(j), "KickMsg", reason);

                        kickUser(userList.get(j));
                    }
                }
                break;

            //更新遊戲
            case "/updateGame":
                roomList = gameExt.getZone().getRoomListFromGroup("game" + json.getString("gameID"));
                for(int i = 0; i < roomList.size(); i++)
                {
                    Room room = roomList.get(i);

                    CreateRoomSettings setting = new CreateRoomSettings();
                    setting.setName(room.getName());
                    setting.setPassword(room.getPassword());
                    setting.setMaxUsers(room.getMaxUsers());
                    setting.setExtension(new CreateRoomSettings.RoomExtensionSettings("game", room.getExtension().getExtensionFileName()));
                    setting.setMaxVariablesAllowed(room.getMaxRoomVariablesAllowed());
                    setting.setGame(room.isGame());
                    setting.setGroupId(room.getGroupId());
                    setting.setMaxSpectators(room.getMaxSpectators());

                    userList = room.getUserList();
                    for(int j = 0; j < userList.size(); j++)
                        kickUser(userList.get(j));

                    gameExt.getZone().removeRoom(room);

                    try {
                        gameExt.getZone().createRoom(setting);
                    } catch (SFSCreateRoomException e) {
                        gameExt.controlLog.error("[" + zoneName + "]" + "SFSCreateRoomException:" + e);
                    }
                }
                break;

            //踢Zone的人
            case "/kickZoneUser":
                userList = (List<User>) gameExt.getZone().getUserList();
                for(int i = 0; i < userList.size(); i++)
                    kickUser(userList.get(i));
                break;

            //踢遊戲的人
            case "/kickGameUser":
                roomList = gameExt.getZone().getRoomListFromGroup("game" + json.getString("gameID"));

                String msg = json.getString("msg");

                for(int i = 0; i < roomList.size(); i++)
                {
                    userList = roomList.get(i).getUserList();

                    for(int j = 0; j < userList.size(); j++)
                    {
                        if(!msg.equals(""))
                            sendMsg(userList.get(j), "KickMsg", msg);

                        kickUser(userList.get(j));
                    }
                }
                break;

            //踢該渠道玩家
            case "/kickChannelUser":
                userList = (List<User>) gameExt.getZone().getUserList();

                for(int i = 0; i < userList.size(); i++)
                {
                    if(String.valueOf(userList.get(i).getProperty("APIUserChannelID")).equals(json.getString("channelID")))
                    {
                        kickUser(userList.get(i));
                        affectedUser.add(userList.get(i).getName());
                    }
                }

                break;

            //踢渠道遊戲玩家
            case "/kickChannelGameUser":
                roomList = gameExt.getZone().getRoomListFromGroup("game" + json.getString("gameID"));

                for(int i = 0; i < roomList.size(); i++)
                {
                    userList = roomList.get(i).getUserList();

                    for(int j = 0; j < userList.size(); j++)
                    {
                        if(String.valueOf(userList.get(i).getProperty("APIUserChannelID")).equals(json.getString("channelID")))
                            kickUser(userList.get(i));
                    }
                }

                break;

            //踢單人
            case "/kickAccount":
                userList = (List<User>) gameExt.getZone().getUserList();

                boolean isConnAccount = false;
                for(int i = 0; i < userList.size(); i++)
                {
                    if(String.valueOf(userList.get(i).getName()).equals(json.getString("account")))
                    {
                        isConnAccount = true;
                        kickUser(gameExt.getZone().getUserByName(json.getString("account")));
                    }
                }

                if(isConnAccount == false)
                    result = 307;

                break;

            //踢單人
            case "/kickUser":
                userList = (List<User>) gameExt.getZone().getUserList();

                boolean isConnUser = false;
                for(int i = 0; i < userList.size(); i++)
                {
                    if(String.valueOf(userList.get(i).getProperty("MemberID")).equals(json.getString("memberID")))
                    {
                        isConnUser = true;
                        kickUser(userList.get(i));
                    }
                }

                if(isConnUser == false)
                    result = 307;

                break;

            //黑名單
            case "/banUserAccount":

                userList = (List<User>) gameExt.getZone().getUserList();

                boolean isBanUser = false;
                for(int i = 0; i < userList.size(); i++)
                {
                    if(String.valueOf(userList.get(i).getName()).equals(json.getString("account")))
                    {
                        isBanUser = true;

                        int time = json.getString("time").equals("") ? 10:Integer.parseInt(json.getString("time"));

                        banUser(userList.get(i), "", time);
                    }
                }

                if(isBanUser == false)
                    result = 307;

                break;

            //更新機率
            case "/updateGameProb":
                int prob = (int) Float.parseFloat(json.getString("prob"));

                gameExt.controlLog.info("[" + zoneName + "]" + "updateGameProb:" + "GameProb" + json.getString("gameID") + " " + gameExt.getZone().getProperty("GameProb" + json.getString("gameID")) + " => " + prob);

                gameExt.getZone().setProperty("GameProb" + json.getString("gameID"), prob);
                gameConfig.setProperty("GameProb" + json.getString("gameID"), String.valueOf(prob));

                saveGameConfig(json.getString("zone"));
                break;

            //zone廣播
            case "/broadcastZone":
                gameExt.controlLog.info("[" + zoneName + "]" + "broadcastZone:" + json.getString("message"));

                if(gameExt.getZone().getSessionList().size() > 0)
                    SmartFoxServer.getInstance().getAPIManager().getSFSApi().sendModeratorMessage(null, json.getString("message"), null, gameExt.getZone().getSessionList());
                else
                    result = 306;
                break;

            //遊戲廣播
            case "/broadcastGame":
                gameExt.controlLog.info("[" + zoneName + "]" + "broadcastGame:" + json.getString("message"));

                if(gameExt.getZone().getSessionsInGroup("game" + json.getString("gameID")).size() > 0)
                    SmartFoxServer.getInstance().getAPIManager().getSFSApi().sendModeratorMessage(null, json.getString("message"), null, gameExt.getZone().getSessionsInGroup("game" + json.getString("gameID")));
                else
                    result = 306;
                break;

            //API補點
            case "/updateUserPoints":
                userList = (List<User>) gameExt.getZone().getUserList();

                boolean hasUser = false;
                for(int i = 0; i < userList.size(); i++)
                {
                    if(String.valueOf(userList.get(i).getProperty("MemberID")).equals(json.getString("memberID")) && String.valueOf(userList.get(i).getName()).equals(json.getString("account")))
                    {
                        hasUser = true;

                        User player = userList.get(i);
                        Room room = player.getLastJoinedRoom();

                        ISFSObject extData = new SFSObject();
                        extData.putUtfString("Points", json.getString("points"));

                        //接收房間回傳
                        updatePointResp = room.getExtension().handleInternalMessage("resetPoint", extData);

                        //封包
                        ISFSObject data = new SFSObject();
                        data.putUtfString("Points", json.getString("points"));

                        gameExt.controlLog.info("[" + zoneName + "]" + "Player : " + player +  ", updateUserPoints : " + data.toJson() + ", Resp" + updatePointResp);

                        //送封包
                        gameExt.send("UpdatePoints", data, player);
                    }
                }

                if(hasUser == false)
                    result = 309;
                break;
        }

        return result;
    }

    public void kickUser(User player)
    {
        gameExt.getApi().kickUser(player, player, null, 0);
        gameExt.controlLog.info("[" + zoneName + "]" + "kickUser:" + player + ", MemberID:" + player.getProperty("MemberID"));
    }

    public void sendMsg(User player, String cmd, String msg)
    {
        ISFSObject params = new SFSObject();
        params.putUtfString("Msg", msg);

        gameExt.send(cmd, params, player);
    }

    public void banUser(User player, String msg, int time)
    {
        gameExt.getApi().banUser(player, null, msg, BanMode.BY_NAME, time, 0);
        gameExt.controlLog.info("[" + zoneName + "]" + "banUser:" + player + ", account:" + player.getName() + ", memberID:" + player.getProperty("MemberID"));
    }

    public String respErrorMsg(int errorCode, String action)
    {
        ISFSObject resp = new SFSObject();
        String errorMsg = "";

        switch(errorCode)
        {
            case 0:
                errorMsg = "執行成功";
                break;
            case 100:
                errorMsg = "參數名稱有誤";
                break;
            case 300:
                errorMsg = "遊戲編號有誤";
                break;
            case 301:
                errorMsg = "機率高於120";
                break;
            case 302:
                errorMsg = "機率低於80";
                break;
            case 303:
                errorMsg = "memberID為空值";
                break;
            case 304:
                errorMsg = "account為空值";
                break;
            case 305:
                errorMsg = "message為空值";
                break;
            case 306:
                errorMsg = "廣播對象不存在";
                break;
            case 307:
                errorMsg = "踢人對象不存在";
                break;
            case 308:
                errorMsg = "channelID為空值";
                break;
            case 309:
                errorMsg = "補點對象不存在";
                break;
            case 801:
                errorMsg = "更新機率失敗 寫入DB紀錄錯誤";
                break;
            case 999:
            default:
                errorCode = 999;
                errorMsg = "系統錯誤";
                break;
        }

        resp.putInt("errorCode", errorCode);
        resp.putUtfString("errorMsg", errorMsg);

        if(action.equals("/kickChannelUser"))
        {
            resp.putUtfString("affectedUsers", affectedUser.toString());

            affectedUser.clear();
        }

        if(action.equals("/updateUserPoints"))
            gameExt.controlLog.info("[" + zoneName + "]" + action + " resp:" + resp.toJson());
        else
            gameExt.controlLog.warn("[" + zoneName + "]" + action + " resp:" + resp.toJson());


        return resp.toJson();
    }

    public void loadGameConfig(String zoneName)
    {
        FileInputStream fis = null;

        try
        {
            fis = new FileInputStream("extensions/config/" + zoneName + "Config.properties");
            gameConfig.load(fis);
            gameExt.controlLog.info("[" + zoneName + "]" + "loadGameConfig:" + gameConfig);
        }
        catch (IOException e)
        {
            gameExt.controlLog.error("[" + zoneName + "]" + "loadGameConfig is error:" + e);
        }
        finally
        {
            if (fis != null)
            {
                try
                {
                    fis.close();
                }
                catch (Exception e1)
                {
                    gameExt.controlLog.error("[" + zoneName + "]" + "loadGameConfig close is error:" + e1);
                }
            }
        }
    }

    public void saveGameConfig(String zoneName)
    {
        FileOutputStream fos = null;

        try
        {
            fos = new FileOutputStream("extensions/config/" + zoneName + "Config.properties");
            gameConfig.store(fos, zoneName);

            gameExt.controlLog.info("[" + zoneName + "]" + "saveGameConfig:" + gameConfig);
        }
        catch (IOException e)
        {
            gameExt.controlLog.error("[" + zoneName + "]" + "saveGameConfig is error:" + e);
        }
        finally
        {
            if (fos != null)
            {
                try
                {
                    fos.close();
                }

                catch (Exception e1)
                {
                    gameExt.controlLog.error("[" + zoneName + "]" + "saveGameConfig is error:" + e1);
                }
            }
        }
    }
}
