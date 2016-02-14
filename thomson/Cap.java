/* Kevin Zielnicki
 */


class Cap
{
  Point[] points;
  int[] coorNumArray;
  IntList[] adjArray;
  IntList border;
  boolean[] isCorner;
  Point center;
  public int id, totCharge, numSides, sideLength;

  Cap() {}

  Cap(Cap cap)
  {
    points = new Point[cap.points.length];
    coorNumArray = new int[points.length];
    adjArray = new IntList[points.length];
    isCorner = new boolean[points.length];

    border = new IntList(cap.border);
    center = new Point(cap.center);
    id = cap.id;
    totCharge = cap.totCharge;
    numSides = cap.numSides;
    sideLength = cap.sideLength;

    for( int i = 0; i < points.length; ++i ) {
      points[i] = new Point(cap.points[i]);
      coorNumArray[i] = cap.coorNumArray[i];
      adjArray[i] = new IntList(cap.adjArray[i]);
      isCorner[i] = cap.isCorner[i];
    }
  }


  Cap(Point[] points_, IntList[] adjArray_, int[] coorNumArray_, int totCharge_, int numSides_, int sideLength_)
  {
    points = points_;
    adjArray = adjArray_;
    totCharge = totCharge_;
    numSides = numSides_;
    sideLength = sideLength_;
    coorNumArray = coorNumArray_;

    border = new IntList();
    isCorner = new boolean[points.length];
    int borderPrev=-1, borderCur=-1;
    center = new Point();

    // border points have coordination number 6, but fewer than 6 neighbors
    for(int i = 0; i < points.length; ++i) {
      if( coorNumArray[i] != adjArray[i].length() ) {
        border.add(i);
        borderCur = i;
        if( adjArray[i].length() == 3 ) {
          center.add(points[i]);
          isCorner[i] = true;
        } else {
          isCorner[i] = false;
        }
        break;
      }
    }

    // construct the border
    for(int i=1; i < numSides * sideLength; ++i) {
      IntList.Iterator adjItr = adjArray[borderCur].getIterator();
      while( adjItr.hasNext() ) {
        int idx = adjItr.next();
        if( coorNumArray[idx] != adjArray[idx].length() && idx != borderPrev) {
          border.add(idx);
          borderPrev = borderCur;
          borderCur = idx;
          if( adjArray[idx].length() == 3 ) {
            center.add(points[idx]);
            isCorner[idx] = true;
          } else {
            isCorner[idx] = false;
          }
          break;
        }
      }
    }
    center.normalize();
  }

  public Point[] getPoints()
  {
    return points;
  }

  public int numPoints()
  {
    return points.length;
  }

  public IntList[] getAdjArray()
  {
    return adjArray;
  }

  public int[] getCoorNumArray()
  {
    return coorNumArray;
  }

  public Sphere toSphere()
  {
    autoRelax();

    Sphere sphere = new Sphere();
    sphere.setPoints(points);

    // Pretty bad waste and style here
    DelaunaySphere d = (DelaunaySphere)sphere.getDelaunay();
    d.setAdjArray(adjArray);
    d.setCoorNumArray(coorNumArray);

    return sphere;
  }

  /*
   * increase the number of points in the cap by
   * adding a border of 6 coordinated points
   */
  public void grow(int n)
  {
    for(int i=0; i<n; ++i) {
      if( i%3 == 2 )
        autoRelax();
      grow();
    }
  }

