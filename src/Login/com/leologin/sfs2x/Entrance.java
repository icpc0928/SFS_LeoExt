package Login.com.leologin.sfs2x;

import com.log.sfs2x.ControlLog;
import com.log.sfs2x.LoginLog;
import com.log.sfs2x.ZoneLog;
import com.smartfoxserver.v2.core.SFSEventParam;
import com.smartfoxserver.v2.core.SFSEventType;
import com.smartfoxserver.v2.entities.Zone;
import com.smartfoxserver.v2.extensions.SFSExtension;

public class Entrance extends SFSExtension {

    String zoneName;
    ControlLog controlLog = new ControlLog("control");
    LoginLog loginLog = null;
    ZoneLog zoneLog = null;


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

        loginLog = new LoginLog(zoneName);
        zoneLog = new ZoneLog(zoneName);





    }

    @Override
    public void destroy() {
        //斷線
        super.destroy();

        trace("Zone Extension -- stopped");
    }

    public Zone getZone(){
        return this.getParentZone();
    }

    @Override
    public Object handleInternalMessage(String action, Object params) {

        Control api = new Control();
        api.setGameExt(zoneName, this);

        Object resp = null;

        try{
            resp = api.control(action, params);
        }catch (Exception e){
            controlLog.error("handleInternalMessage error:" + e);
        }

        return resp;

    }
}


