/*
 * A general holder class to represent a Graph.
 */

abstract class Graph
{
  // The set of points the graph connects
  protected Point[] p;

  // The adjacency array
  protected IntList[] adjArray;

  // abstract constructor
  Graph(Point[] points)
  {
    p = points;
  }

  abstract public boolean isValid();
  abstract public int[] getCoorNumArray();
  abstract public DirectedEdge[] getEdges();
  abstract public void setAdjArray(IntList[] newAdjArray);
  abstract public TriangleByIndex[] getFaces();
  abstract public EdgeByIndex[] getDualEdges();
  abstract public Point[] getCircumcenters();

  // Given a list of edges, we can construct the adjacency array
  protected IntList[] makeAdjArrayFromEdges(EdgeByIndex[] edges)
  {
    IntList[] adjArray = new IntList[p.length];
    for( int i = 0; i < adjArray.length; ++i )
      adjArray[i] = new IntList();

    EdgeByIndex e;
    int start, end;
    for( int i = 0; i < edges.length; ++i ) {
      e = edges[i];
      start = e.getStartIndex();
      end = e.getEndIndex();
      adjArray[start].add(end);
      adjArray[end].add(start);
    }

    return adjArray;
  }

  // Accessor method
  public IntList[] getAdjArray()
  {
    if( adjArray != null )
      return adjArray;

    return adjArray = makeAdjArrayFromEdges(getEdges());
  }

  protected Point circumcenter3D(Point a, Point b, Point c)
  {
    /* Use coordinates relative to point `a' of the triangle. */
    double xba = b.x - a.x;
    double yba = b.y - a.y;
    double zba = b.z - a.z;
    double xca = c.x - a.x;
    double yca = c.y - a.y;
    double zca = c.z - a.z;

    double xcrossbc = yba * zca - yca * zba;
    double ycrossbc = zba * xca - zca * xba;
    double zcrossbc = xba * yca - xca * yba;

    /* Calculate the denominator of the formulae. */
    double denominator = 0.5 / (xcrossbc * xcrossbc + ycrossbc * ycrossbc + zcrossbc * zcrossbc);

    /* Squares of lengths of the edges incident to `a'. */
    double balength = xba * xba + yba * yba + zba * zba;
    double calength = xca * xca + yca * yca + zca * zca;

    /* Calculate offset (from `a') of circumcenter. */
    double xcirca = ((balength * yca - calength * yba) * zcrossbc -
		     (balength * zca - calength * zba) * ycrossbc) * denominator;
    double ycirca = ((balength * zca - calength * zba) * xcrossbc -
		     (balength * xca - calength * xba) * zcrossbc) * denominator;
    double zcirca = ((balength * xca - calength * xba) * ycrossbc -
		     (balength * yca - calength * yba) * xcrossbc) * denominator;

    return new Point(xcirca + a.x, ycirca + a.y, zcirca + a.z);
  }

  // A general class for vertices of the graph
  static class Vertex
  {
    public Point p;
    public int index;

    public Vertex(Point point, int index_)
    {
      p = point;
      index = index_;
    }

    public String toString()
    {
      return Integer.toString(index);
    }
  }
}