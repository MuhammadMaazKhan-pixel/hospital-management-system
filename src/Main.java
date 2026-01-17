import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Scanner;




public class Main {
    static Connection conn = DBConnection.getConnection();
    public static void main(String[] args) {

        if (conn == null) {
            System.out.println("Failed to connect to database. Exiting.");
            return;
        }
         Scanner scanner = new Scanner(System.in);
          runConsole(scanner);

    } 

       public static void runConsole(Scanner scanner) {

        displayHelp(); // show menu at start

        while (true) {
            System.out.print("hospital> ");
            String line = scanner.nextLine().trim();

            if (line.equalsIgnoreCase("quit")) {
                System.out.println("Exiting...");
                break;
            }

            if (line.isEmpty()) {
                continue; // ignore empty input
            }

            // Split input into command and arguments
            String[] parts = line.split("\\s+");
            String command = parts[0].toLowerCase();
            String[] args = new String[0];

            if (parts.length > 1) {
                args = new String[parts.length - 1];
                System.arraycopy(parts, 1, args, 0, parts.length - 1);
            }
            switch (command) {
                case "a" -> handleA();
                case "b" -> handleB();
                case "c" -> handleC();
                case "d" -> handleD();
                case "e" -> handleE();
                case "f" -> handleF();
                case "g" -> handleG();
                case "h" -> handleH();
                case "i" -> {
                    if (args.length > 0) {
                        handleI( args[0]);
                    } else {
                        System.out.println("Usage: g <patient_id>");
                    }
                }
                case "help" -> displayHelp();
                default -> System.out.println("Unknown command. Type 'help' to see the menu.");
            }
        }
    }


    // ---------------- Help Menu ----------------
    public static void displayHelp() {

        System.out.println("------------------------ Hospital Management Help Menu ------------------------");
        System.out.println("help - Show this menu");
        System.out.println("quit - Exit the program");
        System.out.println("------------------------------ Database Actions Menu --------------------------");
        System.out.println("a - To Populate the Database");
        System.out.println("b - To Clear The Database");
        System.out.println("--------------------------------- Analytics Menu ------------------------------");
        System.out.println("c - List all Doctors by Revenue per Branch");
        System.out.println("d - List all Patients with Multiple Treatments in a Month");
        System.out.println("e - List avg revenue per appoinment per Doctor");
        System.out.println("f - List which doctor face the most canceled and no show appointments");
        System.out.println("g - List High-Cost Treatments per Patient");
        System.out.println("h - Full Patient Summary (Visits + Treatments + Billing)");
        System.out.println("i - Pateints treatments and when the treatment was done by entering there id - eg. (g P001)");
    }

    public static void handleA(){
    if (isDatabseEmpty()) {
        System.out.println("Populating Database...");
        DBUtils.populateDatabase(conn, "hospital_setup.sql");
          
        
    } else {
        System.out.println("Database is already populated !");

    }

          
    }

