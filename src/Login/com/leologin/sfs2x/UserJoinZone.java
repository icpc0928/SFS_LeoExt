package Login.com.leologin.sfs2x;

import com.smartfoxserver.v2.core.ISFSEvent;
import com.smartfoxserver.v2.core.SFSEventParam;
import com.smartfoxserver.v2.entities.Room;
import com.smartfoxserver.v2.entities.User;
import com.smartfoxserver.v2.entities.data.ISFSObject;
import com.smartfoxserver.v2.entities.data.SFSObject;
import com.smartfoxserver.v2.exceptions.SFSException;
import com.smartfoxserver.v2.extensions.BaseServerEventHandler;

import java.util.List;

public class UserJoinZone extends BaseServerEventHandler {

    @Override
    public void handleServerEvent(ISFSEvent event) throws SFSException {

        System.out.println("UserJoinZone");

        //取得Entrance物件
        Entrance gameExt = (Entrance) getParentExtension();
        User player = (User) event.getParameter(SFSEventParam.USER);

        //創帳號重登
        if((Integer.parseInt(player.getSession().getProperty("LoginState").toString()) == 0)){
            getApi().logout(player);
            return;
        }else if ((Integer.parseInt(player.getSession().getProperty("LoginState").toString()) == 4)){
            System.out.println("Session.LoginState == 4");
        }


        //找出所有Room 其GroupID定義為Lobby的所有Room
        List<Room> roomList = gameExt.getZone().getRoomListFromGroup("Lobby");  //要在後台自定義GroupID
        Room roomInfo = roomList.get((int) (Math.random() * roomList.size()));      //因為目前只有一個Room的GroupID叫Lobby(就是Lobby本人)所以目前只會進Lobby
        String lobby = roomInfo.getName();

        //將Session的屬性寫到player的屬性
        player.setProperty("GameID",player.getSession().getProperty("APIUserGameID"));
        player.setProperty("LoginState", player.getSession().getProperty("LoginState"));


        //傳送回應給客端
        ISFSObject response = new SFSObject();
        response.putUtfString("LobbyName", lobby);
        response.putInt("RoomListSize", roomList.size());
        for(int i = 0; i < roomList.size(); i++){
            response.putUtfString("Room"+i, roomList.get(i).getName());
        }
        gameExt.send("LobbyInfo", response, player);    //回客端onExtResponse()




    }
}
