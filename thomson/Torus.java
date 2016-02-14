/*
 * A set of points constrained to the surface of a torus.
 *
 * Provide methods for evolving the point set, creating a graph,
 * creating a graphics model, etc.
 *
 * Cris Cecka
 */

class Torus extends Shell
{
  final double r1 = 1.0;
  double r2 = r1 / Math.sqrt(2);

  double nuPrefactor;
  double nuInfactor;

  Torus()
  {
    setNuFactors();
  }

  Torus(Torus torus)
  {
    super(torus);
    r2 = torus.r2;
    setNuFactors();
  }

  public static String getName()
  {
    return "Torus";
  }

  final private void setNuFactors()
  {
    double minSq  = Math.sqrt(r1 - r2);
    double plusSq = Math.sqrt(r1 + r2);

    nuPrefactor = 2*r2/(minSq*plusSq);
    nuInfactor = minSq/plusSq;
  }

  synchronized public void setRatio(double ratio)
  {
    r2 = r1/ratio;
    setNuFactors();
    if( atom != null ) {
      for( int i = 0; i < atom.length; ++i ) {
        normalizeToShell(atom[i]);
      }
    }

    d = null;
  }

  public double getRatio()
  {
    return r1/r2;
  }

  /** An approximately flat distribution to generate a random point on
   * the torus
   */
  public Point randomPointOnShell()
  {
    double Rsum = r1 + r2;
    double alpha, u, R;
    do {
      alpha = Const.getRandom() * Const.TWOPI;
      u     = Const.getRandom() * Rsum;
      R     = r1+r2*Math.cos(alpha);
    } while( u > R );
    double theta = Const.getRandom() * Const.TWOPI;
    return new Point(R*Math.cos(theta), R*Math.sin(theta), r2*Math.sin(alpha));
  }

  public void spiralPoints(double c)
  {
    c *= 10;
    double sqrtN = Math.sqrt(atom.length);

    for( int i = 0; i < atom.length; ++i ) {
      double theta = ((double)i)/atom.length*Const.TWOPI;
      double alpha = (double)i*c/sqrtN;
      double r1r2ca = r1 + r2*Math.cos(alpha);
      double x = r1r2ca*Math.cos(theta);
      double y = r1r2ca*Math.sin(theta);
      double z = r2*Math.sin(alpha);
      atom[i].set(x, y, z);
    }

    d = null;
  }

  /** Update the atoms in the direction of force by a size scale
   * Since the surface of the torus is non-convex,
   * the normal component of the force is removed.
   * TODO: Optimize
   */
  final public double updateWithForce(Point[] force, double scale)
  {
    Point iAtom, iForce;
    Point temp = new Point();

    double maxCrossSq = 0, tempCross;
    for( int i = 0; i < atom.length; ++i ) {
      iAtom = atom[i]; iForce = force[i];
      temp.set(iAtom.x, iAtom.y, 0);
      temp.scale(r1/temp.mag());
      tempCross = iAtom.minus(temp).crossProdMagSq(iForce);
      if( tempCross > maxCrossSq )
        maxCrossSq = tempCross;
    }

    if( maxCrossSq == 0.0 )
      return 0.0;

    // Original scaling
    // TODO: Explain
    double scalor = scale/(atom.length*Math.pow(maxCrossSq,
                                                .5f-.5f/(2*potential.id()+3)));

    for( int i = 0; i < atom.length; ++i ) {
      iAtom = atom[i]; iForce = force[i];
      temp = getPointNormal(iAtom);
      iForce.sub(temp.scale(iForce.dot(temp))).scale(scalor);
      normalizeToShell(iAtom.add(iForce));
    }

    return maxCrossSq;
  }

  /** Return a unit normal vector for the Point a with respect to the torus
   */
  final public Point getPointNormal(Point a)
  {
    Point rad = new Point(a.x, a.y, 0);
    rad.scale(r1/rad.mag()).sub(a);
    return rad.scale(-1/rad.mag());
  }

  synchronized public Graph getDelaunay()
  {
    if( d == null )
      d = new DelaunayTorus(atom, this);
    return d;
  }

  public void normalizeToShell(Point a)
  {
    double theta = getTheta(a);
    double xymag = Math.sqrt(a.x*a.x + a.y*a.y);
    double alpha = getAlpha(a);

    double r1r2ca = r1 + r2*Math.cos(alpha);
    double x = r1r2ca*Math.cos(theta);
    double y = r1r2ca*Math.sin(theta);
    double z = r2*Math.sin(alpha);
    a.set(x, y, z);
  }

  /*
   * A point's theta angle is the outer angle (looking down) between the
   * positive x direction and the projection of the point onto the x-y plane.
   *
   * Returns a point's theta value in [-pi,pi]
   */
  public static double getTheta(Point a)
  {
    return Math.atan2(a.y, a.x);
  }

  /*
   * A point's alpha angle is the interior tube angle between the outward
   * radial direction and vector from the center of the tube to the point.
   *
   * Returns a point's alpha value in [-pi,pi]
   */
  public double getAlpha(Point a)
  {
    return Math.atan2(a.z, Math.sqrt(a.x*a.x+a.y*a.y) - r1);
  }

  public double getNu(Point a)
  {
    return nuPrefactor * Math.atan(Math.tan(getAlpha(a)/2) * nuInfactor);
  }

  public double getMaxNu()
  {
    return Const.PI*nuPrefactor/2;
  }

  final public double getShellRadius()
  {
    return r1 + r2;
  }

  final public double surfaceArea()
  {
    return 4*Const.PI*Const.PI*r1*r2;
  }

  final public int numFaces()
  {
    return 2*atom.length;
  }

}
