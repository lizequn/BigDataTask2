import com.datastax.driver.core.*;

import java.io.*;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.zip.GZIPInputStream;

public class DataInput {
    private final String filePath;
    private final String fileName;
    private final DateFormat dateFormat = new SimpleDateFormat("[dd/MMM/yyyy:HH:mm:ss z]");

    public DataInput(String inputpath,String filename){
        this.filePath = inputpath;
        this.fileName = filename;
    }

    public long transferData() throws IOException, ParseException, InterruptedException {
        System.out.println("begin transfer");
        final File dataDir = new File(filePath);
        final File logFile = new File(dataDir, fileName);
        try (
                final FileInputStream fileInputStream = new FileInputStream(logFile);
                final GZIPInputStream gzipInputStream = new GZIPInputStream(fileInputStream) ;
                final InputStreamReader inputStreamReader = new InputStreamReader(gzipInputStream);
                final BufferedReader bufferedReader = new BufferedReader(inputStreamReader)
        ) {
            CassandraController controller = CassandraController.getInstance();
            Session session = controller.getSession();
            PreparedStatement statement1 = session.prepare("insert into log (clientid,accesstime,action,status,size) values(?,?,?,?,?)");
            BlockingQueue<ResultSetFuture> resultQueen = new LinkedBlockingQueue<>();
            String line = null;
            Date date = null;
            long i = 0;
            int id = 0;
            String action = null;
            int status = 0;
            int size = 0;
            BatchStatement batchStatement = new BatchStatement(BatchStatement.Type.UNLOGGED);
            //32600    [30/Apr/1998:21:30:17   +0000]  "GET  /images/hm_bg.jpg    HTTP/1.0"  200   24736
            while((line = bufferedReader.readLine())!= null && line.length()!=0) {
                i++;
                String[] tokens = line.split(" ");
                if(tokens.length != 8){
                    System.out.println("error happens");
                    String[] oldTokens = tokens;
                    tokens = new String[8];
                    int j;
                    for(j = 0;j<oldTokens.length;j++){
                        tokens[j] = oldTokens[j];
                    }
                    for(;j<8;j++){
                        tokens[j] = "";
                    }
                }

                id = Integer.parseInt(tokens[0]);

                if(!tokens[1].equals("")&&!tokens[2].equals("")){
                    String dateString = tokens[1]+" "+tokens[2];
                    date = dateFormat.parse(dateString);
                }else {
                    date = new Date(0);
                }

                if(tokens[3].equals("")||tokens[4].equals("")||tokens[5].equals("")){
                    action =tokens[3]+tokens[4]+tokens[5];
                } else {
                    action = tokens[3].substring(1)+tokens[4]+tokens[5].substring(0,tokens[5].length()-1);
                }

                if(tokens[6].equals("")){
                    status = 0;
                }else {
                    status = Integer.parseInt(tokens[6]);
                }
                if(tokens[7].equals("-")){
                    size = 0;
                }else {
                    size = Integer.parseInt(tokens[7]);
                }
                batchStatement.add(new BoundStatement(statement1).bind(id, date, action, status, size));
//                resultSet = session.execute(new BoundStatement(statement1).bind(id, date, action, status, size));
                if(i%10== 0){
                    resultQueen.add(session.executeAsync(batchStatement));
                    batchStatement = new BatchStatement(BatchStatement.Type.UNLOGGED);
                }
                if(i%100 == 0){
                    while(!resultQueen.isEmpty()){
                        ResultSetFuture resultSetFuture = resultQueen.take();
                        resultSetFuture.getUninterruptibly();
                    }

                }
                if(i%1000000 == 0){
                    System.out.println(i);

                }

            }
            session.shutdown();
            return i;



        }
    }

}
