package Room1.gameRoom1.sfs2x;

import com.smartfoxserver.v2.entities.User;
import com.smartfoxserver.v2.entities.data.ISFSObject;
import com.smartfoxserver.v2.entities.data.SFSObject;
import com.smartfoxserver.v2.extensions.BaseClientRequestHandler;

public class GetGameHistory extends BaseClientRequestHandler {
    @Override
    public void handleClientRequest(User player, ISFSObject params) {

        Entrance gameExt = (Entrance) getParentExtension();
        gameExt.log.info( "[" + gameExt.zoneName + "]" + "[" + gameExt.roomName + "]" + "GetGameHistory," + player.getName() + "," + params.toJson());

        int day = Integer.parseInt(params.getUtfString("Day"));
        int page = Integer.parseInt(params.getUtfString("Page"));
        int quantity = Integer.parseInt(params.getUtfString("Quantity"));

        ISFSObject respon1 = new SFSObject();
        ISFSObject history = new SFSObject();

        if(day <= 0 || day > 7 || page <= 0 || quantity <= 0 || quantity > 30)
        {
            history.putInt("State", 3);
            gameExt.send("GetGameHistoryResult", history, player);
            return;
        }

        history = gameExt.getGameHistory(player, day, page, quantity);

        //送回客端
        gameExt.send("GetGameHistoryResult", history, player);
        gameExt.log.info( "[" + gameExt.zoneName + "]" + "[" + gameExt.roomName + "]" + "GetGameHistoryResult," + respon1.toJson());
    }
}
