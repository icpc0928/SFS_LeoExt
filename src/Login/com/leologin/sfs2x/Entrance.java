package Login.com.leologin.sfs2x;

import com.smartfoxserver.v2.core.SFSEventParam;
import com.smartfoxserver.v2.core.SFSEventType;
import com.smartfoxserver.v2.entities.Zone;
import com.smartfoxserver.v2.extensions.SFSExtension;

public class Entrance extends SFSExtension {

    String zoneName;


    @Override
    public void init() {

        System.out.println("Entrance");

        //取得zoneName 在SmartFoxServer內設定Zone Extension 的Main Class 才會認得這支類別
        zoneName = this.getParentZone().getName();

        //登入事件
        addEventHandler(SFSEventType.USER_LOGIN, Login.class);
        //進入Zone事件
        addEventHandler(SFSEventType.USER_JOIN_ZONE, UserJoinZone.class);
        //離開Zone事件 (斷線)
        addEventHandler(SFSEventType.USER_DISCONNECT, UserLeaveZone.class);





    }

    public Zone getZone(){
        return this.getParentZone();
    }
}
