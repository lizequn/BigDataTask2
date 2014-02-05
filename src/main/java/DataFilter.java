import com.datastax.driver.core.*;

import java.util.*;

/**
 * @Auther: Li Zequn
 * Date: 04/02/14
 */
public class DataFilter {
    private final Session session;
    public DataFilter(){
        session = CassandraController.getInstance().getSession();
    }
    public List<SiteSession> getSessionById(long id){
        final List<SiteSession> result = new ArrayList<SiteSession>();
        SiteSession lastSession = null;

        HashMap<Long,SiteSession> sessions = new LinkedHashMap<Long,SiteSession>() {
            protected boolean removeEldestEntry(Map.Entry eldest) {
                SiteSession siteSession = (SiteSession)eldest.getValue();
                boolean shouldExpire = siteSession.isExpired();
                if(shouldExpire) {
                  result.add(siteSession);
                }
                return siteSession.isExpired();
            }
        };

        PreparedStatement statement = session.prepare("select clientid,accesstime,action from log where clientid = ?");
        ResultSet resultSet = session.execute(new BoundStatement(statement).bind(id).setFetchSize(1000));
        long counter =0;
        Iterator<Row> iterator = resultSet.iterator();
        while(iterator.hasNext()){
            if(resultSet.getAvailableWithoutFetching() == 1000 && !resultSet.isFullyFetched()){
                resultSet.fetchMoreResults();
                System.out.println("do fetch");
            }
            Row row = iterator.next();
            counter++;
            System.out.println(row);
            if(sessions.size() == 0){
                lastSession = new SiteSession(id+"",row.getDate(1).getTime(),row.getString(2));
                sessions.put(counter,lastSession);

            }else {
                SiteSession siteSession = lastSession.update(row.getDate(1).getTime(),row.getString(2));
                if(siteSession != null){
                    sessions.put(counter,siteSession);
                    lastSession = siteSession;
                }
            }
        }
        if(sessions.size()>0){
            assert (sessions.size() == 1);
            for(Map.Entry<Long,SiteSession> entry :sessions.entrySet())
            result.add(entry.getValue());
        }


        return result;
    }

}
