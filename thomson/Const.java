/*
 * General Repository for constant values and functions
 *
 * Cris Cecka
 */

import java.math.*;
import java.util.*;

final public class Const
{
  final public static double PI = Math.PI;
  final public static double TWOPI = 2*Const.PI;
  final public static double ROOT3 = Math.sqrt(3);
  final public static double EIGHTPIOVERROOT3 = 8*Const.PI/ROOT3;

  private static Random rand = new Random(System.currentTimeMillis());
  final public static double getRandom()
  {
    return rand.nextDouble();
  }
  final public static double getRandom(double A, double B)
  {
    return A + (B-A)*getRandom();
  }
  final public static double getRandomNormal()
  {
    return rand.nextGaussian();
  }
  final public static void setRandomSphericalPoint( Point p )
  {
    double randPhi = getRandom() * Const.TWOPI;
    double randCosTheta = 2.0 * getRandom() - 1.0;
    double st = Math.sin( Math.acos(randCosTheta) );
    p.set( Math.cos(randPhi)*st, Math.sin(randPhi)*st, randCosTheta );
  }

  final public static Potential[] potentialList = { new Pot0(),
						   new Pot1(),
						   new Pot2(),
						   new Pot3(),
						   new Pot4(),
						   new Pot5(),
						   new Pot6(),
						   new Pot7(),
						   new Pot8(),
						   new Pot9(),
						   new Pot10(),
						   new Pot11(),
						   new Pot12() };

  // Create a public blank OutputWriter
  // This can be overwritten to guide output to any object
  public static OutputWriter out = new OutputWriter();


  // Compute the ceiling of a float
  final public static int ceil(float x)
  {
    //return (int)Math.ceil(x);
    return 1 + (int)x;
  }

  // Compute the Standard Deviation of a set of data
  final public static double stdDev(double[] list, double mean)
  {
    double dev = 0;
    for( int i = list.length - 1; i >= 0; --i ) {
      double t = list[i] - mean;
      dev += t * t;
    }
    return Math.sqrt( dev/(list.length-1) );
  }


  // Rounds a double to a specified number of digits
  // Used for pretty printing
  final public static double round(double a, int digits)
  {
    double pow = Math.pow(10,digits - (int)Math.log10(a));
    return ((long)Math.floor(a*pow + .5))/pow;
  }

  final public static String format(double a, int digits)
  {
    int dig = digits - (int) Math.ceil(1 + Math.log10(a));
    return String.format("%20." + dig + "f", a);
  }

  final public static int clamp(int a, int low, int high)
  {
    return (a < low ? low : (a > high ? high : a));
  }
}