    private static  boolean isDatabseEmpty(){
         String[] tables = { "patients", "appointments", "treatments", "billing", "doctors" };
         for (String table : tables) {
        String query = "SELECT COUNT(*) FROM " + table;
        try(Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)){
           
            if (rs.next()) {
                int count = rs.getInt(1);
                if (count > 0) {
                    return false; 
                }
            }
        }
         catch (SQLException e) {
            System.out.println("Empty Database Creating Table : " + table);
        }
    }
     return true; 
    }  

    
    public static void handleB(){
        if (isDatabseEmpty()) {
            System.out.println("Database Already empty. Nothing to Clear !");
        } else {
            System.out.println("Clearing Database...");
            DBUtils.clearDatabase(conn);
            
        }
    }


    public static void handleC() {
        String sql = "SELECT d.doctor_id, d.first_name, d.last_name, d.hospital_branch, SUM(b.amount) AS revenue " +
                     "FROM doctors d " +
                     "JOIN appointments a ON d.doctor_id = a.doctor_id " +
                     "JOIN treatments t ON t.appointment_id = a.appointment_id " +
                     "JOIN billing b ON b.treatment_id = t.treatment_id " +
                     "GROUP BY d.doctor_id, d.first_name, d.last_name, d.hospital_branch";

        try{ PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery() ;

            System.out.println("ID | First Name | Last Name | Branch | Revenue");
            System.out.println("----------------------------------------------------");

            while (rs.next()) {
                System.out.printf("%s | %s | %s | %s | %.2f\n",
                        rs.getString("doctor_id"),
                        rs.getString("first_name"),
                        rs.getString("last_name"),
                        rs.getString("hospital_branch"),
                        rs.getDouble("revenue"));
            }

        } catch (SQLException e) {
            System.out.println("Error fetching doctor revenue: " + e.getMessage());
        }
    }
    


    public static void handleD() {
    String sql = "SELECT " +
                 "p.patient_id, " +
                 "p.first_name, " +
                 "p.last_name, " +
                 "strftime('%Y-%m', t.treatment_date) AS treatment_month, " +
                 "COUNT(t.treatment_id) AS treatment_count " +
                 "FROM patients p " +
                 "JOIN appointments a ON p.patient_id = a.patient_id " +
                 "JOIN treatments t ON a.appointment_id = t.appointment_id " +
                 "GROUP BY p.patient_id, p.first_name, p.last_name, treatment_month " +
                 "HAVING treatment_count > 1 " +
                 "ORDER BY treatment_count DESC";

    try {
        PreparedStatement pstmt = conn.prepareStatement(sql);
        ResultSet rs = pstmt.executeQuery();

        System.out.println("ID | First Name | Last Name | Month | Treatment Count");
        System.out.println("-------------------------------------------------------");

        while (rs.next()) {
            System.out.printf("%s | %s | %s | %s | %d\n",
                    rs.getString("patient_id"),
                    rs.getString("first_name"),
                    rs.getString("last_name"),
                    rs.getString("treatment_month"),
                    rs.getInt("treatment_count"));
        }

    } catch (SQLException e) {
        System.out.println("Error fetching patients: " + e.getMessage());
    }
}
public static void handleE() {
    String sql = "SELECT " +
                 "d.doctor_id, " +
                 "d.first_name, " +
                 "d.last_name, " +
                 "COUNT(DISTINCT a.appointment_id) AS total_appointments, " +
                 "SUM(t.cost) AS total_revenue, " +
                 "ROUND(SUM(t.cost) * 1.0 / COUNT(DISTINCT a.appointment_id), 2) AS avg_revenue_per_appointment " +
                 "FROM doctors d " +
                 "JOIN appointments a ON d.doctor_id = a.doctor_id " +
                 "JOIN treatments t ON a.appointment_id = t.appointment_id " +
                 "GROUP BY d.doctor_id, d.first_name, d.last_name " +
                 "ORDER BY total_revenue DESC";

    try {
       PreparedStatement pstmt = conn.prepareStatement(sql);
        ResultSet rs = pstmt.executeQuery();

        System.out.println("Doctor ID | First Name | Last Name | Total Appointments | Total Revenue | Avg Revenue/Appointment");
        System.out.println("-----------------------------------------------------------------------------------------------");

        while (rs.next()) {
            System.out.printf("%s | %s | %s | %d | %.2f | %.2f\n",
                    rs.getString("doctor_id"),
                    rs.getString("first_name"),
                    rs.getString("last_name"),
                    rs.getInt("total_appointments"),
                    rs.getDouble("total_revenue"),
                    rs.getDouble("avg_revenue_per_appointment"));
        }


    } catch (SQLException e) {
        System.out.println("Error fetching doctor revenue: " + e.getMessage());
    }
}

public static void handleF() {
    String sql = "SELECT d.doctor_id, d.first_name, d.last_name, COUNT(status) AS appointment_count " +
                 "FROM appointments a " +
                 "NATURAL JOIN doctors d " +
                 "WHERE a.status = 'Cancelled' OR a.status = 'No-show' " +
                 "GROUP BY d.doctor_id, d.first_name, d.last_name " +
                 "ORDER BY appointment_count DESC";

    try {
        PreparedStatement pstmt = conn.prepareStatement(sql);
        ResultSet rs = pstmt.executeQuery();

        System.out.println("Doctor ID | First Name | Last Name | Cancelled/No-show Appointments");
        System.out.println("-------------------------------------------------------------------");

        while (rs.next()) {
            System.out.printf("%s | %s | %s | %d\n",
                    rs.getString("doctor_id"),
                    rs.getString("first_name"),
                    rs.getString("last_name"),
                    rs.getInt("appointment_count"));
        }

      
    } catch (SQLException e) {
        System.out.println("Error fetching doctor cancelled/no-show appointments: " + e.getMessage());
    }
}

