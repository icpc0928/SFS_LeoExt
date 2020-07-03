package Lobby.com.leolobby.sfs2x;

import com.smartfoxserver.v2.core.ISFSEvent;
import com.smartfoxserver.v2.core.SFSEventParam;
import com.smartfoxserver.v2.entities.User;
import com.smartfoxserver.v2.entities.data.ISFSObject;
import com.smartfoxserver.v2.entities.data.SFSObject;
import com.smartfoxserver.v2.exceptions.SFSException;
import com.smartfoxserver.v2.extensions.BaseServerEventHandler;

public class UserJoinLobby  extends BaseServerEventHandler {

    @Override
    public void handleServerEvent(ISFSEvent event) throws SFSException {
        System.out.println("UserJoinLobby");



        Entrance gameExt = (Entrance) getParentExtension();
        User player = (User) event.getParameter(SFSEventParam.USER);

        System.out.println("player name is: " +player.getName());

        ISFSObject response = new SFSObject();
        response.putInt("n1",10);
        response.putInt("n2",3);



        gameExt.send("Test01",response, player);
    }
}
