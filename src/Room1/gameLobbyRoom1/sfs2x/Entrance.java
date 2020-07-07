package Room1.gameLobbyRoom1.sfs2x;

import com.smartfoxserver.v2.core.SFSEventType;
import com.smartfoxserver.v2.entities.Room;
import com.smartfoxserver.v2.entities.Zone;
import com.smartfoxserver.v2.extensions.SFSExtension;

public class Entrance extends SFSExtension {

    int gameID;
    String zoneName;
    String roomName;
    String gameName;

    int roomCount = 0;

    @Override
    public void init() {
        System.out.println("Room1.init()");
        zoneName = getZone().getName();
        roomName = getGameRoom().getName();

        gameID = Integer.parseInt(getConfigProperties().getProperty("GameID"));
        gameName = getConfigProperties().getProperty("GameName");

        roomCount = Integer.parseInt(getConfigProperties().getProperty("RoomCount"));

        getZone().setProperty("GameName" + gameID, gameName);
        getGameRoom().setProperty("Config", getConfigProperties());



        System.out.println("zoneName: " + zoneName + " roomName: " + roomName + " gameName: " + gameName);
        System.out.println("gameID: " + gameID + " roomCount: " + roomCount);



        //監聽進入大廳事件
        addEventHandler(SFSEventType.USER_JOIN_ROOM, UserJoinLobby.class);
        //監聽離開大廳事件
        addEventHandler(SFSEventType.USER_LEAVE_ROOM, UserLeaveLobby.class);
        //監聽玩家斷線事件
        addEventHandler(SFSEventType.USER_DISCONNECT, UserLeaveLobby.class);

        //大廳桌資訊
        this.addRequestHandler("SlotTableInfo", TableInfo.class);



    }


    public Zone getZone()
    {
        return this.getParentZone();
    }
    public Room getGameRoom()
    {
        return this.getParentRoom();
    }


}



