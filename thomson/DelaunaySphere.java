/*
 * Computes a Delaunay Triangulation of a set of points constrained
 * to the surface of a sphere.
 *
 * Uses an iterative method with a simplified quadedge data structure
 *
 * The Basic algorithm:
 *
 * Select point p to add to Delaunay Triangulation T
 * Traverse the graph T to find the triangle which contains p
 * Connect p to the vertices of triangle
 * Recursively flip bad edges to restore Delaunay Triangulation
 *
 * Cris Cecka
 */


class DelaunaySphere
    extends Graph
{
  private Edge startEdge;      // The starting vertex for every search
  private Edge[] edges;
  private Edge[] faces;
  int[] coorNum;

  Point[] circumcenters;
  private EdgeByIndex[] dualEdges;

  // Constructs a Delaunay Triangulation from a set of points
  // assumed to be constrained to the sphere.
  DelaunaySphere(Point[] points)
  {
    super(points);
	
    // Create the first face by hand

    Vertex v0 = new Vertex(p[0],0);
    Vertex v1 = new Vertex(p[1],1);

    startEdge = new Edge(v0);

    Vertex v2 = new Vertex(p[2],2);
    Edge e2 = new Edge(v2, startEdge);
    Edge e1 = new Edge(v1, e2);
    startEdge.next = e1;

    Edge e3 = new Edge(v2, null, startEdge);
    Edge e4 = new Edge(v0, e3, e1);
    Edge e5 = new Edge(v1, e4, e2);
    e3.next = e5;

    startEdge.twin = e3;
    e1.twin = e4;
    e2.twin = e5;

    startEdge.faceAt = e1.faceAt = e2.faceAt = new Face();
    e3.faceAt = e4.faceAt = e5.faceAt = new Face();

    // Insert the rest of the points

    for( int i = 3; i < p.length; ++i ) {
      insert(new Vertex(p[i], i));
    }
  }

  // Create an instance with a specified adjacency array
  // Attempts to construct the corresponding graph
  DelaunaySphere(Point[] points, IntList[] adjArray)
  {
    super(points);
    setAdjArray(adjArray);
  }

  // ================== Algorithm Methods =================== //


  // If the points change, we can see if a previous triangulation is still valid
  public boolean isValid()
  {
    return false;

    /*
      getEdges();
		
      // For each edge
      for( int i = 0; i < edges.length; ++i ) {
      Edge e = edges[i];
			
      e.faceAt.isNotValid = true;

      // Check to see if the face can see the opposite point
      if( e.canFaceSee( e.twin.next.dest.p ) )
      return false;
      }
		
      return true;
    */
  }


  // Inserts a vertex v into the Delaunay Triangulation by
  // finding which face contains it,
  // adding edges between v and the vertices of the face,
  // and then restoring the Delaunay Triangulation.
  private void insert(Vertex v)
  {
    Point t = v.p;

    // Find a face that can see the vertex.
    Edge e = overWhichFace(t);
    //if( e == null )
    //	return;

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

    // Add Faces
    e1.faceAt = e6.faceAt = e.faceAt;
    e.faceAt.isNotValid = true;
    e3.faceAt = e2.faceAt = e2.next.faceAt = new Face();
    e5.faceAt = e4.faceAt = e4.next.faceAt = new Face();

    // Restore Delaunay
    organize(e2.next, t);
    organize(e4.next, t);
    organize(e6.next, t);
  }


  // TODO: Instead of creating a new DT every time, perhaps we can use parts of an
  // old one and just update it
  /*
    public boolean update()
    {
    getEdges();

    if( edges.length != p.length*3 - 6 )
    return false;

    return true;

    // check if any neighbor faces can see this point and swap() if they can
    // BUT, that's not enough, need to check that neighbor face
    // projections are increasing as we walk away from the point.
    // if they aren't, this point was kicked in the head and we need to
    // do alot of work...

    // we could _attempt_ to update, and return a boolean if it's not as simple as we'd like.
    // or if it's not simple, just redo everything automatically. (even reusing old edge objects)
    }
  */

  /*
   * Returns an edge of a triangle that can 'see' the point t.
   * Operates in O(logN) time by traversing the graph in a search.
   * The Face is found by maximizing the dot product of t with
   * other points in the Triangulation until we find a face which can see t.
   */
  private Edge overWhichFace(Point t)
  {
    Edge e = startEdge;

    if( e.canFaceSee(t) )
      return e;

    double highest, current;
    boolean lowIsTwinP;
    double a0 = t.dot(e.twin.dest.p);
    double a1 = t.dot(e.dest.p);
    double a2 = t.dot(e.next.dest.p);

    if( a2 > a0 ) {
      if( a2 > a1 ) {
        highest = a2;
        if( lowIsTwinP = (a1 > a0) ) {
          e = e.next.twin.next;
        } else {
          e = e.next.next.twin.next;
        }
      } else {
        highest = a1;
        e = e.next.twin.next;
        lowIsTwinP = false;
      }
    } else {
      if( a2 > a1 ) {
        highest = a0;
        e = e.next.next.twin.next;
        lowIsTwinP = true;
      } else {
        e = e.twin.next;
        if( lowIsTwinP = (a1 > a0) ) {
          highest = a1;
        } else {
          highest = a0;
        }
      }
    }

    // If next.p.dot(t) is largest,  go between this and lasthigh
    // If next.p.dot(t) is mid,      go between highest and this
    // If next.p.dot(t) is smallest, go between highest and this

    //while( !e.canFaceSee(t) ) { // More operations than necessary
    while( true ) {

      // Generalization for non-Spherical conditions
      //if( t is inside simplex <e.face, Origin> OR t is coplanar with e.Face )
      //	return null;

      current = t.dot(e.dest.p);

      if( current > highest ) {
        highest = current;
        if( lowIsTwinP ) {
          e = e.next.twin.next;
          lowIsTwinP = false;
        } else {
          e = e.twin.next;
          lowIsTwinP = true;
        }
      } else {
        if( e.canFaceSee(t) )  // only check when new is not high. May cause an extra
          return e;          // iteration towards the end, but saves for big systems.
        if( lowIsTwinP ) {
          e = e.next.twin.next;
          //lowIsTwinP = true;
        } else {
          e = e.twin.next;
          //lowIsTwinP = false;
        }
      }
    }
  }

  /*
   * Flips edges so that they satisfy convex hull criterion.
   * No Face should be able to "see" a point which is not one of its vertices.
   */
  private void organize(Edge e, Point t)
  {
    if( e.twin.canFaceSee(t) ) {
      swap(e);
      // e.dest.p is now t. But we need to check the two bordering faces.
      organize(e.next.next, t);
      organize(e.twin.next, t);
    }
  }

  /*
   * Flips an edge between two faces.
   * Edge e will point to e.next.dest and e.twin will point to e.twin.next.dest.
   * All graph assumptions are conserved.
   */
  private void swap(Edge e)
  {
    e.faceAt.isNotValid = e.twin.faceAt.isNotValid = true;
    e.next.faceAt = e.twin.faceAt;
    e.twin.next.faceAt = e.faceAt;

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
   * Constructs a list of edges using a Breadth First Search.
   */
  public DirectedEdge[] getEdges()
  {
    if( edges != null )
      return edges;

    coorNum = new int[p.length];
    edges = new Edge[3*p.length - 6];
    Edge eCurr, e;
    int numEdges = 0, stackIndex = 0;

    try {
      e = startEdge;
      do {
        if( e.edgeIndex == -1 && e.twin.edgeIndex == -1 ) {
          e.edgeIndex = numEdges;
          edges[numEdges++] = e;
          ++coorNum[e.dest.index];
          ++coorNum[e.twin.dest.index];
        }
        e = e.twin.next.twin;
      } while( e != startEdge );

      while( stackIndex != numEdges ) {
        eCurr = edges[stackIndex++];

        e = eCurr.next.twin;
        if( e.edgeIndex == -1 && e.twin.edgeIndex == -1 ) {
          e.edgeIndex = numEdges;
          edges[numEdges++] = e;
          ++coorNum[e.dest.index];
          ++coorNum[e.twin.dest.index];
        }

        e = eCurr.next.next.twin;
        if( e.edgeIndex == -1 && e.twin.edgeIndex == -1 ) {
          e.edgeIndex = numEdges;
          edges[numEdges++] = e;
          ++coorNum[e.dest.index];
          ++coorNum[e.twin.dest.index];
        }
      }
    } catch( NullPointerException npe ) {
      System.out.println("Incomplete Graph: " + numEdges);
    }
		
    if( numEdges != edges.length ) {  // Recovery: Something odd happened
      Edge[] tempEdges = edges;
      edges = new Edge[numEdges];
      System.arraycopy(tempEdges, 0, edges, 0, numEdges);
    }
		
    return edges;
  }

  /* Returns the faces of a Graph.
   * Constructs a list of faces using a Breadth First Search.
   */
  public TriangleByIndex[] getFaces()
  {
    if( faces != null )
      return faces;

    faces = new Edge[2*p.length - 4];
    Edge eCurr, e;
    int stackIndex = 0, numFaces = 0;

    faces[numFaces++] = startEdge;
    faces[numFaces++] = startEdge.twin;
    startEdge.faceAt.faceIndex = 0;
    startEdge.twin.faceAt.faceIndex = 1;

    try {
      while( stackIndex != numFaces ) {
        eCurr = faces[stackIndex++];

        e = eCurr.next.twin;
        if( e.faceAt.faceIndex == -1 ) {
          e.faceAt.faceIndex = numFaces;
          faces[numFaces++] = e;
        }

        e = eCurr.next.next.twin;
        if( e.faceAt.faceIndex == -1 ) {
          e.faceAt.faceIndex = numFaces;
          faces[numFaces++] = e;
        }
      }
    } catch( NullPointerException npe ) {
      System.out.println("Incomplete Graph: " + numFaces);

    }
		
    if( numFaces != faces.length ) { // Recover, something went wrong
      Edge[] tempFaces = faces;
      faces = new Edge[numFaces];
      System.arraycopy(tempFaces, 0, faces, 0, numFaces);
    }

    return faces;
  }

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
    
  protected Point circumcenter3D(Point a, Point b, Point c)
  {
    double xba, yba, zba, xca, yca, zca;

    /* Use coordinates relative to point `a' of the triangle. */
    xba = a.x - b.x;
    yba = a.y - b.y;
    zba = a.z - b.z;
    xca = c.x - a.x;
    yca = c.y - a.y;
    zca = c.z - a.z;

    Point result = new Point(yba * zca - yca * zba, zba * xca - zca * xba, xba * yca - xca * yba);
    result.normalize();

    return result;
  }

  public EdgeByIndex[] getDualEdges()
  {
    if( dualEdges != null )
      return dualEdges;

    // Quickie class to make EdgeByIndexes
    class DualEdge implements EdgeByIndex {
      int start, end;
      DualEdge(int s, int e) { start = s; end = e;}
      public int getStartIndex() {return start;}
      public int getEndIndex() {return end;}
    }

    getEdges();    // Create edges
    getFaces();    // Create faces
    dualEdges = new DualEdge[edges.length];

    for( int i = 0; i < edges.length; ++i ) {
      Edge e = edges[i];
      dualEdges[i] = new DualEdge(e.faceAt.faceIndex, e.twin.faceAt.faceIndex);
    }

    return dualEdges;
  }


  /*
   * Create the graph based on adjacency array information
   * Basic procedure:
   *    1) Create edges and twins for each set of linked points
   *    2) Find all the faces and use that information to set the next pointers
   * !!THIS FUNCTION ASSUMES A VALID ADJACENCY ARRAY IS GIVEN!!
   */
  public void setAdjArray(IntList[] newAdjArray)
  {
    Edge[][] edgesByIds = new Edge[newAdjArray.length][];
    Edge[] tempEdges = new Edge[newAdjArray.length*8];
    int counter = 0;

    adjArray = newAdjArray;
    coorNum = new int[adjArray.length];

    // create one vertex for every point
    Vertex[] vtx = new Vertex[adjArray.length];

    // create all vertices, edges, and their twins
    for( int i = 0; i < adjArray.length; ++i ) {
      vtx[i] = new Vertex(p[i], i);

      IntList.Iterator J = adjArray[i].getIterator();
      coorNum[i] = adjArray[i].length();
      edgesByIds[i] = new Edge[adjArray.length];
      while( J.hasNext() ) {
        int j = J.next();
        if( j < i ) {
          Edge edgeJI = new Edge(vtx[i]);
          Edge edgeIJ = new Edge(vtx[j], null, edgeJI);
          edgeJI.twin = edgeIJ;
          edgesByIds[i][j] = edgeIJ;
          edgesByIds[j][i] = edgeJI;
          // store one edge for every pair in the final array
          tempEdges[counter++] = edgeIJ;
        }
      }
    }

    // find each face consisting of a counterclockwise
    // rotation of connected vertices A, B, C
    for( int A = 0; A < adjArray.length; ++A ) {
      // find every face that contains vertex A
      IntList.Iterator adjA = adjArray[A].getIterator();
      while( adjA.hasNext() ) {
        int B = adjA.next();
        if( B > A ) {     // check that B has not already been visited
          // find a C that leads back to A in a counterclockwise direction
          IntList.Iterator adjB = adjArray[B].getIterator();
          while( adjB.hasNext() ) {
            int C = adjB.next();
            if( C > A && adjArray[C].contains(A) ) {
              // if C leads back to A, see if we're going counterclockwise
              // if so, we've found our face
              Point normal = p[A].minus(p[B]).cross( p[C].minus(p[B]) );
              double innerProj;
              if( (innerProj = p[B].dot(normal)) > 0 ) {
                // create the face and break from the while loop
                edgesByIds[A][B].next = edgesByIds[B][C];
                edgesByIds[B][C].next = edgesByIds[C][A];
                edgesByIds[C][A].next = edgesByIds[A][B];

                edgesByIds[A][B].faceAt = edgesByIds[B][C].faceAt = edgesByIds[C][A].faceAt = new Face(normal, innerProj);
                break;
              }
            }

          } // end of while for checking B's neighbors
        } // end of if B has been visited
      } // end of while for checking A's neighbors
    } // end of for loop, all faces (hopefully) created


    // any edge can be the start edge, so lets pick the first one
    startEdge = tempEdges[0];

    // replace the edge array with the new edges
    // and set the edge indices for the stored edges
    edges = new Edge[counter];
    for( int i = 0; i < counter; ++i ) {
      edges[i] = tempEdges[i];
      edges[i].edgeIndex = i;
    }
  }

  // The getEdges method automatically generates the coordination number array
  public int[] getCoorNumArray()
  {
    if( coorNum == null )
      getEdges();

    return coorNum;
  }

  // BAD FORM, don't know what to do about this
  public void setCoorNumArray(int[] coorNum_)
  {
    coorNum = coorNum_;
  }


  // A class for an edge of a delaunay graph over a sphere
  class Edge
      implements DirectedEdge
  {
    public Vertex dest;	             //destination point of the Edge
    public Edge next, twin;	         //points to the next Edge around dest
    public Face faceAt;              //normal of the face defined by this edge
    public int edgeIndex = -1;

    public Edge(Vertex v)
    {
      dest = v;
    }

    public Edge(Vertex v, Edge n)
    {
      dest = v;
      next = n;
    }

    public Edge(Vertex v, Edge n, Edge t)
    {
      dest = v;
      next = n;
      twin = t;
    }

    public boolean canFaceSee(Point p)
    {
      if( faceAt.isNotValid )
        faceAt.update(this);
      return faceAt.canFaceSee(p);
    }

    public Point getNormal()
    {
      if( faceAt.isNotValid )
        faceAt.update(this);
      return faceAt.normal;
    }

    // I hate this method. There should be a much more elegant way to
    // do this without having to do as much (erroneous) bookkeeping.
    public void setTwin(DirectedEdge e)
    {
      Edge edge = (Edge)e;

      if( this.edgeIndex == -1 ) {
        edges[twin.edgeIndex] = this;
        this.edgeIndex = twin.edgeIndex;
        twin.edgeIndex = -1;
      }
      if( edge.edgeIndex != -1 ) {
        edges[edge.edgeIndex] = edge.twin;
        edge.twin.edgeIndex = edge.edgeIndex;
        edge.edgeIndex = -1;
      }

      twin = edge;
      edge.twin = this;
    }

    public void setNext(DirectedEdge e)
    {
      next = (Edge)e;
    }

    public String toString()
    {
      return "<"+twin.dest+", "+dest+">";
    }

    public Vertex getDest()
    {
      return dest;
    }

    public DirectedEdge getTwin()
    {
      return twin;
    }

    public DirectedEdge getNext()
    {
      return next;
    }

    public void setDest(Vertex v)
    {
      dest = v;
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

    // Orientation is important for A, B, C

    public int getA()
    {
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
  }

  static class Face
  {
    public Point normal = new Point();
    private double d;          // The inner projection of the face
    public boolean isNotValid;
    public int faceIndex;

    Face()
    {
      isNotValid = true;
      faceIndex = -1;
    }

    Face(Point normal_, double d_)
    {
      normal = normal_;
      d = d_;
      isNotValid = false;
      faceIndex = -1;
    }

    public void update(Edge e)
    {
      Point a = e.dest.p;
      Edge e2 = e.next;
      Point b = e2.next.dest.p;
      Point c = e2.dest.p;
      double bax = b.x - a.x;
      double bay = b.y - a.y;
      double baz = b.z - a.z;
      double cbx = c.x - b.x;
      double cby = c.y - b.y;
      double cbz = c.z - b.z;
      normal.set(bay*cbz - baz*cby, baz*cbx - bax*cbz, bax*cby - bay*cbx);
      d = normal.dot(a);
      isNotValid = false;
    }

    public boolean canFaceSee(Point p)
    {
      return normal.dot(p) > d;
    }
  }
}
