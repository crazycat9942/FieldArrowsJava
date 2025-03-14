import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.awt.geom.Point2D;

public class Main extends JComponent//screenX/Y is the pixel position, coordX/Y is the math coordinate position
{
    int window_screenX = 1600;
    int window_screenY = 900;
    boolean scaleVectors = false;
    static boolean running = true;
    private static JFrame frame = new JFrame("shitter");
    boolean userPressed = false;
    Point lastPoint;

    public Main()
    {
        //JPopupMenu epilepsyWarning = new JPopupMenu("! ! ! Epilepsy warning ! ! !");
        frame.setSize(window_screenX, window_screenY);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        Panel panel = new Panel();
        frame.add(panel);
        frame.add(panel.menu.getContentPane(), BorderLayout.EAST);
        frame.pack();
        frame.setBackground(Color.black);
        frame.setVisible(true);
    }

}
class Panel extends JPanel {
    ArrayList<Arrow> arrows = new ArrayList<>();
    Timer timer;
    Graphics g;
    double time = 0;
    double window_screenX = 1600;
    double window_screenY = 900;
    Menu menu = new Menu(this);
    double window_maxCoordX = 5*menu.zoomSlider.getValue();
    double maxMagnitude = -1;
    double maxCurl = -1;
    double maxDivergence = -1;
    double window_maxCoordY = menu.zoomSlider.getValue()*window_maxCoordX*window_screenY/window_screenX;
    Point userMouse;
    Axes axes = new Axes(this);
    int arrowsX = 30;
    int arrowsY = 30;
    Panel()
    {
        setBackground(Color.BLACK);
        setForeground(Color.WHITE);

        refreshScreen();
        setSize((int)window_screenX, (int)window_screenY);
        for(double i = -1*window_maxCoordX; i < window_maxCoordX; i+= window_maxCoordX/arrowsX)
        {
            for(double j = -1*window_maxCoordY; j < window_maxCoordY; j+= window_maxCoordY/arrowsY)
            {
                arrows.add(new Arrow(i, j, this));
            }
        }
    }
    public void updateArrows()
    {
        for(Arrow i: arrows)
        {
            i.update(g, this);
        }
    }
    public Point2D.Double getVector(double coordX, double coordY)
    {
        //here's where you put the function the vector field is representing
        Expression pExpression = new ExpressionBuilder(menu.P.getText())
                .variables("x", "y", "t")
                .build()
                .setVariable("x", coordX)
                .setVariable("y", coordY)
                .setVariable("t", time);
        Expression qExpression = new ExpressionBuilder(menu.Q.getText())
                .variables("x", "y", "t")
                .build()
                .setVariable("x", coordX)
                .setVariable("y", coordY)
                .setVariable("t", time);
        double P = pExpression.evaluate();//x component of vector field
        double Q = qExpression.evaluate();//y component of vector field
        //double P = coordY/(Math.pow(coordX, 2) + Math.pow(coordY, 2)) + Math.cos(time);
        //double Q = -coordX/(Math.pow(coordX, 2) + Math.pow(coordY, 2)) + Math.sin(time);
        double magnitude = Math.pow(P, 2) + Math.pow(Q, 2);
        double angle = -Math.atan2(Q, P);
        return new Point2D.Double(magnitude, angle);
    }
    public void vector_to_rgb(Arrow i)
    {
        if(maxMagnitude == -1)
        {
            maxMagnitude = maxMagnitude();
        }
        if(maxCurl == -1)
        {
            maxCurl = maxCurl();
        }
        if(maxDivergence == -1)
        {
            maxDivergence = maxDivergence();
        }
        i.angle = i.angle % (2 * Math.PI);
        if (i.angle < 0)
        {
            i.angle += 2 * Math.PI;
        }
        /*Point2D arrowDX = getVector(i.coordX + 0.001, i.coordY);
        Point2D arrowDY = getVector(i.coordX, i.coordY + 0.001);
        double DPDX = (arrowDX.getX()*Math.cos(arrowDX.getY()) - i.magnitude*Math.cos(i.angle))/0.001;//dP/dx
        double DPDY = (arrowDY.getX()*Math.cos(arrowDY.getY()) - i.magnitude*Math.cos(i.angle))/0.001;//dP/dy
        double DQDX = (arrowDX.getX()*Math.sin(arrowDX.getY()) - i.magnitude*Math.sin(i.angle))/0.001;//dQ/dx
        double DQDY = (arrowDY.getX()*Math.sin(arrowDY.getY()) - i.magnitude*Math.sin(i.angle))/0.001;//dQ/dy*/
        //System.out.println(maxMagnitude + " " + i.magnitude + " " + maxCurl + " " + i.curl);
        float magScale = (float)(i.magnitude / maxMagnitude);
        if(!menu.scaleWithMag.isSelected())
        {
            magScale = 1;
        }
        if(menu.colorWithCurl.isSelected())
        {
            i.color = hslToRGB((float)  (180 * i.curl / maxCurl + 180) * magScale, (float) ((Math.abs(i.curl) / maxCurl)) * magScale, (float) ((Math.abs(i.curl) / maxCurl)) * magScale);
        }
        else if(menu.colorWithDiv.isSelected())
        {
            i.color = hslToRGB((float) (180 * i.divergence / maxDivergence + 180) * magScale, (float) ((Math.abs(i.divergence) / maxDivergence)) * magScale, (float) ((Math.abs(i.divergence) / maxDivergence)) * magScale);
        }
        else
        {
            //System.out.println("asid");
            i.color = hslToRGB((float)((180f*i.angle / Math.PI)), (float) (i.magnitude / maxMagnitude), (float) (i.magnitude / maxMagnitude));
        }
        //System.out.println(maxMagnitude + " " + i.magnitude + " " + (float)(i.magnitude/maxMagnitude));
        //i.color = hslToRGB((float)(180f*i.angle/Math.PI), (float)(0.99 + 0*(Math.abs(i.curl)/maxCurl)*(i.magnitude/maxMag)), (float) (0.99 + 0*(Math.abs(i.curl)/maxCurl)*(i.magnitude/maxMag)));
        //i.color = hslToRGB((float)(((360*i.curl) / maxCurl) + 360), (float) (i.magnitude / maxMag), (float) (i.magnitude / maxMag));
    }
    public Color hslToRGB(float h, float s, float l)
    {//h is [0,360) (degrees), s is [0, 1] l is [0, 1]
        //System.out.println(h + " " + s + " " + l);
        //l = 0.8f*l + 0.2f;
        if(s >= 1){s = 0.9999f;}
        if(l >= 1){l = 0.9999f;}
        s = 0.7f*s + 0.29999f;
        float C = (1-Math.abs(2*l - 1f))*s;
        float H_ = h/60;
        float X = C*(1-Math.abs(H_%2f - 1));
        float m = l - (C/2f);
        float R1;
        float G1;
        float B1;

        if(0f <= H_ && H_ < 1f){R1 = C; G1 = X; B1 = 0f;}
        else if(1f <= H_ && H_ < 2f){R1 = X; G1 = C; B1 = 0f;}
        else if(2f <= H_ && H_ < 3f){R1 = 0f; G1 = C; B1 = X;}
        else if(3f <= H_ && H_ < 4f){R1 = 0f; G1 = X; B1 = C;}
        else if(4f <= H_ && H_ < 5f){R1 = X; G1 = 0f; B1 = C;}
        else{R1 = C; G1 = 0f; B1 = X;}//(5f <= H_ && H_ < 6f
        return new Color(R1 + m, G1 + m, B1 + m);
    }
    public double getCurl(double x, double y)
    {
        Point2D arrowDX = getVector(x + 0.001, y);
        Point2D arrowDY = getVector(x, y + 0.001);
        Point2D arrowInit = getVector(x, y);
        double DPDX = (arrowDX.getX()*Math.cos(arrowDX.getY()) - arrowInit.getX()*Math.cos(arrowInit.getY()))/0.001;//dP/dx
        //System.out.println(arrowDX.toString() + " " + DPDX + " " + arrowInit.toString());
        double DPDY = (arrowDY.getX()*Math.cos(arrowDY.getY()) - arrowInit.getX()*Math.cos(arrowInit.getY()))/0.001;//dP/dy
        double DQDX = (arrowDX.getX()*Math.sin(arrowDX.getY()) - arrowInit.getX()*Math.sin(arrowInit.getY()))/0.001;//dQ/dx
        double DQDY = (arrowDY.getX()*Math.sin(arrowDY.getY()) - arrowInit.getX()*Math.sin(arrowInit.getY()))/0.001;//dQ/dy
        return DQDX - DPDY;
    }
    public double getDiv(double x, double y)
    {
        Point2D arrowDX = getVector(x + 0.001, y);
        Point2D arrowDY = getVector(x, y + 0.001);
        Point2D arrowInit = getVector(x, y);
        double DPDX = (arrowDX.getX()*Math.cos(arrowDX.getY()) - arrowInit.getX()*Math.cos(arrowInit.getY()))/0.001;//dP/dx
        //System.out.println(arrowDX.toString() + " " + DPDX + " " + arrowInit.toString());
        double DPDY = (arrowDY.getX()*Math.cos(arrowDY.getY()) - arrowInit.getX()*Math.cos(arrowInit.getY()))/0.001;//dP/dy
        double DQDX = (arrowDX.getX()*Math.sin(arrowDX.getY()) - arrowInit.getX()*Math.sin(arrowInit.getY()))/0.001;//dQ/dx
        double DQDY = (arrowDY.getX()*Math.sin(arrowDY.getY()) - arrowInit.getX()*Math.sin(arrowInit.getY()))/0.001;//dQ/dy
        return DPDX + DQDY;
    }
    public double getMag(double x, double y)
    {
        return getVector(x, y).x;
    }
    public double maxMagnitude()
    {
        double max = 0;
        for(Arrow i: arrows)
        {
            if(i.magnitude > max)
            {
                max = i.magnitude;
            }
        }
        return max;
    }
    public double maxCurl()
    {
        double max = 0;
        for(Arrow i: arrows)
        {
            if(Math.abs(i.curl) > max)
            {
                max = Math.abs(i.curl);
            }
        }
        return max;
    }
    public double maxDivergence()
    {
        double max = 0;
        for(Arrow i: arrows)
        {
            if(Math.abs(i.divergence) > max)
            {
                max = Math.abs(i.divergence);
            }
        }
        return max;
    }
    @Override
    protected void paintComponent(Graphics gInit) {
        super.paintComponent(gInit);
        window_maxCoordX = 5*menu.zoomSlider.getValue();
        window_maxCoordY = window_maxCoordX*window_screenY/window_screenX;
        g = gInit;
        updateArrows();
        axes.update(g);
        g.setFont(new Font("Calibri", Font.PLAIN, 30));
        g.setColor(new Color(120, 0, 200));
        userMouse = MouseInfo.getPointerInfo().getLocation();
        Point2D.Double forceAtMouse = getVector(userMouse.getX(), userMouse.getY());
        /*if(!Double.isNaN(forceAtMouse.getX()))
        {
            double shortenedForce = BigDecimal.valueOf(forceAtMouse.getX()).round(new MathContext(5)).doubleValue();
            g.setColor(Color.white);
            g.fillRect(0,0,350,90);
            g.setColor(new Color(120, 0, 200));
            g.drawString("Force at cursor: " + shortenedForce + " N", 30, 30);
            g.drawString("Curl at cursor: " + BigDecimal.valueOf(getCurl(axes.screenToCoordsX(userMouse.x), axes.screenToCoordsY(userMouse.y))).round(new MathContext(5)).doubleValue(), 30, 80);
        }
        else
        {
            g.drawString("Force at cursor: Infinity N", 30, 50);
        }*/

        g.setColor(Color.white);
        g.setFont(new Font("Calibri", Font.PLAIN, 15));
        //g.drawString(time + " time (s)", 50, 870);
        menu.update(g);
        maxCurl = -1;
        maxDivergence = -1;
        maxMagnitude = -1;
        time += 0.049;

    }
    public void refreshScreen() {
        timer = new Timer(0, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                repaint();
            }
        });
        timer.setRepeats(true);
        // Aprox. 60 FPS
        timer.setDelay(17);
        timer.start();
    }
    @Override
    public Dimension getPreferredSize() {
        return new Dimension((int)window_screenX, (int)window_screenY);
    }
}