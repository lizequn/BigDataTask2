import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.HostDistance;
import com.datastax.driver.core.PoolingOptions;
import com.datastax.driver.core.Session;

/**
 * Created with IntelliJ IDEA.
 * User: zzz
 * Date: 02/02/14
 * Time: 19:08
 */
public class CassandraController {

    private Cluster cluster;
    private Session session;
    private final String keyspace;
    private static CassandraController INSTANCE = new CassandraController();
    private CassandraController(){
        this.keyspace = "task2";
    }

    public static CassandraController getInstance(){
        return INSTANCE;
    }

    public void shutDown(){
        closeConnect();
    }

    public Session getSession(){
        setupConnect();
        return session;
    }

    public Session getSession(int num){
        setupPoolingConnect(num);
        return session;
    }

    private void setupConnect(){
        cluster = new Cluster.Builder().addContactPoint("127.0.0.1").build();
        session = cluster.connect(keyspace);
    }
    private void setupPoolingConnect(int num){
        int numberOfConnections = num;
        cluster = new Cluster.Builder().addContactPoint("127.0.0.1").build();
        PoolingOptions poolingOptions = cluster.getConfiguration().getPoolingOptions();
        poolingOptions.setCoreConnectionsPerHost(HostDistance.LOCAL, numberOfConnections);
        poolingOptions.setMaxConnectionsPerHost(HostDistance.LOCAL, numberOfConnections);
        poolingOptions.setCoreConnectionsPerHost(HostDistance.REMOTE, numberOfConnections);
        poolingOptions.setMaxConnectionsPerHost(HostDistance.REMOTE, numberOfConnections);

        session = cluster.connect(keyspace);
    }

    private void closeConnect(){
        session.shutdown();
        cluster.shutdown();
    }





}
