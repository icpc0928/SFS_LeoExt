package Room1.gameRoom1.sfs2x;

import Room1.gameRoom1Logic.sfs2x.GameState;
import com.smartfoxserver.v2.core.ISFSEvent;
import com.smartfoxserver.v2.core.SFSEventParam;
import com.smartfoxserver.v2.core.SFSEventType;
import com.smartfoxserver.v2.entities.Room;
import com.smartfoxserver.v2.entities.User;
import com.smartfoxserver.v2.exceptions.SFSException;
import com.smartfoxserver.v2.extensions.BaseServerEventHandler;

import java.util.Map;

public class UserLeaveRoom extends BaseServerEventHandler {

    @SuppressWarnings("unchecked")
    @Override
    public void handleServerEvent(ISFSEvent event) throws SFSException {

        Entrance gameExt = (Entrance) getParentExtension();
        Room gameRoom = gameExt.getGameRoom();
        User player = (User) event.getParameter(SFSEventParam.USER);
        Integer oldPlayerId;

        if(event.getType() == SFSEventType.USER_DISCONNECT){  //斷線
            Map<Room, Integer> playerIdsByRoom = (Map<Room, Integer>) event.getParameter(SFSEventParam.PLAYER_IDS_BY_ROOM);
            oldPlayerId = playerIdsByRoom.get(gameRoom);
        }
        else{   //離開房間
            oldPlayerId = (Integer) event.getParameter(SFSEventParam.PLAYER_ID);
        }

        //離開回傳主遊戲將號
        if(!gameExt.gameState.equals(GameState.STATE_BASE_GAME)){
            gameExt.log.info( "[" + gameExt.zoneName + "]" + "[" + gameExt.roomName + "]" + "====== UserLeaveRoom without finish ======");
        }

        gameExt.log.info( "[" + gameExt.zoneName + "]" + "[" + gameExt.roomName + "]" + "UserLeaveRoom," + player.getName());
        gameExt.log.info( "[" + gameExt.zoneName + "]" + "[" + gameExt.roomName + "]" + "UserList," + gameExt.getGameRoom().getUserList());
        gameExt.log.info( "[" + gameExt.zoneName + "]" + "[" + gameExt.roomName + "]" + "SpectatorsList," + gameExt.getGameRoom().getSpectatorsList());

        gameExt.getGameRoom().setProperty("isPlaying", "0");
        gameExt.log.info( "[" + gameExt.zoneName + "]" + "[" + gameExt.roomName + "]" + "isPlaying = " + gameExt.getGameRoom().getProperty("isPlaying"));

        if(oldPlayerId > 0 && oldPlayerId != null){
            gameExt.userMap.remove(oldPlayerId - 1);    //離桌
            gameExt.userPoint = 0;                          //玩家現有金額
            gameExt.sequenceNumber = "";
            gameExt.freeLight = new int[gameExt.maxReel];
        }

    }
}
