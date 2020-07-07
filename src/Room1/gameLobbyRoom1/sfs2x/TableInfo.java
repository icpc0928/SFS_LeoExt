package Room1.gameLobbyRoom1.sfs2x;

import com.smartfoxserver.v2.api.CreateRoomSettings;
import com.smartfoxserver.v2.entities.Room;
import com.smartfoxserver.v2.entities.User;
import com.smartfoxserver.v2.entities.data.ISFSObject;
import com.smartfoxserver.v2.entities.data.SFSObject;
import com.smartfoxserver.v2.exceptions.SFSCreateRoomException;
import com.smartfoxserver.v2.exceptions.SFSTooManyRoomsException;
import com.smartfoxserver.v2.extensions.BaseClientRequestHandler;

public class TableInfo extends BaseClientRequestHandler {
    @Override
    public void handleClientRequest(User player, ISFSObject params) {
        System.out.println("TableInfo");

        Entrance gameExt = (Entrance) getParentExtension();

        //創房
        Room slotRoom = null;
        try
        {
            slotRoom = gameExt.getZone().getRoomByName(getRoomName(gameExt.getGameRoom().getName().replace("GameLobby","game")));
        }
        catch(SFSCreateRoomException | SFSTooManyRoomsException e)
        {
            System.out.println("SFSCreateRoomException:" + e.toString());
        }

        ISFSObject result = new SFSObject();

        result.putUtfString("TableName", slotRoom.getName());

        send("SlotTableInfoResult", result, player);
    }

    public String getRoomName(String gameID) throws  SFSCreateRoomException, SFSTooManyRoomsException{

        String roomName = "";

        Entrance gameExt = (Entrance) getParentExtension();

        CreateRoomSettings setting = new CreateRoomSettings();

        for(int i = 1; i <= gameExt.roomCount; i++){
            roomName = gameExt.gameName + String.format("%03d", i);     //後面補三位數字

            //沒房創房
            if(gameExt.getZone().getRoomByName(roomName) == null){
                CreateRoomSettings.RoomExtensionSettings ExtensionSetting =
                        new CreateRoomSettings.RoomExtensionSettings("game", "gameRoom1.sfs2x.Entrance");
                setting.setName(roomName);
                setting.setPassword(null);
                setting.setMaxUsers(1);
                setting.setPassword("");
                setting.setExtension(ExtensionSetting);
                setting.setMaxVariablesAllowed(10);
                setting.setGame(true);
                setting.setGroupId(gameID);
                setting.setMaxSpectators(0);
                setting.setDynamic(false);
                setting.setRoomProperties(gameExt.getConfigProperties());
                gameExt.getZone().createRoom(setting);

                System.out.println("CreateRoom: " + "GameID:" + gameID +",RoomName:" + roomName);
                break;

            //有房沒人
            }else if(gameExt.getZone().getRoomByName(roomName).getUserList().size() == 0 && gameExt.getZone().getRoomByName(roomName).getProperty("isPlaying").equals("0")){
                System.out.println("有房沒人");
                break;
            }
        }

        return roomName;
    }
}
