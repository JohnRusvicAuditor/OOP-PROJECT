package library.ui;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JTabbedPane;

import library.service.LibraryService;

/** The main application window: a menu bar plus one tab per area of the system. */
public class MainFrame extends JFrame {

    public MainFrame(LibraryService service) {
        super("Library Management System");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setJMenuBar(buildMenuBar());
        add(buildTabs(service));
        setSize(1100, 700);
        setLocationRelativeTo(null);
    }

    /**
     * Builds the tabs. Kept separate (and public) so the tabs can be created
     * without opening a window, e.g. to generate documentation screenshots.
     */
    public static JTabbedPane buildTabs(LibraryService service) {
        JTabbedPane tabs = new JTabbedPane();
        List<RefreshablePanel> panels = new ArrayList<>();
        Runnable refreshAll = () -> panels.forEach(RefreshablePanel::refresh);

        panels.add(new ItemsPanel(service, refreshAll));
        panels.add(new MembersPanel(service, refreshAll));
        panels.add(new CirculationPanel(service, refreshAll));
        panels.add(new ReportsPanel(service, refreshAll));

        tabs.addTab("Items", panels.get(0));
        tabs.addTab("Members", panels.get(1));
        tabs.addTab("Borrow / Return", panels.get(2));
        tabs.addTab("Reports", panels.get(3));

        // Polymorphism: every tab is a RefreshablePanel, each refreshes itself its own way.
        tabs.addChangeListener(e -> ((RefreshablePanel) tabs.getSelectedComponent()).refresh());
        refreshAll.run();
        return tabs;
    }

    private JMenuBar buildMenuBar() {
        JMenu fileMenu = new JMenu("File");
        JMenuItem exit = new JMenuItem("Exit");
        exit.addActionListener(e -> dispose());
        fileMenu.add(exit);

        JMenu helpMenu = new JMenu("Help");
        JMenuItem about = new JMenuItem("About");
        about.addActionListener(e -> JOptionPane.showMessageDialog(this,
                "Library Management System\nJava OOP Project - Encapsulation, Inheritance,\n"
                        + "Polymorphism and Abstraction.",
                "About", JOptionPane.INFORMATION_MESSAGE));
        helpMenu.add(about);

        JMenuBar bar = new JMenuBar();
        bar.add(fileMenu);
        bar.add(helpMenu);
        return bar;
    }
}