  public void grow()
  {
    // the new border will add numSides*(sideLength+1) points
    Point[] newPoints = new Point[points.length + numSides*(sideLength+1)];
    int[] newCoorNum = new int[newPoints.length];
    IntList[] newAdjArray = new IntList[newPoints.length];
    IntList newBorder = new IntList();
    boolean[] newCorners = new boolean[newPoints.length];
    boolean clockwise;
    int i;

    // check if we're going around the border clockwise or counterclockwise
    if( points[border.peek(0)].cross( points[border.peek(1)] ).dot(center) < 0 )
      clockwise = true;
    else
      clockwise = false;

    for(i=0; i < points.length; ++i) {
      newPoints[i] = points[i];
      newCoorNum[i] = coorNumArray[i];
      newAdjArray[i] = adjArray[i];
    }

    IntList.Iterator borderIter = border.getIterator();
    int curPoint = borderIter.next(), prevPoint;
    Matrix3D rotate = new Matrix3D();
    rotate.prot(center, (clockwise ? 1 : -1)*Math.PI/3);

    // add the new border
    i = points.length;
    boolean finished = false;
    while(!finished) {
      prevPoint = curPoint;
      if(borderIter.hasNext()) {
        curPoint = borderIter.next();
      } else {
        // loop back to the front to finish the border
        curPoint = border.peek();
        finished = true;
      }
      Point difference = points[curPoint].minus(points[prevPoint]);
      rotate.transform(difference);
      newPoints[i] = points[prevPoint].plus(difference).normalized();

      // update adjacency array
      newAdjArray[prevPoint].add(i);
      newAdjArray[curPoint].add(i);
      newAdjArray[i] = new IntList();
      newAdjArray[i].add(prevPoint);
      newAdjArray[i].add(curPoint);

      if( i > points.length )
        newAdjArray[i].add(i-1);
      else
        newAdjArray[i].add(newPoints.length - 1);

      if( i < newPoints.length - 1 )
        newAdjArray[i].add(i+1);
      else
        newAdjArray[i].add(points.length);

      newBorder.add(i);
      newCorners[i] = false;
      ++i;

      if( isCorner[curPoint] ) {
        double mag = difference.mag();
        difference = points[curPoint].minus(center);
        difference.scale( mag/difference.mag() );
        newPoints[i] = points[curPoint].plus(difference).normalized();

        // update adjacency array
        newAdjArray[curPoint].add(i);
        newAdjArray[i] = new IntList();
        newAdjArray[i].add(curPoint);
        if( i > points.length )
          newAdjArray[i].add(i-1);
        else
          newAdjArray[i].add(newPoints.length - 1);
        if( i < newPoints.length - 1 )
          newAdjArray[i].add(i+1);
        else
          newAdjArray[i].add(points.length);

        newBorder.add(i);
        newCorners[i] = true;
        ++i;
      }
    }

    for(i=points.length; i < newPoints.length; ++i) {
      newCoorNum[i] = 6;
    }

    ++sideLength;
    points = newPoints;
    coorNumArray = newCoorNum;
    adjArray = newAdjArray;
    border = newBorder;
    isCorner = newCorners;
  }

  /*
   * change the physical radius of the cap by a factor of s
   * by moving points further from or closer to eachother
   */
  public void scale(double s)
  {
    for(int i=0; i < points.length; ++i) {
      Point distance = points[i].minus(center);
      points[i].add(distance.scale(s-1));
      points[i].normalize();
    }
  }

  /*
   * resize the cap so it will take up the right ammount of
   * surface area on a sphere of size N
   */
  public void scaleToSphere(int N)
  {
    /*
    // this version is based on the number of points
    // which would seem preferable, but often results in incompatable
    // sizes when combining caps with different numbers of sides
    double desiredArea = 4*Math.PI * ownedPoints() / N;
    scale( Math.sqrt( desiredArea / surfaceArea() ) );
    */

    // Calculate the desired area based on how many of this cap it
    // would take to tile the sphere.
    // Normally, the formula would be total charge / 12, but here 12
    // is replaced by a higher number to result in slightly smaller caps
    // because if the caps are only a little bit too large they will begin
    // to overlap eachother and create "lattice scars" that do not easily
    // relax away
    double desiredArea = 4*Math.PI * totCharge / 18;
    scale( Math.sqrt( desiredArea / surfaceArea() ) );
  }

  /*
   * returns the number of points that would be uniquely associated
   * with this cap if it was joined with other caps on the surface
   * of a sphere
   */
  public double ownedPoints()
  {
    // a cap on a sphere "owns" all its inner points plus:
    //   1/2 of its non-corner edge points
    //   1/3 of its corner points
    return points.length - border.length() + (double)numSides/3 + (double)numSides*(sideLength - 1)/2;
  }

  /*
   * rotate the cap theta radians about its center
   */
  public void rotate(double theta)
  {
    rotate(center, theta);
  }

  /*
   * rotate the cap theta radian about an arbitrary axis, p
   */
  public void rotate(Point p, double theta)
  {
    Matrix3D rotate = new Matrix3D();
    rotate.prot(p, theta);
    for( int i = 0; i < points.length; ++i ) {
      rotate.transform(points[i]);
      points[i].normalize();
    }
    rotate.transform(center);
  }


