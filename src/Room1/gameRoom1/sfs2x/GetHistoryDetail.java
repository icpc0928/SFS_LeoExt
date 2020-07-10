package Room1.gameRoom1.sfs2x;

import com.smartfoxserver.v2.entities.User;
import com.smartfoxserver.v2.entities.data.ISFSObject;
import com.smartfoxserver.v2.entities.data.SFSObject;
import com.smartfoxserver.v2.extensions.BaseClientRequestHandler;

public class GetHistoryDetail extends BaseClientRequestHandler {
    @Override
    public void handleClientRequest(User player, ISFSObject params) {
        Entrance gameExt = (Entrance) getParentExtension();
        gameExt.log.info( "[" + gameExt.zoneName + "]" + "[" + gameExt.roomName + "]" + "GetHistoryDetail," + player.getName() + "," + params.toJson());

        String gameNumber = params.getUtfString("GameNumber");

        ISFSObject detail = gameExt.getHistoryDetail(player, gameNumber);

        //送回客端
        gameExt.send("GetHistoryDetailResult", detail, player);
        gameExt.log.info( "[" + gameExt.zoneName + "]" + "[" + gameExt.roomName + "]" + "GetHistoryDetailResult," + detail.toJson());
    }
}
