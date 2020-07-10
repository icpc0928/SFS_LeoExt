package Room1.gameRoom1.sfs2x;

import Room1.gameRoom1Logic.sfs2x.GameState;
import com.basedb.sfs2x.MemberLock;
import com.smartfoxserver.v2.core.ISFSEvent;
import com.smartfoxserver.v2.core.SFSEventParam;
import com.smartfoxserver.v2.entities.User;
import com.smartfoxserver.v2.exceptions.SFSException;
import com.smartfoxserver.v2.extensions.BaseServerEventHandler;

public class UserJoinRoom extends BaseServerEventHandler {
    @Override
    public void handleServerEvent(ISFSEvent event) throws SFSException {

        Entrance gameExt = (Entrance) getParentExtension();

        User player = (User) event.getParameter(SFSEventParam.USER);
        int playerId = player.getPlayerId();

        if(playerId > 0){

            gameExt.initGrid();
            gameExt.userPoint = 0;
            gameExt.userIdle = 0;
            gameExt.gameState = GameState.STATE_BASE_GAME;

            gameExt.round = 0;
            gameExt.grpID = gameExt.getGrpID();

            gameExt.log.info( "[" + gameExt.zoneName + "]" + "[" + gameExt.roomName + "]" + "**************************************" + player.getName() + "**************************************");
            gameExt.log.info( "[" + gameExt.zoneName + "]" + "[" + gameExt.roomName + "]" + "PlayerProperties," + player.getProperties());
            gameExt.log.info( "[" + gameExt.zoneName + "]" + "[" + gameExt.roomName + "]" + "RoomProperties," + gameExt.getGameRoom().getProperties());
            gameExt.log.info( "[" + gameExt.zoneName + "]" + "[" + gameExt.roomName + "]" + "UserJoinRoom," + player.getName());
            gameExt.log.info( "[" + gameExt.zoneName + "]" + "[" + gameExt.roomName + "]" + "UserList," + gameExt.getGameRoom().getUserList());
            gameExt.log.info( "[" + gameExt.zoneName + "]" + "[" + gameExt.roomName + "]" + "SpectatorsList," + gameExt.getGameRoom().getSpectatorsList());

            //取玩家登入channel
            if(player.getProperties().containsKey("APIUserChannelID") && player.getProperty("APIUserChannelID") != ""){
                gameExt.channelID = player.getProperty("APIUserChannelID").toString();

                //重讀bank
                gameExt.loadBankProps(gameExt.zoneName, gameExt.roomName);
            }

            gameExt.getGameRoom().setProperty("isPlaying", "1");
            gameExt.log.info( "[" + gameExt.zoneName + "]" + "[" + gameExt.roomName + "]" + "isPlaying = " + gameExt.getGameRoom().getProperty("isPlaying"));

            //更改玩家位置
            MemberLock memberLockClass = new MemberLock();
            memberLockClass.setUserLockState(player, gameExt.zoneName, gameExt.roomName, Integer.valueOf(player.getProperty("LoginState").toString()), Integer.valueOf(player.getProperty("MemberID").toString()), gameExt.gameID);
        }
    }
}
