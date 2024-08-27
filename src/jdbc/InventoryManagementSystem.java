
package jdbc;

import java.sql.*;
import java.util.Scanner;

public class InventoryManagementSystem {

    private static final String URL = "jdbc:mysql://localhost:3306/inventory_db";
    private static final String USERNAME = "root";
    private static final String PASSWORD = "Pvbiradar@27";
    private static Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        try {
            while (true) {
                System.out.println();
                System.out.println("INVENTORY MANAGEMENT SYSTEM");
                System.out.println("1. Add New Item");
                System.out.println("2. View Inventory");
                System.out.println("3. Update Stock");
                System.out.println("4. Process Order");
                System.out.println("5. Reorder Stock");
                System.out.println("6. Inventory Valuation");
                System.out.println("7. Exit");
                System.out.print("Choose an option: ");
                int choice = scanner.nextInt();
                scanner.nextLine(); // Consume newline

                switch (choice) {
                    case 1:
                        addItem();
                        break;
                    case 2:
                        viewInventory();
                        break;
                    case 3:
                        updateStock();
                        break;
                    case 4:
                        processOrder();
                        break;
                    case 5:
                        reorderStock();
                        break;
                    case 6:
                        inventoryValuation();
                        break;
                    case 7:
                        System.out.println("Exiting system.");
                        return;
                    default:
                        System.out.println("Invalid choice. Try again.");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USERNAME, PASSWORD);
    }

    private static void addItem() throws SQLException {
        System.out.print("Enter item name: ");
        String itemName = scanner.nextLine();
        System.out.print("Enter quantity: ");
        int quantity = scanner.nextInt();
        System.out.print("Enter price: ");
        double price = scanner.nextDouble();
        System.out.print("Enter reorder threshold: ");
        int reorderThreshold = scanner.nextInt();

        String sql = "INSERT INTO inventory (item_name, quantity, price, reorder_threshold) VALUES (?, ?, ?, ?)";
        try (Connection connection = getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, itemName);
            pstmt.setInt(2, quantity);
            pstmt.setDouble(3, price);
            pstmt.setInt(4, reorderThreshold);
            pstmt.executeUpdate();
            System.out.println("Item added successfully.");
        }
    }

    private static void viewInventory() throws SQLException {
        String sql = "SELECT * FROM inventory";
        try (Connection connection = getConnection();
             Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            System.out.println("Inventory:");
            while (rs.next()) {
                System.out.printf("ID: %d, Name: %s, Quantity: %d, Price: %.2f, Reorder Threshold: %d\n",
                        rs.getInt("item_id"), rs.getString("item_name"), rs.getInt("quantity"),
                        rs.getDouble("price"), rs.getInt("reorder_threshold"));
            }
        }
    }

    private static void updateStock() throws SQLException {
        System.out.print("Enter item ID to update: ");
        int itemId = scanner.nextInt();
        System.out.print("Enter new quantity: ");
        int quantity = scanner.nextInt();

        String sql = "UPDATE inventory SET quantity = ? WHERE item_id = ?";
        try (Connection connection = getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, quantity);
            pstmt.setInt(2, itemId);
            pstmt.executeUpdate();
            System.out.println("Stock updated successfully.");
        }
    }

    private static void processOrder() throws SQLException {
        System.out.print("Enter item ID: ");
        int itemId = scanner.nextInt();
        System.out.print("Enter quantity: ");
        int quantity = scanner.nextInt();
        System.out.print("Enter order type (purchase/sale): ");
        String orderType = scanner.next();

        String sql = "INSERT INTO orders (item_id, quantity, order_type) VALUES (?, ?, ?)";
        try (Connection connection = getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, itemId);
            pstmt.setInt(2, quantity);
            pstmt.setString(3, orderType);
            pstmt.executeUpdate();
            updateStockAfterOrder(itemId, quantity, orderType);
            System.out.println("Order processed successfully.");
        }
    }

    private static void updateStockAfterOrder(int itemId, int quantity, String orderType) throws SQLException {
        String sql = "UPDATE inventory SET quantity = quantity + ? WHERE item_id = ?";
        if (orderType.equals("sale")) {
            sql = "UPDATE inventory SET quantity = quantity - ? WHERE item_id = ?";
        }
        try (Connection connection = getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, quantity);
            pstmt.setInt(2, itemId);
            pstmt.executeUpdate();
        }
    }

    private static void reorderStock() throws SQLException {
        String sql = "SELECT * FROM inventory WHERE quantity <= reorder_threshold";
        try (Connection connection = getConnection();
             Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            System.out.println("Items below reorder threshold:");
            while (rs.next()) {
                System.out.printf("ID: %d, Name: %s, Quantity: %d, Reorder Threshold: %d\n",
                        rs.getInt("item_id"), rs.getString("item_name"), rs.getInt("quantity"),
                        rs.getInt("reorder_threshold"));
            }
        }
    }

    private static void inventoryValuation() throws SQLException {
        String sql = "SELECT SUM(quantity * price) AS total_value FROM inventory";
        try (Connection connection = getConnection();
             Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                double totalValue = rs.getDouble("total_value");
                System.out.printf("Total Inventory Valuation: $%.2f\n", totalValue);
            }
        }
    }
}
