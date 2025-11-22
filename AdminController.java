/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Controller;
import brothermanagement.Admistration;
import Model.Product;
import UI.BillFrame;
import database.brotherconnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import javax.swing.JTable;
import javax.swing.*;
import java.io.File;
import UI.PlaceOrderPanel; 
import brothermanagement.ProductItemPanel;
import java.util.ArrayList;

// changes
import java.sql.ResultSet; 
import java.text.NumberFormat;
import java.util.Locale;
import java.sql.Statement;
/**
 *
 * @author Dell Precision T5810
 */
public class AdminController {
    private JComboBox<String> productTypeList;
    private JComboBox<String> statusTypeList;
    private JTable table;
    private JLabel inventoryImageLabel;
    private JPanel productContainer;
    private JScrollPane scrollPane;
    private JPanel placeorder_pane;
    private JPanel tab_menu;
    private String imagePath;
    private int batchSize = 12;
    private int loadedCount = 0;
    private ArrayList<Product> list;
    
    // changes:
    private JTable place_table; // set Table placeorder
    private JTextField Total; // set Textfield Total
    private PlaceOrderPanel orderPanel; // reference to PlaceOrderPanel
    
    public AdminController(){};
    
    public AdminController(Admistration adminView, PlaceOrderPanel orderPanel) {
        this.productTypeList = adminView.getProductTypeList();
        this.statusTypeList = adminView.getStatusTypeList();
        this.inventoryImageLabel = adminView.getInventoryImageLabel();
        this.table = adminView.getTable();
        this.tab_menu = adminView.get_tabmenu();
        this.productContainer = orderPanel.getProductContainer();
        this.scrollPane = orderPanel.getScrollPane();
        this.placeorder_pane = orderPanel.getPlaceorderpanel();
        
        //changes
        this.place_table = orderPanel.getPlaceTable(); //get Table placeorder
        this.Total = orderPanel.getTotalTextField(); // set Textfield total
        this.orderPanel = orderPanel;
        setupDeleteButton();
        setupResetButton();
    }
    
    public AdminController(JPanel orderPanel){
        
    }
    
    public JPanel getPanel(){
        return placeorder_pane;
    }
    
    public void loadProductTypes() {
        String[] types = {"Meal", "Drink", "Combo", "Dessert"};
        productTypeList.removeAllItems();
        for (String type : types) {
            productTypeList.addItem(type);
        }
    }
    
    public void loadStatusTypes() {
        String[] types = {"Available", "Unavailable"};
        statusTypeList.removeAllItems();
        for (String type : types) {
            statusTypeList.addItem(type);
        }
    }
    
    public void loadProductsFromDatabase() {
        DefaultTableModel model = (DefaultTableModel) table.getModel();
        model.setRowCount(0);

        String query = "SELECT * FROM product";
        try (Connection con = brotherconnection.getConnection();
             PreparedStatement ps = con.prepareStatement(query)) {

            var rs = ps.executeQuery();
            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getInt("pro_id"),
                    rs.getString("pro_name"),
                    rs.getString("pro_type"),
                    String.format("%,.0f VND", rs.getDouble("pro_price")),
                    rs.getInt("pro_stock"),
                    rs.getString("pro_status"),
                    rs.getString("pro_image")
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error loading products: " + e.getMessage());
        }
        }
    
    
    public void loadProductsToPanel() {
        
        productContainer.removeAll();    
        loadedCount = 0;                
        String sql = "SELECT * FROM product";
        try (Connection con = brotherconnection.getConnection();
            PreparedStatement ps = con.prepareStatement(sql);
            var rs = ps.executeQuery()) {

           while (rs.next()) {
               Product product = new Product(
                   rs.getInt("pro_id"),
                   rs.getString("pro_name"),
                   rs.getString("pro_type"),
                   rs.getDouble("pro_price"),
                   rs.getString("pro_status"),
                   rs.getString("pro_image"),
                   rs.getInt("pro_stock")
               );

               ProductItemPanel productPanel = new ProductItemPanel(product);
               productPanel.setAdminController(this); // changes to recieve the value of the jspinner
               productContainer.add(productPanel.getitems_panel());
           }

       } catch (SQLException ex) {
           ex.printStackTrace();
           JOptionPane.showMessageDialog(null, "Database error: " + ex.getMessage());
       }

        productContainer.revalidate();
        productContainer.repaint();
    }
    
   
    
