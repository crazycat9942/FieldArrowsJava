import org.nfunk.jep.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class Main extends JComponent//screenX/Y is the pixel position, coordX/Y is the math coordinate position
{
    static int window_screenX = 1600;
    static int window_screenY = 900;
    boolean scaleVectors = false;
    static boolean running = true;
    private static JFrame frame = new JFrame("adjoiwj");
    boolean userPressed = false;
    public static void main(String[] args)
    {
        //JPopupMenu epilepsyWarning = new JPopupMenu("! ! ! Epilepsy warning ! ! !");
        frame.setSize(window_screenX, window_screenY);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        Panel panel = new Panel(window_screenX, window_screenY);
        frame.add(panel);
        frame.add(panel.menu.getContentPane(), BorderLayout.EAST);
        frame.pack();
        frame.setBackground(Color.black);
        frame.setVisible(true);
    }

}
