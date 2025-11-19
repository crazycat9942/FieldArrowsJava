import com.formdev.flatlaf.themes.FlatMacDarkLaf;
import net.miginfocom.swing.MigLayout;
import org.nfunk.jep.type.Complex;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Point2D;
import java.text.DecimalFormat;
import java.util.ArrayList;
class Menu extends JFrame
{
    static Panel panel;
    ArrayList<JCheckBox> checkBoxes = new ArrayList<>();
    JCheckBox colorWithCurl;
    JCheckBox colorWithDiv;
    JCheckBox colorWithMag;
    JCheckBox scaleWithMag;
    JTextArea timeArea;
    JTextArea fpsArea;
    JTextArea curlArea;
    JTextArea divArea;
    JTextArea magArea;
    JTextField P;
    JTextField Q;
    JTextField PText;
    JTextField QText;
    //JSlider zoomSlider;//not used anymore
    JTextArea jacobArea;
    JSlider complexDetail;
    JSlider mandelbrotDetail;
    JTextArea coordAtPoint;
    JTextArea outputAtPoint;
    JButton contourFreeform;
    JButton contourFreeformClosed;
    JButton contourCircular;
    boolean contourFreeformActive = false;
    boolean contourFreeformClosedActive = false;
    boolean contourCircularActive1 = false;
    boolean contourCircularActive2 = false;
    boolean contourDone = false;
    boolean circularContourDone = false;
    boolean contourCircularActive3 = false;//true when user clicks again after clicking once for center and once for radius
    double lastTime;
    Point lastMousePos = new Point(-1,-1);//where the mouse was last frame regardless of if it was pressed
    ArrayList<Point> contourFreeformPos = new ArrayList<>();
    ArrayList<Complex> contourFreeformVal = new ArrayList<>();
    Point circularContourCenter = new Point(-1, -1);
    double circularContourRadius = -1;
    JButton addTestPoint;
    boolean testPointActive = false;
    ArrayList<Point> testPoints = new ArrayList<>();
    Point windowPos = new Point(0,0);
    public Menu(Panel panelInit)
    {
        panel = panelInit;
        checkBoxes.add(colorWithCurl);
        checkBoxes.add(colorWithDiv);
        checkBoxes.add(colorWithMag);
        init();
    }

