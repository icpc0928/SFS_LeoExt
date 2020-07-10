package Login.com.leologin.sfs2x;

import com.smartfoxserver.v2.entities.data.ISFSObject;
import com.smartfoxserver.v2.entities.data.SFSObject;
import org.json.JSONException;

public class UpdatePoint {
    Entrance gameExt = null;
    String zoneName = "";

    public void setGameExt(String zoneName, Entrance ext)
    {
        gameExt = (Entrance) ext;

        this.zoneName = zoneName;
    }

    public String update(String action, Object params) throws JSONException
    {
        return toWork();
    }

    private String toWork()
    {
        ISFSObject resp = new SFSObject();



        return resp.toJson();
    }
}
