public class Main {
    public static void main(String[] args) {

        // Create the database
        Database db = new Database();

        // Start metric collection

        // Start the UI
        UI ui = new UI();
        Thread uiThread = new Thread(ui);
        uiThread.start();
    }
}
