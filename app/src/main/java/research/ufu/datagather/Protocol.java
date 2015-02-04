package research.ufu.datagather;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by fynn on 30/01/15.
 */
public class Protocol {
    public static String buildAcceptedJoinJson(String rid, String uid)
    {
        JSONObject jobj = new JSONObject();
        try
        {
            jobj.put("id", rid);
            jobj.put("uid", uid);
        }
        catch(JSONException e)
        {
        }
        return jobj.toString();
    }

}
