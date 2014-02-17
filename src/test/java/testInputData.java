import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.text.ParseException;

/**
 * Created with IntelliJ IDEA.
 * User: zzz
 * Date: 04/02/14
 * Time: 19:14
 */
public class testInputData {
    @Test
    public void dataInput() throws IOException, ParseException, InterruptedException {
        //DataStream dataStream = new DataStream("/largedata","loglite");
       // DataStream dataStream = new DataStream("D:/","loglite");
        DataStream dataStream = new DataStream("/home/ubuntu/data/cassandra-test-dataset","CSC8101-logfile.gz");
        long result = dataStream.transferData();
        System.out.println(result);
        Assert.assertTrue(result > 0);
    }

   // @Test
    public void dataCount() {
        DataCount dataCount1 = new DataCount();
        long i =dataCount1.getCount();
        System.out.println(i);
        Assert.assertTrue(i>0);
    }
}