public static void handleG() {
    String sql = "SELECT p.patient_id, p.first_name, p.last_name, t.treatment_type, t.cost " +
                 "FROM patients p " +
                 "JOIN appointments a ON p.patient_id = a.patient_id " +
                 "JOIN treatments t ON a.appointment_id = t.appointment_id " +
                 "WHERE t.cost > (SELECT AVG(cost) FROM treatments) " +
                 "ORDER BY t.cost DESC";

    try {
        PreparedStatement pstmt = conn.prepareStatement(sql);
        ResultSet rs = pstmt.executeQuery();

        System.out.println("Patient ID | First Name | Last Name | Treatment Type | Cost");
        System.out.println("------------------------------------------------------------");

        while (rs.next()) {
            System.out.printf("%s | %s | %s | %s | %.2f\n",
                    rs.getString("patient_id"),
                    rs.getString("first_name"),
                    rs.getString("last_name"),
                    rs.getString("treatment_type"),
                    rs.getDouble("cost"));
        }

    } catch (SQLException e) {
        System.out.println("Error fetching high-cost treatments: " + e.getMessage());
    }
}

public static void handleH() {
    String sql = "SELECT p.patient_id, p.first_name, p.last_name, " +
                 "COUNT(DISTINCT a.appointment_id) AS total_appointments, " +
                 "COUNT(t.treatment_id) AS total_treatments, " +
                 "SUM(b.amount) AS total_billed " +
                 "FROM patients p " +
                 "LEFT JOIN appointments a ON p.patient_id = a.patient_id " +
                 "LEFT JOIN treatments t ON a.appointment_id = t.appointment_id " +
                 "LEFT JOIN billing b ON t.treatment_id = b.treatment_id " +
                 "GROUP BY p.patient_id " +
                 "ORDER BY total_billed DESC";

    try {
        PreparedStatement pstmt = conn.prepareStatement(sql);
        ResultSet rs = pstmt.executeQuery();

        System.out.println("Patient ID | First Name | Last Name | Total Appointments | Total Treatments | Total Billed");
        System.out.println("------------------------------------------------------------------------------------------");

        while (rs.next()) {
            System.out.printf("%s | %s | %s | %d | %d | %.2f\n",
                    rs.getString("patient_id"),
                    rs.getString("first_name"),
                    rs.getString("last_name"),
                    rs.getInt("total_appointments"),
                    rs.getInt("total_treatments"),
                    rs.getDouble("total_billed"));
        }

        
    } catch (SQLException e) {
        System.out.println("Error fetching patient summary: " + e.getMessage());
    }
}

public static void handleI(String patientId) {
    String sql = "SELECT p.patient_id, p.first_name, p.last_name, " +
                 "t.treatment_date, t.treatment_type " +
                 "FROM billing b " +
                 "NATURAL JOIN patients p " +
                 "JOIN treatments t ON t.treatment_id = b.treatment_id " +
                 "WHERE p.patient_id = ? " +
                 "ORDER BY t.treatment_date";

    try {
        PreparedStatement pstmt = conn.prepareStatement(sql);
        pstmt.setString(1, patientId);  // set the patient ID parameter

        ResultSet rs = pstmt.executeQuery();

        System.out.println("Patient ID | First Name | Last Name | Treatment Date | Treatment Type");
        System.out.println("---------------------------------------------------------------------");

        boolean hasResults = false;
        while (rs.next()) {
            hasResults = true;
            System.out.printf("%s | %s | %s | %s | %s\n",
                    rs.getString("patient_id"),
                    rs.getString("first_name"),
                    rs.getString("last_name"),
                    rs.getString("treatment_date"),
                    rs.getString("treatment_type"));
        }

        if (!hasResults) {
            System.out.println("No treatments found for patient ID: " + patientId);
        }

    } catch (SQLException e) {
        System.out.println("Error fetching patient treatments: " + e.getMessage());
    }
}

}
