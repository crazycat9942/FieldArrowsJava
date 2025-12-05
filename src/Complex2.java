import org.nfunk.jep.type.Complex;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

public class Complex2 {
    public BigDecimal re;
    public BigDecimal im;

    public Complex2()
    {
        re = BigDecimal.ZERO;
        im = BigDecimal.ZERO;
    }
    public Complex2(double reInit)
    {
        re = new BigDecimal(reInit);
        im = BigDecimal.ZERO;
    }
    public Complex2(BigDecimal reInit)
    {
        re = reInit;
        im = BigDecimal.ZERO;
    }
    public Complex2(double reInit, double imInit)
    {
        re = new BigDecimal(reInit);
        im = new BigDecimal(imInit);
    }
    public Complex2(BigDecimal reInit, BigDecimal imInit)
    {
        re = reInit;
        im = imInit;
    }

    public Complex2 add(Complex2 c2)
    {
        return new Complex2(this.re.add(c2.re), this.im.add(c2.im));
    }
    public Complex2 add(double addRe)
    {
        return new Complex2(re.add(new BigDecimal(addRe)), im);
    }
    public Complex2 add(BigDecimal addRe)
    {
        return new Complex2(re.add(addRe), im);
    }

    public Complex2 mul(double factor)
    {
        return new Complex2(this.re.multiply(new BigDecimal(factor)).setScale(100, RoundingMode.FLOOR), this.im.multiply(new BigDecimal(factor)).setScale(100, RoundingMode.FLOOR));
    }
    public Complex2 mul(Complex2 c2)
    {
        return new Complex2(this.re.multiply(c2.re).subtract(this.im.multiply(c2.im)).setScale(50, RoundingMode.FLOOR), this.im.multiply(c2.re).add(this.re.multiply(c2.im)).setScale(50, RoundingMode.FLOOR));
    }

    public BigDecimal magnitude()
    {
        return (this.re.multiply(this.re).add(this.im.multiply(this.im))).sqrt(new MathContext(10));
    }

    public Complex toComplex()
    {
        return new Complex(re.doubleValue(), im.doubleValue());
    }
}
