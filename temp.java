import org.nfunk.jep.JEP;
import org.nfunk.jep.Node;
import org.nfunk.jep.ParseException;
import org.nfunk.jep.type.Complex;

public class temp
{
    public static void main(String[] args)
    {
        try
        {
            JEP eq = new JEP();
            eq.addVariable("x", 3.14);
            eq.addVariable("y", 1.59);
            eq.setImplicitMul(true);
            //eq.addComplex();
            eq.addStandardFunctions();
            eq.addStandardConstants();
            Node n = eq.parseExpression("cos(x)");
            System.out.println(eq.evaluate(n));
        }catch(Exception e){};
    }
}