    public void init()
    {
        lastTime = System.nanoTime();
        setTitle("da90wjion");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(new Dimension(350, 900));
        MigLayout ml = new MigLayout();
        setLayout(ml);
        FlatMacDarkLaf.setup();
        FlatMacDarkLaf.updateUI();
        colorWithCurl = new JCheckBox("Color with curl");
        colorWithDiv = new JCheckBox("Color with divergence");
        colorWithMag = new JCheckBox("Color with magnitude", true);
        scaleWithMag = new JCheckBox("Scale with magnitude", true);
        //zoomSlider = new JSlider(1, 200);//not used anymore
        PText = new JTextField("P: ");
        QText = new JTextField("Q: ");
        P = new JTextField("cos(0.3x + 0.6y)");
        P.setMinimumSize(new Dimension(300, 40));
        Q = new JTextField("sin(0.3y - 0.6x)");
        Q.setMinimumSize(new Dimension(300, 40));
        P.setFont(new Font(P.getFont().getFontName(), Font.PLAIN, 23));
        Q.setFont(new Font(Q.getFont().getFontName(), Font.PLAIN, 23));
        //System.out.println(colorWithCurl.isSelected());
        ml.setLayoutConstraints("width 350px!, wrap");
        this.add(colorWithMag);
        this.add(colorWithCurl);
        //gbc.gridy = GridBagConstraints.FIRST_LINE_END;
        this.add(colorWithDiv);
        this.add(scaleWithMag);
        timeArea = new JTextArea("time: " + panel.time + " (s)");
        fpsArea = new JTextArea();
        curlArea = new JTextArea();
        divArea = new JTextArea();
        magArea = new JTextArea();
        jacobArea = new JTextArea();
        this.add(timeArea, "bottom");
        this.add(fpsArea);
        this.add(curlArea);
        this.add(divArea);
        this.add(magArea);
        this.add(PText);
        PText.setEditable(false);
        this.add(P);
        this.add(QText);
        QText.setEditable(false);
        this.add(Q);
        //this.add(new JTextArea("Zoom:"));
        //this.add(zoomSlider);//not used anymore
        this.add(jacobArea);
        complexDetail = new JSlider(1, 50, 20);
        complexDetail.setInverted(true);
        this.add(new JTextField("Complex equation detail:"));
        this.add(complexDetail);
        mandelbrotDetail = new JSlider(10, 10000, 100);
        this.add(new JTextField("Mandelbrot set detail:"));
        this.add(mandelbrotDetail);
        coordAtPoint = new JTextArea();
        coordAtPoint.setEditable(false);
        outputAtPoint = new JTextArea();
        outputAtPoint.setEditable(false);
        this.add(coordAtPoint);
        this.add(outputAtPoint);

        contourFreeform = new JButton("Contour integral (freeform)");
        contourFreeform.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent f) {
                Component temp = (Component)f.getSource();
                Window window = SwingUtilities.windowForComponent(temp);
                windowPos = window.getLocation();
                if(!f.getActionCommand().contains("closed")) {
                    contourFreeformActive = true;
                }
                else {
                    contourFreeformClosedActive = true;
                }
                if(!contourDone) {
                    contourDone = true;
                    panel.addMouseMotionListener(new MouseMotionAdapter() {
                        @Override
                        public void mouseDragged(MouseEvent e) {
                            super.mouseDragged(e);
                            if (contourFreeformActive || contourFreeformClosedActive) {
                                //System.out.println("mouse dragged " + panel.userMouse.getLocation());
                                //g2d.drawLine(lastMousePos.x, lastMousePos.y, panel.userMouse.x, panel.userMouse.y);
                                lastMousePos = panel.userMouse.getLocation();
                                lastMousePos.translate(-windowPos.x, -windowPos.y);//offset
                                lastMousePos.translate(-6, -31);
                                contourFreeformPos.add(lastMousePos);
                                double posX = panel.screenToCoordsX(lastMousePos.x);
                                double posY = panel.screenToCoordsY(lastMousePos.y);
                                panel.parser.addComplexVariable("z", posX, posY);
                                panel.parser.addVariable("t", panel.time);
                                panel.parser.parseExpression(P.getText());
                                contourFreeformVal.add(panel.parser.getComplexValue());
                            }
                        }
                    });
                    panel.addMouseListener(new MouseAdapter() {
                        @Override
                        public void mousePressed(MouseEvent e) {
                            super.mousePressed(e);
                            panel.userPressed = true;
                            if (contourFreeformActive) {
                                System.out.println("mouse pressed " + panel.userMouse.getLocation());
                            }
                        }

                        @Override
                        public void mouseReleased(MouseEvent e) {
                            super.mouseReleased(e);
                            panel.userPressed = false;
                            if (contourFreeformActive) {
                                System.out.println("mouse released " + panel.userMouse.getLocation());
                            }
                            contourFreeformActive = false;
                            contourFreeformClosedActive = false;
                            contourFreeformPos.clear();
                        }
                    });
                }
                /*while (panel.userPressed) {
                    System.out.println(panel.userMouse.getLocation());
                }*/
                System.out.println("done");
            }
        });
        this.add(contourFreeform);
        contourFreeformClosed = new JButton("Closed contour integral (closed)");
        contourFreeformClosed.addActionListener(contourFreeform.getActionListeners()[0]);
        this.add(contourFreeformClosed);
        contourCircular = new JButton("Circular contour integral");
        contourCircular.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                contourCircularActive1 = true;
                contourCircularActive2 = false;
                System.out.println("Choose a center");
                if(!circularContourDone) {
                    panel.addMouseListener(new MouseAdapter() {
                        @Override
                        public void mouseClicked(MouseEvent e) {
                            super.mouseClicked(e);
                            if(contourCircularActive3)
                            {
                                contourCircularActive3 = false;
                                circularContourDone = true;
                                circularContourCenter = new Point(-1,-1);
                                circularContourRadius = -1;
                            }
                            if (contourCircularActive2) {
                                Point2D.Double center = new Point2D.Double(panel.screenToCoordsX(circularContourCenter.x), panel.screenToCoordsY(circularContourCenter.y));
                                Point2D.Double outerRad = new Point2D.Double(panel.screenToCoordsX(e.getPoint().x), panel.screenToCoordsY(e.getPoint().y));
                                circularContourRadius = center.distance(outerRad);
                                System.out.println("Radius chosen as " + circularContourRadius);
                                contourCircularActive2 = false;
                                contourCircularActive3 = true;
                            }
                            if (contourCircularActive1) {
                                circularContourCenter = e.getPoint();
                                System.out.println("Center chosen at " + circularContourCenter);
                                contourCircularActive1 = false;
                                contourCircularActive2 = true;
                            }
                        }
                    });
                }
            }
        });
        this.add(contourCircular);
        addTestPoint = new JButton("Add test point");
        addTestPoint.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                testPointActive = true;
                panel.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        super.mouseClicked(e);
                        if(testPointActive)
                        {
                            testPoints.add(e.getPoint());
                            System.out.println("Mouse clicked at " + e.getPoint());
                            testPointActive = false;
                        }
                    }
                });
            }
        });
        this.add(addTestPoint);
    }
    public static void main(String[] args)
    {
        EventQueue.invokeLater(() -> new Menu(panel).setVisible(true));
    }

    public void update(Graphics g)
    {
        DecimalFormat df = new DecimalFormat("#.####");
        if((contourFreeformActive || contourFreeformClosedActive) && lastMousePos.x != -1) {
            Complex freeformIntVal = new Complex();
            for(int i = 1; i < contourFreeformPos.size(); i++)
            {//gamma(t) = p1.x + (p2.x - p1.x)t + ip1.y + (p2.y - p1.y)it
                //gamma'(t) = p2.x - p1.x + p2.y - p1.y = p2 - p1
                //int_{0}^{1}f(gamma(t))*gamma'(t)
                Complex p1 = new Complex(panel.screenToCoordsX(contourFreeformPos.get(i-1).x), panel.screenToCoordsY(contourFreeformPos.get(i-1).y));
                Complex p2 = new Complex(panel.screenToCoordsX(contourFreeformPos.get(i).x), panel.screenToCoordsY(contourFreeformPos.get(i).y));
                double dist = Math.sqrt(Math.pow(p1.re() - p2.re(), 2) + Math.pow(p1.im() - p2.im(), 2));
                Complex gammaPrime = new Complex(p2.re() - p1.re(), p2.im() - p1.im());
                Complex addVal1 = contourFreeformVal.get(i-1).mul(gammaPrime);
                Complex addVal2 = contourFreeformVal.get(i).mul(gammaPrime);
                Complex addVal3 = new Complex(addVal1.re()*0.5 + addVal2.re()*0.5, addVal1.im()*0.5 + addVal2.im()*0.5);
                freeformIntVal = new Complex(freeformIntVal.re() + addVal3.re(), freeformIntVal.im() + addVal3.im());
                g.drawLine(contourFreeformPos.get(i-1).x, contourFreeformPos.get(i-1).y, contourFreeformPos.get(i).x, contourFreeformPos.get(i).y);
            }
            if(contourFreeformClosedActive)
            {
                Complex p1 = new Complex(panel.screenToCoordsX(contourFreeformPos.getFirst().x), panel.screenToCoordsY(contourFreeformPos.getFirst().y));
                Complex p2 = new Complex(panel.screenToCoordsX(contourFreeformPos.getLast().x), panel.screenToCoordsY(contourFreeformPos.getLast().y));
                double dist = Math.sqrt(Math.pow(p1.re() - p2.re(), 2) + Math.pow(p1.im() - p2.im(), 2));
                Complex gammaPrime = new Complex(p2.re() - p1.re(), p2.im() - p1.im());
                Complex addVal1 = contourFreeformVal.getFirst().mul(gammaPrime);
                Complex addVal2 = contourFreeformVal.getLast().mul(gammaPrime);
                Complex addVal3 = new Complex(addVal1.re()*0.5 + addVal2.re()*0.5, addVal1.im()*0.5 + addVal2.im()*0.5);
                freeformIntVal = new Complex(freeformIntVal.re() + addVal3.re(), freeformIntVal.im() + addVal3.im());
                g.drawLine(contourFreeformPos.getFirst().x, contourFreeformPos.getFirst().y, contourFreeformPos.getLast().x, contourFreeformPos.getLast().y);
            }
            g.drawString(freeformIntVal.toString(), 50, 50);
        }
        if(contourCircularActive2 || contourCircularActive3)
        {
            Point temp = panel.userMouse;
            lastMousePos.translate(-windowPos.x, -windowPos.y);
            lastMousePos.translate(-6, -31);
            int rad = (int)temp.distance(circularContourCenter);
            g.drawOval(circularContourCenter.x - rad, circularContourCenter.y - rad, 2*rad, 2*rad);
            //gamma(theta) = rad*e^{i*theta} where 0 <= theta < 2 pi
            //gamma'(theta) = i*rad*e^{i*theta}
            //integral = \int_{0}^{2\pi}f(rad*e^{i*\theta})*i*rad*e^{i*\theta}d\theta
            // = \int_{0}^{2\pi}
            // = \big|_{0}^{2\pi}
            Complex freeformIntVal = new Complex(0,0);
            int max = 1000;
            double dTheta = 2*Math.PI/max;
            panel.parser.addVariable("t", panel.time);
            for(double i = 0; i < 2*Math.PI; i = i + dTheta)
            {
                Complex p1 = new Complex(panel.screenToCoordsX(circularContourCenter.x) + circularContourRadius*Math.cos(i), panel.screenToCoordsY(circularContourCenter.y) + circularContourRadius*Math.sin(i));
                //Complex p2 = new Complex(panel.screenToCoordsX(circularContourCenter.x) + circularContourRadius*Math.cos((i+1)*dTheta), panel.screenToCoordsY(circularContourCenter.y) + circularContourRadius*Math.sin((i+1)*dTheta));
                System.out.println(circularContourRadius);
                panel.parser.addComplexVariable("z", p1.re(), p1.im());
                panel.parser.parseExpression(P.getText());
                Complex val = panel.parser.getComplexValue();
                Complex addVal = val.mul(new Complex(0,1)).mul(circularContourRadius).mul(new Complex(Math.cos(i), Math.sin(i))).mul(dTheta);
                freeformIntVal = new Complex(freeformIntVal.re() + addVal.re(), freeformIntVal.im() + addVal.im());
            }
            g.drawString(freeformIntVal.toString(), 50, 50);
        }
        jacobArea.setText("zoomFactor: " + panel.zoomFactor);
        fpsArea.setText("fps: " + df.format(1000000000/(System.nanoTime() - lastTime)));
        lastTime = System.nanoTime();
        /*for(JCheckBox i: checkBoxes)
        {
            if(i.isSelected())
            {
                for(JCheckBox j: checkBoxes)
                {
                    if(i != j)
                    {
                        j.setSelected(false);
                    }
                }
            }
        }*/
        timeArea.setText("time: " + df.format(panel.time) + " (s)");
        if(!P.getText().contains("z")) {
            Q.setEnabled(true);
            addTestPoint.setEnabled(true);
            colorWithCurl.setEnabled(true);
            colorWithDiv.setEnabled(true);
            colorWithMag.setEnabled(true);
            scaleWithMag.setEnabled(true);
            complexDetail.setEnabled(false);
            mandelbrotDetail.setEnabled(false);
            curlArea.setText("Curl at cursor: " + df.format(panel.getCurl(panel.userMouse.x - 6, panel.userMouse.y - 31)));
            divArea.setText("Divergence at cursor: " + df.format(panel.getDiv(panel.userMouse.x - 6, panel.userMouse.y - 31)));
            magArea.setText("Magnitude at cursor: " + df.format(panel.getMag(panel.userMouse.x - 6, panel.userMouse.y - 31)));
            magArea.setText(df.format(panel.minCoordX) + " " + df.format(panel.maxCoordX) + " " + df.format(panel.minCoordY) + " " + df.format(panel.maxCoordY));
            double mouseCoordX = panel.screenToCoordsX(panel.userMouse.x - 6);
            double mouseCoordY = panel.screenToCoordsY(panel.userMouse.y - 31);
            coordAtPoint.setText("Mouse coordinates: (" + mouseCoordX + ", " + mouseCoordY + ")");
            panel.parser.addVariable("x", mouseCoordX);
            panel.parser.addVariable("y", mouseCoordY);
            panel.parser.addVariable("t", panel.time);
            panel.parser.parseExpression(P.getText());
            float tempX = (float)panel.parser.getValue();
            panel.parser.parseExpression(Q.getText());
            float tempY = (float)panel.parser.getValue();
            outputAtPoint.setText("Output at mouse: (" + tempX + ", " + tempY + ")");
        }
        else if(!P.getText().contains("zm"))
        {
            Q.setEnabled(false);
            addTestPoint.setEnabled(false);
            colorWithCurl.setEnabled(false);
            colorWithDiv.setEnabled(false);
            colorWithMag.setEnabled(false);
            scaleWithMag.setEnabled(false);
            complexDetail.setEnabled(true);
            mandelbrotDetail.setEnabled(false);
            double mouseCoordX = panel.screenToCoordsX(panel.userMouse.x - 6);
            double mouseCoordY = panel.screenToCoordsY(panel.userMouse.y - 31);
            coordAtPoint.setText("Mouse coordinates: " + mouseCoordX + " + " + mouseCoordY + "i");
            panel.parser.addComplexVariable("z", mouseCoordX, mouseCoordY);
            panel.parser.addVariable("t", panel.time);
            panel.parser.parseExpression(P.getText());
            Complex temp = panel.parser.getComplexValue();
            outputAtPoint.setText("Output at mouse: " + (float)temp.re() + " + " + (float)temp.im() + "i");
        }
        else
        {
            Q.setEnabled(false);
            colorWithCurl.setEnabled(false);
            colorWithDiv.setEnabled(false);
            colorWithMag.setEnabled(false);
            scaleWithMag.setEnabled(false);
            complexDetail.setEnabled(false);
            mandelbrotDetail.setEnabled(true);
            double mouseCoordX = panel.screenToCoordsX(panel.userMouse.x - 6);
            double mouseCoordY = panel.screenToCoordsY(panel.userMouse.y - 31);
            coordAtPoint.setText("Mouse coordinates: " + (float)mouseCoordX + " + " + (float)mouseCoordY + "i");
            outputAtPoint.setText("Number of iterations to diverge: " + panel.mandelbrotDiverge(new Complex(mouseCoordX, mouseCoordY)) + " (max " + (int)panel.mandelbrotDetail + ")");
        }
        //Arrow tempArrow = new Arrow(panel.axes.screenToCoordsX(panel.userMouse.x), panel.axes.screenToCoordsY(panel.userMouse.y), panel);
        //jacobArea.setText("Jacobian:\n   x  y  t\nP   " + tempArrow.DPDX + "  " + tempArrow.DPDY + "  3\nQ   4  5  6");
        for(Point i: testPoints)
        {
            panel.g.drawOval(i.x, i.y, 2, 2);
            double tempX = i.x;
            double tempY = i.y;
            for(int j = 0; j < 500 && (tempX > 0 && tempY > 0); j++) {
                double tempX2 = tempX;
                double tempY2 = tempY;
                Arrow temp = new Arrow(panel.screenToCoordsX(tempX), panel.screenToCoordsY(tempY), panel);
                tempX += 3*Math.cos(temp.angle);
                tempY += 3*Math.sin(temp.angle);
                panel.g.drawLine((int)tempX2, (int)tempY2, (int)tempX, (int)tempY);
            }
            tempX = i.x;
            tempY = i.y;
            for(int j = 0; j < 500 && (tempX > 0 && tempY > 0); j++) {
                double tempX2 = tempX;
                double tempY2 = tempY;
                Arrow temp = new Arrow(panel.screenToCoordsX(tempX), panel.screenToCoordsY(tempY), panel);
                tempX -= 3*Math.cos(temp.angle);
                tempY -= 3*Math.sin(temp.angle);
                panel.g.drawLine((int)tempX2, (int)tempY2, (int)tempX, (int)tempY);
            }
        }
    }
}
