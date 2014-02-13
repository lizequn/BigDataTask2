import com.datastax.driver.core.*;

import java.util.*;

/**
 * @Auther: Li Zequn
 * Date: 04/02/14
 */
public class DataFilter {
    private final Session session;
    private final HashMap<Integer,SiteSession> sessions;
    private final List<SiteSession> result;
    public DataFilter(){
        session = CassandraController.getInstance().getSession();

        result = new ArrayList<SiteSession>();

        sessions = new LinkedHashMap<Integer,SiteSession>() {
            protected boolean removeEldestEntry(Map.Entry eldest) {
                SiteSession siteSession = (SiteSession)eldest.getValue();
                boolean shouldExpire = siteSession.isExpired();
                if(shouldExpire) {
                    result.add(siteSession);
                }
                return siteSession.isExpired();
            }
        };
    }
    public void processSession(int id,Date accessTime,String action,int status,int size){

        SiteSession lastSession = sessions.get(id);
        if(lastSession == null){
            lastSession = new SiteSession(id,accessTime.getTime(),action);
            sessions.put(id,lastSession);
        }else {
            SiteSession siteSession = lastSession.update(accessTime.getTime(),action);
            if(siteSession != null){
                result.add(lastSession);
                sessions.put(id,siteSession);
            }
        }


    }
    public void flush(){

        this.shutDown();
    }

    private void shutDown(){
        session.shutdown();
    }

}
