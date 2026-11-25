import java.util.ArrayList;
import java.util.StringTokenizer;
import java.io.BufferedReader;
import java.io.FileReader;
import java.nio.charset.StandardCharsets;
import java.io.InputStreamReader;

import java.sql.*;
import java.io.File;

public class MyDBMSTool {
 
    private String url, user, passwd;
    private String curr_database;
    private Connection conn = null;

      
    public MyDBMSTool(String url, String user, String passwd) { 
        this.url = url;
        this.user = user;
        this.passwd = passwd;

        int s = url.indexOf("//");
        int e = -1;
        if(s>=0) {
            e = url.indexOf("/", s+2);
            if(e>=0) {
                curr_database = url.substring(e);
                curr_database = this.curr_database.replace("/", "");
            }
            else curr_database = "";
        }
        else curr_database = "";

        System.out.println("Current DB is "+curr_database);
    }
 

    public boolean connect() {
        try {
            conn = DriverManager.getConnection(url, user, passwd);
            return true;
        } catch(Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean disconnect(String url, String user, String passwd) {
        try {
            conn.close();
            return true;
        } catch(Exception e) {
            e.printStackTrace();
        }
        return false;
    }
 

    // This function converts ResultSet to the string.
    // Note that the string contains multiple lines with separater \n
    // (printHeader=true   => Include the attribute names, 
    //   printHeader=false => Do not include the attribute names)
    public String convertToString(ResultSet rs, boolean printHeader) {
        try {
            ResultSetMetaData rsm = rs.getMetaData();
            String str = "";

            ///////////// YOUR CODE //////////////////////
            int colCount = rsm.getColumnCount();
            StringBuilder sb = new StringBuilder();

            // attribute names + separator line
            if(printHeader) {
                String header = "";
                for(int i=1; i<=colCount; i++) {
                    if(i>1) header += "\t";
                    header += rsm.getColumnLabel(i);
                }
                sb.append(header).append("\n");
                sb.append("-".repeat(header.length()*2+1)).append("\n");
            }

            // records (tuples)
            while(rs.next()) {
                for(int i=1; i<=colCount; i++) {
                    if(i>1) sb.append("\t");
                    sb.append(rs.getString(i));
                }
                sb.append("\n");
            }

            if(printHeader) {
                int headerLen = sb.indexOf("\n");
                sb.append("=".repeat(headerLen*2+1)).append("\n");
            }

            str = sb.toString();
            //////////////////////////////////////////////
            return str;
        }
        catch (Exception e) {
            e.printStackTrace();;
        }
        return "";
    }


    // This function runs the SELECT statement and convert the results to the string
    // (printHeader=true   => Include the attribute names, 
    //   printHeader=false => Do not include the attribute names)
    public String runSelectStr(String select, boolean printHeader) {       
        try {
            String resultStr = "";
            /////////////////// YOUR CODE //////////////////////
            // use convertToString()
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(select);
            resultStr = convertToString(rs, printHeader);
            rs.close();
            stmt.close();
            ////////////////////////////////////////
            return resultStr;
        } catch(Exception e) {
            e.printStackTrace();
        }

        return "";
    }



    // This function runs the CREATE DATABASE, CREATE TABLE, INSERT, DELETE or Update statement
    public void runUpdate(String sql) {    
         try {
            /////////////// YOUR CODE /////////////////
            Statement stmt = conn.createStatement();
            stmt.executeUpdate(sql);
            stmt.close();
            ///////////////////////////////////////
            
         } catch(Exception e) {
             e.printStackTrace();
         }
    }

 
    
    // This function converts each line to the INSERT INTO statement.
    // * Thie code is the same as that of the Week5 exercise.
    private String convert(String line, String tableName) {

        //////////////// YOUR CODE /////////////////////////
        String sql ="";

        // split(",", -1) keeps empty values (e.g. "a,,c" or "a,b,")
        String [] vals = line.split(",", -1);
        sql = "INSERT INTO " + tableName + " VALUES (";
        for(int i=0; i<vals.length; i++) {
            if(i>0) sql += ", ";
            String v = vals[i].trim().replace("\\", "\\\\").replace("'", "''");
            sql += "'" + v + "'";
        }
        sql += ")";

        return sql;
        ////////////////////////////////////////////////////
        
    }


    // This function reads the table-like file and generates SQL statements as follows:
    //  - Table Creation Statement: Generate the CREATE TABLE statement from the first line (header)  
    //  - Insert Statements:  Generate the INSERT statements from each line
    // Assume the we have only one attribute type varchar(100)
    public ArrayList<String> convertToSQLsWithTableSchema(String fileName, String tableName) {
        try {
            BufferedReader br = new BufferedReader(new FileReader(fileName, StandardCharsets.UTF_8));
            String header = br.readLine();  // read the header
            String line = null;

            ArrayList<String> sqlList = new ArrayList<String>();

            //////////////////// YOUR CODE //////////////////////////////
            // Use the convert(line, tableName) method

            // 1) CREATE TABLE statement from the header (all types are varchar(100))
            if(header == null) {
                br.close();
                return sqlList;
            }
            if(header.startsWith("\uFEFF")) header = header.substring(1); // remove UTF-8 BOM

            String [] cols = header.split(",");
            String create = "CREATE TABLE " + tableName + " (";
            for(int i=0; i<cols.length; i++) {
                if(i>0) create += ", ";
                create += "`" + cols[i].trim() + "` varchar(100)";
            }
            create += ")";
            sqlList.add(create);

            // 2) INSERT statements from each line
            while((line = br.readLine()) != null) {
                if(line.trim().equals("")) continue; // skip empty lines
                sqlList.add(convert(line, tableName));
            }
            br.close();
            //////////////////////////////////////////////////

            return sqlList;

        } catch(Exception e) {
            e.printStackTrace();
        }

        return null;
    } 

    


    // This fucntion runs multiple queries using "executeBatch()"
    public void runMultipleQueries(ArrayList<String> sqlList) {
        
        try {

            //////////////////// YOUR CODE ////////////////////////////
            if(sqlList == null || sqlList.isEmpty()) return;

            Statement stmt = conn.createStatement();
            boolean oldAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);
            try {
                for(String sql: sqlList) stmt.addBatch(sql);
                stmt.executeBatch();
                conn.commit();
            } catch(Exception e) {
                // e.g. the table already exists => do not leave partially inserted records
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(oldAutoCommit);
                stmt.close();
            }
            //////////////////////////////////////////////////////
        } catch(Exception e) {
            e.printStackTrace();
        }
        return;
    }



    // This fucntion imports csvFile to the table named tabName
    // 1) create a table schema
    // 2) insert data in csvFile into the table
    public void importData(String csvFile, String tabName) {

        ArrayList<String> sqlList = convertToSQLsWithTableSchema(csvFile, tabName);

        runMultipleQueries(sqlList);
    }


    // This fucntion imports all the csv files in "path" to the database named dbName
    //   1) create a database if it does not exist (ref. CREATE DATABASE IF NOT EXISTS ...)
    //   2) get csv file names in "path"
    //   3) import each csv file using importData()  (ref. You can create a table in a different database using DB_NAME.TABLE_NAME)
    public void dimportData(String path, String dbName) {

        //////////////// YOUR CODE //////////////////////
        // 1) create a database if it does not exist
        runUpdate("CREATE DATABASE IF NOT EXISTS " + dbName);

        // 2) get csv file names in "path"
        File dir = new File(path);
        File [] files = dir.listFiles();
        if(files == null) {
            System.out.println(" #directory error: " + path);
            return;
        }
        java.util.Arrays.sort(files);

        // 3) import each csv file (department.csv => DB_NAME.department)
        for(File file: files) {
            String fileName = file.getName();
            if(file.isDirectory() || !fileName.toLowerCase().endsWith(".csv")) continue;

            String tabName = fileName.substring(0, fileName.lastIndexOf("."));
            importData(file.getPath(), dbName + "." + tabName);
        }
        /////////////////////////////////////////////////
    }

 
 
    // This function interprets the command "cmd"
    // Available commands:
    //   - help, pwd, ls, exit: implemnated
    //   - import, dimport, read, show databases, show tables, desc, find table: You should implement the commands
    public String interpret(String cmd) {
        cmd = cmd.toLowerCase();
        cmd = cmd.trim();
        
        String resultStr = "";

        // print avaiable commands
        if(cmd.equals("help")) {
            resultStr = 
"\n#Usage#\n"+
"help : print tool commands\n"+
"pwd: print the current working directory \n"+
"ls [path]: print files in \"path\" \n"+
"exit  : exit the tool\n"+
"show databases: print all database names\n"+
"show tables DB_NAME: print all tables in the DB_NAME database\n"+
"import FILE_NAME TAB_NAME: import FILE_NAME to TAB_NAME\n"+
"dimport DIR DB_NAME: import all csv files in DIR to the database named DB_NAME\n"+
"read TAB_NAME: print all records in TAB_NAME\n"+
"desc TAB_NAME: print column names and column types for TAB_NAME in the column order\n"+
"find info DB_NAME NICK_NAME: Identify all tables in the database DB_NAME that have a nickname attribute, then retrieve all records from each identified table where the nickname value matches NICK_NAME\n"
;

        }
        // print the current working directory
        else if(cmd.equals("pwd")) {
            resultStr = "\n"+System.getProperty("user.dir")+"\n";
        }
        // show the file list in the current directy (ls) or in the specified directory (ls PATH)
        else if(cmd.equals("ls") || cmd.startsWith("ls ")) {
            String specifiedPath = cmd.replaceFirst("ls","").trim();
            File dir = null;
            if(specifiedPath.equals("")) dir = new File(".");
            else dir = new File(specifiedPath);
            resultStr = "\n";
            for (File file: dir.listFiles()) {
                if(file.isDirectory()) resultStr += "("+file.getName() +")\n";
                else resultStr += file.getName() +"\n";
            }
        }
        // Show all the databases
        // - Usage: show databases
        else if(cmd.equals("show databases")) {  
            ///////////// YOUR CODE ////////////////
            // Use runSelectStr()
            resultStr = runSelectStr("SELECT SCHEMA_NAME FROM information_schema.SCHEMATA", true);
            ///////////////////////////////////////        
        }  
        // show tables in the database called DB_NAME
        // - Usage: show tables DB_NAME
        else if(cmd.startsWith("show tables")) { 
            ///////////// YOUR CODE ////////////////
            // Use runSelectStr()
            String dbName = cmd.replaceFirst("show tables", "").trim();
            if(dbName.equals("")) dbName = curr_database; // no DB_NAME => current database

            resultStr = runSelectStr(
                "SELECT TABLE_NAME FROM information_schema.TABLES "+
                "WHERE LOWER(TABLE_SCHEMA) = '" + dbName + "' ORDER BY TABLE_NAME", true);
            ///////////////////////////////////////        
        }
        // import the csv file to the table in DBMS  
        // - Usage: import FILE_NAME TAB_NAME
        else if(cmd.startsWith("import ")) { 
            String [] toks = cmd.split(" ");
            if(toks.length<3) {
                resultStr = " #command error";
            }
            else {
                String filePath = toks[1]; // csv file path
                String tabName = toks[2]; // table name to be created
                importData(filePath, tabName);
            }
        }
        // import all the csv files in filePath to the database named dbName  
        // - Usage: dimport DIR DB_NAME: import all csv files in DIR to the database named DB_NAME
        // * Remove the file extension from each file name and use it as the table name
        else if(cmd.startsWith("dimport ")) { 
            String [] toks = cmd.split(" ");
            if(toks.length<2) {
                resultStr = " #command error";
            }
            else {
                String filePath = toks[1]; // csv file path
                String dbName = toks[2]; // database name
                dimportData(filePath, dbName);
            }
        }
        // Show data in the table
        // You should support both TABLE_NAME and DB_NAME.TABLE_NAME notations         
        // - Usage: read TAB_NAME 
        else if(cmd.startsWith("read ")) { 
            ///////////// YOUR CODE ////////////////
            // Use runSelectStr()
            // Both TABLE_NAME and DB_NAME.TABLE_NAME work in the FROM clause
            String name = cmd.replaceFirst("read ", "").trim();
            resultStr = runSelectStr("SELECT * FROM " + name, true);
            ///////////////////////////////////////
        }

        // print the column names and column type names of the TAB_NAME table in the column order
        // You should support both TABLE_NAME and DB_NAME.TABLE_NAME notations         
        // - Usage: desc TAB_NAME
        else if(cmd.startsWith("desc ")) {  
            String name = cmd.replaceFirst("desc ", "");

            ///////////// YOUR CODE ////////////////
            // Use runSelectStr()
            // Use the ORDINAL_POSITION attribute in the COLUMNS table to sort the results in the column order
            name = name.trim();
            String dbName = curr_database;  // TABLE_NAME notation => current database
            String tabName = name;
            int dot = name.indexOf(".");
            if(dot>=0) {                    // DB_NAME.TABLE_NAME notation
                dbName = name.substring(0, dot);
                tabName = name.substring(dot+1);
            }

            resultStr = runSelectStr(
                "SELECT COLUMN_NAME, COLUMN_TYPE FROM information_schema.COLUMNS "+
                // cmd is lower-cased by interpret(), so compare case-insensitively (e.g. SCHEMATA on Linux)
                "WHERE LOWER(TABLE_SCHEMA) = '" + dbName + "' AND LOWER(TABLE_NAME) = '" + tabName + "' "+
                "ORDER BY ORDINAL_POSITION", true);
            ///////////////////////////////////////         
        }

        // Identify all tables in the database DB_NAME that have a nickname attribute,
        //    then retrieve all records from each identified table where the nickname value matches NICK_NAME
        // - Usage: find info DB_NAME NICK_NAME
        else if(cmd.startsWith("find info ")) { 
            
            ///////////// YOUR CODE ////////////////
            String [] toks = cmd.split("\\s+");
            if(toks.length<4) {
                resultStr = " #command error";
            }
            else {
                String dbName = toks[2];
                String nickName = toks[3].replace("'", "''");

                // 1) identify all tables in dbName that have a nickname attribute
                String tabStr = runSelectStr(
                    "SELECT TABLE_NAME FROM information_schema.COLUMNS "+
                    "WHERE TABLE_SCHEMA = '" + dbName + "' AND COLUMN_NAME = 'nickname' "+
                    "ORDER BY TABLE_NAME", false);

                // 2) retrieve the records matching nickName from each identified table
                for(String tabName: tabStr.split("\n")) {
                    if(tabName.equals("")) continue;
                    String fullName = dbName + "." + tabName;
                    resultStr += "[Table: " + fullName + "\t]\n";
                    resultStr += runSelectStr(
                        "SELECT * FROM " + fullName + " WHERE nickname = '" + nickName + "'", false);
                    resultStr += "\n";
                }
            }
            ////////////////////////////////////////
        }

        return resultStr;
    }



    public static void main(String [] args) throws Exception {
 
        String url = "jdbc:mysql://localhost:3306/mydb"; // Change the database name properly
        
        // Change user and passwd properly
        String user = "root";
        String passwd = "Crafting663!";
    
        MyDBMSTool tool = new MyDBMSTool(url, user, passwd);
        if(tool.connect()==false) {
            System.out.println("# Connection Error");
            return;
        }


        BufferedReader consolReader = new BufferedReader(new InputStreamReader(System.in));
        while(true) {
            System.out.print("KIM HJ>");  // Your Name
            String cmd = consolReader.readLine();

            if( cmd.trim().toLowerCase().equals("exit")) break;
            String cmdResult = tool.interpret(cmd);
            System.out.println(cmdResult);
        }
    }

}
