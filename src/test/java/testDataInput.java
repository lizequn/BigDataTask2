import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.text.ParseException;

/**
 * Created with IntelliJ IDEA.
 * User: zzz
 * Date: 04/02/14
 * Time: 01:14
 */
public class testDataInput {
    @Test
    public void dataInput() throws IOException, ParseException {
        DataInput dataInput = new DataInput("/home/zzz/cassandra1/DevCenter","loglite");
        long result = dataInput.transferData();
        System.out.println(result);
        Assert.assertTrue(result>0);
    }
}
