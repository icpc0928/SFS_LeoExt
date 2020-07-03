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
//        ISession session = (ISession) event.getParameter(SFSEventParam.SESSION);                    //取得Session

        //測試客端登入所帶的參數




//        //回傳值這裡還不能回傳，因為還在Login階段 還沒結束確認Login
//        ISFSObject outData = (ISFSObject) event.getParameter(SFSEventParam.LOGIN_OUT_DATA);
//        outData.putUtfString("LoginState",loginInData.getUtfString("LoginState"));
//        outData.putUtfString("Welcome",account);
//        outData.putUtfString("Password",password);
//        outData.putInt("sum",sum);
//
//        gameExt.send("Login",outData,player);

    }
}
