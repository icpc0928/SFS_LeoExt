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
        //客端接到onJoinRoom:Lobby後傳送來的，目的是要進遊戲Room
        System.out.println("GameLobbyInfo()");
        Entrance gameExt = (Entrance) getParentExtension();

        String gameID = params.getUtfString("GameID");                                //客端onJoinRoom."Lobby" putUtfString 塞入 Room1~Room5 擇一
        String gameLobbyName = "";

        //取得遊戲大廳房間列表
        List<Room> roomList = gameExt.getZone().getRoomListFromGroup("GameLobby");              //目前定義的Room1~Room5 GroupID都是GameLobby
        for(int i = 0; i < roomList.size(); i++){
            //把原本GameLobbyXX 改成 gameXX
            if(roomList.get(i).getName().equals(gameID)){                               //roomList名稱: Room1~Room5
                gameLobbyName = roomList.get(i).getName();                              //這就是我們待會要進的目標房間
            }
        }



        ISFSObject resp = new SFSObject();
        resp.putUtfString("GameLobbyName", gameLobbyName);

        gameExt.send("GameLobbyInfoResult", resp, player);                      //回傳給客端
    }
}
