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

        System.out.println("UserJoinZone test01");

        //取得Entrance物件
        Entrance gameExt = (Entrance) getParentExtension();
        User player = (User) event.getParameter(SFSEventParam.USER);

        System.out.println("Debug1");


        int sum = 13;




        List<Room> roomList = gameExt.getZone().getRoomList();


        //回傳Lobby資訊
        ISFSObject response = new SFSObject();

        response.putInt("RoomListSize", roomList.size());
        System.out.println("Debug3");
        for(int i = 0; i < roomList.size(); i++){
            response.putUtfString("Room"+i, roomList.get(i).getName());
        }
        response.putInt("Sum",sum);

        System.out.println("Debug4");
        gameExt.send("LobbyInfo", response, player);




    }
}
