import java.awt.*;
import java.math.RoundingMode;
import java.text.DecimalFormat;

public class Axes
{
    double maxCX;//max x coord
    double maxCY;
    double SX;//max x pixel (screen)
    double SY;
    Panel panel;
    double tickDistance = 100;
    DecimalFormat df = new DecimalFormat("#.####E0");
    DecimalFormat df2 = new DecimalFormat("#.####");
    public Axes(Panel panelInit)
    {
        panel = panelInit;
        maxCX = panel.maxCoordX;
        maxCY = panel.maxCoordY;
        SX = panel.window_screenX;
        SY = panel.window_screenY;
        df.setRoundingMode(RoundingMode.CEILING);
    }
    public void update(Graphics g)
    {
        g.setColor(Color.white);
        g.setFont(new Font("Calibri", Font.BOLD, 15));
        //double[] origin = panel.transformPoint(0,0);
        g.drawLine(0, (int)(SY/2), (int)SX, (int)(SY/2));//horizontal line
        for(double i = 0; i < panel.window_screenX; i += tickDistance)
        {
            if(i + tickDistance/2 != 0)
            {
                g.drawLine((int)(i + tickDistance / 2), 440, (int)(i + tickDistance / 2), 460);
                String text = String.valueOf(screenToCoordsX(i + tickDistance/2));
                g.setColor(Color.black);
                g.setFont(new Font("Calibri", Font.BOLD, 18));
                if(text.contains("E")) {
                    g.drawString(df.format(Double.parseDouble(text)), (int) (i + tickDistance / 2), 470);
                }
                else
                {
                    g.drawString(df2.format(Double.parseDouble(text)), (int) (i + tickDistance / 2), 470);
                }
                g.setFont(new Font("Calibri", Font.PLAIN, 17));
                g.setColor(Color.white);
                if(text.contains("E")) {
                    g.drawString(df.format(Double.parseDouble(text)), (int) (i + tickDistance / 2), 470);
                }
                else
                {
                    g.drawString(df2.format(Double.parseDouble(text)), (int) (i + tickDistance / 2), 470);
                }
            }
        }
        g.drawLine((int)(SX/2), 0, (int)(SX/2), (int)SY);//vertical line
        for(double i = 0; i < panel.window_screenY; i += tickDistance)
        {
            if(screenToCoordsY(i + tickDistance/2) != 0)
            {
                String text = String.valueOf(screenToCoordsY(i + tickDistance/2));
                g.setColor(Color.black);
                g.setFont(new Font("Calibri", Font.BOLD, 18));
                if(text.contains("E")) {
                    g.drawString(df.format(Double.parseDouble(text)), 820, (int) (i + tickDistance / 2));
                }
                else
                {
                    g.drawString(df2.format(Double.parseDouble(text)), 820, (int) (i + tickDistance / 2));
                }
                g.setFont(new Font("Calibri", Font.PLAIN, 17));
                g.setColor(Color.white);
                g.drawLine(790, (int)(i + tickDistance / 2), 810, (int)(i + tickDistance / 2));
                if(text.contains("E")) {
                    g.drawString(df.format(Double.parseDouble(text)), 820, (int) (i + tickDistance / 2));
                }
                else
                {
                    g.drawString(df2.format(Double.parseDouble(text)), 820, (int) (i + tickDistance / 2));
                }
            }
        }
    }
    public double screenToCoordsX(double screenX) {return panel.centerX -0*(panel.minCoordX - panel.origMaxMin[2]) + (panel.maxCoordX - panel.minCoordX) *(screenX - panel.window_screenX/2)/panel.window_screenX;}
    public double screenToCoordsY(double screenY) {return panel.centerY -0*(panel.minCoordY - panel.origMaxMin[3]) -(panel.maxCoordY - panel.minCoordY) *(screenY - panel.window_screenY/2)/panel.window_screenY;}
    //public double coordsToScreenX(double CX) {return maxSX * (CX - panel.minCoordX + panel.origMaxMin[2])/(panel.maxCoordX - panel.minCoordX) + maxSX/2;}
    //public double coordsToScreenY(double CY) {return maxSY * (CX - panel.minCoordX + panel.origMaxMin[2])/(panel.maxCoordX - panel.minCoordX) + maxSX/2;}
    public double coordsToScreenX(double CX) {return CX*panel.window_screenX/(panel.maxCoordX - panel.minCoordX) + panel.window_screenX/2;}
    public double coordsToScreenY(double CY) {return -CY*panel.window_screenY/(panel.maxCoordY - panel.minCoordY) + panel.window_screenY/2;}
}
