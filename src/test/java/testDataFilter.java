import org.junit.Test;

import java.util.List;

/**
 * @Auther: Li Zequn
 * Date: 05/02/14
 */
public class testDataFilter {
    @Test
    public void getSessions(){
        final long id =2;
        DataFilter filter = new DataFilter();
        List<SiteSession> list =  filter.getSessionById(id);
        for(SiteSession siteSession : list){
            System.out.println(siteSession);
        }

    }
}
