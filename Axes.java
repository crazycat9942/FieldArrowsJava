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
    DecimalFormat df = new DecimalFormat("#.####");
    public Axes(Panel panelInit)
    {
        panel = panelInit;
        maxCX = panel.window_maxCoordX;
        maxCY = panel.window_maxCoordY;
        SX = panel.window_screenX;
        SY = panel.window_screenY;
        df.setRoundingMode(RoundingMode.CEILING);
    }
    public void update(Graphics g)
    {
        g.setColor(Color.white);
        g.setFont(new Font("Calibri", Font.BOLD, 15));
        g.drawLine(0, (int)(SY/2), (int)SX, (int)(SY/2));//horizontal line
        for(double i = 0; i < panel.window_screenX; i += tickDistance)
        {
            if(i + tickDistance/2 != 0)
            {
                g.drawLine((int)(i + tickDistance / 2), 440, (int)(i + tickDistance / 2), 460);
                String text = String.valueOf(screenToCoordsX(i + tickDistance/2));
                g.setColor(Color.black);
                g.setFont(new Font("Calibri", Font.BOLD, 18));
                g.drawString(text, (int)(i + tickDistance / 2), 470);
                g.setFont(new Font("Calibri", Font.PLAIN, 17));
                g.setColor(Color.white);
                g.drawString(text, (int)(i + tickDistance/2), 470);
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
                g.drawString(text, 820, (int)(i + tickDistance/2));
                g.setFont(new Font("Calibri", Font.PLAIN, 17));
                g.setColor(Color.white);
                g.drawLine(790, (int)(i + tickDistance / 2), 810, (int)(i + tickDistance / 2));
                g.drawString(text, 820, (int)(i + tickDistance/2));
            }
        }
    }
    public double screenToCoordsX(double screenX) {return Double.parseDouble(df.format(2*panel.window_maxCoordX*(screenX - panel.window_screenX/2)/panel.window_screenX));}
    public double screenToCoordsY(double screenY) {return Double.parseDouble(df.format(-2*panel.window_maxCoordY*(screenY - panel.window_screenY/2)/panel.window_screenY));}
    public double coordsToScreenX(double coordX) {return coordX*panel.window_screenX/maxCX;}
    public double coordsToScreenY(double coordY) {return coordY*panel.window_screenY/maxCY;}
}
