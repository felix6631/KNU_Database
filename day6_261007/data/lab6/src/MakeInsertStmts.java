import java.util.ArrayList;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.nio.charset.StandardCharsets;

 
public class MakeInsertStmts {

    private ArrayList<String> fileNameList;
    private ArrayList<String> tableNameList;


    public MakeInsertStmts() {
        fileNameList = new ArrayList<String>();
        tableNameList = new ArrayList<String>();
    }


    public void addFile(String fileName, String tableName) {
        fileNameList.add(fileName);
        tableNameList.add(tableName);
    }

    // This function converts each line to the INSERT INTO statement.
    private String convert(String line, String tableName) {

        String[] values = line.split(",");
        StringBuilder sb = new StringBuilder();
        sb.append("INSERT INTO ").append(tableName).append(" VALUES (");
        for (int i = 0; i < values.length; i++) {
            sb.append("'").append(values[i].trim()).append("'");
            if (i < values.length - 1) {
                sb.append(", ");
            }
        }
        sb.append(");");
        return sb.toString();
    }



    // This function reads the table-like file, converts each line to SQL
    // and returns the SQL string which contains a set of SQL statememts.
    public String convertToSQLs(String fileName, String tableName) {
        try {
            BufferedReader br = new BufferedReader(new FileReader(fileName, StandardCharsets.UTF_8));
            String line = br.readLine();  // Skip the header
            String sqls = "";
            while( (line=br.readLine()) != null) {
                sqls = sqls + convert(line, tableName) + "\n";
            }

            br.close();

            return sqls;

        } catch(Exception e) {
            e.printStackTrace();
        }

        return null;
    }



    public void makeSQLFile(String outputFileName) {
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(outputFileName, StandardCharsets.UTF_8));

            int len = fileNameList.size();
            for (int i=0; i<len; i++) {
                String sqls = convertToSQLs(fileNameList.get(i), tableNameList.get(i));
                bw.write(sqls);
            }

            bw.close();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    


    public static void main(String [] args) {
        MakeInsertStmts make = new MakeInsertStmts();

        // Change the following paths accordingly
        make.addFile("../data_v0.7/student.csv", "student");
        make.addFile("../data_v0.7/career_path.csv", "career_path");
        make.addFile("../data_v0.7/eating_style.csv", "eating_style");
        make.addFile("../data_v0.7/favorite_course.csv", "favorite_course");
        make.addFile("../data_v0.7/favorite_food.csv", "favorite_food");
        make.addFile("../data_v0.7/friend_info.csv", "friend_info");
        make.addFile("../data_v0.7/mbti.csv", "mbti");
        make.addFile("../data_v0.7/preferred_pl.csv", "preferred_pl");

        make.makeSQLFile("total.txt");

    }

}
