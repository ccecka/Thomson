/* Kevin Zielnicki
 */

import java.util.Vector;
import java.util.Random;

class CapSet
{
  final static Random rand = new Random();
  int totCharge;
  Vector<Point> points;
  Vector<IntList> adjArray;
  Vector<Cap> subCaps, openSubCaps;
  String capList;

  CapSet(Cap cap)
  {
    SubCap newCap = new SubCap(cap);

    totCharge = 0;
    points = new Vector<Point>();
    adjArray = new Vector<IntList>();
    subCaps = new Vector<Cap>();
    openSubCaps = new Vector<Cap>();
    capList = newCap.sideLength+":"+newCap.id+"-r-r-r-r:";

    // initialize the capset with the first cap
    totCharge = newCap.totCharge;

    newCap.pointIdx = new int[newCap.points.length];
    for( int i = 0; i < newCap.points.length; ++i ) {
      points.add( newCap.points[i] );
      newCap.pointIdx[i] = i;
    }

    for( int i = 0; i < newCap.adjArray.length; ++i )
      adjArray.add( new IntList(newCap.adjArray[i]) );

    subCaps.add(newCap);
    openSubCaps.add(newCap);
  }

  /*
   * copy constructor
   */
  CapSet(CapSet caps)
  {
    totCharge = caps.totCharge;
    capList = caps.capList;

    points = new Vector<Point>();
    adjArray = new Vector<IntList>();
    for( int i = 0; i < caps.points.size(); ++i ) {
      points.add( new Point((Point)caps.points.get(i)) );
      adjArray.add( new IntList((IntList)caps.adjArray.get(i)) );
    }

    subCaps = new Vector<Cap>();
    openSubCaps = new Vector<Cap>();
    for( int i = 0; i < caps.subCaps.size(); ++i ) {
      subCaps.add( new SubCap((SubCap)caps.subCaps.get(i)) );
      if( caps.openSubCaps.contains( caps.subCaps.get(i) ) )
        openSubCaps.add( subCaps.get(i) );
    }
  }

  /*
   * an alias for addToEdge, which is the default
   * behavoir of adding a cap to the capset
   */
  public void addCap(Cap cap)
  {
    addToEdge(cap);
  }

  /*
   * randomly add a cap to the capset
   */
  public void addToRandom(Cap cap) {
    SubCap newCap = new SubCap(cap);
    SubCap mate = nextOpenCap();

    if( ( rand.nextInt(2) % 2 == 0 && mate.hasOpenEdge() ) || !mate.hasOpenCorner() )
      mergeByEdge( newCap, newCap.randomOpenEdge(), mate, mate.randomOpenEdge() );
    else
      mergeByCorner( newCap, newCap.randomOpenCorner(), mate, mate.randomOpenCorner() );
  }


  /*
   * add a cap to the capset, joining a predetermined edge
   * to a predetermined edge on a predetermined cap
   */
  public void addToEdge(Cap cap, int mateIdx, int edgeIdx, int mateEdgeIdx)
  {
    SubCap newCap = new SubCap(cap);
    SubCap mate = (SubCap)subCaps.get(mateIdx);

    // the edge we're joining to better be open
    //assert mate.edges[mateEdgeIdx].isOpen;

    mergeByEdge(newCap, newCap.edges[edgeIdx], mate, mate.edges[mateEdgeIdx]);
  }

  /*
   * add a cap to the capset, joining a random edge to
   * a predetermined edge on a predetermined cap
   */
  public void addToEdge(Cap cap, int mateIdx, int mateEdgeIdx)
  {
    SubCap newCap = new SubCap(cap);
    SubCap mate = (SubCap)subCaps.get(mateIdx);

    // the edge we're joining to better be open
    //assert mate.edges[mateEdgeIdx].isOpen;

    mergeByEdge(newCap, newCap.randomOpenEdge(), mate, mate.edges[mateEdgeIdx]);
  }

  /*
   * add a cap to the capset, joining a random edge to
   * a random edge on a predetermined cap
   */
  public void addToEdge(Cap cap, int mateIdx)
  {
    SubCap newCap = new SubCap(cap);
    SubCap mate = (SubCap)subCaps.get(mateIdx);

    // the cap we're joining to better be open
    //assert mate.isOpen();

    mergeByEdge(newCap, newCap.randomOpenEdge(), mate, mate.randomOpenEdge());
  }


  /*
   * randomly add a cap to the capset using an edge join
   */
  public void addToEdge(Cap cap)
  {
    // if we don't still have open caps, something is wrong
    //assert !openSubCaps.isEmpty();

    SubCap newCap = new SubCap(cap);
    SubCap mate = nextOpenCap();

    mergeByEdge( newCap, newCap.randomOpenEdge(), mate, mate.randomOpenEdge() );
  }

  /*
   * merge a new subcap into the capset at the given edges
   */
  private void mergeByEdge(SubCap newCap, CapEdge newEdge, SubCap mate, CapEdge mateEdge)
  {
    capList += newCap.id+"-e-"+subCaps.indexOf(mate)+"-"+newEdge.index+"-"+mateEdge.index+":";
    mateEdge.join(newEdge);

    totCharge += newCap.totCharge;
    subCaps.add(newCap);

    openSubCaps.add(newCap);
  }


  /*
   * add a cap to the capset, joining a predetermined corner
   * to a predetermined corner on a predetermined cap
   */
  public void addToCorner(Cap cap, int mateIdx, int cornerIdx, int mateCornerIdx)
  {
    SubCap newCap = new SubCap(cap);
    SubCap mate = (SubCap)subCaps.get(mateIdx);

    // the corner we're joining to better be open
    //assert mate.corners[mateCornerIdx].isOpen;

    mergeByCorner(newCap, newCap.corners[cornerIdx], mate, mate.corners[mateCornerIdx]);
  }