  /*
   * find the average cartesian length of a side assuming
   * a cap is a regular polygon
   */
  public double sideLength()
  {
    double perim = 0;
    IntList.Iterator borderIter = border.getIterator();

    int p1 = borderIter.next();
    int start = p1;

    // walk around the border and find the perimeter
    while( borderIter.hasNext() ) {
      int p2 = borderIter.next();
      perim += points[p1].distanceFrom( points[p2] );
      p1 = p2;
    }

    // loop back around to the starting point
    perim += points[p1].distanceFrom( points[start] );

    return perim/numSides;
  }


  /*
   *  To find the area of a cap, we approximate it as a two dimensional
   * regular polygon. This approximation is best for small caps and will
   * slightly underestimate the area of large caps (by a maximum of 50%)
   * because it doesn't account for the curve of the sphere. However, this
   * effect is counteracted somewhat by the fact that caps are not precisely
   * regular, which would tend to introduce a slight overestimation.
   *
   * Additionally, it assumes the cap is smaller than a hemisphere
   */
  public double areaOfPolygon()
  {
    // the formula for the area of a regular polygon is:
    //    n*b^2 / ( 4 tan(pi/n) )
    // where b is the side length and n is the number of sides
    double b = sideLength();
    return ( b * b * numSides ) / ( 4 * Math.tan( Math.PI/numSides ) );
  }


  /*
   * Find the total surface area of the cap by calculating the area
   * of each face
   */
  public double surfaceArea()
  {
    double area = 0;

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
              Point BA = points[A].minus( points[B] );
              Point BC = points[C].minus( points[B] );
              Point norm = BA.cross(BC);
              if( points[B].dot(norm) > 0 ) {
                // add the face's area and break from the while loop
                area += norm.mag()/2;
                break;
              }
            }

          } // end of while for checking B's neighbors
        } // end of if B has been visited
      } // end of while for checking A's neighbors
    } // end of for loop, all faces found


    return area;
  }


  /*
   * use lattice energy to relax the cap
   */
  public void autoRelax()
  {
    // according to Kevin's cap rule =P
    //    F = 2V - 2 - #borderPoints
    int faces = 2*points.length - 2 - border.length();

    // Lattice Constant defined by surfaceArea/#faces = a*sqrt(3)/4 (area of eq-Tri)
    double latticeConst = Math.sqrt( ( 4/Math.sqrt(3) ) * areaOfPolygon() / faces );

    // run the relaxation until tStep gets small enough
    double tStep = 1.0;
    double lastMeasure = Double.POSITIVE_INFINITY;
    while( tStep > .0005 ) {
      double currentMeasure = runElasticStrain(tStep, latticeConst);

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
   * and a latticeConst which defines the desired average side length
   */
  public double runElasticStrain(double tStep, double latticeConst)
  {
    Point[] force = new Point[points.length];
    for( int i = 0; i < force.length; ++i )
      force[i] = new Point();

    Point iPoint, iForce, temp;
    double maxCrossSq = 0;
    double tempCross, scalor, tempMag, tempMagMa;

    IntList.Iterator J;
    int j;

    for( int i = 0; i < points.length; ++i ) {

      iPoint = points[i]; iForce = force[i];
      J = adjArray[i].getIterator();

      while( J.hasNext() ) {
        j = J.next();
        if( j < i )
          continue;

        temp = iPoint.minus(points[j]);
        tempMag = temp.mag();
        temp.scale((tempMag - latticeConst)/tempMag);

        iForce.sub(temp);
        force[j].add(temp);
      }
      tempCross = iPoint.crossProdMagSq(iForce); // A measure of the motion over the Sphere
      if( tempCross > maxCrossSq )
        maxCrossSq = tempCross;
    }

    if( maxCrossSq != 0.0 )
      scalor = tStep/(Math.sqrt(maxCrossSq)*points.length);
    else
      scalor = tStep/points.length;      // *shellR^2

    for( int i = 0; i < points.length; ++i ) {
      iPoint = points[i]; iForce = force[i];   // Since the force can be inwards,
      iForce.scale(scalor);                 // it can penetrate through the shell
      iPoint.scale(1-iPoint.dot(iForce)).add(iForce); // Subtract off radial component of force.
      iPoint.scale(1.0/iPoint.mag());
    }

    return maxCrossSq;
  }
}
