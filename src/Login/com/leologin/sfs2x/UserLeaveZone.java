package Login.com.leologin.sfs2x;

import com.smartfoxserver.v2.core.ISFSEvent;
import com.smartfoxserver.v2.core.SFSEventParam;
import com.smartfoxserver.v2.entities.User;
import com.smartfoxserver.v2.exceptions.SFSException;
import com.smartfoxserver.v2.extensions.BaseServerEventHandler;

public class UserLeaveZone extends BaseServerEventHandler {
    @Override
    public void handleServerEvent(ISFSEvent event) throws SFSException {
        Entrance gameExt = (Entrance) getParentExtension();

        User player = (User) event.getParameter(SFSEventParam.USER);


        System.out.println("UserLeaveZone886 " + player.getName());



    }
}
