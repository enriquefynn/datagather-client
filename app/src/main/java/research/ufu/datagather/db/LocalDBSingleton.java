package research.ufu.datagather.db;

import research.ufu.datagather.utils.Global;

public class LocalDBSingleton {
    private static LocalDB db;

    public static LocalDB getDB()
    {
        if(db == null)
        {
            db = new LocalDB(Global.loggerctx, null, null, 1);
        }
        return db;
    }
}