  /*
   * add a cap to the capset, joining a random corner to
   * a predetermined corner on a predetermined cap
   */
  public void addToCorner(Cap cap, int mateIdx, int mateCornerIdx)
  {
    SubCap newCap = new SubCap(cap);
    SubCap mate = (SubCap)subCaps.get(mateIdx);

    // the corner we're joining to better be open
    //assert mate.corners[mateCornerIdx].isOpen;

    mergeByCorner(newCap, newCap.randomOpenCorner(), mate, mate.corners[mateCornerIdx]);
  }

  /*
   * add a cap to the capset, joining a random corner to
   * a random corner on a predetermined cap
   */
  public void addToCorner(Cap cap, int mateIdx)
  {
    SubCap newCap = new SubCap(cap);
    SubCap mate = (SubCap)subCaps.get(mateIdx);

    // the cap we're joining to better be open
    //assert mate.isOpen();

    mergeByCorner(newCap, newCap.randomOpenCorner(), mate, mate.randomOpenCorner());
  }


  /*
   * randomly add a cap to the capset using an corner join
   */
  public void addToCorner(Cap cap)
  {
    // if we don't still have open caps, something is wrong
    //assert !openSubCaps.isEmpty();

    SubCap newCap = new SubCap(cap);
    SubCap mate = nextOpenCap();

    mergeByCorner( newCap, newCap.randomOpenCorner(), mate, mate.randomOpenCorner() );
  }


  /*
   * merge a new subcap into the capset at the given corners
   */
  private void mergeByCorner(SubCap newCap, CapCorner newCorner, SubCap mate, CapCorner mateCorner)
  {
    capList += newCap.id+"-c-"+subCaps.indexOf(mate)+"-"+newCorner.index+"-"+mateCorner.index+":";

    mateCorner.join(newCorner);

    totCharge += newCap.totCharge;
    subCaps.add(newCap);

    openSubCaps.add(newCap);
  }

  /*
   * returns the next available open subcap
   */
  private SubCap nextOpenCap()
  {
    if( openSubCaps.isEmpty() )
      return null;

    SubCap result = (SubCap)openSubCaps.firstElement();
    if( !result.isOpen() ) {
      openSubCaps.remove(result);
      return nextOpenCap();
    }

    return result;
  }


  /*
   * returns true if the capset can hold any more caps
   */
  public boolean isOpen()
  {
    return nextOpenCap() != null;
  }

  /*
   * returns true if the capset has a total charge of 12
   * based on the adjacency array
   */
  public boolean isValidSphere()
  {
    int charge = 0;
    for( int i = 0; i < adjArray.size(); ++i ) {
      if( points.get(i) != null ) {
        charge += 6 - ((IntList)adjArray.get(i)).length();
      }
    }

    return charge == 12;
  }

  public Sphere toSphere()
  {
    Sphere sphere = new Sphere();
    int[] translate = new int[points.size()];

    // find out how many null points are in the capset
    int nullCount = 0;
    for( int i = 0; i < points.size(); ++i )
      if( points.get(i) == null)
        ++nullCount;

    // create the final point set by not including null points
    // keep track of the index changes so we can fix the adjArray
    Point[] pFinal = new Point[points.size() - nullCount];
    int idx = 0;
    for( int i = 0; i < points.size(); ++i ) {
      if( points.get(i) != null ) {
        pFinal[idx] = (Point)points.get(i);
        translate[i] = idx;
        ++idx;
      }
    }

    // create the final adjArray by translating index values
    // to their value in pFinal
    IntList[] adjFinal = new IntList[pFinal.length];
    idx = 0;
    for( int i = 0; i < adjArray.size(); ++i ) {
      if( points.get(i) != null ) {
        IntList newAdj = new IntList();
        IntList.Iterator adjIter = ((IntList)adjArray.get(i)).getIterator();

        // replace the adjacency array point indices with the capset indices
        while( adjIter.hasNext() ) {
          newAdj.add( translate[adjIter.next()] );
        }
        adjFinal[idx++] = newAdj;
      }
    }

    sphere.setPoints(pFinal);
    sphere.setAdjArray(adjFinal);

    return sphere;
  }

  public int numCaps()
  {
    return subCaps.size();
  }

  public String toString()
  {
    return capList;
  }

  /*
   * use lattice energy to relax the CapSet
   */
  public void autoRelax()
  {
    double tStep = 8.0;
    double lastMeasure = Double.POSITIVE_INFINITY;
    while( tStep > .0005 ) {
      double currentMeasure = runElasticStrain(tStep);

      // increase or decrease tStep
      if( currentMeasure < lastMeasure )
        tStep *= 1.005;
      else
        tStep *= 0.5;

      lastMeasure = currentMeasure;
    }

  }


  /*
   * run a single iteration of lattice relaxation given a tStep
   */
  private double runElasticStrain(double tStep)
  {
    // Lattice Constant defined by 4piR^2/faces = a*sqrt(3)/4 (area of eq-Tri)
    double a = Math.sqrt(Const.EIGHTPIOVERROOT3/(points.size() - 2));

    Point[] force = new Point[points.size()];
    for( int i = 0; i < force.length; ++i )
      force[i] = new Point();

    Point iPoint, iForce, temp;
    double maxCrossSq = 0;
    double tempCross, scalor, tempMag, tempMagMa;

    IntList.Iterator J;
    int j;

    for( int i = 0; i < points.size(); ++i ) {
      iPoint = (Point)points.get(i);
      if( iPoint != null ) {
        iForce = force[i];
        J = ((IntList)adjArray.get(i)).getIterator();

        while( J.hasNext() ) {
          j = J.next();
          if( j < i )
            continue;

          temp = iPoint.minus( (Point)points.get(j) );
          tempMag = temp.mag();
          temp.scale((tempMag - a)/tempMag);

          iForce.sub(temp);
          force[j].add(temp);
        }
        tempCross = iPoint.crossProdMagSq(iForce); // A measure of the motion over the Sphere
        if( tempCross > maxCrossSq )
          maxCrossSq = tempCross;
      }
    }

    if( maxCrossSq != 0.0 )
      scalor = tStep/(Math.sqrt(maxCrossSq)*points.size());
    else
      scalor = tStep/points.size();      // *shellR^2

    for( int i = 0; i < points.size(); ++i ) {
      iPoint = (Point)points.get(i);
      if( iPoint != null ) {
        iForce = force[i];   // Since the force can be inwards,
        iForce.scale(scalor);                 // it can penetrate through the shell
        iPoint.scale(1-iPoint.dot(iForce)).add(iForce); // Subtract off radial component of force.
        iPoint.scale(1.0/iPoint.mag());
      }
    }

    return maxCrossSq;
  }











