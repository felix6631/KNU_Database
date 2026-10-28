import java.sql.Statement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;

public class App {
    public static void main(String[] args) throws Exception {
        String query = "SELECT nickname, section FROM lab7.student";
        String url = "jdbc:mysql://localhost:3306/lab7";
        String user = "root";
        String password = "root";
        try {
            Connection conn = DriverManager.getConnection(url, user, password);
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            while (rs.next()) {
                String nickname = rs.getString("nickname");
                String section = rs.getString("section");
                System.out.println(nickname + " - " + section);
            }
            rs.close();
            stmt.close();
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
