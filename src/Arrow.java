import java.awt.*;
import java.awt.geom.Point2D;

public class Arrow
{
    double angle;
    double coordX;
    double coordY;
    double screenX;
    double screenY;
    double length = 4;
    Color color;
    double magnitude;
    double maxCX;
    double maxCY;
    double maxSX;
    double maxSY;
    double curl;
    double divergence;
    Panel panel;
    double DPDX;
    double DPDY;
    double DPDT;
    double DQDX;
    double DQDY;
    double DQDT;
    int endX;
    int endY;
    public Arrow(double initX, double initY, Panel panelInit)
    {
        panel = panelInit;
        coordX = initX;
        coordY = initY;
        maxSX = panel.window_screenX;
        maxSY = panel.window_screenY;
        maxCX = panel.maxCoordX;
        maxCY = panel.maxCoordY;
        screenX = coordsToScreenX(coordX);
        screenY = coordsToScreenY(coordY);
        Point2D tempPoint = panel.getVector(coordX, coordY);
        magnitude = tempPoint.getX();
        angle = tempPoint.getY();
        Point2D arrowDX = panel.getVector(coordX + 0.001, coordY);
        Point2D arrowDY = panel.getVector(coordX, coordY + 0.001);
        DPDX = (arrowDX.getX()*Math.cos(arrowDX.getY()) - magnitude*Math.cos(angle))/0.001;//dP/dx
        //System.out.println(arrowDX.toString() + " " + DPDX + " " + arrowInit.toString());
        DPDY = (arrowDY.getX()*Math.cos(arrowDY.getY()) - magnitude*Math.cos(angle))/0.001;//dP/dy
        DQDX = (arrowDX.getX()*Math.sin(arrowDX.getY()) - magnitude*Math.sin(angle))/0.001;//dQ/dx
        DQDY = (arrowDY.getX()*Math.sin(arrowDY.getY()) - magnitude*Math.sin(angle))/0.001;
        curl = DQDX - DPDY;
        divergence  = DQDY + DPDX;
    }
    public void update(Graphics g, Panel panel)
    {
        maxCX = panel.maxCoordX;
        maxCY = panel.maxCoordY;
        coordX = screenToCoordsX(screenX);
        coordY = screenToCoordsY(screenY);
        Point2D tempPoint = panel.getVector(coordX, coordY);
        magnitude = tempPoint.getX();
        curl = panel.getCurl(coordX, coordY);
        angle = tempPoint.getY();
        panel.vector_to_rgb(this);
        endX = (int)(screenX + 2*length*Math.cos(angle));
        endY = (int)(screenY + 2*length*Math.sin(angle));
        /*int leftX = endX - (int)(length*Math.sin(angle));
        int leftY = endY + (int)(length*Math.cos(angle));
        int rightX = endX + (int)(length*Math.sin(angle));
        int rightY = endY - (int)(length*Math.cos(angle));
        int topX = endX + (int)(length*Math.cos(angle));
        int topY = endY + (int)(length*Math.sin(angle));*/
        g.setColor(color);
        Graphics2D g2d = (Graphics2D)g;
        g2d.setStroke(new BasicStroke(3));
        g2d.drawLine((int) screenX, (int)screenY, endX, endY);
        g2d.drawPolygon(new int[] {endX - (int)(length*Math.sin(angle)), endX + (int)(length*Math.sin(angle)), endX + (int)(length*Math.cos(angle))},new int[] {endY + (int)(length*Math.cos(angle)), endY - (int)(length*Math.cos(angle)), endY + (int)(length*Math.sin(angle))}, 3);
        /*
        endX
        length = Math.sqrt((end[0]-start[0])**2+(end[1]-start[1])**2)
        rotation =np.atan2(end[0]-start[0],end[1]-start[1])
        //end = (start[0] + length*np.cos(rotation), start[1] + length*np.sin(rotation))
        color ='white'
        //print(start)
        pygame.draw.line(window,color,(start[0],start[1]),(end[0],end[1]),5)
        leftSide =(end[0]-0.2*length*np.sin(rotation),end[1]+0.2*length*np.cos(rotation))
        rightSide =(end[0]+0.2*length*np.sin(rotation),end[1]-0.2*length*np.cos(rotation))
        top =(end[0]+0.2*length*np.cos(rotation),end[1]+0.2*length*np.sin(rotation))
        pygame.draw.polygon(window,

                vector_to_rgb(np.pi*rotation/180, length), (leftSide,rightSide,top),0)*/
    }
    public double screenToCoordsX(double screenX) {return panel.centerX -0*(panel.minCoordX - panel.origMaxMin[2]) + (panel.maxCoordX - panel.minCoordX) *(screenX - maxSX/2)/maxSX;}
    public double screenToCoordsY(double screenY) {return panel.centerY -0*(panel.minCoordY - panel.origMaxMin[3]) -(panel.maxCoordY - panel.minCoordY) *(screenY - maxSY/2)/maxSY;}
    //public double coordsToScreenX(double CX) {return maxSX * (CX - panel.minCoordX + panel.origMaxMin[2])/(panel.maxCoordX - panel.minCoordX) + maxSX/2;}
    //public double coordsToScreenY(double CY) {return maxSY * (CX - panel.minCoordX + panel.origMaxMin[2])/(panel.maxCoordX - panel.minCoordX) + maxSX/2;}
    public double coordsToScreenX(double CX) {return CX*maxSX/(panel.maxCoordX - panel.minCoordX) + maxSX/2;}
    public double coordsToScreenY(double CY) {return -CY*maxSY/(panel.maxCoordY - panel.minCoordY) + maxSY/2;}
}
