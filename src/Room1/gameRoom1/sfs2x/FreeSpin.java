package Room1.gameRoom1.sfs2x;

import Room1.gameRoom1Logic.sfs2x.GameState;
import com.smartfoxserver.v2.entities.User;
import com.smartfoxserver.v2.entities.data.ISFSObject;
import com.smartfoxserver.v2.entities.data.SFSObject;
import com.smartfoxserver.v2.extensions.BaseClientRequestHandler;

import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class FreeSpin extends BaseClientRequestHandler {
    @Override
    public void handleClientRequest(User player, ISFSObject params) {

        Entrance gameExt = (Entrance) getParentExtension();
        gameExt.log.info( "[" + gameExt.zoneName + "]" + "[" + gameExt.roomName + "]" + "FreeSpin," + player.getName() + "," + params.toJson());

        int state = 0;
        long totalWin = 0;

        //非免費時間押注
        if(!gameExt.gameState.equals(GameState.STATE_FREE_GAME)){
            state = 1;
            gameExt.log.info( "[" + gameExt.zoneName + "]" + "[" + gameExt.roomName + "]" + "NO STATE_FREE_GAME");
            return;
        }

        //押注成功
        totalWin = gameExt.getFreeResult(gameExt.reSpinBet);

        //閒置時間歸零
        gameExt.userIdle = 0;
        int baseLevelWin = 0;
        int freeLevelWin = 0;

        ISFSObject respon1 = new SFSObject();
        ISFSObject respon2 = new SFSObject();

        //噴錢效果
        baseLevelWin = gameExt.getLevelWin(gameExt.logic.getTotalBet(gameExt.reSpinBet), totalWin);

        if(gameExt.freeSpinCount == 0)
            freeLevelWin = gameExt.getLevelWin(gameExt.logic.getTotalBet(gameExt.reSpinBet), gameExt.freeSpinWin);

        //押注
        ISFSObject resp = gameExt.setGameBet(player, GameState.STATE_FREE_GAME.getId(), gameExt.userPoint, 0, 2);

        if(resp.getInt("Ret") == 0){
            gameExt.userPoint = resp.getLong("Points");
            gameExt.sequenceNumber = resp.getUtfString("GameSeqNumber");
            gameExt.log.info( "[" + gameExt.zoneName + "]" + "[" + gameExt.roomName + "]" + "setGameBet success!! resp:" + resp.toJson());

            //賽果
            try{
                resp = gameExt.setGameResult(player, GameState.STATE_FREE_GAME.getId(), 0,
                        totalWin, gameExt.userPoint, "", 0, gameExt.grid, 2);

                if(resp.getInt("Ret") == 0){
                    gameExt.userPoint = resp.getLong("Point");
                    gameExt.log.info( "[" + gameExt.zoneName + "]" + "[" + gameExt.roomName + "]" + "setGameResult success!! resp:" + resp.toJson());

                    //免費遊戲結束時 回傳主遊戲將號 確認是否結束當局
                    if(gameExt.gameState.equals(GameState.STATE_BASE_GAME)){
                        ISFSObject comResp = new SFSObject();
                        comResp = gameExt.setGameComplete(player, gameExt.logic.getTotalBet(gameExt.reSpinBet),
                                gameExt.seqOpenWin + gameExt.freeSpinWin, GameState.STATE_FREE_GAME.getId());

                        int completeResp = comResp.getInt("Ret");

                        //api版 寫賽果 成功記點數 失敗踢掉
                        if(gameExt.getZone().getProperty("DBMode").equals("Api")){
                            if(completeResp == 0){
                                gameExt.userPoint = comResp.getLong("Points");
                                gameExt.log.info( "[" + gameExt.zoneName + "]" + "[" + gameExt.roomName + "]" + "setGameComplete success!! resp : " + comResp.toJson());
                            }
                            else{
                                gameExt.log.error( "[" + gameExt.zoneName + "]" + "[" + gameExt.roomName + "]" + "setGameComplete error!! resp : " + comResp.toJson());
                                getApi().kickUser(player, player, null, 0);
                                return;
                            }
                        }
                        else
                            gameExt.log.info( "[" + gameExt.zoneName + "]" + "[" + gameExt.roomName + "]" + "setGameComplete resp : " + completeResp);
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

        //結束才顯示真實餘額
        long userPointDisplay = gameExt.userPoint - gameExt.freeSpinWin;

        if(gameExt.freeSpinCount <= 0)
            userPointDisplay = gameExt.userPoint;

        //大獎廣播 風控用
        if(baseLevelWin > 2)
            gameExt.bigWinBroadcast(player, baseLevelWin, gameExt.logic.getTotalBet(gameExt.reSpinBet), totalWin);

        //包裝封包
        respon1.putInt("State", state);
        respon1.putIntArray("Grid", IntStream.of(gameExt.grid).boxed().collect(Collectors.toList()));
        respon1.putUtfString("TotalWinPoint", gameExt.reSetPointToClient(totalWin));
        respon1.putUtfString("UserPointAfter", gameExt.reSetPointToClient(userPointDisplay));
        respon1.putInt("GameState", gameExt.gameState.getId());
        respon1.putInt("Count", gameExt.freeSpinCount - gameExt.freeToFree);
        respon1.putUtfString("FreeSpinWin", gameExt.reSetPointToClient(gameExt.freeSpinWin));
        respon1.putIntArray("FreeLight", IntStream.of(gameExt.freeLight).boxed().collect(Collectors.toList()));
        respon1.putInt("FreeToFree", gameExt.freeToFree);
        respon1.putInt("SuperSpinCount", gameExt.superSpinCount);
        respon1.putInt("BaseLevelWin", baseLevelWin);
        respon1.putInt("FreeLevelWin", freeLevelWin);

        respon2.putInt("Round", gameExt.round);
        respon2.putUtfString("GroupID", gameExt.grpID + "-" + gameExt.round);

        if(player.getProperty("LoginState").equals("3"))
            respon2.putUtfString("GroupID", "");

        //送回客端
        gameExt.send("FreeSpinResult", respon1, player);
        gameExt.log.info( "[" + gameExt.zoneName + "]" + "[" + gameExt.roomName + "]" + "FreeSpinResult," + respon1.toJson());

        gameExt.send("GroupID", respon2, player);

    }
}
