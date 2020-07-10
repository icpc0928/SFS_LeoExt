package Room1.gameRoom1.sfs2x;

import Room1.gameRoom1Logic.sfs2x.GameState;
import com.smartfoxserver.v2.entities.User;
import com.smartfoxserver.v2.entities.data.ISFSObject;
import com.smartfoxserver.v2.entities.data.SFSObject;
import com.smartfoxserver.v2.extensions.BaseClientRequestHandler;

import java.util.stream.Collectors;
import java.util.stream.IntStream;

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
        if(!gameExt.gameState.equals(GameState.STATE_BASE_GAME)){
            state = 1;
            gameExt.log.info( "[" + gameExt.zoneName + "]" + "[" + gameExt.roomName + "]" + "no bet time");
            respon1.putInt("State", state);
            gameExt.send("BetState", respon1, player);
            return;
        }
        else if(totalBet > gameExt.userPoint){  //押注超過本金
            state = 2;
            gameExt.log.info("[" + gameExt.zoneName + "]" + "[" + gameExt.roomName + "]" + "totalBet > userPoint" + ",totalBet:" + totalBet + ",userPoint:" + gameExt.userPoint);
            respon1.putInt("State",state);
            gameExt.send("BetState", respon1, player);
            return;
        }

        // 押注成功
        totalWin = gameExt.getBaseResult(lineBet);

        gameExt.userIdle = 0;
        int levelWin = 0;

        ISFSObject respon2 = new SFSObject();

        //噴錢效果
        levelWin = gameExt.getLevelWin(totalBet, totalWin);

        //押注
        ISFSObject resp = gameExt.setGameBet(player, GameState.STATE_BASE_GAME.getId(), gameExt.userPoint, totalBet, 0);

        if(resp.getInt("Ret") == 0){
            gameExt.userPoint = resp.getLong("Point");
            userPointBefore = gameExt.userPoint;
            gameExt.sequenceNumber = resp.getUtfString("GameSeqNumber");
            gameExt.log.info( "[" + gameExt.zoneName + "]" + "[" + gameExt.roomName + "]" + "setGameBet success!! resp:" + resp.toJson());

            //賽果
            try{
                resp = gameExt.setGameResult(player, GameState.STATE_BASE_GAME.getId(), totalBet, totalWin,
                        gameExt.userPoint, "", 0, gameExt.grid, 0);
                if(resp.getInt("Ret") == 0){
                    gameExt.userPoint = resp.getLong("Points");
                    gameExt.log.info( "[" + gameExt.zoneName + "]" + "[" + gameExt.roomName + "]" + "setGameResult success!! resp:" + resp.toJson());

                    //回傳主遊戲將號 確認是否結束當局
                    if(gameExt.gameState.equals(GameState.STATE_BASE_GAME)){
                        ISFSObject comResp = new SFSObject();
                        comResp = gameExt.setGameComplete(player, totalBet, totalWin, GameState.STATE_BASE_GAME.getId());

                        int completeResp = comResp.getInt("Ret");

                        //API版 寫賽果 成功記點數 失敗踢掉
                        if(gameExt.getZone().getProperty("DBMode").equals("Api")){
                            if(completeResp == 0){
                                gameExt.userPoint = comResp.getLong("Points");
                                gameExt.log.info( "[" + gameExt.zoneName + "]" + "[" + gameExt.roomName + "]" + "setGameComplete success!! resp:" + resp.toJson());
                            }
                            else{
                                gameExt.log.error( "[" + gameExt.zoneName + "]" + "[" + gameExt.roomName + "]" + "setGameComplete error!! resp:" + resp.toJson());
                                getApi().kickUser(player, player, null, 0);
                                return;
                            }
                        }
                        else{
                            gameExt.log.info( "[" + gameExt.zoneName + "]" + "[" + gameExt.roomName + "]" + "setGameComplete resp:" + completeResp);
                        }
                    }
                }
                else{
                    gameExt.log.error( "[" + gameExt.zoneName + "]" + "[" + gameExt.roomName + "]" + "setGameResult error!! resp:" + resp.getInt("Ret"));
                    getApi().kickUser(player, player, null, 0);
                    return;
                }
            }catch(Exception e){
                gameExt.log.error("[" + gameExt.zoneName + "]" + "[" + gameExt.roomName + "]" + "setGameResult JSONException:" + e);
                return;
            }
        }
        else{
            gameExt.log.error( "[" + gameExt.zoneName + "]" + "[" + gameExt.roomName + "]" + "setGameBet error!! resp:" + resp.getInt("Ret"));
            getApi().kickUser(player, player, null, 0);
            return;
        }

        //大獎廣播 風控用
        if(levelWin > 2)
            gameExt.bigWinBroadcast(player, levelWin, totalBet, totalWin);

        //包裝封包
        respon1.putInt("State", state);
        respon1.putIntArray("Grid", IntStream.of(gameExt.grid).boxed().collect(Collectors.toList()));
        respon1.putUtfString("TotalWinPoint", gameExt.reSetPointToClient(totalWin));
        respon1.putUtfString("UserPointBefore", gameExt.reSetPointToClient(userPointBefore));
        respon1.putUtfString("UserPointAfter", gameExt.reSetPointToClient(gameExt.userPoint));
        respon1.putInt("GameState", gameExt.gameState.getId());
        respon1.putIntArray("FreeLight", IntStream.of(gameExt.freeLight).boxed().collect(Collectors.toList()));
        respon1.putInt("FreeSpinCount", gameExt.freeSpinCount);
        respon1.putInt("SuperSpinCount", gameExt.superSpinCount);
        respon1.putInt("BaseLevelWin", levelWin);

        respon2.putInt("Round", gameExt.round);
        respon2.putUtfString("GroupID", gameExt.grpID + "-" + gameExt.round);

        if(player.getProperty("LoginState").equals("3"))
            respon2.putUtfString("GroupID", "");

        //送回客端
        gameExt.send("BetResult", respon1, player);
        gameExt.log.info( "[" + gameExt.zoneName + "]" + "[" + gameExt.roomName + "]" + "BetResult," + respon1.toJson());

        gameExt.send("GroupID", respon2, player);
    }
}
