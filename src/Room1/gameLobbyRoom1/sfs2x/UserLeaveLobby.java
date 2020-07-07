package Room1.gameLobbyRoom1.sfs2x;

import com.smartfoxserver.v2.core.ISFSEvent;
import com.smartfoxserver.v2.core.SFSEventParam;
import com.smartfoxserver.v2.core.SFSEventType;
import com.smartfoxserver.v2.entities.Room;
import com.smartfoxserver.v2.entities.User;
import com.smartfoxserver.v2.exceptions.SFSException;
import com.smartfoxserver.v2.extensions.BaseServerEventHandler;

import java.util.Map;

public class UserLeaveLobby extends BaseServerEventHandler {
    @Override
    public void handleServerEvent(ISFSEvent event) throws SFSException {
        Entrance gameExt = (Entrance) getParentExtension();
        Room gameRoom = gameExt.getGameRoom();
        User player = (User) event.getParameter(SFSEventParam.USER);
        Integer oldPlayerId;

        if(event.getType() == SFSEventType.USER_DISCONNECT){
            Map<Room, Integer> playerIdsByRoom = (Map<Room, Integer>) event.getParameter(SFSEventParam.PLAYER_IDS_BY_ROOM);
            oldPlayerId = playerIdsByRoom.get(gameRoom);
        }else{
            oldPlayerId = (Integer) event.getParameter(SFSEventParam.PLAYER_ID);
        }

        if(oldPlayerId > 0 && oldPlayerId != null){
            System.out.println("UserLeaveGameLobby: " + player.getName());
        }

    }
}
