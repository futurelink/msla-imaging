package futurelink.msla.ui;

import javax.swing.*;
import java.awt.*;

public class MainWindow extends JFrame {
    private ProjectPanel projectPanel;
    private JPanel mainPanel;

    private JToolBar toolBar;

    public MainWindow() {
        setTitle("mSLA PCB Toolbox");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(800, 600));
        setSize( 800, 600 );
        setMenuBar(createMenuBar());

        var layout = new BorderLayout();
        setLayout(layout);

        // Create toolbar
        toolBar = new JToolBar("Still draggable");
        toolBar.setFloatable( false);
        var addSVGBtn = new JButton("Add SVG");
        toolBar.add(addSVGBtn);
        var addPNGBtn = new JButton("Add PNG");
        toolBar.add(addPNGBtn);
        add(toolBar, BorderLayout.PAGE_START);
        var printerChoice = new JComboBox<String>();
        // PhotonWorkshopFileDefaults.getSupported().forEach(printerChoice::addItem);
        add(printerChoice);
        toolBar.add(printerChoice);
        var exportBtn = new JButton("Export");
        toolBar.add(exportBtn);

        // Create main area
        projectPanel = new ProjectPanel();
        mainPanel = new JPanel();
        add(new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, projectPanel, mainPanel));

        pack();
        setVisible(true);

        projectPanel.setProject(new Project());
    }

    private MenuBar createMenuBar() {
        var menuBar = new MenuBar();
        var fileMenu = new Menu("File");
        fileMenu.add(new MenuItem("Exit"));
        menuBar.add(fileMenu);
        return menuBar;
    }
}