    public Product addProduct(Product pro){
        if (pro == null) {
        JOptionPane.showMessageDialog(null, "Invalid product data!", "Error", JOptionPane.ERROR_MESSAGE);
        return null;
        }

        if (pro.getName() == null || pro.getName().trim().isEmpty() ||
            pro.getType() == null || pro.getType().trim().isEmpty() ||
            pro.getStatus() == null || pro.getStatus().trim().isEmpty()) {
            JOptionPane.showMessageDialog(null, "Please fill all required fields!", "Warning", JOptionPane.WARNING_MESSAGE);
            return null;
        }


        String query = "INSERT INTO product (pro_id, pro_name, pro_type, pro_price, pro_image, pro_stock, pro_status) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection con = brotherconnection.getConnection();
             PreparedStatement ps = con.prepareStatement(query)) {

            ps.setInt(1, pro.getId());
            ps.setString(2, pro.getName());
            ps.setString(3, pro.getType());
            ps.setDouble(4, pro.getPrice());
            ps.setString(5, pro.getImage());
            ps.setInt(6, pro.getStock());
            ps.setString(7, pro.getStatus());
            ps.executeUpdate();

            JOptionPane.showMessageDialog(null, "Product added successfully!");
            loadProductsFromDatabase();
            return pro;

        } catch (SQLIntegrityConstraintViolationException e) {
            System.out.print(e.getMessage());
            JOptionPane.showMessageDialog(null, "Product ID or product's name already exists!");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Database error: " + e.getMessage());
        }

