#  Hospital Management System (Terminal-Based)

A Java-based terminal application that simulates a Hospital Management System using SQLite and relational database concepts. The system allows database management and advanced analytical queries through a command-line interface.


##  Features

### Database Management
- Populate the database with sample hospital data
- Clear all records from the database safely
- Prevent duplicate population using database state checks

### Analytics & Reporting
- List doctors by total revenue per branch
- Identify patients with multiple treatments in a single month
- Calculate average revenue per appointment per doctor
- Find doctors with the highest cancelled and no-show appointments
- Display high-cost treatments per patient
- Generate full patient summaries (appointments, treatments, billing)
- View treatment type and date for a specific patient by ID

### Terminal UI
- Interactive command-based interface
- Structured help menu
- Input validation and error handling


##  Technologies Used

- Java (Core Java, JDBC)
- SQLite
- SQL (JOINs, GROUP BY, HAVING, Aggregations)
- Relational Database Design
- Command-Line Interface (CLI)

## How to Run
- Compile the files
```bash
javac -d bin -cp "lib/sqlite-jdbc-3.51.1.0.jar" src/*.java
```
- Run the Application
```bash
java -cp "bin;lib/sqlite-jdbc-3.51.1.0.jar" Main
```


##  Database Schema

The application uses the following tables:
- patients (patient_id [pk], first_name, last_name, gender, date_of_birth, contact_number, address, registration_date, insurance_provider, insurance_number, email)
- doctors (doctor_id [pk], first_name, last_name, specialization, phone_number, years_experience, hospital_branch, email)
- appointments (appointment_id [pk], patient_id [fk], doctor_id [fk], appointment_date, appointment_time, reason_for_visit, status)
- treatments (treatment_id [pk], appointment_id [fk], treatment_type, description, cost, treatment_date)
- billing (bill_id [pk], patient_id [fk], treatment_id [fk], bill_date, amount, payment_method, payment_status)

### ER Diagram

Er Digram for this Dtabase is on 
```file
hospitalER.drawio.png
```
Primary and foreign key constraints are used to enforce relationships.

## SQL Analytics Queries

### Doctors by Revenue per Branch
```sql
SELECT 
d.doctor_id, 
d.first_name,
 d.last_name,
       SUM(t.cost) AS total_revenue
FROM doctors d
JOIN appointments a ON d.doctor_id = a.doctor_id
JOIN treatments t ON a.appointment_id = t.appointment_id
GROUP BY d.doctor_id;
```
### Patients with Multiple Treatments in a Month
```sql
SELECT 
    p.patient_id,
    p.first_name , 
	p.last_name,
    strftime('%Y-%m', t.treatment_date) AS treatment_month,
    COUNT(t.treatment_id) AS treatment_count
FROM patients p
JOIN appointments a ON p.patient_id = a.patient_id
JOIN treatments t ON a.appointment_id = t.appointment_id
GROUP BY p.patient_id,p.first_name , p.last_name, treatment_month
HAVING treatment_count > 1
ORDER BY treatment_count DESC;
```
### Average revenue per appointment per Doctor
```sql
SELECT 
    d.doctor_id,
    d.first_name ,
	d.last_name ,
    COUNT(DISTINCT a.appointment_id) AS total_appointments,
    SUM(t.cost) AS total_revenue,
    ROUND(SUM(t.cost) * 1.0 / COUNT(DISTINCT a.appointment_id), 2) AS avg_revenue_per_appointment
FROM doctors d
JOIN appointments a ON d.doctor_id = a.doctor_id
JOIN treatments t ON a.appointment_id = t.appointment_id
GROUP BY d.doctor_id , d.first_name , d.last_name 
ORDER BY total_revenue DESC;
```
### Which doctor face the most canceled and no show appointment
```sql
SELECT 
d.doctor_id,
 d.first_name, 
 d.last_name, 
 COUNT(status) AS appointment_count
FROM appointments a 
NATURAL JOIN doctors d 
WHERE a.status = 'Cancelled' OR a.status = 'No-show' 
GROUP by d.doctor_id,d.first_name,d.last_name 
ORDER by appointment_count DESC ;
```
### High-Cost Treatments per Patient
```sql
SELECT 
p.patient_id,
p.first_name ,
p.last_name,
t.treatment_type,
t.cost
FROM patients p
JOIN appointments a ON p.patient_id = a.patient_id
JOIN treatments t ON a.appointment_id = t.appointment_id
WHERE t.cost > (
    SELECT AVG(cost) FROM treatments
)
ORDER BY t.cost DESC;
```
### Full Patient Summary (Visits + Treatments + Billing)
```sql
SELECT  
p.patient_id,
 p.first_name , 
 p.last_name, 
 COUNT(DISTINCT a.appointment_id) AS total_appointments,
 COUNT(t.treatment_id) AS total_treatments, 
 SUM(b.amount) AS total_billed
FROM patients p
LEFT JOIN appointments a ON p.patient_id = a.patient_id
LEFT JOIN treatments t ON a.appointment_id = t.appointment_id
LEFT JOIN billing b ON t.treatment_id = b.treatment_id
GROUP BY p.patient_id
ORDER BY total_billed DESC;
```
### patients treatments and when the treatment was done by entering there id 
```sql
Select 
p.patient_id,
p.first_name,
p.last_name,
t.treatment_date,
t.treatment_type 
FROM billing b 
NATURAL JOIN patients p 
JOIN treatments t on t.treatment_id = b.treatment_id
 WHERE patient_id = 'P001';
```


