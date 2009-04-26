import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.filechooser.FileNameExtensionFilter;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;


public class ShpToOsmGUI {

    private JFrame f;
    protected File fileToConvert;
    private JLabel fileNameLabel;

    public ShpToOsmGUI() {
        initGUI();
    }

    private void initGUI() {
        f = new JFrame("SHP to OSM");
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JPanel cp = new JPanel(new BorderLayout());
        
        // File chooser on the top bar
        JPanel fileChooserPanel = new JPanel(new BorderLayout());
        fileChooserPanel.add(new JLabel("Input Shapefile:"), BorderLayout.BEFORE_LINE_BEGINS);
        fileNameLabel = new JLabel("None");
        fileChooserPanel.add(fileNameLabel, BorderLayout.CENTER);
        JButton browseButton = new JButton("Browse...");
        browseButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JFileChooser fc = new JFileChooser();
                fc.setFileFilter(new FileNameExtensionFilter("Shapefile", "shp"));
                fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
                int retVal = fc.showOpenDialog(f);
                if(retVal == JFileChooser.APPROVE_OPTION) {
                    fileToConvert = fc.getSelectedFile();
                    updateFileLabel();
                }
            }
        });
        fileChooserPanel.add(browseButton, BorderLayout.AFTER_LINE_ENDS);
        cp.add(fileChooserPanel, BorderLayout.NORTH);
        
        // Rules set in the middle
        RulesPanel rulesPanel = new RulesPanel();
        cp.add(rulesPanel, BorderLayout.CENTER);
        
        // "Go" button on the bottom
        JPanel buttonPanel = new JPanel(new BorderLayout());
        cp.add(buttonPanel, BorderLayout.SOUTH);
        
        f.setContentPane(cp);
    }

    protected void updateFileLabel() {
        if (fileToConvert != null) {
            try {
                fileNameLabel.setText(fileToConvert.getCanonicalPath());
            } catch (IOException e) {
                e.printStackTrace();
                fileNameLabel.setText("None");
            }
        } else {
            fileNameLabel.setText("None");
        }
    }

    public void start() {
        f.pack();
        f.setVisible(true);
    }
}
