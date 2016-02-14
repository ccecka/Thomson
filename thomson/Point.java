/**
 * A class to represent a vector-like spatial point.
 */

import java.math.*;
import java.util.*;
import java.io.*;

final class Point
{
  public double x, y, z;

  public Point()
  {
    x = y = z = 0.0;
  }

  Point(Point p)
  {
    x = p.x; y = p.y; z = p.z;
  }

  Point(double a, double b, double c)
  {
    x = a; y = b; z = c;
  }

  final public Point set(double a, double b, double c)
  {
    x = a; y = b; z = c;
    return this;
  }

  final public double mag()
  {
    return Math.sqrt(x*x + y*y + z*z);
  }

  final public double magSq()
  {
    return x*x + y*y + z*z;
  }

  final public Point scale(double s)
  {
    x *= s; y *= s; z *= s;
    return this;
  }

  final public Point scaled(double s)
  {
    return new Point(x*s, y*s, z*s);
  }

  final public Point add(Point p)
  {
    x += p.x; y += p.y; z += p.z;
    return this;
  }

  final public Point add(double dx, double dy, double dz)
  {
    x += dx; y += dy; z += dz;
    return this;
  }

  final public Point sub(Point p)
  {
    x -= p.x; y -= p.y; z -= p.z;
    return this;
  }

  final public Point sub(double dx, double dy, double dz)
  {
    x -= dx; y -= dy; z -= dz;
    return this;
  }

  final public Point plus(Point p)
  {
    return new Point(x + p.x, y + p.y, z + p.z);
  }

  final public Point plus(double a, double b, double c)
  {
    return new Point(x + a, y + b, z + c);
  }

  final public Point minus(Point p)
  {
    return new Point(x - p.x, y - p.y, z - p.z);
  }

  final public Point minus(double a, double b, double c)
  {
    return new Point(x - a, y - b, z - c);
  }

  final public Point normalize()
  {
    double s = 1.0/Math.sqrt(x*x+y*y+z*z);
    x *= s; y *= s; z *= s;
    return this;
  }

  final public Point normalized()
  {
    double s = 1.0/Math.sqrt(x*x+y*y+z*z);
    return new Point(x*s, y*s, z*s);
  }

  final public double distanceFrom(Point a)
  {
    double dx = x - a.x;
    double dy = y - a.y;
    double dz = z - a.z;
    return Math.sqrt(dx*dx + dy*dy + dz*dz);
  }

  final public double distanceFromSq(Point a)
  {
    double dx = x - a.x;
    double dy = y - a.y;
    double dz = z - a.z;
    return dx*dx + dy*dy + dz*dz;
  }

  final public double dot(Point p)
  {
    return (x*p.x + y*p.y + z*p.z);
  }

  final public double dot(double a, double b, double c)
  {
    return x*a + y*b + z*c;
  }

  final public Point cross(Point p)
  {
    double a = y*p.z-z*p.y;
    double b = z*p.x-x*p.z;
    double c = x*p.y-y*p.x;
    return new Point(a, b, c);
  }

  final public double crossProdMagSq(Point p)
  {
    double a = y*p.z-z*p.y;
    double b = z*p.x-x*p.z;
    double c = x*p.y-y*p.x;
    return a*a + b*b + c*c;
  }

  final public double crossProdMagSq(double px, double py, double pz)
  {
    double a = y*pz-z*py;
    double b = z*px-x*pz;
    double c = x*py-y*px;
    return a*a + b*b + c*c;
  }

  final public double angleWith(Point a)
  {
    return Math.acos(this.dot(a)/(this.mag()*a.mag()));
  }

  // Extra spaces for better alignment
  final public String toString()
  {
    return (x > 0 ? " " + x : x)
        + "\t" + (y > 0 ? " " + y : y)
        + "\t" + (z > 0 ? " " + z : z);
  }

  final public static String getFormattedString(Point[] p)
  {
    StringBuffer result = new StringBuffer();

    if( p.length > 0 )
      result.append(p[0].toString());

    for( int i = 1; i < p.length; ++i ) {
      result.append("\n").append(p[i].toString());
    }

    return result.toString();
  }

  /*
   * Parses a Point contained in a string (any separated format)
   */
  final public static Point parsePoint(String str)
  {
    // Split the string along anything that doesn't make a number
    String[] doubles = str.split("[^-+.Ee0-9]+");

    // The first can be null if non-number prefix
    int k = 0;
    if( doubles[0].equals("") )
      ++k;

    double x = Double.parseDouble(doubles[k]);
    double y = Double.parseDouble(doubles[k+1]);
    double z = Double.parseDouble(doubles[k+2]);

    return new Point(x,y,z);
  }

  /*
   * Reads Points from a large String containing an unspecified number
   * parses and places them into Shell.
   */
  final public static Point[] getPointsFromString(String s)
  {
    Vector<Point> result = new Vector<Point>();
    BufferedReader inStr = new BufferedReader(new StringReader(s));
    String str = null;
    int start;

    try {
      while( (str = inStr.readLine()) != null ) {

	str = str.trim();

	// Skip empty lines
	if( str.equals("") )               // Skip empty lines
	  continue;

	// Remove index tags
	if( (start = str.indexOf(':')) != -1 )   // Remove index tag
	  str = str.substring(start+1,str.length()).trim();

	result.add( Point.parsePoint(str) );
      }
    } catch( Exception e ) {
      Const.out.println("Error Loading Points In Line: " + str);
      return null;
    }

    return (Point[]) result.toArray(new Point[result.size()]);
  }

  /*
   * Converts a byte array to a point array
   */
  final public static Point[] bytes2Points( byte[] data )
  {
    if( data == null ) return null;

    double[] darray = ByteConvert.toDoubleA( data );
    Point[] result = new Point[ darray.length / 3 ];
    for( int k = 0; k < result.length; ++k )
      result[k] = new Point( darray[3*k+0], darray[3*k+1], darray[3*k+2] );

    return result;
  }

  final public static byte[] points2Bytes( Point[] data )
  {
    if( data == null ) return null;

    byte[] result = new byte[3 * 8 * data.length];
    for( int k = 0; k < data.length; ++k ) {
      Point p = data[k];
      System.arraycopy(ByteConvert.toByta(p.x), 0, result, (3*k+0)*8, 8);
      System.arraycopy(ByteConvert.toByta(p.y), 0, result, (3*k+1)*8, 8);
      System.arraycopy(ByteConvert.toByta(p.z), 0, result, (3*k+2)*8, 8);
    }
    return result;
  }
}
