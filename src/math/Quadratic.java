package math;

// Polynomial of the form ax²+bx+c
public class Quadratic {
    private double a;
    private double b;
    private double c;
    public Quadratic(double a, double b, double c) {
        this.a = a;
        this.b = b;
        this.c = c;
    }
    public Quadratic() {
        this(0, 0, 0);
    }
    public Quadratic(Quadratic other) {
        this(other.a, other.b, other.c);
    }
    public double discriminant() {
        return b*b - 4*a*c;
    }
    public double solution1() {
        return (-b - Math.sqrt(discriminant())) / (2*a);
    }
    public double solution2() {
        return (-b + Math.sqrt(discriminant())) / (2*a);
    }
}