  class SubCap extends Cap
  {
    CapEdge[] edges;
    CapCorner[] corners;
    CapCorner[] cornerByIdx;
    // pointIdx keeps track of each point's index in the capset
    int[] pointIdx;

    /*
     * construct a subcap from an ordinary cap
     */
    SubCap(Cap cap)
    {
      points = cap.points;
      coorNumArray = cap.coorNumArray;
      adjArray = cap.adjArray;
      border = cap.border;
      isCorner = cap.isCorner;
      center = cap.center;
      id = cap.id;
      totCharge = cap.totCharge;
      numSides = cap.numSides;
      sideLength = cap.sideLength;

      int[] nextEdge = new int[sideLength + 1];
      edges = new CapEdge[numSides];
      corners = new CapCorner[numSides];
      cornerByIdx = new CapCorner[points.length];

      // travel along the border from corner to corner
      // to discover the cap edges
      IntList.Iterator borderIter = border.getIterator();

      // advance the iterator to the first corner
      int p;
      while( !isCorner[ p = borderIter.next() ] ) {}

      // create the edges
      int edgeNum = 0;
      int i = 0;
      while( borderIter.hasNext() ) {
        nextEdge[i++] = p;
        p = borderIter.next();

        // if the point is a corner, it needs to
        // go in both this edge and the next one
        if( isCorner[p] ) {
          nextEdge[i] = p;
          edges[edgeNum] = new CapEdge(nextEdge, this, edgeNum);
          ++edgeNum;
          i = 0;
        }
      }

      // go back to the begining of the border to
      // finish the final edge
      borderIter = border.getIterator();
      do {
        nextEdge[i++] = p;
        p = borderIter.next();
      } while( !isCorner[p] );
      nextEdge[i] = p;
      edges[edgeNum] = new CapEdge(nextEdge, this, edgeNum);


      // create the corners
      for( i = 0; i < corners.length; ++i ) {
        corners[i] = new CapCorner(edges[i], i);
        cornerByIdx[ edges[i].edgePoints[0] ] = corners[i];
      }
    }

    /*
     * Copy Constructor
     */
    SubCap(SubCap cap)
    {
      super(cap);

      pointIdx = new int[points.length];
      for( int i = 0; i < points.length; ++i )
        pointIdx[i] = cap.pointIdx[i];

      // initialize edges and corners
      edges = new CapEdge[numSides];
      corners = new CapCorner[numSides];
      for( int i = 0; i < numSides; ++i ) {
        edges[i] = new CapEdge();
        corners[i] = new CapCorner();
      }

      // copy the data to the edges and corners
      cornerByIdx = new CapCorner[points.length];
      for( int i = 0; i < numSides; ++i ) {
        edges[i].copy(cap.edges[i], this);
        corners[i].copy(cap.corners[i], this);
        cornerByIdx[ corners[i].cornerPt ] = corners[i];
      }
    }


    /*
     * Returns true if the cap has any open edges
     */
    public boolean hasOpenEdge()
    {
      for( int i = 0; i < edges.length; ++i )
        if( edges[i].canBeJoined() )
          return true;

      return false;
    }

    /*
     * Returns true if the cap has any open corners
     */
    public boolean hasOpenCorner()
    {
      for( int i = 0; i < corners.length; ++i )
        if( corners[i].canBeJoined() )
          return true;

      return false;
    }

    /*
     * Returns true if the cap has any open edges or corners
     */
    public boolean isOpen()
    {
      return ( hasOpenEdge() || hasOpenCorner() );
    }

    /*
     * Return a random open (unjoined) edge
     */
    public CapEdge randomOpenEdge()
    {
      // create an array of all the free edges
      CapEdge[] openEdges = new CapEdge[edges.length];
      int counter = 0;
      for( int i = 0; i < edges.length; ++i )
        if( edges[i].canBeJoined() )
          openEdges[counter++] = edges[i];

      // this function should not be called on a cap with no open edges
      //assert counter != 0;

      // otherwise, randomly choose an edge
      return openEdges[ rand.nextInt(counter) ];
    }

    /*
     * Return a random open (unjoined) corner
     */
    public CapCorner randomOpenCorner()
    {
      // create an array of all the free corners
      CapCorner[] openCorners = new CapCorner[corners.length];
      int counter = 0;
      for( int i = 0; i < corners.length; ++i )
        if( corners[i].canBeJoined() )
          openCorners[counter++] = corners[i];

      // this function should not be called on a cap with no open corners
      //assert counter != 0;

      // otherwise, randomly choose an edge
      return openCorners[ rand.nextInt(counter) ];
    }
  }















  private class CapEdge
  {
    int[] edgePoints;
    SubCap owner;
    CapEdge twin;
    int index;
    boolean isOpen, reversed;

    CapEdge() {}

    CapEdge(int[] edgePoints_, SubCap owner_, int index_)
    {
      //System.out.println("Creating edge: "+edgePoints_[0]+"-"+edgePoints_[1]);
      edgePoints = new int[edgePoints_.length];
      for( int i = 0; i < edgePoints_.length; ++i )
        edgePoints[i] = edgePoints_[i];
      owner = owner_;
      index = index_;
      isOpen = true;
      reversed = false;
    }

    /*
     * effectively a copy constructor, since one is not possible
     */
    public void copy(CapEdge edge, SubCap owner_)
    {
      owner = owner_;

      index = edge.index;
      isOpen = edge.isOpen;
      reversed = edge.reversed;

      edgePoints = new int[ edge.edgePoints.length ];
      for( int i = 0; i < edge.edgePoints.length; ++i )
        edgePoints[i] = edge.edgePoints[i];

      twin = owner.edges[ edge.twin.index ];
    }


