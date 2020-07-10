package Room1.gameRoom1.sfs2x;

import com.basedb.sfs2x.Wager;
import com.smartfoxserver.v2.entities.User;
import com.smartfoxserver.v2.entities.data.ISFSObject;
import com.smartfoxserver.v2.entities.data.SFSObject;
import com.smartfoxserver.v2.extensions.BaseClientRequestHandler;

import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Table extends BaseClientRequestHandler {
    @Override
    public void handleClientRequest(User player, ISFSObject params) {
        Entrance gameExt = (Entrance) getParentExtension();
        gameExt.log.info( "[" + gameExt.zoneName + "]" + "[" + gameExt.roomName + "]" + "Table," + player.getName() + "," + params.toJson());

        gameExt.userMap.put(player.getPlayerId() - 1, player); //進桌

        if(gameExt.getZone().getProperty("DBMode").equals("PP")){
            Wager wagerClass = new Wager();
            ISFSObject wagerQuery = wagerClass.wagerQuery(gameExt.zoneName, gameExt.roomName, Integer.valueOf(player.getProperty("LoginState").toString()),
                    Integer.parseInt(player.getProperty("MemberID").toString()), 0);
            gameExt.log.info( "[" + gameExt.zoneName + "]" + "[" + gameExt.roomName + "]" + "wagerQuery," + wagerQuery.toJson());

            //取點成功
            if(wagerQuery.getInt("State") == 0){
                gameExt.userPoint = wagerQuery.getLong("Point");
            }
            else{
                gameExt.log.error( "[" + gameExt.zoneName + "]" + "[" + gameExt.roomName + "]" + "wagerQeury error");
                getApi().kickUser(player, player, null, 0);
                return;
            }
        }
        else if(gameExt.getZone().getProperty("DBMode").equals("Api")){
            gameExt.userPoint = Long.parseLong(player.getProperty("Points").toString());
            gameExt.log.info( "[" + gameExt.zoneName + "]" + "[" + gameExt.roomName + "]" + "set userPoints : " + gameExt.userPoint);
        }

        ISFSObject respon1 = new SFSObject();
        ISFSObject respon2 = new SFSObject();

        String lineBet[] = new String[gameExt.betRange.length];
        String lineTotalBet[] = new String[gameExt.betRange.length];

        for(int i = 0; i < gameExt.betRange.length; i++){
            lineBet[i] = gameExt.reSetPointToClient(gameExt.betRange[i]);
            lineTotalBet[i] = gameExt.reSetPointToClient(gameExt.logic.getTotalBet(gameExt.betRange[i]));
        }

        respon1.putInt("BetTimes", gameExt.betTimes);
        respon1.putIntArray("PayTable", IntStream.of(gameExt.payTable).boxed().collect(Collectors.toList()));
        respon1.putUtfStringArray("LineBet", Arrays.asList(lineBet));
        respon1.putUtfStringArray("LineTotalBet", Arrays.asList(lineTotalBet));
        respon1.putIntArray("Grid", IntStream.of(gameExt.grid).boxed().collect(Collectors.toList()));
        respon1.putUtfString("UserPoint", gameExt.reSetPointToClient(gameExt.userPoint));
        respon1.putIntArray("LevelWinPoint", IntStream.of(gameExt.levelWinPoint).boxed().collect(Collectors.toList()));

        respon2.putInt("Round", gameExt.round);
        respon2.putUtfString("GroupID", gameExt.grpID + "-" + gameExt.round);

        if(player.getProperty("LoginState").equals("3")){
            respon2.putUtfString("Group", gameExt.sequenceNumber);
        }

        //送回客端
        gameExt.send("TableInfo", respon1, player);
        gameExt.log.info( "[" + gameExt.zoneName + "]" + "[" + gameExt.roomName + "]" + "TableInfo," + respon1.toJson());

        gameExt.send("GroupID", respon2, player);
    }
}
