import com.formdev.flatlaf.themes.FlatMacDarkLaf;
import net.miginfocom.swing.MigLayout;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.LegendItem;
import org.jfree.chart.LegendItemSource;
import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.entity.LegendItemEntity;
import org.jfree.chart.labels.StandardXYToolTipGenerator;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.PaintScale;
import org.jfree.chart.renderer.xy.XYBlockRenderer;
import org.jfree.chart.title.LegendGraphic;
import org.jfree.chart.title.LegendItemBlockContainer;
import org.jfree.chart.title.PaintScaleLegend;
import org.jfree.chart.ui.RectangleEdge;
import org.jfree.chart.ui.RectangleInsets;
import org.jfree.data.xy.DefaultXYZDataset;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYZDataset;

import javax.swing.*;
import java.awt.*;
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
    JTextArea curlArea;
    JTextArea divArea;
    JTextArea magArea;
    JTextField P;
    JTextField Q;
    JTextField PText;
    JTextField QText;
    JSlider zoomSlider;
    JTextArea jacobArea;
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
        setTitle("sharter");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(new Dimension(200, 900));
        MigLayout ml = new MigLayout();
        setLayout(ml);
        FlatMacDarkLaf.setup();
        FlatMacDarkLaf.updateUI();
        colorWithCurl = new JCheckBox("Color with curl");
        colorWithDiv = new JCheckBox("Color with divergence");
        colorWithMag = new JCheckBox("Color with magnitude", true);
        scaleWithMag = new JCheckBox("Scale with magnitude", true);
        zoomSlider = new JSlider(1, 10);
        PText = new JTextField("P: ");
        QText = new JTextField("Q: ");
        P = new JTextField("cos(0.3x + 0.6y)");
        Q = new JTextField("sin(0.3y - 0.6x)");
        P.setFont(new Font(P.getFont().getFontName(), Font.PLAIN, 23));
        Q.setFont(new Font(Q.getFont().getFontName(), Font.PLAIN, 23));
        //System.out.println(colorWithCurl.isSelected());
        ml.setLayoutConstraints("width 200px!, wrap");
        this.add(colorWithMag);
        this.add(colorWithCurl);
        //gbc.gridy = GridBagConstraints.FIRST_LINE_END;
        this.add(colorWithDiv);
        this.add(scaleWithMag);
        timeArea = new JTextArea("time: " + panel.time + " (s)");
        curlArea = new JTextArea();
        divArea = new JTextArea();
        magArea = new JTextArea();
        jacobArea = new JTextArea();
        this.add(timeArea, "bottom");
        this.add(curlArea);
        this.add(divArea);
        this.add(magArea);
        this.add(PText);
        PText.setEditable(false);
        this.add(P);
        this.add(QText);
        QText.setEditable(false);
        this.add(Q);
        this.add(new JTextArea("Zoom:"));
        this.add(zoomSlider);
        this.add(new JTextArea("Legend"));
        //PaintScaleLegend
        //jacobArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 10));
        //jacobArea.setSize(180, 180);
        //this.add(jacobArea);
        //panel.add(this.getContentPane(), BorderLayout.EAST);
        ChartPanel chartPanel = new ChartPanel(createChart(createDataset()));
        this.add(chartPanel);
    }
    public static void main(String[] args)
    {
        EventQueue.invokeLater(() -> new Menu(panel).setVisible(true));
    }

    public void update(Graphics g)
    {
        //g.setColor(Color.white);
        /*g.drawLine(1550, 30, 1575, 30);
        g.drawLine(1550, 40, 1575, 40);
        g.drawLine(1550, 50, 1575, 50);*/
        DecimalFormat df = new DecimalFormat("#.###");
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
        curlArea.setText("Curl at cursor: " + df.format(panel.getCurl(panel.userMouse.x, panel.userMouse.y)));
        divArea.setText("Divergence at cursor: " + df.format(panel.getDiv(panel.userMouse.x, panel.userMouse.y)));
        magArea.setText("Magnitude at cursor: " + df.format(panel.getMag(panel.userMouse.x, panel.userMouse.y)));
        //Arrow tempArrow = new Arrow(panel.axes.screenToCoordsX(panel.userMouse.x), panel.axes.screenToCoordsY(panel.userMouse.y), panel);
        //jacobArea.setText("Jacobian:\n   x  y  t\nP   " + tempArrow.DPDX + "  " + tempArrow.DPDY + "  3\nQ   4  5  6");
    }
    private static JFreeChart createChart(XYDataset dataset) {
        NumberAxis xAxis = new NumberAxis("x Axis");
        NumberAxis yAxis = new NumberAxis("y Axis");
        XYPlot plot = new XYPlot(dataset, xAxis, yAxis, null);
        XYBlockRenderer r = new XYBlockRenderer();
        r.setDefaultToolTipGenerator(new StandardXYToolTipGenerator());
        test.SpectrumPaintScale ps = new test.SpectrumPaintScale(0, 1);
        r.setPaintScale(ps);
        plot.setRenderer(r);
        JFreeChart chart = new JFreeChart("Title",
                JFreeChart.DEFAULT_TITLE_FONT, plot, false);
        NumberAxis scaleAxis = new NumberAxis("Scale");
        scaleAxis.setAxisLinePaint(Color.white);
        scaleAxis.setTickMarkPaint(Color.white);
        PaintScaleLegend legend = new PaintScaleLegend(ps, scaleAxis);
        legend.setSubdivisionCount(10);
        legend.setAxisLocation(AxisLocation.TOP_OR_RIGHT);
        legend.setPadding(new RectangleInsets(25, 10, 50, 10));
        legend.setStripWidth(20);
        legend.setPosition(RectangleEdge.RIGHT);
        legend.setBackgroundPaint(Color.WHITE);
        chart.addSubtitle(legend);
        chart.setBackgroundPaint(Color.white);
        return chart;
    }
    private static XYZDataset createDataset() {
        DefaultXYZDataset dataset = new DefaultXYZDataset();
        double[][] data = new double[3][128 * 128];
        for (int i = 0; i < 128 * 128; i++) {
            var x = i % 128;
            var y = i / 128;
            data[0][i] = x;
            data[1][i] = y;
            data[2][i] = x * y;
        }
        dataset.addSeries("Series", data);
        return dataset;
    }
}
class SpectrumPaintScale implements PaintScale {

    private static final float H1 = 0f;
    private static final float H2 = 1f;
    private final double lowerBound;
    private final double upperBound;

    public SpectrumPaintScale(double lowerBound, double upperBound) {
        this.lowerBound = lowerBound;
        this.upperBound = upperBound;
    }

    @Override
    public double getLowerBound() {
        return lowerBound;
    }

    @Override
    public double getUpperBound() {
        return upperBound;
    }

    @Override
    public Paint getPaint(double value) {
        float scaledValue = (float) (value / (getUpperBound() - getLowerBound()));
        float scaledH = H1 + scaledValue * (H2 - H1);
        return Color.getHSBColor(scaledH, 1f, 1f);
    }
}