    /*
     * returns a point representing a vector from the begining
     * of an edge to the end
     */
    private Point edgeVector()
    {
      Point start = owner.points[edgePoints[0]];
      Point end = owner.points[edgePoints[ edgePoints.length - 1 ]];
      return end.minus(start);
    }

    /*
     * returns true if the edge points in a clockwise direction
     * around the cap
     */
    private boolean isCW()
    {
      return edgeVector().cross( getNext().edgeVector() ).dot(owner.center) < 0;
    }

    /*
     * returns the next edge, ensuring it points in the same
     * direction as this edge
     */
    private CapEdge getNext()
    {
      int next;
      if( !reversed ) {
        next = index + 1;
        if( next == owner.edges.length )
          next = 0;
        // make sure the next edge isn't reversed
        if( owner.edges[next].reversed )
          owner.edges[next].reverse();
        return owner.edges[next];
      } else {
        next = index -1;
        if( next == -1 )
          next = owner.edges.length - 1;
        // make sure the next edge is reversed
        if( !owner.edges[next].reversed )
          owner.edges[next].reverse();
        return owner.edges[next];
      }
    }

    /*
     * returns the previous edge, ensuring it points in the same
     * direction as this edge
     */
    private CapEdge getPrev()
    {
      int prev;
      if( !reversed ) {
        prev = index -1;
        if( prev == -1 )
          prev = owner.edges.length - 1;
        // make sure the prev edge isn't reversed
        if( owner.edges[prev].reversed )
          owner.edges[prev].reverse();
        return owner.edges[prev];
      } else {
        prev = index + 1;
        if( prev == owner.edges.length )
          prev = 0;
        // make sure the prev edge is reversed
        if( !owner.edges[prev].reversed )
          owner.edges[prev].reverse();
        return owner.edges[prev];
      }
    }

    /*
     * returns the twin edge, ensuring it points in the same
     * direction as this edge
     */
    private CapEdge getTwin()
    {
      if( twin == null )
        return null;

      // reverse rhs if it isn't pointing in the same direction as this edge
      // note that the two edges point in the same direction only if one has a
      // clockwise rotation and the other is counterclockwise
      Point myVector = edgeVector();
      Point twinVector = twin.edgeVector();
      boolean myCW = myVector.cross( getNext().edgeVector() ).dot(owner.center) < 0;
      boolean twinCW = twinVector.cross( twin.getNext().edgeVector() ).dot(twin.owner.center) < 0;
      if( myCW == twinCW )
        twin.reverse();

      return twin;
    }

    /*
     * reverse the edge such that the first point is last
     * and the last point is first
     */
    private CapEdge reverse()
    {
      reversed = !reversed;

      int left = 0;
      int right = edgePoints.length-1;
      while( left < right) {
        int temp = edgePoints[left];
        edgePoints[left] = edgePoints[right];
        edgePoints[right] = temp;

        left++;
        right--;
      }

      return this;
    }

    /*
     * returns the corner defining the beginning of this edge
     */
    private CapCorner getStartCorner()
    {
      return owner.cornerByIdx[ edgePoints[0] ];
    }

    /*
     * returns the corner defining the end of this edge
     */
    private CapCorner getEndCorner()
    {
      return owner.cornerByIdx[ edgePoints[ edgePoints.length - 1 ] ];
    }

    /*
     * to make sure the edge is suitable, we must check that
     * neighboring edges are also free
     */
    private boolean canBeJoined()
    {
      if( !isOpen )
        return false;

      // check each neighboring edge by going around in my direction
      // if we reach a null pointer exception, we break out
      try {
        CapEdge nextEdge = getNext().getTwin().getPrev().reverse();
        while( nextEdge != this ) {
          if( !nextEdge.isOpen )
            return false;
          nextEdge = nextEdge.getNext().getTwin().getPrev().reverse();
        }
      } catch(NullPointerException e) {}

      // check each neighboring edge by going around in the opposite direction
      // if we reach a null pointer exception, we break out
      try {
        CapEdge nextEdge = getPrev().getTwin().getNext().reverse();
        while( nextEdge != this ) {
          if( !nextEdge.isOpen )
            return false;
          nextEdge = nextEdge.getPrev().getTwin().getNext().reverse();
        }
      } catch(NullPointerException e) {}


      return true;
    }

    /*
     * Join this edge with another open edge
     * Assumes this edge comes from a cap already in the capset
     * and rhs comes from a new cap being added to the capset
     */
    private void join(CapEdge rhs)
    {
      //System.out.println("joining ("+index+")"+owner.pointIdx[edgePoints[0]]+"-"+owner.pointIdx[edgePoints[1]]+" to ("+rhs.index+")"+rhs.edgePoints[0]+"-"+rhs.edgePoints[1]);

      /*
       * first reposition the caps so they are physically near eachother
       */

      // make rhs the same size as this edge
      Point myVector = edgeVector();
      Point rhsVector = rhs.edgeVector();
      /* this can cause problems with runaway resizing especially with caps w/
         different numbers of sides, so for now just assume the size was set
         correctly before the cap was added
         rhs.owner.scale( myVector.mag() / rhsVector.mag() );
      */

      // Reverse rhs if it isn't pointing in the same direction as this edge.
      // Note that the two edges point in the same direction only if one has a
      // clockwise rotation and the other is counterclockwise.
      if( isCW() == rhs.isCW() )
        rhs.reverse();

      // move the caps so the edges are on top of eachother
      Point myStart = owner.points[ edgePoints[0] ];
      Point rhsStart = rhs.owner.points[ rhs.edgePoints[0] ];
      double angle = rhsStart.angleWith(myStart);
      // if the angle is 0 or not a number, this means the edges are exactly
      // identical, so we should rotate things just a little bit so that
      // we're working with valid numbers
      if( angle == 0 || Double.isNaN(angle) ) {
        rhs.owner.rotate(.1);
        angle = rhsStart.angleWith(myStart);
      }
      //System.out.println("moving by "+angle+" through "+rhsStart.cross(myStart));
      rhs.owner.rotate( rhsStart.cross(myStart), angle );


      // rotate the caps so the edges point in the same direction
      //   do this twice because the spherical topology causes
      //   an underestimate of large surface angles
      for( int i = 0; i < 2; ++i) {
        myVector = edgeVector();
        rhsVector = rhs.edgeVector();
        boolean rotateCW = rhsVector.cross(myVector).dot( owner.points[edgePoints[0]] ) < 0;
        //System.out.println("rotating by "+(rotateCW ? -1 : 1) * rhsVector.angleWith(myVector));
        rhs.owner.rotate( owner.points[edgePoints[0]], (rotateCW ? -1 : 1) * rhsVector.angleWith(myVector) );
      }

      /*
       * next, update the points and adjacency array
       */

      rhs.owner.pointIdx = new int[rhs.owner.points.length];

      // add all the new points to the point set
      // and keep track of their indices
      for( int i = 0; i < rhs.owner.points.length; ++i ) {
        rhs.owner.pointIdx[i] = points.size();
        points.add(rhs.owner.points[i]);
      }

      // update the adjacency array to include the new points
      for( int i = 0; i < rhs.owner.adjArray.length; ++i ) {
        IntList newAdj = new IntList();
        IntList.Iterator adjIter = rhs.owner.adjArray[i].getIterator();

        // replace the adjacency array point indices with the capset indices
        while( adjIter.hasNext() )
          newAdj.add( rhs.owner.pointIdx[ adjIter.next() ] );

        adjArray.add(newAdj);
      }

      // seal the gap between the two edges
      seal(rhs);
    }

