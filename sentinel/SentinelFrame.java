import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import dirwatch.ConfigIO;
import dirwatch.DirWatcher;
import dirwatch.PathIO;
import java.awt.*;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.*;
import java.nio.file.*;
import java.util.Date;
import java.util.List;
import java.util.ArrayList;
import java.util.Calendar;
import java.io.File;
import java.io.IOException;

public class SentinelFrame extends JFrame {

    private JPanel panel;
    private JButton selectDirButton;
    private JButton startButton;
    private String newDir;
    private ArrayList<Path> dirsToWtach;
    private String[] event;
    private PathIO pathIO;
    private JTable outputTable;
    private DefaultTableModel tableModel;
    private DefaultListModel<Path> listModel;
    private JList<Path> pathList;
    private JButton removeDirButton;
    private ConfigIO config;
    private JPanel northButtonPanel;
    private JPanel southButtonPanel;
    private JPanel pathListPanel;
    private JPanel outputPanel;

    private DefaultTableCellRenderer cellRenderer;
    private boolean watching;

    public SentinelFrame() {
        pathIO = new PathIO(new File("config/paths"));
        config = new ConfigIO("config/config");
        dirsToWtach = pathIO.getPaths();
        watching = false;
        // setSize(300, 300);
        setLocation(400, 0);
        setLayout(new GridLayout(1, 1));
        setResizable(false);
        setTitle("Wtach Bot");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        initComponents();
    }

    private void initComponents() {
        selectDirButton = new JButton("Add Folder");
        removeDirButton = new JButton("Remove Folder");
        selectDirButton.setPreferredSize(removeDirButton.getPreferredSize());
        startButton = new JButton("Start");
        startButton.setPreferredSize(removeDirButton.getPreferredSize());

        panel = new JPanel(new GridBagLayout());
        northButtonPanel = new JPanel();
        northButtonPanel.setLayout(new GridLayout(2,1));
        southButtonPanel = new JPanel();
        pathListPanel = new JPanel(new BorderLayout());
        outputPanel = new JPanel(new BorderLayout());

        tableModel = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
               //all cells false
               return false;
            }
        };
        
        outputTable = new JTable(tableModel);
        tableModel.addColumn("Event");
        tableModel.addColumn("File");
        tableModel.addColumn("Timestamp");

        outputTable.setFillsViewportHeight(true);

        outputTable.getColumnModel().getColumn(0).setPreferredWidth(100);
        outputTable.getColumnModel().getColumn(1).setPreferredWidth(300);
        outputTable.getColumnModel().getColumn(0).setPreferredWidth(20);

        cellRenderer = new DefaultTableCellRenderer();
        cellRenderer.setHorizontalAlignment(JLabel.CENTER);
        outputTable.getColumnModel().getColumn(0).setCellRenderer(cellRenderer);

        listModel = new DefaultListModel<>();
        pathList = new JList<Path>(listModel);
        pathList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        for (int i = 0; i < dirsToWtach.size(); i++) 
            listModel.addElement(dirsToWtach.get(i));
        pathList.setSelectedIndex(0);

        ActionListener selectDirListener = new SelectDirListener();
        selectDirButton.addActionListener(selectDirListener);

        ActionListener removeeDirListener = new removeDirListener();
        removeDirButton.addActionListener(removeeDirListener);

        ActionListener startListener = new StartListener();
        startButton.addActionListener(startListener);

        pathListPanel.add(new JScrollPane(pathList), BorderLayout.CENTER);
        pathListPanel.setBorder(BorderFactory.createTitledBorder("Watchlist"));
        pathListPanel.setPreferredSize(new Dimension(700, 150));

        outputPanel.add(new JScrollPane(outputTable), BorderLayout.CENTER);
        outputPanel.setBorder(BorderFactory.createTitledBorder("Output"));
        outputPanel.setPreferredSize(new Dimension(700, 450));

        northButtonPanel.add(selectDirButton);
        northButtonPanel.add(removeDirButton);
        northButtonPanel.setBorder(BorderFactory.createEmptyBorder(17, 10, 10, 10));

        southButtonPanel.add(startButton);
        southButtonPanel.setBorder(BorderFactory.createEmptyBorder(12, 10, 10, 10));

        GridBagConstraints gbc = new GridBagConstraints();

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(pathListPanel, gbc);

        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.NORTH;
        panel.add(northButtonPanel, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.CENTER;
        panel.add(outputPanel, gbc);

        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.NORTH;
        panel.add(southButtonPanel, gbc);

        add(panel);
        pack();
    }

    public class SelectDirListener implements ActionListener {
        public void actionPerformed(ActionEvent ev) {
            JFileChooser fc = new JFileChooser();
            fc.setDialogTitle("Choose Folder");
            fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            try {
                fc.setCurrentDirectory(new File(config.getProperty("prev_path")));
            } catch (NullPointerException e) {}
            if (fc.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                newDir = "" + fc.getSelectedFile();
                pathIO.addPath(newDir);
                try {config.setProperty("prev_path", newDir);} catch (IOException e) {}
                dirsToWtach.add(Paths.get(newDir));
                listModel.addElement(Paths.get(newDir));
            }
        }
    }

    public class removeDirListener implements ActionListener {
        public void actionPerformed(ActionEvent ev) {
            List<Path> dirToRemove = pathList.getSelectedValuesList();
            for (Path path : dirToRemove) {
                pathIO.deletePath(path.toString());
                dirsToWtach.remove(path);
                listModel.removeElement(path);
            }
        }
    }

    public class StartListener implements ActionListener {

        private Thread dirWatcherhandler;

        public void actionPerformed(ActionEvent ev) {
            if (!watching) {
            watching = true;
            dirWatcherhandler = new DirWatcherHandler();
            dirWatcherhandler.start();
                Date currentTime = Calendar.getInstance().getTime();
                tableModel.addRow(new Object[]{"Watching...", "", currentTime});
            startButton.setText("Stop");
            } else {
                watching = false;
                dirWatcherhandler.interrupt();
                Date currentTime = Calendar.getInstance().getTime();
                tableModel.addRow(new Object[]{"Stopped", "", currentTime});
                startButton.setText("Start");
            }
        }
    }

    public class DirWatcherHandler extends Thread {

        private Thread outputHandler;

        public void run() {
            DirWatcher dw = new DirWatcher();
            while (watching) {
                event = dw.watchDirs(dirsToWtach);
                if (!(event == null)) {   
                    outputHandler = new OutputHandler();
                    outputHandler.start();
                }
            }
        }
    }

    public class OutputHandler extends Thread {
        public void run() {
            tableModel.addRow(new Object[]{event[1], event[2], event[0]});
        }
    }
}