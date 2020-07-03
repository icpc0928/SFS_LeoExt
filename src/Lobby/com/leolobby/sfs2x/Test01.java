package Lobby.com.leolobby.sfs2x;

import com.smartfoxserver.v2.entities.User;
import com.smartfoxserver.v2.entities.data.ISFSObject;
import com.smartfoxserver.v2.entities.data.SFSObject;
import com.smartfoxserver.v2.extensions.BaseClientRequestHandler;

public class Test01 extends BaseClientRequestHandler {
    @Override
    public void handleClientRequest(User player, ISFSObject params) {
        Entrance gameExt = (Entrance) getParentExtension();

        System.out.println("Test01");



        //回傳客端
        ISFSObject response = new SFSObject();
        response.putInt("sum", 13);
        gameExt.send("Test02", response, player);

    }
}