    /*
     * Modify the adjacency array and remove redundant points
     * to seal the gap between two edges and remove redundant points.
     *
     * This method is kinda hard to follow, but all it is doing is
     * swapping point index values around so the edges match up, then
     * recursively doing the same thing to neighboring caps.
     */
    private void seal(CapEdge rhs)
    {
      //System.out.println("sealing ("+index+")"+owner.pointIdx[edgePoints[0]]+"-"+owner.pointIdx[edgePoints[1]]+" to ("+rhs.index+")"+rhs.edgePoints[0]+"-"+rhs.edgePoints[1]);

      // create an array to keep track of which points are in the edge
      // and nullify the edge points in the points array
      boolean[] isInEdge = new boolean[points.size()];
      int[] translate = new int[points.size()];

      for( int i = 0; i < points.size(); ++i ) {
        isInEdge[i] = false;
        translate[i] = i;
      }

      // if the previous edge has a twin, don't join the first corner
      int start = 0;
      if( rhs.getPrev().twin != null)
        ++start;
      // if the next edge has a twin, don't join the last corner
      int end = rhs.edgePoints.length;
      if( rhs.getNext().twin != null)
        --end;

      for( int i = start; i < end; ++i ) {
        int idx = rhs.owner.pointIdx[ rhs.edgePoints[i] ];
        isInEdge[idx] = true;
        points.set( idx, null );
        translate[idx] = owner.pointIdx[ edgePoints[i] ];
        rhs.owner.pointIdx[ rhs.edgePoints[i] ] = owner.pointIdx[ edgePoints[i] ];
      }



      // update the adjacency array to link the edges
      for( int i = 0; i < rhs.owner.adjArray.length; ++i ) {
        IntList newAdj = new IntList();
        IntList.Iterator adjIter = ((IntList)adjArray.get( rhs.owner.pointIdx[i] )).getIterator();

        // replace the adjacency array point indices with the capset indices
        while( adjIter.hasNext() ) {
          int next = adjIter.next();
          int nextIdx = translate[next];

          // add the current point to the adjArray only if
          // it hasn't already been added
          newAdj.addUnique(nextIdx);

          // if the current point is in the edge, we have to link
          // the corresponding point in this cap to rhs
          if( isInEdge[next] )
            ((IntList)adjArray.get( nextIdx )).addUnique( rhs.owner.pointIdx[i] );
        }
        adjArray.set(rhs.owner.pointIdx[i], newAdj);
      }

      // both edges and their corners are now closed, and are eachother's twins
      isOpen = rhs.isOpen = false;
      twin = rhs;
      rhs.twin = this;
      getStartCorner().isOpen = getEndCorner().isOpen = false;
      rhs.getStartCorner().isOpen = rhs.getEndCorner().isOpen = false;



      /*
       * if this cap now borders other caps, seal to their edges or corners too
       */

      // make sure we maintain the reversed-ness of this and rhs between seals
      // because we might have been reversed without knowing it!
      // (this is very rare, but we should watch out for it anyways
      boolean myReversed = reversed;
      boolean rhsReversed = rhs.reversed;

      CapEdge myNextTwin = getNext().getTwin();
      if( myNextTwin != null && rhs.getNext().isOpen ) {
        //System.out.println("sealing my next's twin");
        myNextTwin.getPrev().reverse().seal( rhs.getNext() );
      } else {
        CapCorner rhsCorner = rhs.getNext().getEndCorner();
        if( rhsCorner.isOpen ) {
          try {
            boolean myCW = isCW();
            CapCorner myNextCorner = getNext().getEndCorner().twin;
            CapEdge myNextEdge = (myCW ? myNextCorner.getCwEdge() : myNextCorner.getCcwEdge());
            CapCorner adjCorner = myNextEdge.getNext().getTwin().getPrev().getStartCorner();
            adjCorner.link(rhsCorner);
          } catch(NullPointerException npe) {}
        }
      }

      if(myReversed != reversed) {
        //System.out.println("catastrophe averted!");
        reverse();
      }
      if(rhsReversed != rhs.reversed) {
        //System.out.println("rhs catastrophe averted!");
        rhs.reverse();
      }

      CapEdge myPrevTwin = getPrev().getTwin();
      if( myPrevTwin != null && rhs.getPrev().isOpen ) {
        //System.out.println("sealing my prev's twin");
        myPrevTwin.getNext().seal( rhs.getPrev().reverse() );
      } else {
        CapCorner rhsCorner = rhs.getPrev().getStartCorner();
        if( rhsCorner.isOpen ) {
          try {
            boolean myCW = isCW();
            CapCorner myNextCorner = getPrev().getStartCorner().twin;
            CapEdge myNextEdge = (myCW ? myNextCorner.getCcwEdge() : myNextCorner.getCwEdge());
            CapCorner adjCorner = myNextEdge.getNext().getTwin().getPrev().getStartCorner();
            adjCorner.link(rhsCorner);
          } catch(NullPointerException npe) {}
        }
      }
    }
  }












