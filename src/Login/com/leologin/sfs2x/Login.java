package Login.com.leologin.sfs2x;

import com.smartfoxserver.bitswarm.sessions.ISession;
import com.smartfoxserver.v2.core.ISFSEvent;
import com.smartfoxserver.v2.core.SFSEventParam;
import com.smartfoxserver.v2.entities.User;
import com.smartfoxserver.v2.entities.data.ISFSObject;
import com.smartfoxserver.v2.exceptions.SFSException;
import com.smartfoxserver.v2.extensions.BaseServerEventHandler;

public class Login extends BaseServerEventHandler {

    @Override
    public void handleServerEvent(ISFSEvent event) throws SFSException {

        System.out.println("Login now");



        //取得Entrance物件
        Entrance gameExt = (Entrance) getParentExtension();
        User player = (User) event.getParameter(SFSEventParam.USER);


        //取得登入資訊
        String account = ((String) event.getParameter(SFSEventParam.LOGIN_NAME)).toLowerCase();     //取得帳號轉成小寫
        String password = (String) event.getParameter(SFSEventParam.LOGIN_PASSWORD);                //取得密碼
        ISession session = (ISession) event.getParameter(SFSEventParam.SESSION);                    //取得Session
        ISFSObject loginInData = (ISFSObject) event.getParameter(SFSEventParam.LOGIN_IN_DATA);


        if(loginInData.containsKey("LoginState")){
            //("4")
            if(loginInData.getUtfString("LoginState").equals("4")){
                System.out.println("loginInData Good!");
            }else{
                getApi().disconnect(session);
            }
        }else{
            System.out.println("LoginState not found");
            getApi().disconnect(session);
        }

        //回傳LOGIN_OUT_DATA
        ISFSObject outData = (ISFSObject) event.getParameter(SFSEventParam.LOGIN_OUT_DATA);
        outData.putUtfString("YourAccount", account);
        outData.putUtfString("YourPassword", password);
        outData.putUtfString("LoginState", loginInData.getUtfString("LoginState"));









    }
}
