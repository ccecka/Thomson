/*
 * A complicated class which is used to attempt to construct a cap from
 * a scar.
 *
 * Cris Cecka, Alan Middleton
 */


class LatticeDisk
  implements CapOwner
{
  Point[] p;

  Graph d;
  DirectedEdge[] edges;
  IntList[] adjArray;
  int[] coorNum;

  boolean[] marked;
  boolean[] inBorder;

  IntList markedList;
  IntList border;
  Path cBorder;

  int totCharge, numSides, sideLength;
  boolean ccw;

  private final static int deltax[]  = {1, 0,-1,-1, 0, 1};
  private final static int deltay[]  = {0, 1, 1, 0,-1,-1};

  boolean isValid;

  LatticeDisk(int start, Point[] p_, Graph d_)
  {
    isValid = false;
    p = p_;
    d = d_;

    markedList = new IntList(start);
    edges = d_.getEdges();
    coorNum = d_.getCoorNumArray();

    if( coorNum[start] == 6 ) {
      Const.out.println("Can't Start Cap From 6");
      return;
    }

    adjArray = d_.getAdjArray();

    marked = new boolean[coorNum.length];
    marked[start] = true;
    boolean[] tempInBorder = new boolean[coorNum.length];

    IntList sixBorder = getSixBorder(start, tempInBorder);
    cBorder = new Path();
    if( totCharge == 0 || sixBorder.length() == 0 || !cBorder.setCoords(sixBorder.peek(), tempInBorder) ) {
      Const.out.println("Non-Zero Berger Vector.");
      return;
    }

    if( !cBorder.growSixesToPent(cBorder.setRanges()) ) {
      return;
    }

    border = cBorder.getIndexList();
    inBorder = new boolean[p.length];
    IntList.Iterator I = border.getIterator();
    while( I.hasNext() )
      inBorder[I.next()] = true;

    numSides = 6 - totCharge;
    sideLength = cBorder.length()/numSides;
    isValid = true;
    Const.out.println("Cap Successful: " + totCharge + " Charge");
  }

  public int numSides()
  {
    return numSides;
  }

  public boolean isValid()
  {
    return isValid;
  }

  public IntList getPointIndexList()
  {
    return markedList;
  }

  public Graph getGraph()
  {
    return d;
  }

  public Cap getCap()
  {
    Point[] capPoints = new Point[markedList.length()];
    IntList[] capAdjArray = new IntList[capPoints.length];
    int[] capCoorNum = new int[capPoints.length];

    int[] idxTranslation = new int[p.length];

    IntList.Iterator markedIter = markedList.getIterator();
    int i=0;
    while( markedIter.hasNext() ) {
      int idx = markedIter.next();
      capPoints[i] = p[idx];
      idxTranslation[idx] = i;
      capAdjArray[i] = adjArray[idx];
      if( inBorder[idx] )
	capCoorNum[i] = 6;
      else
	capCoorNum[i] = coorNum[idx];
      ++i;
    }

    // translate the adjacency array from old to new index values
    // by testing and saving only marked entries
    for( i=0; i < capAdjArray.length; ++i ) {
      IntList newNeighbors = new IntList();
      IntList.Iterator adjIter = capAdjArray[i].getIterator();

      while( adjIter.hasNext() ) {
	int idx = adjIter.next();
	if( marked[idx] )
	  newNeighbors.add(idxTranslation[idx]);
      }
      capAdjArray[i] = newNeighbors;
    }

    return new Cap(capPoints, capAdjArray, capCoorNum, totCharge, numSides, sideLength);
  }

  public void rotateCap(double rad)
  {
    cBorder.rotateEdgeRelations(rad > 0);

    Matrix3D mat = new Matrix3D();
    Point axis = new Point();
    IntList.Iterator borderIter = border.getIterator();
    while( borderIter.hasNext() ) {
      axis.add(p[borderIter.next()]);
    }

    mat.prot(axis, rad);
    IntList.Iterator markedIter = markedList.getIterator();
    while( markedIter.hasNext() ) {
      int i = markedIter.next();
      if( marked[i] && !inBorder[i] ) {
	Point ip = p[i];
	mat.transform(ip);
      }
    }
  }

  private IntList getSixBorder(int startLoc, boolean[] inBorder)
  {
    IntList nonTrimmedBorder = new IntList();
    IntList defects = new IntList(startLoc);
    totCharge = 6 - coorNum[startLoc];

    while( defects.length() > 0 ) {
      IntList.Iterator I = adjArray[defects.pop()].getIterator();

      while( I.hasNext() ) {
	int i = I.next();
	if( !marked[i] ) {
	  marked[i] = true;
	  markedList.add(i);

	  if( coorNum[i] == 6 )
	    nonTrimmedBorder.add(i);
	  else {
	    totCharge += 6 - coorNum[i];
	    defects.add(i);
	  }
	}
      }
    }

    // Trim those that have only marked neighbors
    IntList sixBorder = new IntList();
    IntList.Iterator borderIter = nonTrimmedBorder.getIterator();

    while( borderIter.hasNext() ) {
      int i = borderIter.next();
      IntList.Iterator J = adjArray[i].getIterator();
      while( J.hasNext() ) {
	if( !marked[J.next()] ) {
	  sixBorder.add(i);
	  inBorder[i] = true;
	  break;
	}
      }
    }

    //Const.out.println("-+-+-+-+-+- Total charge in loop is " + totCharge);
    return sixBorder;
  }

  private int hexdist(int Ax, int Ay, int Bx, int By)
  {
    int delx = Ax - Bx;
    int dely = Ay - By;
    if (delx < 0) {
      delx = -delx;
      dely = -dely;
    }
    if (dely >= 0) {
      return delx + dely;
    }
    if (delx > -dely)
      return delx;
    else
      return -dely;
  }

  private int countMarkedNbrs(int index)
  {
    IntList.Iterator I = adjArray[index].getIterator();
    int numNonMarkedNbrs = 0;
    while( I.hasNext() ) {
      if( marked[I.next()] )
	++numNonMarkedNbrs;
    }
    return numNonMarkedNbrs;
  }

  // Constructs an empty Path
  class Path
  {
    City lastInsert;
    City lastCity;
    City firstCity;
    int length;  //
    int locDx; // location of the "defect" on the unwrapped flat triang. lattice
    int locDy;

    Path()
    {
      length = 0;
    }

    // Add a City to the Path
    public void add(City c)
    {
      if( lastInsert == null ) {
	c.next = c;
	c.prev = c;
      }
      else {
	c.prev = lastInsert;
	c.next = lastInsert.next;
	c.prev.next = c;
	c.next.prev = c;
      }

      lastInsert = c;
      if (lastCity == null || c.prev == lastCity)
	lastCity = c;
      if (firstCity == null) // note asymmetry vs. lastCity: adds are AFTER
	firstCity = c;
      ++length;
    }

    public void snip(City c)
    {
      if (c == lastCity)   lastCity = c.prev;
      if (c == firstCity)  firstCity = c.next;
      if (lastInsert == c) lastInsert = c.prev;
      c.prev.next = c.next;
      c.next.prev = c.prev;
      c.prev = null;
      c.next = null;
      --length;
    }

    public int length()
    {
      return length;
    }

    // reference the "defect", compute distances to it, for all
    // 6's in the six-path
    // Returns maximum range
    public City setRanges()
    {
      City maxCity = null;
      City c = firstCity; // was lastInsert
      int maxRange = 0;
      do {
	c.distanceTo5 = hexdist(c.x, c.y, locDx, locDy);
	if (c.distanceTo5 > maxRange) {
	  maxCity = c;
	  maxRange = c.distanceTo5;
	}
	c = c.next;
      } while( c != firstCity );
      //Const.out.println("First city loc " + c.x + " " + c.y);
      //System.out.println("MaxDist: " + maxRange);
      return maxCity;
    }

    public boolean growSixesToPent(City maxCity)
    { // maxCity is a farthest city
      // Border path will be grown out to a pentagon of maxRange distance from loc5
      // go around six path - work on neighbors of active sites:
      // if neighbor unmarked and connected to two known
      // cities, can compute position - so do so and mark and potentially add to active sites..
      // repeatedly go around until all at max range.
      // COMPLICATIONS: (1) not really set up as a connectedness search, especially
      // since cities can't be looked up by index. (2) calculating the wrap around cut
      // (3) knowing when to snip a city from the path -
      int maxRange = maxCity.distanceTo5;
      int activeCount = 0;
      City currC = maxCity;
      boolean[] active = new boolean[coorNum.length];

      //Const.out.println("maxRange is " + maxRange);
      do {
	if( currC.distanceTo5 != maxRange ) {
	  activeCount++;
	  active[currC.index] = true;
	} else { // only grow from non max cities
	  active[currC.index] = false;
	}
	currC = currC.next;
      } while (currC != maxCity);

      while (activeCount > 0) {  // repeat until all at max range (or bail if run into defect)
	// Const.out.println("Active cities count is " + activeCount);
	// go through list to find active city - maxRange are inactive, but still in path

	searchForActive:
	do { // look at a currently active city - verify it has unmarked neighbors, if not, snip
	  currC = currC.next;
	  // a city is inactivated if all nbrs are marked - active cities are marked
	  if( active[currC.index] ) {

	    IntList.Iterator I = adjArray[currC.index].getIterator();

	    while( I.hasNext() ) {
	      if( !marked[I.next()] )
		continue searchForActive;
	    }

	    // No marked nbrs, so deactivate
	    City tmp = currC.next;
	    active[currC.index] = false;
	    snip(currC);
	    activeCount--;
	    currC = tmp.prev;
	  }
	} while( active[currC.index] == false && activeCount > 0 );

	if( activeCount > 0 ) {
	  City prevC = currC.prev;
	  // step over neighbors, looking for unmarked one, so not in cluster -
	  IntList.Iterator I = adjArray[currC.index].getIterator();
	  while( I.hasNext() ) {
	    int i = I.next();
	    if( !marked[i] ) { // potential city to add - see if it is connected to the previous city
	      IntList.Iterator J = adjArray[i].getIterator();
	      while( J.hasNext() ) {
		int j = J.next();
		if( j == prevC.index ) { // can triangulate!

		  // add i in, now, using j (=currC.prev) and currC to triangulate
		  // new coords = rotate(curr-prev)+prev
		  int delx, dely;
		  if (currC == firstCity) { // prev City is on other side of cut, so correct delxy
		    delx = locDx + rotateAboutCutX(currC.x - locDx, currC.y - locDy) - prevC.x;
		    dely = locDy + rotateAboutCutY(currC.x - locDx, currC.y - locDy) - prevC.y;
		  } else {
		    delx = currC.x - prevC.x;
		    dely = currC.y - prevC.y;
		  }
		  // now rotate the delta vector
		  int rotx = rotateX(delx, dely);
		  int roty = rotateY(delx, dely);


		  lastInsert = prevC;  // add() cares about lastInsert
		  // new coords are prevC's + rotated delta vector
		  add(new City(i, prevC.x + rotx, prevC.y + roty));
		  City newC = lastInsert;
		  if( newC.prev == lastCity ) // patch up lastCity, if needed
		    lastCity = newC;

		  newC.distanceTo5 = hexdist(newC.x, newC.y, locDx, locDy);

		  // now bookkeeping on countNonMax - check additions and snips:
		  marked[i] = true;                   // mark as part of cluster
		  markedList.add(i);

		  if (newC.distanceTo5 != maxRange) { // see if at active edge
		    if( coorNum[i] != 6 ) {
		      Const.out.println("Not Completing Cap - Collided Defect: "
					+ i + " - " + coorNum[i]);
		      return false;   // growing pentagon has run into a defect - bail
		    }
		    activeCount++;
		    active[newC.index] = true;
		  }
		  break; // once triangulate, go on to next City
		} // end of if: possibility to triangulate
	      } // end of for: j, potential nbrs to i
	    } // end of if marked[i] == 0
	  } // end of for: over nbrs i of currC
	} // end of while (activeCount > 0)
      } // end of growSixesToPent

      return true;
    }


    // go around trimmed border set, start given, and set (x,y)'s
    // Area computed using a lattice Green's theorem - check that
    // placement of defect *reduces* area.
    // The vector field integrated along the path is proportional
    // to the real x-coord of the middle of the segment, which 2*x + y.
    public boolean setCoords(int currPoint, boolean[] inBorder)
    {
      int loopArea = 0;
      int finalx, finaly;
      int t = 0;
      int direction = 0;   // 6 possibilities

      add(new City(currPoint, 0, 0));
      firstCity = lastInsert;
      lastCity = lastInsert;

      outerLoop:
      while (inBorder[currPoint]) {
	inBorder[currPoint] = false; // pull out of unassigned set
	IntList.Iterator I = adjArray[lastInsert.index].getIterator();
	while( I.hasNext() ) {
	  int nbr = I.next();
	  if( inBorder[nbr] ) {
	    direction = (direction+6+(4-countMarkedNbrs(lastInsert.index))) % 6;
	    int newx = lastInsert.x + deltax[direction];
	    int newy = lastInsert.y + deltay[direction];
	    //Const.out.println("curr1 " + lastInsert.x +
	    //		   " " + lastInsert.y + " area: " + loopArea);
	    loopArea += deltay[direction] *
	      (2 * (lastInsert.x + newx) + lastInsert.y + newy);
	    add(new City(nbr, newx, newy));
	    //System.out.println("curr2 " + lastInsert.x +
	    //		   " " + lastInsert.y + " area: " + loopArea);
	    currPoint = nbr;
	    continue outerLoop;
	  }
	}  // end for on nbrs of icurr
      } // end of path construction

      // add in last step, to get coords of "final" point -
      // which is actually duplicate of the first.
      direction = (direction+6+(4-countMarkedNbrs(lastInsert.index))) % 6;
      finalx = lastInsert.x + deltax[direction];
      finaly = lastInsert.y + deltay[direction];
      // add delta from last real one in path to image
      loopArea += deltay[direction] *
	(2 * (lastInsert.x + finalx) + lastInsert.y + finaly);
      // last delta to close path is add (-finalx,-finaly),
      // starting at (fx,fy)
      // connecting to (0,0)
      loopArea += -finaly * (2 * finalx + finaly);
      //System.out.println("final(image) " + finalx + " " + finaly);
      //System.out.println("area of closed path " + loopArea);
      // Now rotate closing segment
      // by pi/3 (1,0) -> (1,-1); (0,1)->(1,0)
      // for negative area, -pi/3 for positive area.
      // location of the "defect" in flat space.

      return loopArea != 0 && findCenterDefect((ccw = loopArea > 0), finalx, finaly);
      //System.out.println("Defect: (" + locDx + ", " + locDy + ")");
    }

    /*
     * A Path can be closed when the Berger Vector is zero. This occurs
     * when the final coordinate is equidistant with the start
     * vertex to two other vertices faithful to the lattice.
     * The rotation direction and magnitude depend on the interior charge.
     *                         cos - sin/sqrt(3)      -2sin/sqrt(3)
     * Triangular rotMatrix =
     *                         2sin/sqrt(3)           cos + sin/sqrt(3)
     */
    private boolean findCenterDefect(boolean ccw, int finalx, int finaly)
    {
      try {
	int s = (ccw ? 1 : -1);
	switch( totCharge*s ) {
	case -3: case 3:        // 0-deg rot, scale 1/2
	  locDx = assertInt(finalx/2.0f);
	  locDy = assertInt(finaly/2.0f);              break;
	case -2:                // pi/6 rot, scale 1/sqrt(3)
	  locDx = assertInt((finalx - finaly)/3.0f);
	  locDy = assertInt((finalx + 2*finaly)/3.0f); break;

	case -1:                // pi/3 rot, scale 1
	  locDx = -finaly;
	  locDy = finaly + finalx;                     break;
	case 1:                 // -pi/3 rot, scale 1
	  locDx = finalx + finaly;
	  locDy = -finalx;                             break;
	case 2:                 // -pi/6 rot, scale 1/sqrt(3)
	  locDx = assertInt((2*finalx + finaly)/3.0f);
	  locDy = assertInt((-finalx + finaly)/3.0f);  break;

	}
      } catch( RuntimeException re ) {
	return false;
      }

      return true;
    }

    private int rotateAboutCutX(int x, int y)
    {
      // NOTE: rotating opposite direction as before
      int s = (ccw ? 1 : -1);

      switch( totCharge*s ) {
      case -3: case 3:        // pi rot
	return -x;
      case -2:                // 2pi/3 rot
	return -x - y;
      case -1:                // pi/3 rot
	return -y;
      case 1:                 // -pi/3 rot
	return x + y;
      case 2:                 // -2pi/3 rot
	return y;
      }
      return 0;
    }

    private int rotateAboutCutY(int x, int y)
    {
      int s = (ccw ? 1 : -1);

      switch( totCharge*s ) {
      case -3: case 3:        // pi rot
	return -y;
      case -2:                // 2pi/3 rot
	return x;
      case -1:                // pi/3 rot
	return x + y;
      case 1:                 // -pi/3 rot
	return -x;
      case 2:                 // -2pi/3 rot
	return -x - y;
      }
      return 0;
    }

    private int rotateX(int x, int y)
    {
      if( ccw )
	return x + y;
      else
	return -y;
    }

    private int rotateY(int x, int y)
    {
      if( ccw )
	return -x;
      else
	return x+y;
    }

    private int assertInt(float a) throws RuntimeException
    {
      if( a != (int)a ) {
	throw new RuntimeException();
      }

      return (int)a;
    }

    public IntList getIndexList()
    {
      IntList result = new IntList();
      City c = firstCity;
      do {
	result.add(c.index);
	c = c.next;
      } while( c != firstCity );

      return result;
    }

    public void rotateEdgeRelations(boolean ccw)
    {
      if( sideLength == 1 )
	return;

      //System.out.println("numSides: " + numSides + "   sideLength: " + sideLength);

      // First, get an edge starting from a corner and pointing to the inside
      DirectedEdge followingEdge = findCornerEdge(getCornerFrom(firstCity));

      // From this edge, follow the graph to find the next corner edge like it
      DirectedEdge leadingEdge = followingEdge.getNext();
      while( countMarkedNbrs(leadingEdge.getEndIndex()) != 3 ) {
	leadingEdge = leadingEdge.getTwin().getNext().getNext().getTwin().getNext();
      }
      leadingEdge = leadingEdge.getTwin();

      //System.out.println("Found Edges <" + followingEdge.getStartIndex() + ", " + followingEdge.getEndIndex() + ">, <"+ leadingEdge.getStartIndex() + ", " + leadingEdge.getEndIndex() + ">");

      // Increment both so we start at the first inside rotating edge
      leadingEdge = leadingEdge.getNext().getTwin().getNext();
      followingEdge = followingEdge.getNext().getTwin().getNext();

      int sideLengthM1 = sideLength - 1;
      DirectedEdge[] origTwin = new DirectedEdge[sideLengthM1];
      Graph.Vertex[] origDest = new Graph.Vertex[sideLengthM1];

      Graph.Vertex newDest = null;

      int iters = (ccw ? numSides - 1 : 1);
      for( int iter = 0; iter < iters; ++iter ) {   // HACK for rotating opposite directions
	for( int i = 0; i < numSides; ++i ) {         // For each side of the cap
	  for( int j = 0; j < sideLengthM1; ++j ) {    // Do sideLength-1 times
	    //System.out.println(followingEdge + ",   " + leadingEdge);

	    // We need to reassign vertices and twins along this inner path

	    // Since we are essentially swapping edges in a circular
	    // pattern, we need to save the first few we overwrite
	    // and use them later.
	    if( i == 0 ) {    // need to save these, we're overwriting them
	      origTwin[j] = followingEdge.getTwin();
	      origDest[j] = followingEdge.getDest();
	      newDest = leadingEdge.getDest();
	      followingEdge.setTwin(leadingEdge.getTwin());
	    } else if( i == numSides - 1 ) { // doing the last side so we need the saved data
	      newDest = origDest[j];
	      followingEdge.setTwin(origTwin[j]);
	    } else {          // middle case, we're fine
	      newDest = leadingEdge.getDest();
	      followingEdge.setTwin(leadingEdge.getTwin());
	    }

	    // Reassign all edges pointing to this vertex to the new Vertex
	    followingEdge.setDest(newDest);
	    followingEdge = followingEdge.getNext().getTwin();
	    followingEdge.setDest(newDest);
	    followingEdge = followingEdge.getNext().getTwin();
	    followingEdge.setDest(newDest);

	    // leading edge needs to catch up
	    leadingEdge = leadingEdge.getNext().getTwin().getNext().getTwin().getNext();
	    followingEdge = followingEdge.getNext();
	  }

	  //System.out.println(followingEdge + ",   " + leadingEdge);
	  leadingEdge = leadingEdge.getTwin().getNext(); // Go around the corner
	  followingEdge = followingEdge.getTwin();
	  followingEdge.setDest(newDest);
	  followingEdge = followingEdge.getNext();
	}
      }

      // *******Need to update AdjArray************
      IntList.Iterator markedIter = markedList.getIterator();
      while( markedIter.hasNext() )
	adjArray[markedIter.next()] = new IntList();

      for( int i = 0; i < edges.length; ++i ) {
	EdgeByIndex e = edges[i];
	int start = e.getStartIndex();
	int end = e.getEndIndex();
	if( marked[start] )
	  adjArray[start].add(end);
	if( marked[end] )
	  adjArray[end].add(start);
      }
    }

    private City getCornerFrom(City start)
    {
      City c = start;
      do {
	c = c.next;
      } while( countMarkedNbrs(c.index) != 3 );
      return c;
    }

    private DirectedEdge findCornerEdge(City c)
    {
      int start = c.index, end = -1;
      IntList.Iterator I = adjArray[c.index].getIterator();
      while( I.hasNext() ) {
	int i = I.next();
	if( marked[i] && i != c.prev.index && i != c.next.index ) {
	  end = i;
	  break;
	}
      }

      for( int i = 0; i < edges.length; ++i ) {
	DirectedEdge e = edges[i];
	int a = e.getStartIndex();
	if( a == start ) {
	  if( e.getEndIndex() == end )
	    return e;
	} else if( a == end ) {
	  if( e.getEndIndex() == start )
	    return e.getTwin();
	}
      }
      return null;
    }

    // class for storing a node in a path
    private class City
    {
      public int index; // index in list of Atoms
      public City prev; // doubly-linked list
      public City next;
      public int x; // indices on flat triangular lattice
      public int y;
      public int distanceTo5;

      City(int i)
      {
	index = i;
      }

      City(int i, int x_, int y_)
      {
	index = i;
	x = x_;
	y = y_;
      }

      City(int i, int x_, int y_, City prev_, City next_)
      {
	index = i;
	x = x_;
	y = y_;
	prev = prev_;
	next = next_;
	prev.next = this;
	next.prev = this;
      }
    }
  }
}
