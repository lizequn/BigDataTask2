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
        DataInput dataInput = new DataInput("D:/","loglite");
        long result = dataInput.transferData();
        System.out.println(result);
        Assert.assertTrue(result>0);
    }
   // @Test
    public void dataCount() {
        DataCount dataCount1 = new DataCount();
        long i =dataCount1.getCount();
        System.out.println(i);
        Assert.assertEquals(i,100000000);
    }
}