  private class CapCorner
  {
    SubCap owner;
    int cornerPt, index;
    CapCorner twin;
    // Note: associated edges have the corner as their first point
    CapEdge cwEdge, ccwEdge;
    int[] cwPseudo, ccwPseudo;
    boolean isOpen, cwReversed, ccwReversed;

    CapCorner() {}

    CapCorner(CapEdge edge, int index_) {
      owner = edge.owner;
      cornerPt = edge.edgePoints[0];
      index = index_;
      isOpen = true;

      if( edge.isCW() ) {
        cwEdge = edge;
        cwReversed = cwEdge.reversed;

        ccwEdge = edge.getPrev();
        ccwReversed = !ccwEdge.reversed;
      } else {
        ccwEdge = edge;
        ccwReversed = ccwEdge.reversed;

        cwEdge = edge.getPrev();
        cwReversed = !cwEdge.reversed;
      }
    }

    /*
     * effectively a copy constructor, since one is not possible
     */
    void copy(CapCorner corner, SubCap owner_) {
      owner = owner_;

      cornerPt = corner.cornerPt;
      index = corner.index;
      isOpen = corner.isOpen;
      cwReversed = corner.cwReversed;
      ccwReversed = corner.ccwReversed;

      cwPseudo = new int[corner.cwPseudo.length];
      for( int i = 0; i < corner.cwPseudo.length; ++i )
        cwPseudo[i] = corner.cwPseudo[i];

      ccwPseudo = new int[corner.ccwPseudo.length];
      for( int i = 0; i < corner.ccwPseudo.length; ++i )
        ccwPseudo[i] = corner.ccwPseudo[i];

      twin = owner.corners[ corner.twin.index ];
      cwEdge = owner.edges[ corner.cwEdge.index ];
      ccwEdge = owner.edges[ corner.ccwEdge.index ];
    }

    /*
     * get the clockwise pointing edge and make sure
     * it's pointing in the right direction
     */
    private CapEdge getCwEdge()
    {
      if( cwReversed != cwEdge.reversed )
        cwEdge.reverse();
      return cwEdge;
    }

    /*
     * get the counterclockwise pointing edge and make
     * sure it's pointing in the right direction
     */
    private CapEdge getCcwEdge()
    {
      if( ccwReversed != ccwEdge.reversed )
        ccwEdge.reverse();
      return ccwEdge;
    }


    /*
     * get the next corner in the direction of edge
     */
    private CapCorner getNextCorner(CapEdge edge)
    {
      return owner.cornerByIdx[ edge.edgePoints[ edge.edgePoints.length - 1 ] ];
    }


    /*
     * to make sure the edge is suitable, we must check that
     * neighboring edges are also free
     */
    private boolean canBeJoined()
    {
      if( !isOpen )
        return false;

      // check each neighboring corner by going around in a CW direction
      // if we reach a null pointer exception, we break out
      try {
        CapCorner nextCW = getNextCorner(getCwEdge()).twin;
        nextCW = nextCW.getNextCorner( nextCW.getCwEdge() );
        while( nextCW != this ) {
          if( !nextCW.isOpen )
            return false;
          nextCW = nextCW.getNextCorner( nextCW.getCwEdge() ).twin;
          nextCW = nextCW.getNextCorner( nextCW.getCwEdge() );
        }
      } catch(NullPointerException e) {}

      // check each neighboring corner by going around in a CCW direction
      // if we reach a null pointer exception, we break out
      try {
        CapCorner nextCCW = getNextCorner(getCcwEdge()).twin;
        nextCCW = nextCCW.getNextCorner( nextCCW.getCcwEdge() );
        while( nextCCW != this ) {
          if( !nextCCW.isOpen )
            return false;
          nextCCW = nextCCW.getNextCorner( nextCCW.getCcwEdge() ).twin;
          nextCCW = nextCCW.getNextCorner( nextCCW.getCcwEdge() );
        }
      } catch(NullPointerException e) {}


      return true;
    }


    /*
     * join two corners and their associated edges
     */
    private void join(CapCorner rhs)
    {
      CapEdge myEdge = getCwEdge();
      CapEdge rhsEdge = rhs.getCcwEdge();

      /*
       * first reposition the caps so they are physically near eachother
       */

      // move the caps so the edges are on top of eachother
      Point myStart = owner.points[ cornerPt ];
      Point rhsStart = rhs.owner.points[ rhs.cornerPt ];
      double angle = rhsStart.angleWith(myStart);
      // if the angle is 0 or not a number, this means the edges are exactly
      // identical, so we should rotate things just a little bit so that
      // we're working with valid numbers
      if( angle == 0 || Double.isNaN(angle) ) {
        rhs.owner.rotate(.1);
        angle = rhsStart.angleWith(myStart);
      }
      //System.out.println("moving by "+angle+" through "+rhsStart.cross(myStart));
      rhs.owner.rotate( rhsStart.cross(myStart), angle );


      // Rotate the caps so that the centers of both caps are
      // colinear with the corner to be joined. This requires a scaled
      // version of the corner point, otherwise the spherical geometry
      // will interfere with the calculation of the angles.
      double centerAngle = owner.center.angleWith( rhs.owner.center );
      Point cornerScaled = owner.points[cornerPt].scaled( Math.cos(centerAngle/2) );

      Point myVector = owner.center.minus( cornerScaled );
      Point rhsVector = cornerScaled.minus( rhs.owner.center );

      boolean rotateCW = rhsVector.cross(myVector).dot( cornerScaled ) < 0;
      angle = rhsVector.angleWith(myVector);
      angle *= (rotateCW ? -1 : 1);
      //System.out.println("rotating by "+angle);
      rhs.owner.rotate( owner.points[ cornerPt ], angle );


      /*
       * next, update the points and adjacency array
       */

      rhs.owner.pointIdx = new int[rhs.owner.points.length];

      // add all the non corner points to the point set
      // and keep track of their indices
      for( int i = 0; i < rhs.owner.points.length; ++i ) {
        if( i != rhs.cornerPt ) {
          rhs.owner.pointIdx[i] = points.size();
          points.add(rhs.owner.points[i]);
        } else {
          rhs.owner.pointIdx[i] = owner.pointIdx[cornerPt];
        }
      }

      // update the adjacency array to include the new points
      for( int i = 0; i < rhs.owner.adjArray.length; ++i ) {
        IntList.Iterator adjIter = rhs.owner.adjArray[i].getIterator();

        if( i != rhs.cornerPt ) {
          IntList newAdj = new IntList();

          // replace the adjacency array point indices with the capset indices
          while( adjIter.hasNext() )
            newAdj.add( rhs.owner.pointIdx[ adjIter.next() ] );

          adjArray.add(newAdj);
        } else {
          // link the corner to its neighbors from rhs
          IntList myCorner = (IntList)adjArray.get( owner.pointIdx[cornerPt] );
          while( adjIter.hasNext() )
            myCorner.add( rhs.owner.pointIdx[ adjIter.next() ] );
        }
      }

      // seal the gap between the two edges
      seal(rhs);
    }

