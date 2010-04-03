package com.yellowbkpk.geo.shp;
import javax.swing.BorderFactory;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultCellEditor;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;


public class RulesPanel extends JPanel {

    private JButton addButton;
    private JButton editButton;
    private JButton removeButton;
    private RuleTableModel rulesTableModel = new RuleTableModel();
    private JTable rulesList;

    public RulesPanel() {
        super(new BorderLayout());
        setBorder(BorderFactory.createTitledBorder("Rules"));
        initGUI();
    }

    private void initGUI() {
        // List of rules on the top
        rulesList = new JTable(rulesTableModel);
        rulesList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        rulesList.setShowVerticalLines(false);
        rulesList.setTableHeader(null);

        JComboBox shpTypesCombo = new JComboBox();
        shpTypesCombo.addItem("outer");
        shpTypesCombo.addItem("inner");
        shpTypesCombo.addItem("line");
        shpTypesCombo.addItem("point");
        rulesList.getColumnModel().getColumn(1).setCellEditor(new DefaultCellEditor(shpTypesCombo));
        
        JTextField tagKeyTextBox = new JTextField();
        rulesList.getColumnModel().getColumn(3).setCellEditor(new DefaultCellEditor(tagKeyTextBox));
        
        JTextField tagValTextBox = new JTextField();
        rulesList.getColumnModel().getColumn(5).setCellEditor(new DefaultCellEditor(tagValTextBox));
        
        JScrollPane rulesScrollPane = new JScrollPane(rulesList);
        rulesScrollPane.setPreferredSize(new Dimension(400, 600));
        add(rulesScrollPane, BorderLayout.CENTER);
        
        // Add, edit, and remove buttons on the bottom
        JPanel buttonPanel = new JPanel(new GridLayout(1,3));
        addButton = new JButton("Add");
        addButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                addNewRule();
            }
        });
        buttonPanel.add(addButton);
        
        editButton = new JButton("Edit");
        editButton.setEnabled(false);
        editButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                editCurrentRule();
            }
        });
        buttonPanel.add(editButton);
        
        removeButton = new JButton("Remove");
        removeButton.setEnabled(false);
        removeButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                removeCurrentRule();
            }
        });
        buttonPanel.add(removeButton);
        
        add(buttonPanel, BorderLayout.SOUTH);
    }

    protected void removeCurrentRule() {
        
    }

    protected void editCurrentRule() {
        
    }

    protected void addNewRule() {
    }
}

class RuleTableModel extends AbstractTableModel {

    private List<Rule> rules = new ArrayList<Rule>();
    
    public void addRule(Rule r) {
        rules.add(r);
        fireTableDataChanged();
    }
    
    public int getColumnCount() {
        return 6;
    }

    public int getRowCount() {
        return rules.size();
    }
    
    public Object getValueAt(int rowIndex, int columnIndex) {
        switch (columnIndex) {
        case 0:
            return "When I see a";
        case 1:
            return "outer";
        case 2:
            return "apply the tag";
        case 4:
            return "with value";
        default:
            return null;
        }
    }
    
}