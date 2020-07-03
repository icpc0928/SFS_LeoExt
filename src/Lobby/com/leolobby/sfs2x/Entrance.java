package Lobby.com.leolobby.sfs2x;

import com.smartfoxserver.v2.core.SFSEventType;
import com.smartfoxserver.v2.entities.Room;
import com.smartfoxserver.v2.entities.User;
import com.smartfoxserver.v2.entities.Zone;
import com.smartfoxserver.v2.entities.data.ISFSObject;
import com.smartfoxserver.v2.entities.data.SFSObject;
import com.smartfoxserver.v2.extensions.IClientRequestHandler;
import com.smartfoxserver.v2.extensions.SFSExtension;

import java.util.Properties;

public class Entrance extends SFSExtension {

    String zoneName;
    String roomName;
    String gameName;

    Properties gameConfig = new Properties();


    @Override
    public void init() {
        zoneName = getZone().getName();
        roomName = getGameRoom().getName();
        gameName = "Lobby";

        //進入Lobby事件
        addEventHandler(SFSEventType.USER_JOIN_ROOM, UserJoinLobby.class);
        //離開Lobby事件
        addEventHandler(SFSEventType.USER_LEAVE_ROOM, UserLeaveLobby.class);
        //離開Lobby事件(斷線)
        addEventHandler(SFSEventType.USER_DISCONNECT, UserLeaveLobby.class);

        //詢問各遊戲大廳資訊
        this.addRequestHandler("GameLobbyInfo", GameLobbyInfo.class);


        this.addRequestHandler("Test01",Test01.class);






    }


    public Zone getZone(){
        return this.getParentZone();

    }
    public Room getGameRoom(){
        return this.getParentRoom();
    }
}