    /*
     * link two corners by deleting one corner point and
     * updating the adjacency arrays accordingly
     */
    private void link(CapCorner rhs)
    {
      int newCornerIdx = owner.pointIdx[ cornerPt ];
      int oldCornerIdx = rhs.owner.pointIdx[ rhs.cornerPt ];

      // update the adjacency array to link the corners
      IntList newAdj = (IntList)adjArray.get( newCornerIdx );
      IntList oldAdj = (IntList)adjArray.get( oldCornerIdx );
      IntList.Iterator oldAdjIter = oldAdj.getIterator();

      while( oldAdjIter.hasNext() ) {
        int nextIdx = oldAdjIter.next();
        newAdj.add(nextIdx);
        ((IntList)adjArray.get(nextIdx)).replace( oldCornerIdx, newCornerIdx );

        //IntList.Iterator alteredAdjIter = ((IntList)adjArray.get(nextIdx)).getIterator();
        //while( alteredAdjIter.hasNext() ) {
        //	if( alteredAdjIter.next() == oldCornerIdx )
        //		alteredAdjIter.setValue( newCornerIdx );
        //}
      }

      // nullify the old point
      points.set( oldCornerIdx, null );
      rhs.owner.pointIdx[ rhs.cornerPt ] = newCornerIdx;

      seal(rhs);
    }

    /*
     * seal the gap between two joined corners
     */
    private void seal(CapCorner rhs)
    {
      //System.out.println("sealing "+owner.pointIdx[cornerPt]+" to "+rhs.owner.pointIdx[rhs.cornerPt]);
      // both corners and their edges are now closed, and are eachother's twins
      isOpen = rhs.isOpen = false;
      twin = rhs;
      rhs.twin = this;

      // connect the points of the associated edges
      CapEdge[] myEdge = new CapEdge[2];
      CapEdge[] rhsEdge = new CapEdge[2];
      myEdge[0] = getCwEdge();
      myEdge[1] = getCcwEdge();
      rhsEdge[0] = rhs.getCcwEdge();
      rhsEdge[1] = rhs.getCwEdge();

      for( int n = 0; n < 2; ++n ) {
        boolean myCW = myEdge[n].isCW();

        // If an edge is open, connect to it by adding points if needed.
        if( myEdge[n].isOpen ) {
          fillGap(myEdge[n], rhsEdge[n]);

          myEdge[n].isOpen = false;
          rhsEdge[n].isOpen = false;

          try {
            CapEdge rhsNextEdge = rhsEdge[n].getNext();

            CapCorner myNext = myEdge[n].getNext().getTwin().getPrev().getStartCorner();
            int[] adjPseudo = (myCW ? myNext.ccwPseudo : myNext.cwPseudo);
            myNext = myNext.twin;
            CapEdge adjEdge = (myCW ? myNext.getCwEdge() : myNext.getCcwEdge()).getNext();

            int[] rhsPseudo = (myCW ? cwPseudo : ccwPseudo);

            if( rhsNextEdge.isOpen )
              adjEdge.seal(rhsNextEdge);

            sealPseudoEdges(adjPseudo, rhsPseudo);

          } catch(NullPointerException npe) {}

          // If an edge is closed, seal the edge of the current cap to the
          // corresponding "pseudo edge" located between the already joined caps
          // and recursively join the following corner
        } else if( rhsEdge[n].isOpen ) {
          int[] pseudoEdge;
          if( myCW )
            pseudoEdge = getNextCorner(myEdge[n]).ccwPseudo;
          else
            pseudoEdge = getNextCorner(myEdge[n]).cwPseudo;

          if( pseudoEdge != null ) {
            sealToPseudoEdge(pseudoEdge, rhsEdge[n]);

            CapCorner myNext = getNextCorner(myEdge[n]).twin;
            CapCorner rhsNext = rhs.getNextCorner(rhsEdge[n]);
            CapEdge myNextEdge, rhsNextEdge;
            if( myCW )
              myNextEdge = myNext.getCwEdge();
            else
              myNextEdge = myNext.getCcwEdge();

            myNext = myNext.getNextCorner(myNextEdge);
            myNextEdge = myNextEdge.getNext();
            rhsNextEdge = rhsEdge[n].getNext();

            myEdge[n].isOpen = false;
            rhsEdge[n].isOpen = false;
            myNext.seal(rhsNext);
          } else {
          }
        }
      }


      // if any pseudo edges were generated, we need to copy them to rhs
      if(cwPseudo != null) {
        rhs.ccwPseudo = new int[cwPseudo.length];
        for( int i = 0; i < cwPseudo.length; ++i )
          rhs.ccwPseudo[ cwPseudo.length - i - 1 ] = cwPseudo[i];
      }
      if(ccwPseudo != null) {
        rhs.cwPseudo = new int[ccwPseudo.length];
        for( int i = 0; i < ccwPseudo.length; ++i )
          rhs.cwPseudo[ ccwPseudo.length - i - 1 ] = ccwPseudo[i];
      }
    }

