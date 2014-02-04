import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.ResultSetFuture;
import com.datastax.driver.core.Session;

import java.io.*;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.zip.GZIPInputStream;

public class DataInput {
    private final String filePath;
    private final String fileName;
    private final DateFormat dateFormat = new SimpleDateFormat("[dd/MMM/yyyy:HH:mm:ss z]");

    public DataInput(String inputpath,String filename){
        this.filePath = inputpath;
        this.fileName = filename;
    }

    public long transferData() throws IOException, ParseException {
        final File dataDir = new File(filePath);
        final File logFile = new File(dataDir, fileName);
        try (
                final FileInputStream fileInputStream = new FileInputStream(logFile);
                final InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream);
                final BufferedReader bufferedReader = new BufferedReader(inputStreamReader)
        ) {
            CassandraController controller = CassandraController.getInstance();
            Session session = controller.getSession();
            PreparedStatement statement1 = session.prepare("insert into log (clientid,accesstime,action,status,size) values(?,?,?,?,?)");
            ResultSetFuture resultSetFuture = null;
            String line = null;
            Date date = null;
            long i = 0;
            int id = 0;
            String action = null;
            int status = 0;
            long size = 0;
            //32600    [30/Apr/1998:21:30:17   +0000]  "GET  /images/hm_bg.jpg    HTTP/1.0"  200   24736
            while((line = bufferedReader.readLine())!= null && line.length()!=0) {
                i++;
                String[] tokens = line.split(" ");
                id = Integer.parseInt(tokens[0]);
                String dateString = tokens[1]+" "+tokens[2];
                date = dateFormat.parse(dateString);
                action = tokens[3].substring(1)+tokens[4]+tokens[5].substring(0,tokens[5].length()-1);
                status = Integer.parseInt(tokens[6]);
                if(tokens[7].equals("-")){
                    size = 0;
                }else {
                    size = Long.parseLong(tokens[7]);
                }
                resultSetFuture = session.executeAsync(new BoundStatement(statement1).bind(id,date,action,status,size));
                if(i%10000 == 0){
                    System.out.println(i);
                }
            }
            resultSetFuture.getUninterruptibly();
            session.shutdown();
            return i;



        }
    }

}
