import com.datastax.driver.core.*;

/**
 * @Auther: Li Zequn
 * Date: 04/02/14
 */
public class DataCount {
    private final CassandraController controller = CassandraController.getInstance();
    private final Session session;
    public DataCount(){
        session = controller.getSession();
    }
    public long getCount(){
        System.out.println("begin");
        ResultSet resultSet = session.execute("Select * from log");
        System.out.println("end");
        long count = 0;
        for(Row row:resultSet){
            count++;
            if(count % 100000 == 0){
                System.out.println(count);
            }
        }
        return count;

    }
}
