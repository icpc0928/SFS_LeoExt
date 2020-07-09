package Room1.gameRoom1.sfs2x;

import com.smartfoxserver.v2.entities.User;
import com.smartfoxserver.v2.entities.data.ISFSObject;
import com.smartfoxserver.v2.entities.data.SFSObject;
import com.smartfoxserver.v2.extensions.BaseClientRequestHandler;

public class Bet extends BaseClientRequestHandler {
    @Override
    public void handleClientRequest(User player, ISFSObject params) {

        Entrance gameExt = (Entrance) getParentExtension();
        gameExt.log.info( "[" + gameExt.zoneName + "]" + "[" + gameExt.roomName + "]" + "Bet," + player.getName() + "," + params.toJson());

        int state = 0;
        ISFSObject respon1 = new SFSObject();

        //押注值不符
        if(Integer.parseInt(params.getUtfString("LineBet")) < 0 || Integer.parseInt(params.getUtfString("LineBet")) >= gameExt.betRange.length){
            gameExt.log.info( "[" + gameExt.zoneName + "]" + "[" + gameExt.roomName + "]" + "LineBet Error");
            return;
        }

        int lineBet = gameExt.betRange[Integer.parseInt(params.getUtfString("LineBet"))];
        long totalBet = gameExt.logic.getTotalBet(lineBet);
        long totalWin = 0;
        long userPointBefore = 0;

        //非主遊戲時間押注


    }
}
