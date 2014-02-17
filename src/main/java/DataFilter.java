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
    private final List<ResultSetFuture> resultSetFutures;
    private BatchStatement batchStatement;
    private long count;
    public DataFilter(){
        session = CassandraController.getInstance().getSession();
        statement = session.prepare("insert into sessions (clientid, starttime,endtime,totalhit,totalurl) values (?,?,?,?,?)");
        batchStatement = new BatchStatement(BatchStatement.Type.UNLOGGED);
        resultSetFutures = new ArrayList<ResultSetFuture>();
        count = 0;

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
           // checkExpire();
        }
    }

    private void checkExpire(){
        int i =0;
        if (sessions.size()>10000){
            for(Map.Entry<Integer,SiteSession> entry : sessions.entrySet()){
                if(entry.getValue().isExpired()){
                    i++;
                    result.add(entry.getValue());
                    sessions.remove(entry.getKey());
                }
            }
        }
        System.out.println("reduce:"+i);

        flush();
    }

    private void flush(){
        for(SiteSession siteSession:result){
            batchStatement.add(statement.bind(siteSession.getId(), new Date(siteSession.getFirstHitMillis()), new Date(siteSession.getLastHitMillis()), siteSession.getHitCount(), siteSession.getHyperLogLog().cardinality()));
            count++;
        }
        ResultSetFuture resultSetFuture = session.executeAsync(batchStatement);
        resultSetFutures.add(resultSetFuture);
        result.clear();
        batchStatement = new BatchStatement(BatchStatement.Type.UNLOGGED);
        if(resultSetFutures.size()>10){
            for(ResultSetFuture resultSetFuture1:resultSetFutures){
                resultSetFuture1.getUninterruptibly();
            }
            resultSetFutures.clear();
        }
    }

    /**
     * avoid the Batchstatement is larger than 65536
     */
    private void finishFlush(){
        for(SiteSession siteSession:result){
            ResultSetFuture resultSetFuture = session.executeAsync(statement.bind(siteSession.getId(), new Date(siteSession.getFirstHitMillis()), new Date(siteSession.getLastHitMillis()), siteSession.getHitCount(), siteSession.getHyperLogLog().cardinality()));
            count++;
            resultSetFutures.add(resultSetFuture);
            if(resultSetFutures.size()>10){
                for(ResultSetFuture resultSetFuture1:resultSetFutures){
                    resultSetFuture1.getUninterruptibly();
                }
                resultSetFutures.clear();
            }
        }

    }

    public void finish(){
        for(Map.Entry<Integer,SiteSession> entry : sessions.entrySet()){
            result.add(entry.getValue());
        }
        finishFlush();
        for(ResultSetFuture resultSetFuture1:resultSetFutures){
            resultSetFuture1.getUninterruptibly();
        }
        resultSetFutures.clear();

    }
    public long getCount(){
        return count;
    }

}
