import com.aparapi.Kernel;
import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;
import org.apache.commons.math3.linear.MatrixUtils;
import org.nfunk.jep.*;
import org.nfunk.jep.type.Complex;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.awt.geom.Point2D;
import java.util.Arrays;
import java.util.concurrent.CountDownLatch;
import com.aparapi.Range;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;

public class Main extends JComponent//screenX/Y is the pixel position, coordX/Y is the math coordinate position
{
    int window_screenX = 1600;
    int window_screenY = 900;
    boolean scaleVectors = false;
    static boolean running = true;
    private static JFrame frame = new JFrame("adjoiwj");
    boolean userPressed = false;
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
    Array2DRowRealMatrix zoom;
    Timer timer;
    Graphics g;
    double time = 0;
    double window_screenX = 1600;
    double window_screenY = 900;
    Menu menu = new Menu(this);
    double zoomFactor = 10;
    double maxCoordX = zoomFactor;
    double minCoordX = -maxCoordX;
    double maxMagnitude = -1;
    double maxCurl = -1;
    double maxDivergence = -1;
    double maxCoordY = maxCoordX *window_screenY/window_screenX;
    double minCoordY = -maxCoordY;
    double[] origMaxMin = new double[]{maxCoordX, maxCoordY, minCoordX, minCoordY};
    Point userMouse;
    Axes axes = new Axes(this);
    int arrowsX = 40;
    int arrowsY = 40;
    JEP parser;
    double centerX = 0;
    double centerY = 0;
    Point lastPoint;
    Point2D lastCenter = new Point2D.Double(0,0);
    double lastZoom;
    //double[][] colors = new double[][]{{0,0,0},{16,63,158},{162,29,0},{80,161,68},{198,190,31}};
    int[][] colors;
    CountDownLatch latch;
    Kernel kernel1 = null;
    Range range = null;
    BufferedImage tempImage = null;
    boolean zoomChanged = true;
    boolean panChanged = true;
    Panel()
    {
        parser = new JEP();
        parser.addStandardFunctions();
        parser.addStandardConstants();
        parser.setImplicitMul(true);
        zoom = new Array2DRowRealMatrix(3,3);
        zoom.setRow(0,new double[]{1,0,0});
        zoom.setRow(1,new double[]{0,1,0});
        zoom.setRow(2,new double[]{0,0,1});
        setBackground(Color.BLACK);
        setForeground(Color.WHITE);

        refreshScreen();
        setSize((int)window_screenX, (int)window_screenY);
        for(double i = minCoordX; i < maxCoordX; i+= (maxCoordX - minCoordX)/arrowsX)
        {
            for(double j = minCoordY; j < maxCoordY; j+= (maxCoordY - minCoordY) /arrowsY)
            {
                arrows.add(new Arrow(i, j, this));
            }
        }
        this.addMouseWheelListener(new MouseWheelListener() {
            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                double z = 1/Math.pow(1.2,e.getPreciseWheelRotation());
                //System.out.println(z);
                zoomChanged = true;
                Point2D userMouse = e.getLocationOnScreen();
                Array2DRowRealMatrix newZoom = new Array2DRowRealMatrix(3,3);
                newZoom.setRow(0, new double[]{z, 0, (userMouse.getX())*(1-z)});
                newZoom.setRow(1, new double[]{0, z, (userMouse.getY())*(1-z)});
                newZoom.setRow(2, new double[]{0,0,1});
                zoom = newZoom.multiply(zoom);
                //double[] tempPoint = zoom.preMultiply(new double[]{maxCoordX, maxCoordY, 1});
                /*Array2DRowRealMatrix tempVect = new Array2DRowRealMatrix(3,1);
                tempVect.setColumn(0, new double[]{axes.coordsToScreenX(maxCoordX), axes.coordsToScreenY(maxCoordY), 1});
                tempVect = zoom.multiply(tempVect);
                double[] tempPoint = tempVect.getColumn(0);*/
                /*maxCoordX = axes.screenToCoordsX(z*(axes.coordsToScreenX(maxCoordX) - userMouse.getX()) + userMouse.getX());
                maxCoordY = axes.screenToCoordsY(z*(axes.coordsToScreenY(maxCoordY) + userMouse.getY()) - userMouse.getY());
                minCoordX = axes.screenToCoordsX(z*(axes.coordsToScreenX(minCoordX) - userMouse.getX()) + userMouse.getX());
                minCoordY = axes.screenToCoordsY(z*(axes.coordsToScreenY(minCoordY) + userMouse.getY()) - userMouse.getY());*/
                RealMatrix inverseZoom = MatrixUtils.inverse(newZoom);
                double[] tempPoint = inverseZoom.preMultiply(new double[]{1600, 900, 1});
                double[] temp1 = new double[]{screenToCoordsX(tempPoint[0]), screenToCoordsY(tempPoint[1])};
                tempPoint = inverseZoom.preMultiply(new double[]{0, 0, 1});
                double[] temp2 = new double[]{screenToCoordsX(tempPoint[0]), screenToCoordsY(tempPoint[1])};
                maxCoordX = Math.max(temp1[0], temp2[0]);
                maxCoordY = Math.max(temp1[1], temp2[1]);
                minCoordX = Math.min(temp1[0], temp2[0]);
                minCoordY = Math.min(temp1[1], temp2[1]);
                /*maxCoordX += 1;
                minCoordX += 1;
                maxCoordY += 1;
                minCoordY += 1;*/
                zoomFactor *= z;
            }
        });
        this.addMouseListener(new MouseAdapter()
        {
            public void mousePressed(MouseEvent m)
            {
                lastPoint = m.getPoint();
            }
        });
        this.addMouseMotionListener(new MouseAdapter()
        {//panning
            public void mouseDragged(MouseEvent m)//when mouse dragged
            {
                if (lastPoint != null)//if lastpoint is defined
                {
                    panChanged = true;
                    lastCenter = new Point2D.Double(centerX, centerY);
                    double addX = -(screenToCoordsX(m.getX())-screenToCoordsX(lastPoint.x));
                    double addY = -(screenToCoordsY(m.getY())-screenToCoordsY(lastPoint.y));
                    centerX += addX;
                    minCoordX += addX;
                    maxCoordX += addX;
                    centerY += addY;
                    minCoordY += addY;
                    maxCoordY += addY;
                    //frame.repaint();
                }
                lastPoint = m.getPoint();
            }
        });
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
        double P;
        double Q;
        Complex R;
        Complex S;
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
            P = pExpression.evaluate();
            Q = qExpression.evaluate();
        //double P = coordY/(Math.pow(coordX, 2) + Math.pow(coordY, 2)) + Math.cos(time);
        //double Q = -coordX/(Math.pow(coordX, 2) + Math.pow(coordY, 2)) + Math.sin(time);
        double magnitude = Math.sqrt(Math.pow(P, 2) + Math.pow(Q, 2));
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
    public void colorComplex(){
        int iStep = 10;
        int jStep = 10;
        int numIStep = (int)(window_screenX/iStep);
        int numJStep = (int)(window_screenY/jStep);
        //System.out.println(numIStep);
        /*
        Kernel kernel = new Kernel()
        {
            @Override
            public void run() {
                int i = getGlobalId(0);
                int j = getGlobalId(1);
                Complex m = new Complex(arrows.getFirst().screenToCoordsX(i * iStep), arrows.getFirst().screenToCoordsY(j * jStep));
                            Color tempColor = mandelbrotColor(m);
                            colorData[i][j] = new double[]{tempColor.getRed(), tempColor.getGreen(), tempColor.getBlue(), m.re(), m.im()};
                            //Complex temp = (Complex) parser.getVarValue("z");
                            //g2d.setColor(mandelbrotColor(m));
                            /*try {
                                wait();
                                //g2d.setColor(mandelbrotColor(m));
                                //g2d.fillRect(i * iStep, j * jStep, iStep * (i + 1), jStep * (j + 1));
                            }
                            catch(InterruptedException e) {Thread.currentThread().interrupt();}
                            notifyAll();
                                    //g.setColor(mandelbrotColor(m));
                                    //g.fillRect(i * iStep, j * jStep, iStep * (i + 1), jStep * (j + 1));
            };
        };*/
        if(menu.P.getText().contains("zm")) {
            //Range range = Range.create2D(numIStep, numJStep);
            //kernel.execute(range);
        }
        else
        {
            parser.addVariable("t", time);
            double[][][] colorData = new double[numIStep][numJStep][5];
            colors = new int[1600][900];
            for(int i = 0; i < numIStep; i++) {
                for (int j = 0; j < numJStep; j++) {
                    Complex m = new Complex(screenToCoordsX(i * iStep), screenToCoordsY(j * jStep));
                    parser.addVariable("z", m);
                    parser.parseExpression(menu.P.getText());
                    Complex R = parser.getComplexValue();
                    //System.out.println(R);
                    double argument = Math.toDegrees(Math.atan2(R.im(), R.re()));
                    if (argument < 0f) {
                        argument = argument + 360f;
                    }
                    double modulus = Math.sqrt(Math.pow(R.re(), 2) + Math.pow(R.im(), 2));
                    modulus = (float) Math.pow((1f - Math.exp(-0.02f * modulus)), 0.3);
                    Color tempColor = hslToRGB((float) argument, (float) modulus, (float) modulus);
                    colorData[i][j] = new double[]{tempColor.getRed(), tempColor.getGreen(), tempColor.getBlue(), m.re(), m.im()};
                    /*for(int k = i*iStep; k < (i + 1)*iStep; k++)
                    {
                        for(int l = j*jStep; l < (j + 1)*jStep; l++)
                        {
                            colors[(k * iStep) - 1][(l * jStep)] = tempColor.getRGB();
                        }
                    }*/
                    g.setColor(tempColor);
                    g.fillRect(i*iStep, j*jStep, (i + 1) * iStep, (j + 1) * jStep);
                }
            }
            //tempImage = createImage(colors);
            //g.drawImage(tempImage, 0, 0, null);
            /*for(int i = 0; i < numIStep; i++)
            {
                for(int j = 0; j < numJStep; j++)
                {
                    g.setColor(new Color((int)colorData[i][j][0], (int)colorData[i][j][1], (int)colorData[i][j][2]));
                    g.fillRect(i * iStep, j * jStep, iStep * (i + 1), jStep * (j + 1));
                }
            }*/
        }
        //recursiveDraw(0, 0, 1599, 899);
        /*try {
            latch = new CountDownLatch(numThreads);
            assignThreads(0, 0, (int) window_screenX / 2 - 1, (int) window_screenY / 2 - 1, 0);
            assignThreads((int) window_screenX / 2, 0, (int) window_screenX - 1, (int) window_screenY / 2 - 1, 0);
            assignThreads(0, (int) window_screenY / 2, (int) window_screenX / 2 - 1, (int) window_screenY - 1, 0);
            assignThreads((int) window_screenX / 2, (int) window_screenY / 2, (int) window_screenX - 1, (int) window_screenY - 1, 0);
            latch.await();
        }catch(InterruptedException e){};*/
        //latch = new CountDownLatch(numThreads);
        if(menu.P.getText().contains("zm"))
        {
            if (kernel1 == null) {
                colors = new int[1600][900];
                kernel1 = new Kernel() {
                    @Override
                    public void run() {
                        int i = getGlobalId();
                        if (i <= 3) {
                            assignKernels(0, 0, (int) window_screenX / 2 - 1, (int) window_screenY / 2 - 1, i, 0);
                        } else if (i <= 7) {
                            assignKernels((int) window_screenX / 2, 0, (int) window_screenX - 1, (int) window_screenY / 2 - 1, i, 0);
                        } else if (i <= 11) {
                            assignKernels(0, (int) window_screenY / 2, (int) window_screenX / 2 - 1, (int) window_screenY - 1, i, 0);
                        } else if (i <= 15) {
                            assignKernels((int) window_screenX / 2, (int) window_screenY / 2, (int) window_screenX - 1, (int) window_screenY - 1, i, 0);
                        }
                    }
                };
                range = Range.create(16);
            }
            if (!zoomChanged && !panChanged && tempImage != null) {
                g.drawImage(tempImage, 0, 0, null);
            }
            /*else if(panChanged && tempImage != null)
            {
                double panX = centerX - lastCenter.getX();
                double panY = centerY - lastCenter.getY();
                int[][] shiftedArray = shiftArray(colors, (int)(coordsToScreenX(panX) - coordsToScreenX(0)), (int)(coordsToScreenY(panY) - coordsToScreenY(0)));
                int[][] tempColors = colors;
                int panXScreen = (int)(coordsToScreenX(panX) - coordsToScreenX(0));
                int panYScreen = (int)(coordsToScreenY(panY) - coordsToScreenY(0));
                /*if(panX > 0)
                {
                    recursiveDraw((int) window_screenX - (int)coordsToScreenX(panX), 0, (int) window_screenX - 1, (int) window_screenY - 1);
                }
                else if(panX < 0)
                {
                    recursiveDraw(0, 0, (int) coordsToScreenX(panX), (int) coordsToScreenY(panY));
                }
                if(panY > 0)
                {
                    recursiveDraw(0, 0, (int)window_screenX, (int)coordsToScreenY(0) - (int)coordsToScreenY(panY));
                }
                else if(panY < 0)
                {
                    recursiveDraw(0, 0, (int) coordsToScreenX(panX), (int) coordsToScreenY(panY));
                }
                if(panX >= 0 && panY >= 0)
                {
                    recursiveDraw(0, 0, (int)window_screenX - 1, panYScreen);
                    recursiveDraw((int)window_screenX - panXScreen, panYScreen, (int)window_screenX - 1, (int)window_screenY - 1);
                }
                if(panX >= 0 && panY < 0)
                {
                    recursiveDraw(0, (int)window_screenY - panYScreen, (int)window_screenX - 1, (int)window_screenY - 1);
                    recursiveDraw((int)window_screenX - panXScreen, (int)window_screenY - panYScreen, (int)window_screenX - 1, (int)window_screenY - panYScreen);
                }
                if(panX < 0 && panY >= 0)
                {
                    recursiveDraw(0, 0, (int)window_screenX - 1, panYScreen);
                    recursiveDraw(0, panYScreen, panXScreen, (int)window_screenY - panYScreen);
                }
                if(panX < 0 && panY < 0)
                {
                    recursiveDraw(0, (int)window_screenY - panYScreen, (int)window_screenX - 1, (int)window_screenY - 1);
                    recursiveDraw(0, 0, panXScreen, (int)window_screenY - panYScreen);
                }
                for(int i = (int)window_screenX - (int)coordsToScreenX(panX); i < window_screenX; i++)
                {
                    for(int j = (int)window_screenY - (int)coordsToScreenY(panY); j < window_screenY; j++)
                    {
                        shiftedArray[i][j] = colors[i][j];
                    }
                }
                colors = tempColors;
                //tempImage = createImage(shiftedArray);
                kernel1.execute(range);
                tempImage = createImage(shiftedArray);
                //g.drawImage(tempImage, (int)coordsToScreenX(0) - (int)coordsToScreenX(panX), (int)coordsToScreenY(0) - (int)coordsToScreenY(panY), null);
                g.drawImage(tempImage, 0, 0, null);
                System.out.println(panX + " " + panY + " " + coordsToScreenX(panX) + " " + coordsToScreenY(panY));
            }*/else {
                kernel1.execute(range);
                tempImage = createImage(colors);
                g.drawImage(tempImage, 0, 0, null);
            }
        }
        //assignThreads(0, 0, (int)window_screenX - 1, (int)window_screenY - 1);
        //MultiThreading temp = new MultiThreading(this, 750, 400, 800, 450);
        //temp.start();
        //recursiveDraw(0, 0, (int)window_screenX - 1, (int)window_screenY - 1);
        /*object1.run(this, 0, 0, (int)window_screenX/2 - 1, (int)window_screenY/2 - 1);
        object2.run(this, (int)window_screenX/2, 0, (int)window_screenX - 1, (int)window_screenY/2 - 1);
        MultiThreading object3 = new MultiThreading();
        object3.run(this, 0, (int)window_screenY/2, (int)window_screenX/2 - 1, (int)window_screenY - 1);
        MultiThreading object4 = new MultiThreading();
        object4.run(this, (int)window_screenX/2, (int)window_screenY/2, (int)window_screenX - 1, (int)window_screenY - 1);*/
        /*for(int i = 0; i < window_screenX; i += iStep)
        {
            for(int j = 0; j < window_screenY; j += jStep)
            {
                    parser.addVariable("z", new Complex(arrows.getFirst().screenToCoordsX(i), arrows.getFirst().screenToCoordsY(j)));
                    if(!menu.P.getText().contains("zmandelbrot"))
                    {
                        parser.parseExpression(menu.P.getText());
                        Complex R = parser.getComplexValue();
                        double argument = Math.toDegrees(Math.atan2(R.im(), R.re()));
                        if (argument < 0f) {
                            argument = argument + 360f;
                        }
                        double modulus = Math.sqrt(Math.pow(R.re(), 2) + Math.pow(R.im(), 2));
                        modulus = (float) Math.pow((1f - Math.exp(-0.02f * modulus)), 0.3);
                        g.setColor(hslToRGB((float) argument, (float) modulus, (float) modulus));//gets the color of the rectangle
                        g.fillRect(i, j, i + iStep, j + jStep);
                    }
                    else
                    {
                        Complex temp = (Complex)parser.getVarValue("z");
                        /*Complex R;
                        if(getLength(temp) > 2)
                        {
                            R = new Complex(0,0);
                        }
                        else {
                            R = mandelbrot(temp);
                        }
                        double argument = Math.toDegrees(Math.atan2(R.im(), R.re()));
                        if (argument < 0f) {
                            argument = argument + 360f;
                        }
                        double modulus = Math.sqrt(Math.pow(R.re(), 2) + Math.pow(R.im(), 2));
                        modulus = (float) Math.pow((1f - Math.exp(-0.02f * modulus)), 0.3);
                        //g.setColor(hslToRGB((float) argument, (float) modulus, (float) modulus));//gets the color of the rectangle
                        g.setColor(mandelbrotColor((Complex)parser.getVarValue("z")));
                        g.fillRect(i, j, i + iStep, j + jStep);
                    }
            }
        }*/
    }
    public int[][] shiftArray(int[][] initArr, int shiftX, int shiftY)
    {
        int[][] temp = new int[initArr.length][initArr[0].length];
        for(int i = 0; i < initArr.length; i++)
        {
            for(int j = 0; j < initArr[0].length; j++)
            {
                if(i - shiftX >= 0 && j - shiftY >= 0 && i - shiftX < initArr.length && j - shiftY < initArr[0].length)
                {
                    temp[i - shiftX][j - shiftY] = initArr[i][j];
                }
            }
        }
        return temp;
    }
    public BufferedImage createImage(int[][] pixels)
    {
        int[] temp = new int[pixels.length*pixels[0].length];
        for(int i = 0; i < pixels.length; i++)
        {
            for(int j = 0; j < pixels[0].length; j++)
            {
                temp[i + pixels.length*j] = pixels[i][j];
            }
        }
        return createImage(temp);
    }
    public BufferedImage createImage(int[] pixels)
    {
        BufferedImage image = new BufferedImage((int)window_screenX, (int)window_screenY, BufferedImage.TYPE_4BYTE_ABGR);
        image.setRGB(0, 0, (int)window_screenX, (int)window_screenY, pixels, 0, (int)window_screenX);
        return image;
    }
    public Color HCLToRGB(double h, double c, double l)
    {//taken from source code of https://mandelbrot.page/?
        h = h % 360;
        if(h < 0)
        {
            h += 360;
        }
        c = Math.min(Math.max(c,0), 100);
        l = Math.min(Math.max(l, 0), 100);

        double hRad = Math.toRadians(h);

        double u = Math.cos(hRad) * c;
        double v = Math.sin(hRad) * c;

        double r = l + 0.09551990792716 * u + 0.05406649830715 * v;
        double g = l - 0.03947567534454 * u - 0.01829165033556 * v;
        double b = l - 0.19638676772419 * u - 0.45388640918783 * v;
        double[] rgb = new double[]{r,g,b};
        for(int i = 0; i < 3; i++)
        {
            double temp;
            if(rgb[i]/100 <= 0.0031308){temp = 12.92*rgb[i]/100;}
            else{temp = 1.055 * Math.pow(rgb[i]/100, 1/2.4) - 0.055;}
            if(temp <= 0){rgb[i] = 0;}
            else if(temp >= 1){rgb[i] = 255;}
            else{rgb[i] = temp * 255;}
        }
        //System.out.println((float)rgb[0] + " " +  (float)rgb[1] + " " +  (float)rgb[2]);
        return new Color((int)rgb[0], (int)rgb[1], (int)rgb[2]);
    }
    public void assignKernels(int minI, int minJ, int maxI, int maxJ, int kernelNum, int num)
    {
        int halfI = minI/2 + maxI/2;
        int halfJ = minJ/2 + maxJ/2;
        if(num == 0)
        {
            if (kernelNum % 4 == 0) {
                recursiveDraw(minI, minJ, halfI, halfJ);
            } else if (kernelNum % 4 == 1) {
                recursiveDraw(halfI, minJ, maxI, halfJ);
            } else if (kernelNum % 4 == 2) {
                recursiveDraw(minI, halfJ, halfI, maxJ);
            } else if (kernelNum % 4 == 3) {
                recursiveDraw(halfI, halfJ, maxI, maxJ);
            }
        }
        else
        {
            assignKernels(minI, minJ, halfI, halfJ, kernelNum, 0);
            assignKernels(halfI, minJ, maxI, halfJ, kernelNum, 0);
            assignKernels(minI, halfJ, halfI, maxJ, kernelNum, 0);
            assignKernels(halfI, halfJ, maxI, maxJ, kernelNum, 0);
        }
    }
    public void assignThreads(int minI, int minJ, int maxI, int maxJ, int num)
    {
        if(num == 0)
        {
            MultiThreading obj1 = new MultiThreading(this, minI, minJ, minI + (maxI - minI) / 2, minJ + (maxJ - minJ) / 2);
            MultiThreading obj2 = new MultiThreading(this, minI + (maxI - minI) / 2, minJ, maxI, minJ + (maxJ - minJ) / 2);
            MultiThreading obj3 = new MultiThreading(this, minI, minJ + (maxJ - minJ) / 2, minI + (maxI - minI) / 2, maxJ);
            MultiThreading obj4 = new MultiThreading(this, minI + (maxI - minI) / 2, minJ + (maxJ - minJ) / 2, maxI, maxJ);
            obj1.start();
            obj2.start();
            obj3.start();
            obj4.start();
        }
        else
        {
            assignThreads(minI, minJ, minI + (maxI - minI) / 2, minJ + (maxJ - minJ) / 2, num - 1);
            assignThreads(minI + (maxI - minI) / 2, minJ, maxI, minJ + (maxJ - minJ) / 2, num - 1);
            assignThreads(minI, minJ + (maxJ - minJ) / 2, minI + (maxI - minI) / 2, maxJ, num - 1);
            assignThreads(minI + (maxI - minI) / 2, minJ + (maxJ - minJ) / 2, maxI, maxJ, num - 1);
        }
    }
    public void recursiveDraw(int minI, int minJ, int maxI, int maxJ)
    {
        int area = (maxI-minI + 1) * (maxJ - minJ + 1);
        //System.out.println(minI + " " + minJ + " " + maxI + " " + maxJ + " " + area);
        if(area < 32)
        {
            for(int tempI = minI; tempI <= maxI; tempI++)
            {
                for(int tempJ = minJ; tempJ <= maxJ; tempJ++)
                {
                    colors[tempI][tempJ] = mandelbrotColor(tempI, tempJ).getRGB();
                }
            }
            /*Complex[] reference = getOrbit((new Complex(arrows.getFirst().screenToCoordsX((double) (maxI - minI) /2 + minI), arrows.getFirst().screenToCoordsY((double) (maxJ - minJ) /2 + minJ))));
            int maxRefIteration = reference.length - 1;
            Complex dz = new Complex(0,0);
            Complex A = new Complex(1,0);
            Complex B = new Complex(0,0);
            Complex C = new Complex(0,0);
            //Complex D = new Complex(0,0);
            //Complex E = new Complex(0,0);
            Complex[] temp = new Complex[3];
            for(int iteration = 1; iteration < reference.length; iteration++)
            {
                temp = new Complex[]{A, B, C};
                Complex temp2 = reference[iteration].mul(2);
                A = temp2.mul(temp[0]).add(new Complex(1));//A_n+1 = 2X_nA_n + 1
                B = temp2.mul(temp[1]).add(temp[0].mul(temp[0]));//B_n+1 = 2X_nB_n + (A_n)^2
                C = temp2.mul(temp[2]).add(temp[0].mul(2).mul(temp[1]));//C_n+1 = 2X_nC_n + 2A_nB_n
                //D = temp2.mul(temp[3]).add(temp[0].mul(2).mul(temp[2])).add(temp[1].mul(temp[1]));//D_n+1 = 2X_nD_n + 2A_nC_n + (B_n)^2
                //E = temp2.mul(2).mul(temp[4]).add(temp[0].mul(2).mul(temp[3])).add(temp[1].mul(2).mul(temp[2]));//E_n+1 = 2X_nE_n + 2A_nD_n + 2B_nC_n
            }
            for(int tempI = minI; tempI <= maxI; tempI++)
            {
                for (int tempJ = minJ; tempJ <= maxJ; tempJ++)
                {
                    Complex delta = new Complex(arrows.getFirst().screenToCoordsX(tempI), arrows.getFirst().screenToCoordsY(tempJ)).sub(reference[0]);
                    Complex epsilon_n = A.mul(delta).add(B.mul(delta.power(2))).add(C.mul(delta.power(3))).add(reference[reference.length - 1]);
                    double length = getLength(epsilon_n);
                    colors[tempI + (int)window_screenX * tempJ] = hslToRGB((float)Math.toDegrees(epsilon_n.arg()), (float)length, (float)length).getRGB();
                    /*double n = Math.log1p(reference.length + 1 - Math.log(Math.log(getLength(dz))))/Math.log(2);
                    //double n = 0.99;
                    double h = (Math.log(reference.length + 20) * 200) % 360;
                    double c_ = 100;
                    double l = 15 * n + 85 * Math.pow(n, 1);
                    colors[tempI + (int)window_screenX * tempJ] = HCLToRGB(h, c_, l*0.0125).getRGB();
                }
            }
            /*
            for(int tempI = minI; tempI <= maxI; tempI++)
            {
                for(int tempJ = minJ; tempJ <= maxJ; tempJ++)
                {
                    Complex dc = new Complex(tempI, tempJ).sub(reference[0]);
                    int iteration = 0; int refIteration = 0;
                    Complex z;
                    dz = new Complex(0,0);
                    double len = 0;
                    while(iteration < maxRefIteration && len <= 4)
                    {
                        dz = dz.mul(2).mul(reference[refIteration]).add(dz.mul(dz).add(dc));
                        z = reference[refIteration].add(dz);
                        len = z.re() * z.re() + z.im() * z.im();
                        /*refIteration++;


                        if(len < dz.re() * dz.re() + dz.im() * dz.im() || refIteration == maxRefIteration)
                        {
                            dz = z;
                            refIteration = 0;
                        }
                        iteration++;
                    }
                    double n = Math.log1p(iteration + 1 - Math.log(Math.log(getLength(dz))))/Math.log(2);
                    //double n = 0.99;
                    double h = (Math.log(iteration + 20) * 200) % 360;
                    double c_ = 100;
                    double l = 15 * n + 85 * Math.pow(n, 1);
                    colors[tempI + (int)window_screenX * tempJ] = HCLToRGB(h, c_, l*0.0125).getRGB();
                    //colors[tempI + (int)window_screenX * tempJ] = mandelbrotColor(tempI, tempJ).getRGB();
                    //g.setColor(mandelbrotColor(tempI, tempJ));
                    //g.fillRect(tempI, tempJ, 1, 1);
                }
            }*/
        }
        else if(tracePerimeter(minI, minJ, maxI, maxJ))
        {
            Color tempColor = mandelbrotColor(minI, minJ);
            for(int i = minI; i <= maxI; i++)
            {
                for(int j = minJ; j <= maxJ; j++)
                {
                    colors[i][j] = tempColor.getRGB();
                }
            }
            //g.fillRect(minI, minJ, maxI - minI + 1, maxJ - minJ + 1);
        }
        else
        {
            int halfI = minI/2 + maxI/2;
            int halfJ = minJ/2 + maxJ/2;
            recursiveDraw(minI, minJ, halfI, halfJ);
            recursiveDraw(halfI, minJ, maxI, halfJ);
            recursiveDraw(minI, halfJ, halfI, maxJ);
            recursiveDraw(halfI, halfJ, maxI, maxJ);
        }
    }
    public boolean tracePerimeter(int minI, int minJ, int maxI, int maxJ)
    {
        Color tempColor = mandelbrotColor(minI, minJ);
        for(int tempI = minI + 1; tempI <= maxI; tempI++)
        {
            if(!mandelbrotColor(tempI, minJ).equals(tempColor) || !mandelbrotColor(tempI, maxJ).equals(tempColor))
            {
                return false;
            }
        }
        for(int tempJ = minJ + 1; tempJ <= maxJ; tempJ++)
        {
            if(!mandelbrotColor(minI, tempJ).equals(tempColor) || !mandelbrotColor(maxI, tempJ).equals(tempColor))
            {
                return false;
            }
        }
        return true;
    }
    public Color mandelbrotColor(int screenX, int screenY)
    {
        return mandelbrotColor(new Complex(screenToCoordsX(screenX), screenToCoordsY(screenY)));
    }
    public Complex[] getOrbit(Complex z)
    {
        ArrayList<Complex> orbit = new ArrayList<>();
        int i = 0;
        Complex c = z;
        double xsqr = 0; double ysqr = 0;
        int maxIter = 1000;
        while(i < maxIter)
        {
            orbit.add(z);
            z = z.mul(z).add(c);
            xsqr = z.re() * z.re();
            ysqr = z.im() * z.im();
            i++;
        }
        orbit.add(z);
        return orbit.toArray(new Complex[0]);
    }
    public Color mandelbrotColor(Complex z)
    {
        int i = 0;
        //double cx = z.re(); double cy = z.im();
        //double zx = 0; double zy = 0;
        double xsqr = 0; double ysqr = 0;
        double x = z.re();
        double y = z.im();
        int maxIter = 1000;
        while(i < maxIter && xsqr + ysqr < 4)
        {
            /*zy *= zx;
            zy += zy + cy;
            zx = xsqr - ysqr + cx;
            xsqr = zx * zx;
            ysqr = zy * zy;*/
            /*double k1 = z.re() * (z.re() + z.im());
            double k2 = z.re() * (z.im() - z.re());
            double k3 = z.im() * (z.re() + z.im());*/
            /*x = xsqr - ysqr + z.re();
            y = w - xsqr - ysqr + z.im();
            xsqr = x * x;
            ysqr = y * y;
            w = (x + y) * (x + y);*/
            xsqr = z.re() * z.re();
            ysqr = z.im() * z.im();
            z = new Complex(xsqr - ysqr + x, 2 * z.re() * z.im() + y);
            //z = z.power(2).add(c);
            i++;
                //double n = i + 1 - Math.log(Math.log(getLength(z)))/Math.log(2);
                //return new Color((int)r, (int)g, (int)b);
        }
        //return new Color(Color.HSBtoRGB(1f, 1f, i/((float)maxIter)));
        if(i > maxIter - 1)
        {
            return Color.BLACK;
        }
        //double n = Math.log1p(i + 1 - Math.log(Math.log(getLength(z))))/Math.log(2);
                /*double[] color1 = colors[(int)(Math.floor(n) % colors.length)];
                double[] color2 = colors[(int)(Math.ceil(n) % colors.length)];
                double r = color1[0] + (n - Math.floor(n)) * (color2[0] - color1[0]);
                double g = color1[1] + (n - Math.floor(n)) * (color2[1] - color1[1]);
                double b = color1[2] + (n - Math.floor(n)) * (color2[2] - color1[2]);*/
        //double n = 0.99;
        double h = (Math.log(i + 20) * 100) % 360;
        //double c_ = 100;
        //double l = 100;
        //return hslToRGB((float)Math.pow(((double)i/maxIter)*360, 1.5) % 360f, 0.5f, ((float)(i/maxIter)));
        return HCLToRGB(h, 100, 1.5);
    }
    public Complex mandelbrot(Complex z)
    {
        //z.add(new Complex(0));
        Complex c = z;
        for(int i = 0; i < 1; i++)
        {
            //z = (z.power(2).add(c)).power(2).add(c);
            z = z.power(2).add(c);
            //if(z.abs())
            //if(z.)
        }
        //double mod = Math.sqrt(Math.pow(z.re(), 2) + Math.pow(z.im(), 2));
        //double temp = Math.pow(2, 64);
        //return new Complex(Math.cos(temp * z.arg()), Math.sin(temp * z.arg())).mul(Math.pow(mod, temp));
        return z;
    }
    public double getLength(Complex z)
    {
        return Math.sqrt(Math.pow(z.re(), 2) + Math.pow(z.im(), 2));
    }
    @Override
    protected void paintComponent(Graphics gInit) {
        super.paintComponent(gInit);
        //System.out.println(maxCoordX+ " " + maxCoordY + " " + minCoordX + " " + minCoordY);
        //double[] tempPoint = transformPoint(window_screenX, window_screenY);
        //maxCoordX = tempPoint[0];
        //maxCoordY = maxCoordX *window_screenY/window_screenX;
        //maxCoordY = tempPoint[1];
        g = gInit;
        if(!menu.P.getText().contains("z"))
        {
            updateArrows();
            g.setFont(new Font("Calibri", Font.PLAIN, 30));
            g.setColor(new Color(120, 0, 200));
            userMouse = MouseInfo.getPointerInfo().getLocation();
            Point2D.Double forceAtMouse = getVector(userMouse.getX(), userMouse.getY());
            g.setColor(Color.white);
            g.setFont(new Font("Calibri", Font.PLAIN, 15));
            //g.drawString(time + " time (s)", 50, 870);
            menu.update(g);
            maxCurl = -1;
            maxDivergence = -1;
            maxMagnitude = -1;
        }
        else
        {
            userMouse = MouseInfo.getPointerInfo().getLocation();
            menu.update(g);
            colorComplex();
        }
        axes.update(g);
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
        time += 0.049;
        zoomChanged = false;
        panChanged = false;
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
    public double screenToCoordsX(double screenX) {return centerX + (maxCoordX - minCoordX) *(screenX - window_screenX/2)/window_screenX;}
    public double screenToCoordsY(double screenY) {return centerY -(maxCoordY - minCoordY) *(screenY - window_screenY/2)/window_screenY;}
    public double coordsToScreenX(double CX) {return CX*window_screenX/(maxCoordX - minCoordX) + window_screenX/2;}
    public double coordsToScreenY(double CY) {return -CY*window_screenY/(maxCoordY - minCoordY) + window_screenY/2;}
}
class MultiThreading extends Thread
{
    Panel panel;
    int minI;
    int minJ;
    int maxI;
    int maxJ;
    public MultiThreading(Panel initPanel, int initMinI, int initMinJ, int initMaxI, int initMaxJ)
    {
        panel = initPanel;
        minI = initMinI;
        minJ = initMinJ;
        maxI = initMaxI;
        maxJ = initMaxJ;
    }
    public void run()
    {
        try
        {
            Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler() {
                @Override
                public void uncaughtException(Thread t, Throwable e) {
                    System.out.println(e);
                }
            });
            //double rand = Math.random();
            //System.out.println("thread " + rand + " started");
            panel.recursiveDraw(minI, minJ, maxI, maxJ);
            panel.latch.countDown();
            //System.out.println("thread " + rand + " finished");
        }
        catch(Throwable e){System.out.println(e);}
    }
}