    private void fillGap(CapEdge myEdge, CapEdge rhsEdge) {
      int[] prevRow = new int[0];
      for( int eIdx = 1; eIdx < myEdge.edgePoints.length; ++eIdx ) {
        int startIdx = myEdge.owner.pointIdx[ myEdge.edgePoints[eIdx] ];
        int endIdx = rhsEdge.owner.pointIdx[ rhsEdge.edgePoints[eIdx] ];

        Point startPt = (Point)points.get(startIdx);
        Point endPt = (Point)points.get(endIdx);
        Point difference = endPt.minus(startPt);

        int[] nextRow = new int[eIdx+1];
        nextRow[0] = startIdx;
        nextRow[eIdx] = endIdx;

        int prevIdx = startIdx;

        for( int newCount = 1; newCount < eIdx; ++newCount ) {
          int nextIdx = points.size();
          Point newPt = startPt.plus( difference.scaled( ((double)newCount)/eIdx ) );
          newPt.normalize();
          nextRow[newCount] = nextIdx;
          points.add(newPt);

          IntList newAdj = new IntList();
          newAdj.add(prevIdx);
          newAdj.add( prevRow[newCount-1] );
          newAdj.add( prevRow[newCount] );
          adjArray.add(newAdj);

          ((IntList)adjArray.get(prevIdx)).add(nextIdx);
          ((IntList)adjArray.get( prevRow[newCount-1] )).add(nextIdx);
          ((IntList)adjArray.get( prevRow[newCount] )).add(nextIdx);

          prevIdx = nextIdx;
        }

        ((IntList)adjArray.get(prevIdx)).addUnique(endIdx);
        ((IntList)adjArray.get(endIdx)).addUnique(prevIdx);
        prevRow = nextRow;
      }
      if( myEdge.isCW() )
        cwPseudo = prevRow;
      else
        ccwPseudo = prevRow;
    }

    private void sealToPseudoEdge(int[] pseudoEdge, CapEdge rhsEdge) {
      // create an array to keep track of which points are in the edge
      // and nullify the edge points in the points array
      boolean[] isInEdge = new boolean[points.size()];
      int[] translate = new int[points.size()];

      for( int i = 0; i < points.size(); ++i ) {
        isInEdge[i] = false;
        translate[i] = i;
      }

      // if rhs's next edge is already closed, we have a special case
      // this occurs for the last cap added to the sphere because we
      // do a pseudoEdge seal for every edge and work our way back to
      // the beginning
      int end = rhsEdge.edgePoints.length;
      if( !rhsEdge.getNext().isOpen )
        --end;

      for( int i = 1; i < end; ++i ) {
        int idx = rhsEdge.owner.pointIdx[ rhsEdge.edgePoints[i] ];
        isInEdge[idx] = true;
        points.set( idx, null );
        translate[idx] = pseudoEdge[i];
        rhsEdge.owner.pointIdx[ rhsEdge.edgePoints[i] ] = pseudoEdge[i];
      }

      // update the adjacency array to link the edges
      for( int i = 0; i < rhsEdge.owner.adjArray.length; ++i ) {
        IntList newAdj = new IntList();
        IntList.Iterator adjIter = ((IntList)adjArray.get( rhsEdge.owner.pointIdx[i] )).getIterator();

        // replace the adjacency array point indices with the capset indices
        while( adjIter.hasNext() ) {
          int next = adjIter.next();
          int nextIdx = translate[next];

          // add the current point to the adjArray only if
          // it hasn't already been added
          newAdj.addUnique(nextIdx);

          // if the current point is in the edge, we have to link
          // the corresponding point in this cap to rhs
          if( isInEdge[next] )
            ((IntList)adjArray.get( nextIdx )).addUnique( rhsEdge.owner.pointIdx[i] );
        }
        adjArray.set(rhsEdge.owner.pointIdx[i], newAdj);
      }
    }

    private void sealPseudoEdges(int[] myPseudo, int[] rhsPseudo) {
      try{
        boolean[] inEdge = new boolean[points.size()];

        // delete the points from rhs' pseudo edge
        for( int i = 1; i < rhsPseudo.length - 1; ++i ) {
          inEdge[ rhsPseudo[i] ] = true;
          points.set( rhsPseudo[i], null );
        }

        // update the adjacency array
        for( int i = 1; i < myPseudo.length - 1; ++i ) {
          if( myPseudo[i] != rhsPseudo[i] ) {
            IntList.Iterator oldAdjIter = ((IntList)adjArray.get(rhsPseudo[i])).getIterator();
            IntList updatedAdj = (IntList)adjArray.get(myPseudo[i]);

            while(oldAdjIter.hasNext()) {
              int nextIdx = oldAdjIter.next();
              if( !inEdge[nextIdx] ) {
                updatedAdj.addUnique(nextIdx);
                ((IntList)adjArray.get(nextIdx)).delete(rhsPseudo[i]);
                ((IntList)adjArray.get(nextIdx)).addUnique(myPseudo[i]);
              }
            }
          }
        }

        // purge any nullified points we might be linking to
        IntList.Iterator oldAdjIter = ((IntList)adjArray.get(myPseudo[myPseudo.length - 2])).getIterator();
        IntList newAdj = new IntList();
        while(oldAdjIter.hasNext()) {
          int nextIdx = oldAdjIter.next();
          if( points.get(nextIdx) != null )
            newAdj.add(nextIdx);
        }
        adjArray.set(myPseudo[myPseudo.length - 2], newAdj);
      }catch(NullPointerException e){
        e.printStackTrace();
      }
    }
  }
}
