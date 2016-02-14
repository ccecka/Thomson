/*
 * A class which keeps hold of an Icosahedron for me. This is used
 * primarily as a skeleton for the construct (m,n) method.
 * Right now, the points are analytical. Analytical points can confuse the
 * Delaunay Triangulation because they tend to cause coplanar points in the
 * resulting (m,n) graph. One might consider going back to computational points
 *
 * Cris Cecka
 */


class Icosahedron
{
  private final static Graph dIco;
  private final static Point[] points;

  // Define the points and create the graph just once.
  static {
    points = new Point[12];


    double goldR = (Math.sqrt(5) - 1) / 2;
    double scale = 1.0 / Math.sqrt(1+goldR*goldR);
    goldR *= scale;

    // Do not reorder these points
    // Reordering can cause coplanar errors in the Delaunay
    points[0]  = new Point(     0, -goldR, -scale);
    points[1]  = new Point(     0,  goldR,  scale);
    points[2]  = new Point(     0, -goldR,  scale);
    points[3]  = new Point( scale,      0,  goldR);
    points[4]  = new Point( goldR, -scale,      0);
    points[5]  = new Point(-goldR, -scale,      0);
    points[6]  = new Point(-scale,      0,  goldR);
    points[7]  = new Point(     0,  goldR, -scale);
    points[8]  = new Point( goldR,  scale,      0);
    points[9]  = new Point(-goldR,  scale,      0);
    points[10] = new Point( scale,      0, -goldR);
    points[11] = new Point(-scale,      0, -goldR);

    // Triangulate the points
    dIco = new DelaunaySphere(points);
  }

  // =============== Accessor Methods ========================//

  final public static TriangleByIndex[] getFaces()
  {
    return dIco.getFaces();
  }

  final public static Point[] getPoints()
  {
    return points;
  }

  final public static EdgeByIndex[] getEdges()
  {
    return dIco.getEdges();
  }
}