        return null;
    }
    
    public Product updateProduct(Product pro){
        if (pro == null) {
        JOptionPane.showMessageDialog(null, "Invalid product data!", "Error", JOptionPane.ERROR_MESSAGE);
        return null;
        }

        if (pro.getName() == null || pro.getName().trim().isEmpty() ||
            pro.getType() == null || pro.getType().trim().isEmpty() ||
            pro.getStatus() == null || pro.getStatus().trim().isEmpty()) {
            JOptionPane.showMessageDialog(null, "Please fill all required fields!", "Warning", JOptionPane.WARNING_MESSAGE);
            return null;
        }


        String query = "UPDATE product SET pro_name=?, pro_type=?, pro_price=?, pro_image=?, pro_stock=?, pro_status=? WHERE pro_id=?";
        try (Connection conn = brotherconnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setString(1, pro.getName());
            ps.setString(2, pro.getType());
            ps.setDouble(3, pro.getPrice());
            ps.setString(4, pro.getImage());
            ps.setInt(5, pro.getStock());
            ps.setString(6, pro.getStatus());
            ps.setInt(7, pro.getId());

            int rows = ps.executeUpdate();
            if (rows > 0) {
                JOptionPane.showMessageDialog(null, "Product updated successfully!");
                loadProductsFromDatabase();
            } else {
                JOptionPane.showMessageDialog(null, "No product found with ID " + pro.getId());
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Database error: " + e.getMessage());
        }

        return pro;
    } 
    
    public void deleteProduct(JTable table){
        int selectedRow = table.getSelectedRow();

        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(null, "Please select a product to delete!", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String query = "DELETE FROM product WHERE pro_id = ?";
        int confirm = JOptionPane.showConfirmDialog(
            null,
            "Are you sure you want to delete this product?",
            "Confirm Delete",
            JOptionPane.YES_NO_OPTION
        );

        if (confirm == JOptionPane.YES_OPTION) {
            DefaultTableModel model = (DefaultTableModel) table.getModel();
            int productId = (int) model.getValueAt(selectedRow, 0); 
            boolean deleted = deleteProductFromDatabase(productId);
            
            if (deleted) {
                model.removeRow(selectedRow);
                JOptionPane.showMessageDialog(null, "Product deleted successfully!");
            } else {
                JOptionPane.showMessageDialog(null, "Failed to delete product from database!", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private boolean deleteProductFromDatabase(int productId) {
        String query = "DELETE FROM product WHERE pro_id = ?";

        try (java.sql.Connection conn = brotherconnection.getConnection();
             java.sql.PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, productId);
            int rows = stmt.executeUpdate();
            return rows > 0;

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Database error: " + e.getMessage());
            return false;
        }
    }
    
    public void importImage() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Image select");
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
                "Image Files", "png", "jpg", "jpeg"));

       
        int result = fileChooser.showOpenDialog(null);

        if (result == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            imagePath = file.getAbsolutePath();

            ImageIcon icon = new ImageIcon(new ImageIcon(imagePath)
                    .getImage().getScaledInstance(195, 175, java.awt.Image.SCALE_SMOOTH));

            if (inventoryImageLabel != null) {
                inventoryImageLabel.setIcon(icon);
                inventoryImageLabel.repaint();
            } else {
                JOptionPane.showMessageDialog(null, "Label not found!");
            }
        }
    }
    
    public void loadMoreProducts(JPanel productContainer) {
        int end = Math.min(loadedCount + batchSize, list.size());

        for (int i = loadedCount; i < end; i++) {
            ProductItemPanel item = new ProductItemPanel(list.get(i));
            productContainer.add(item);
        }

        loadedCount = end;
        productContainer.revalidate();
        productContainer.repaint();
    }

    
    public void enableAutoLoad() {
        scrollPane.getVerticalScrollBar().addAdjustmentListener(e -> {
            JScrollBar bar = scrollPane.getVerticalScrollBar();
            int extent = bar.getModel().getExtent();
            int value = bar.getValue();
            int max = bar.getMaximum();

            if (value + extent >= max - 50) {
                loadMoreProducts(productContainer);
            }
    });
}


    public String getImagePath() {
        return imagePath;
    }
    
    //Changes : update table
    public void addProductToTable(int productId, int quantity) {
    String sql = "SELECT pro_name, pro_type, pro_price, pro_stock FROM product WHERE pro_id = ?";

    try (Connection con = brotherconnection.getConnection();
         PreparedStatement ps = con.prepareStatement(sql)) {

        ps.setInt(1, productId);
        ResultSet rs = ps.executeQuery();

        if (rs.next()) {
            String name = rs.getString("pro_name");
            String type = rs.getString("pro_type");
            double price = rs.getDouble("pro_price");
            
            int stock = rs.getInt("pro_stock");
            // Check if stock is enough
            
            if (quantity > stock ) {
                JOptionPane.showMessageDialog(null, 
                    "Not enough stock for product: " + name + "\nAvailable: " + stock, 
                    "Stock Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            
            DefaultTableModel model = (DefaultTableModel) place_table.getModel();
            boolean found = false;

            // 🔁 Check if product is already in the table
            for (int i = 0; i < model.getRowCount(); i++) {
                int currentId = Integer.parseInt(model.getValueAt(i, 0).toString());
                if (currentId == productId) {
                    // If found, update quantity and subtotal
                    int currentQty = Integer.parseInt(model.getValueAt(i, 3).toString());
                    int newQty = currentQty + quantity;
                    double newSubtotal = newQty * price;

                    model.setValueAt(newQty, i, 3);       // Update quantity
                    model.setValueAt(newSubtotal, i, 5);  // Update subtotal
                    found = true;
                    break;
                }
            }

            // ➕ If not found, add as new row
            if (!found) {
                double subtotal = price * quantity;
                model.addRow(new Object[]{
                    productId,
                    name,
                    type,
                    quantity,
                    price,
                    subtotal
                });
            }

            updateTotal(); // Update total after adding/updating row
        }

    } catch (Exception e) {
        e.printStackTrace();
        JOptionPane.showMessageDialog(null, "Error adding product: " + e.getMessage());
    }
}
    
    // changes: set text total
    private void updateTotal() {
    DefaultTableModel model = (DefaultTableModel) place_table.getModel();
    double total = 0;

    for (int i = 0; i < model.getRowCount(); i++) {
        Object obj = model.getValueAt(i, 5); // subtotal
        double subtotal = obj instanceof Number ? ((Number) obj).doubleValue() : 0;
        total += subtotal;
    }
    
    NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
    Total.setText(currencyFormat.format(total));
}


    
    // changes: Setup delete button listener
    private void setupDeleteButton() {
    if (orderPanel != null && orderPanel.getDeleteButton() != null) {
        orderPanel.getDeleteButton().addActionListener(e -> deleteSelectedRow());
    }
}
    // changes: Setup delete button listener
    private void setupResetButton() {
    if (orderPanel != null && orderPanel.getResetButton() != null) {
        orderPanel.getResetButton().addActionListener(e -> ResetTable());
    }
}

    //changes: delete row
    private void deleteSelectedRow() {
    JTable placeTable = orderPanel.getPlaceTable();
    DefaultTableModel model = (DefaultTableModel) placeTable.getModel();
    int selectedRow = placeTable.getSelectedRow();

    if (selectedRow >= 0) {
        model.removeRow(selectedRow);   // Remove row
        updateTotal();                  // Update total after deletion
    } else {
        JOptionPane.showMessageDialog(null, "Please select a row to delete!", "Warning", JOptionPane.WARNING_MESSAGE);
    }
}
    //changes: reset table
    private void ResetTable(){
        JTable placeTable = orderPanel.getPlaceTable();
        DefaultTableModel model = (DefaultTableModel) placeTable.getModel();
        
        model.setRowCount(0); // Clear the table
        updateTotal();  
    }

    //changes: order and print button
    public void handleOrderAndPrint() {
    DefaultTableModel model = (DefaultTableModel) place_table.getModel();

    if (model.getRowCount() == 0) {
        JOptionPane.showMessageDialog(null, "No items in the order!", "Warning", JOptionPane.WARNING_MESSAGE);
        return;
    }

    StringBuilder receipt = new StringBuilder();
    receipt.append("************ Brother Chicken ***********\n");
    receipt.append("----------------------------------------\n");
    receipt.append(String.format("%-20s %-5s %s\n", "Item", "Qty", "Price"));
    receipt.append("----------------------------------------\n");

    double total = 0;

    for (int i = 0; i < model.getRowCount(); i++) {
        String item = model.getValueAt(i, 1).toString();  // Name
        int qty = (int) model.getValueAt(i, 3);           // Quantity
        double subtotal = (double) model.getValueAt(i, 5); // Subtotal as double

        receipt.append(String.format("%-20s %-5d %,.0f\n", item, qty, subtotal));
        total += subtotal;
    }

    receipt.append("----------------------------------------\n");
    receipt.append(String.format("TOTAL: %,.0f VND\n", total));
    receipt.append("----------------------------------------\n");
    receipt.append("Thanks for choosing us!\n");

    new BillFrame(receipt.toString(), orderPanel) {{
        setLocationRelativeTo(null);
        setDefaultCloseOperation(javax.swing.JFrame.DISPOSE_ON_CLOSE);
        setVisible(true);
    }};

    
}

    //changes: update items in bill, bill_items
    public void saveBillToDatabase(DefaultTableModel model, double total) {
    try (Connection conn = brotherconnection.getConnection()) {
        conn.setAutoCommit(false);

        // insert into bill table
        String sqlBill = "INSERT INTO bill (bill_date, total_amount) VALUES (NOW(), ?)";
        PreparedStatement psBill = conn.prepareStatement(sqlBill, Statement.RETURN_GENERATED_KEYS);
        psBill.setDouble(1, total);
        psBill.executeUpdate();

        ResultSet rs = psBill.getGeneratedKeys();
        int billId = -1;
        if (rs.next()) {
            billId = rs.getInt(1);
        }

        // insert into bill_items
        String sqlItem = "INSERT INTO bill_items (bill_id, product_id, product_name, qty, subtotal) VALUES (?, ?, ?, ?, ?)";
        PreparedStatement psItem = conn.prepareStatement(sqlItem);

        // update product stock
        String sqlUpdateStock = "UPDATE product SET pro_stock = pro_stock - ? WHERE pro_id = ?";
        PreparedStatement psStock = conn.prepareStatement(sqlUpdateStock);

        for (int i = 0; i < model.getRowCount(); i++) {
            int productId = Integer.parseInt(model.getValueAt(i, 0).toString());
            String productName = model.getValueAt(i, 1).toString();
            int qty = Integer.parseInt(model.getValueAt(i, 3).toString());
            double subtotal = Double.parseDouble(model.getValueAt(i, 5).toString());

            // Insert bill item
            psItem.setInt(1, billId);
            psItem.setInt(2, productId);
            psItem.setString(3, productName);
            psItem.setInt(4, qty);
            psItem.setDouble(5, subtotal);
            psItem.addBatch();

            // Reduce stock
            psStock.setInt(1, qty);
            psStock.setInt(2, productId);
            psStock.addBatch();
        }

        psItem.executeBatch();
        psStock.executeBatch();
        conn.commit();

    } catch (SQLException e) {
        e.printStackTrace();
        JOptionPane.showMessageDialog(null, "Error saving bill: " + e.getMessage());
    }
    
    model.setRowCount(0); // Clear the table
    updateTotal();        // Reset total
}
}

    
