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
    private final PreparedStatement statement;
    private BatchStatement batchStatement;
    public DataFilter(){
        session = CassandraController.getInstance().getSession();
        statement = session.prepare("insert into sessions (clientid, starttime,endtime,totalhit,totalurl) values (?,?,?,?,?)");
        batchStatement = new BatchStatement(BatchStatement.Type.UNLOGGED);

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
        if(lastSession == null) {
            lastSession = new SiteSession(id,accessTime.getTime(),action);
            sessions.put(id,lastSession);
        }else {
            SiteSession siteSession = lastSession.update(accessTime.getTime(),action);
            if(siteSession != null){
                result.add(lastSession);
                sessions.remove(id);
                sessions.put(id,siteSession);
            }
        }
        if(result.size()>50){
            flush();
        }
    }

    public void flush(){
        for(SiteSession siteSession:result){
            batchStatement.add(statement.bind(siteSession.getId(), new Date(siteSession.getFirstHitMillis()), new Date(siteSession.getLastHitMillis()), siteSession.getHitCount(), siteSession.getHyperLogLog().cardinality()));
        }
        session.execute(batchStatement);
        result.clear();
        batchStatement = new BatchStatement(BatchStatement.Type.UNLOGGED);
    }

    public void finish(){
        for(Map.Entry<Integer,SiteSession> entry : sessions.entrySet()){
            result.add(entry.getValue());
        }
        flush();

    }

}
