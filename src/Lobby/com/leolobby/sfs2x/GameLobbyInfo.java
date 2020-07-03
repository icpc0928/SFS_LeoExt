package Lobby.com.leolobby.sfs2x;

import com.smartfoxserver.v2.entities.Room;
import com.smartfoxserver.v2.entities.User;
import com.smartfoxserver.v2.entities.data.ISFSObject;
import com.smartfoxserver.v2.entities.data.SFSObject;
import com.smartfoxserver.v2.extensions.BaseClientRequestHandler;

import java.util.List;

public class GameLobbyInfo extends BaseClientRequestHandler {
    @Override
    public void handleClientRequest(User player, ISFSObject params) {

        Entrance gameExt = (Entrance) getParentExtension();

        String gameID = params.getUtfString("GameID");

        String gameLobbyName = "";

        //取得遊戲大廳房間列表
        List<Room> roomList = gameExt.getZone().getRoomListFromGroup("GameLobby");

//        for(int i = 0; i < roomList.size(); i++){
//            //把原本GameLobbyXX 改成 gameXX
//            if(roomList.get(i).getName().equals(gameID.replace("game","GameLobby"))){
//                gameLobbyName = roomList.get(i).getName();
//            }
//        }

        gameLobbyName = params.getUtfString("GameID");

        ISFSObject resp = new SFSObject();
        resp.putUtfString("GameLobbyName", gameLobbyName);

        gameExt.send("GameLobbyInfoResult", resp, player);
    }
}
