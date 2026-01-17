import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class DBUtils {

    public static void populateDatabase(Connection conn, String sqlFilePath) {
        try (BufferedReader br = new BufferedReader(new FileReader(sqlFilePath))) {
            StringBuilder sqlBuilder = new StringBuilder();
            String line;
            
            Statement stmt = conn.createStatement();

            while ((line = br.readLine()) != null) {
                line = line.trim();
             
                if (line.isEmpty() || line.startsWith("--") || line.startsWith("//")) {
                    continue;
                }

                sqlBuilder.append(line).append(" ");

               
                if (line.endsWith(";")) {
                    String sqlStatement = sqlBuilder.toString();
                    stmt.execute(sqlStatement); 
                    sqlBuilder.setLength(0);   
                }
            }

            System.out.println("Database populated successfully!");

        } catch (IOException e) {
            System.out.println("Error reading SQL file: " + e.getMessage());
        } catch (SQLException e) {
            System.out.println("Error executing SQL: " + e.getMessage());
        }
    }

    public static void clearDatabase(Connection conn) {
    try (Statement stmt = conn.createStatement()) {
        
       String[] tableName = {"appointments","billing","doctors","patients","treatments"}; 

        for (String tableName1 : tableName) {
            stmt.executeUpdate("DROP TABLE IF EXISTS " + tableName1);
            System.out.println("Dropped table: " + tableName1);
        }

        System.out.println("Database cleared successfully!");
    } catch (SQLException e) {
        System.out.println("Error clearing database: " + e.getMessage());
    }
}

}
