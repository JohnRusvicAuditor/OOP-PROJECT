package library;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import library.service.LibraryService;
import library.service.SampleData;
import library.ui.MainFrame;

/** Entry point of the Library Management System. */
public class Main {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {
                // fall back to the default look and feel
            }
            LibraryService service = new LibraryService();
            SampleData.load(service);
            new MainFrame(service).setVisible(true);
        });
    }
}
