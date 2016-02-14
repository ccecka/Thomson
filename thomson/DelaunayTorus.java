/*
 * Computes a Delaunay Triangulation of a set of points constrained
 * to the surface of a torus.
 *
 * Uses an iterative method with a simplified quadedge data structure
 *
 * The Basic algorithm:
 *
 * Project the points of the Torus to the plane via a conformal mapping
 * Select point p to add to Delaunay Triangulation T
 * Traverse the graph T to find the triangle which contains p
 * Connect p to the vertices of triangle
 * Recursively flip bad edges to restore Delaunay Triangulation
 *
 * Cris Cecka
 */

import java.awt.*;

class DelaunayTorus
    extends Graph
{
  private Edge startEdge;
  private Edge[] edges;
  private Edge[] faces;
  int[] coorNum;

  Point[] circumcenters;
  private EdgeByIndex[] dualEdges;

  // Constructs a Delaunay Triangulation from a set of points
  // assumed to be constrained to a torus.
  DelaunayTorus(Point[] points, Torus torus)
  {
    super(points);

    // Our bounding 'box' for efficient node insertion. These are ignored on output.
    // This triangle will contain all other points.
    VertexTorus n = new VertexTorus(null, 0, -3000);
    VertexTorus se = new VertexTorus(null, 3000, 2000);
    VertexTorus sw = new VertexTorus(null, -2000, 2000);

    startEdge = new Edge(sw);
    Edge e2 = new Edge(n);
    Edge e3 = new Edge(se);
    Edge e4 = new Edge(sw);
    Edge e5 = new Edge(n);
    Edge e6 = new Edge(se);

    startEdge.next = e3;
    e3.next = e5;
    e5.next = startEdge;

    startEdge.setTwins(e2);
    e3.setTwins(e4);
    e5.setTwins(e6);

    double nuFactor = torus.getMaxNu();
    double epSlide = Const.TWOPI;
    double nuSlide = 2*nuFactor;

    for( int i = 0; i < points.length; ++i ) {
      Point r = p[i];

      // Conformal mapping to (ep, nu)

      double epsilon = torus.getTheta(r) + Const.PI;  // ep in [0,2pi]
      double nu = torus.getNu(r) + nuFactor;          // nu in [0,2MaxNu]

      // Keep all vertices in the first quandrant for efficient searching

      double epShift = epsilon + epSlide;
      double nuShift = nu + nuSlide;

      // In order to make the boundaries periodic,
      // we 4-tile the set of points and
      // only save (use data from) those that make up the center

      boolean saveQ1 = false;
      boolean saveQ2 = false;
      boolean saveQ3 = false;
      boolean saveQ4 = false;


      if( epsilon > Const.PI ) {
        if( nu > nuFactor ) {
          saveQ3 = true;      // Save Quadrant 3
        } else {     // nuShift < 3*nuFactor/2
          saveQ2 = true;      // Save Quadrant 2
        }
      } else {         // epShift < 3pi/2
        if( nu > nuFactor ) {
          saveQ4 = true;      // Save Quadrant 4
        } else {    // nuShift < 3*nuFactor/2
          saveQ1 = true;      // Save Quadrant 1
        }
      }

      insert(new VertexTorus(r, epShift, nuShift, i, saveQ1));
      insert(new VertexTorus(r, epsilon, nuShift, i, saveQ2));
      insert(new VertexTorus(r, epsilon, nu, i, saveQ3));
      insert(new VertexTorus(r, epShift, nu, i, saveQ4));
    }

    // The accessor methods need an edge inside the center of the 4-tile
    // Even better: startEdge is an edge of a face which is on the border of
    // the acceptance region.
    startEdge = inWhichTriangle(new VertexTorus(null, epSlide, nuFactor));
  }

  public boolean isValid()
  {
    return false;
  }


  // ================== Algorithm Methods =================== //


  // Inserts a vertex v into the Delaunay Triangulation by
  // finding which triangle contains it,
  // adding edges between v and the vertices of the face,
  // and then restoring the Delaunay Triangulation.
  private void insert(VertexTorus v)
  {
    // Find the triangle v is contained in
    Edge e = inWhichTriangle(v);

    // Add edges
    Edge e2 = new Edge(e.dest);
    Edge e4 = new Edge(e.next.dest);
    Edge e6 = new Edge(e.twin.dest);

    Edge e1 = new Edge(v, e6, e2);
    Edge e3 = new Edge(v, e2, e4);
    Edge e5 = new Edge(v, e4, e6);

    e2.next = e.next;
    e4.next = e.next.next;
    e6.next = e;
    e.next.next.next = e5;
    e.next.next = e3;
    e.next = e1;

    e2.twin = e1;
    e4.twin = e3;
    e6.twin = e5;

    // Restore Delaunay
    organize(e6.next);
    organize(e2.next);
    organize(e4.next);
  }


  /*
   * Returns an edge of a triangle that contains the point t.
   * Operates in O(logN) time by binary search.
   */
  private Edge inWhichTriangle(VertexTorus t)
  {
    // Right now, startEdge is the bottom of the BBox
    Edge e = startEdge;

    while( isOnLeft(t, e.twin.dest, e.dest) ) {
      Edge n = e.next;
      VertexTorus a = n.dest;
      if( (t.y * a.x) - (t.x * a.y) > 0 )   // isOnLeft(e.next.dest, origin, t))
        e = n.twin;
      else
        e = n.next.twin;
    }
    return e.twin;
  }

  // Returns true if t is on the left of the line from a to b
  private boolean isOnLeft(VertexTorus t, VertexTorus a, VertexTorus b)
  {
    return ((a.x * (t.y - b.y) + a.y * (b.x - t.x) - (b.x * t.y) + (b.y * t.x)) > 0);
  }

  /*
   * Flips edges so that they satisfy the circle criterion.
   * The circle defined by the three points of a triangle should contain no other point
   */
  private void organize(Edge e)
  {
    if( !isBbox(e) && inCircle(e.dest, e.next.dest, e.twin.dest, e.twin.next.dest) ) {
      swap(e);
      organize(e.next.next);
      organize(e.twin.next);
    }
  }

  // Returns true if Edge e is one connected to our bounding 'box'
  private boolean isBbox(Edge e)
  {
    return (e.dest.index == -1 && e.twin.dest.index == -1);
  }

  // True if s is inside the circle formed by p, q, and r
  private boolean inCircle(VertexTorus p, VertexTorus q, VertexTorus r, VertexTorus s)
  {
    double ax = q.x - p.x;
    double ay = p.y - q.y;
    double aa = (ax * ax) + (ay * ay);
    double bx = r.x - p.x;
    double by = p.y - r.y;
    double bb = (bx * bx) + (by * by);
    double cx = s.x - p.x;
    double cy = p.y - s.y;
    double cc = (cx * cx) + (cy * cy);
    return (( aa * (bx * cy - by * cx)
              - bb * (ax * cy - ay * cx)
              + cc * (ax * by - ay * bx)) < 0);
  }

  /*
   * Flips an edge between two triangles.
   * Edge e will point to e.next.dest and e.twin will point to e.twin.next.dest.
   */
  private void swap(Edge e)
  {
    // assuming e is not the bbox

    e.dest = e.next.dest;
    e.twin.dest = e.twin.next.dest;
    e.next.next.next = e.twin.next;
    e.twin.next.next.next = e.next;
    e.next = e.next.next;
    e.twin.next = e.twin.next.next;
    e.next.next.next = e;
    e.twin.next.next.next = e.twin;
  }


  // ================== Accessor Methods ===================== //

  /* Returns the edges (no twins) of a Graph.
   * Constructs a list of edges using constrained Breadth First Search.
   */
  synchronized public DirectedEdge[] getEdges()
  {
    if( edges != null )
      return edges;

    // A boolean table to keep track of which edges we've kept
    boolean[][] eSet = new boolean[p.length][p.length];
    coorNum = new int[p.length];
    edges = new Edge[p.length*3];
    Edge eCurr, e;
    VertexTorus v0, v1;
    int i0, i1;
    int numEdges = 0, stackIndex = 0;

    // Initialize with a starting triangle
    Edge start = startEdge;
    for( int i = 0; i < 3; ++i ) {
      v0 = start.dest;
      v1 = start.twin.dest;
      i0 = v0.index;
      i1 = v1.index;
      // Only add edges that aren't BBox edges, define an adjacency which hasn't been accounted for
      // and have at least one vertex which is inside the center of the 4-tile
      if( i1 != -1 && !(eSet[i0][i1] || eSet[i1][i0]) && (v0.saved || v1.saved) ) {
        eSet[i1][i0] = true;
        edges[numEdges++] = start;
        ++coorNum[i0];
        ++coorNum[i1];
      }

      start = start.twin.next.twin;
    }

    // Constrained Breadth First Search
    while( stackIndex != numEdges )
    {
      eCurr = edges[stackIndex++];

      e = eCurr.next.twin;
      v0 = e.dest;
      i0 = v0.index;
      v1 = e.twin.dest;
      i1 = v1.index;
      if( i1 != -1 && !(eSet[i0][i1] || eSet[i1][i0]) && (v0.saved || v1.saved) ) {
        eSet[i1][i0] = true;
        edges[numEdges++] = e;
        ++coorNum[i0];
        ++coorNum[i1];
      }

      e = eCurr.next.next.twin;
      v0 = e.dest;
      i0 = v0.index;
      v1 = e.twin.dest;
      i1 = v1.index;
      if( i0 != -1 && !(eSet[i0][i1] || eSet[i1][i0]) && (v0.saved || v1.saved) ) {
        eSet[i1][i0] = true;
        edges[numEdges++] = e;
        ++coorNum[i0];
        ++coorNum[i1];
      }
    }

    if( numEdges != edges.length ) {      // Recovery, something went wrong
      System.out.println("Incomplete Graph: " + numEdges);
      Edge[] tempEdges = edges;
      edges = new Edge[numEdges];
      System.arraycopy(tempEdges, 0, edges, 0, numEdges);
    }

    return edges;
  }

  /* Returns the faces of a Delaunay Torus graph.
   * A directed edge represents a face.
   * This algorithm begins with two home faces that are known
   * to be contained in our saved graph (inside the center of the 4-tile)
   * and uses Breadth First Search to find the rest.
   *
   * Note: Two different objects can refer to the same face in the returned list.
   *            (though only one or two, which occur at the corners of
   *                 the acceptance region)
   */
  synchronized public TriangleByIndex[] getFaces()
  {
    if( faces != null )
      return faces;

    Edge[] tempFaces = new Edge[p.length*3];   // should be *2, but can't get uniqueness

    int stackIndex = 0, numFaces = 0;

    // Give the edges a face and add the startFace
    tempFaces[numFaces++] = startEdge;
    startEdge.faceAt = startEdge.next.faceAt = startEdge.next.next.faceAt = new Face(0);
    tempFaces[numFaces++] = startEdge.twin;
    startEdge.twin.faceAt = startEdge.twin.next.faceAt = startEdge.twin.next.next.faceAt = new Face(1);

    while( stackIndex != numFaces ) {
      Edge eCurr = tempFaces[stackIndex++];

      Edge e0 = eCurr.next.twin;
      VertexTorus v0 = e0.dest;
      VertexTorus v2 = e0.twin.dest;
      // Only add a face if it hasn't been created yet and one of its vertices
      // are contained in the acceptance region.
      if( e0.faceAt == null && (v0.saved || v2.saved) ) { //twoOrMore(e0.dest.saved, e0.next.dest.saved, e0.twin.dest.saved) ) {
        e0.faceAt = e0.next.faceAt = e0.next.next.faceAt = new Face(numFaces);
        tempFaces[numFaces++] = e0;
      }

      e0 = eCurr.next.next.twin;
      v0 = e0.dest;
      v2 = e0.twin.dest;
      if( e0.faceAt == null && (v0.saved || v2.saved) ) { //twoOrMore(e0.dest.saved, e0.next.dest.saved, e0.twin.dest.saved) ) {
        e0.faceAt = e0.next.faceAt = e0.next.next.faceAt = new Face(numFaces);
        tempFaces[numFaces++] = e0;
      }
    }

    faces = new Edge[numFaces];             // Due to the periodic boundary conditions,
    // the faces are not guaranteed to be unique
    System.arraycopy(tempFaces, 0, faces, 0, numFaces);

    return faces;
  }

  //private boolean twoOrMore(boolean a, boolean b, boolean c)
  //{
  //	return (a && (b || c)) || (b && (a || c)) || (c && (a || b));
  //}

  public Point[] getCircumcenters()
  {
    if( circumcenters != null )
      return circumcenters;

    getFaces();    // Set the faceIndex's
    circumcenters = new Point[faces.length];

    for( int i = 0; i < faces.length; ++i ) {
      Edge e = faces[i];
      circumcenters[e.faceAt.faceIndex] = circumcenter3D(e.dest.p, e.next.dest.p, e.twin.dest.p);
    }

    return circumcenters;
  }

  public EdgeByIndex[] getDualEdges()
  {
    if( dualEdges != null )
      return dualEdges;
    if( faces == null )
      getFaces();

    // Quickie class to make EdgeByIndexes
    class DualEdge implements EdgeByIndex {
      int start, end;
      DualEdge(int s, int e) { start = s; end = e;}
      public int getStartIndex() {return start;}
      public int getEndIndex() {return end;}
    }

    getEdges();    // Create edges
    dualEdges = new DualEdge[edges.length];

    for( int i = 0; i < edges.length; ++i ) {
      Edge e = edges[i];
      dualEdges[i] = new DualEdge(e.faceAt.faceIndex, e.twin.faceAt.faceIndex);
    }

    return dualEdges;
  }


  // Dummy for now.
  // Same as in DelaunaySphere except need point normals to determine inner projection
  public void setAdjArray(IntList[] newAdjArray)
  {
  }

  // The getEdges method automatically generates the coordination number array
  public int[] getCoorNumArray()
  {
    if( coorNum == null )
      getEdges();

    return coorNum;
  }


  static class VertexTorus extends Vertex
  {
    public double x, y;
    public boolean saved;

    VertexTorus(Point p_, double xVal, double yVal, int index_, boolean saved_)
    {
      super(p_, index_);
      x = xVal;
      y = yVal;
      saved = saved_;
    }

    VertexTorus(Point p_, double xVal, double yVal, int index_)
    {
      super(p_, index_);
      x = xVal;
      y = yVal;
    }

    VertexTorus(Point p_, double xVal, double yVal)
    {
      super(p_, -1);
      x = xVal;
      y = yVal;
    }

    public String toString()
    {
      return "("+x+", "+y+"):"+index;
    }
  }

  class Edge
      implements DirectedEdge
  {
    public VertexTorus dest;	  //destination point of the Edge
    public Edge next, twin;	  //points to the next Edge around dest
    public Face faceAt;

    public Edge(VertexTorus v)
    {
      dest = v;
    }

    public Edge(VertexTorus v, Edge n)
    {
      dest = v;
      next = n;
    }

    public Edge(VertexTorus v, Edge n, Edge t)
    {
      dest = v;
      next = n;
      twin = t;
    }

    public DirectedEdge getTwin()
    {
      return twin;
    }

    public DirectedEdge getNext()
    {
      return next;
    }

    public void setTwin(DirectedEdge e)
    {
      Edge edge = (Edge)e;

      /*if( this.edgeIndex == -1 ) {
        edges[twin.edgeIndex] = this;
        this.edgeIndex = twin.edgeIndex;
        twin.edgeIndex = -1;
        }
        if( edge.edgeIndex != -1 ) {
        edges[edge.edgeIndex] = edge.twin;
        edge.twin.edgeIndex = edge.edgeIndex;
        edge.edgeIndex = -1;
        }*/

      twin = edge;
      edge.twin = this;
    }

    public void setNext(DirectedEdge e)
    {
      next = (Edge)e;
    }

    public void setTwins(Edge e)
    {
      twin = e;
      e.twin = this;
    }

    public String toString()
    {
      return "<"+dest+">";
    }

    public Vertex getDest()
    {
      return dest;
    }

    public void setDest(Vertex v)
    {
      dest = (VertexTorus)v;
      if( faceAt != null )
        faceAt.isNotValid = true;
    }

    public int getStartIndex()
    {
      return twin.dest.index;
    }

    public int getEndIndex()
    {
      return dest.index;
    }

    public int getA()          // Orientation of the Face is what's
    {                          // important for A, B, and C
      return dest.index;
    }

    public int getB()
    {
      return twin.dest.index;
    }

    public int getC()
    {
      return next.dest.index;
    }

    public Point getNormal()
    {
      if( faceAt.isNotValid )
        faceAt.update(this);
      return faceAt.norm;
    }
  }

  static class Face
  {
    public Point norm;
    private double d;    // The inner projection of the face
    public boolean isNotValid;
    public int faceIndex;

    Face(int index)
    {
      isNotValid = true;
      faceIndex = index;
    }

    public void update(Edge e)
    {
      Point a = e.dest.p;
      Edge e2 = e.next;
      Point b = e2.next.dest.p;
      Point c = e2.dest.p;
      norm = b.minus(a).cross(c.minus(b));
      d = norm.dot(a);
    }
  }
}
