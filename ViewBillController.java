/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Controller;

import javax.swing.JTable;
import DAO.BillDAO;
import DAO.BillItemDAO;
import Model.Bill;
import Model.BillItem;
import UI.ViewBillPanel;
import java.security.Timestamp;
import java.util.List;
import javax.swing.JTextArea;
import javax.swing.table.DefaultTableModel;
/**
 *
 * @author thaibao
 */
public class ViewBillController {
    private ViewBillPanel view;
    
    public ViewBillController(ViewBillPanel view){
        this.view = view;
        
        
        loadBillTable();
        addRowClickListener();
    }
    
    
    private void loadBillTable(){
        JTable table = view.getBillTable();
        
        DefaultTableModel model = (DefaultTableModel) table.getModel();
        model.setRowCount(0);
        
        BillDAO dao = new BillDAO();
        List<Bill> bills = dao.getAllBills();
        
        for (Bill b : bills) {
            java.sql.Timestamp ts = b.getDate();

            String date = ts.toLocalDateTime().toLocalDate().toString();  // YYYY-MM-DD
            String time = ts.toLocalDateTime().toLocalTime().toString();
            model.addRow(new Object[]{
                b.getId(),
                date,
                time,
                b.getTotal()
            });
        }
        
    }
    
    private void addRowClickListener() {
    view.getBillTable().addMouseListener(new java.awt.event.MouseAdapter() {
        @Override
        public void mouseClicked(java.awt.event.MouseEvent evt) {
            showBillItems();  // call your function
        }
    });
}
    
    private void showBillItems() {
    JTable table = view.getBillTable();
    JTextArea detail = view.getDetailTextArea();

    int row = table.getSelectedRow();
    if (row < 0) return;

    int billId = (int) table.getValueAt(row, 0);

    BillItemDAO dao = new BillItemDAO();
    List<BillItem> items = dao.getBillItemsByBillId(billId);

    StringBuilder sb = new StringBuilder();
    sb.append("*********** Brother Chicken **********\n");
    sb.append("------------- BILL DETAILS -----------\n\n");
    sb.append(String.format("%-20s %-5s %s\n", "Item", "Qty", "Subtotal"));
    sb.append("--------------------------------------\n");

    double total = 0;

    for (BillItem it : items) {
        sb.append(String.format(
            "%-20s %-5d %,.0f\n",
            it.getProductName(),
            it.getQuantity(),
            it.getSubTotal()
        ));
        total += it.getSubTotal();
    }

    sb.append("--------------------------------------\n");
    sb.append(String.format("TOTAL: %,.0f VND\n", total));
    sb.append("Bill ID: ").append(billId).append("\n");
    sb.append("--------------------------------------\n");

    detail.setText(sb.toString());
}


}
