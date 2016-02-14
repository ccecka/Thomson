/*
 * A set of points constrained to the surface of a unit sphere.
 *
 * Provide methods for evolving the point set, creating a graph,
 * creating a graphics model, etc.
 *
 * Cris Cecka
 */

import java.util.*;

class Sphere extends Shell
{
  Sphere() {}

  Sphere(Sphere sphere)
  {
    super(sphere);
  }

  public static String getName()
  {
    return "Sphere";
  }

  public Point randomPointOnShell()
  {
    Point p = new Point();
    Const.setRandomSphericalPoint( p );
    return p;
  }

  public void normalizeToShell(Point a)
  {
    a.normalize();
  }

  /** Create N spiral points over a sphere.
   * Steps down constant theta (determined by N) and across constant phi
   * (determined by c) on each step.
   */
  synchronized public void spiralPoints(double c)
  {
    if( Math.abs(c) < 1e-10 )
      c = 1e-10;

    double phi = 0.0, z = -1.0;
    double dz = 2.0/(atom.length-1), dphi = c*3.806/Math.sqrt(atom.length);

    atom[0].set(0,0,1);
    atom[1].set(0,0,-1);

    for( int i = 2; i < atom.length; ++i ) {
      z += dz;
      double sinTheta = Math.sqrt(1-z*z);
      phi += dphi/sinTheta;
      atom[i].set(Math.cos(phi)*sinTheta, Math.sin(phi)*sinTheta, z);
    }

    invalidateDelaunay();
  }

  /** Updates the atom positions along the provided force vectors
   * Computes and scales the force by the maximum tangential component
   */
  final public double updateWithForce(Point[] force, double scale)
  {
    double maxCrossSq = 0, tempCross;
    for( int i = 0; i < atom.length; ++i ) {
      tempCross = atom[i].crossProdMagSq(force[i]);
      if( tempCross > maxCrossSq )
        maxCrossSq = tempCross;
    }

    if( maxCrossSq == 0.0 )
      return 0.0;

    /* Experimental scaling
    // Scales the forces so that the largest force moves a particle
    // tStep * average length between particles
    double scalor = tStep * Math.sqrt(4.0 / (3.0*atom.length*maxCrossSq));
    */

    // Original scaling
    // TODO: Explain
    double scalor = scale/(atom.length*Math.pow(maxCrossSq,
                                                .5f-.5f/(2*potential.id()+3)));

    for( int i = 0; i < atom.length; ++i ) {
      atom[i].add(force[i].scale(scalor)).normalize();
    }

    return maxCrossSq;
  }

  /*
   * Returns a unit normal vector for the Point a with respect to the sphere
   */
  final public Point getPointNormal(Point a)
  {
    return new Point(a);
  }

  synchronized public Graph getDelaunay()
  {
    return d == null ? d = new DelaunaySphere(atom) : d;
  }

  /*
   * Overrides super class to return an optimized version of the
   * Gouraud Shading Model in which no zBuffer is required.
   */
  synchronized public Model3DSphere getModel3DEGouraud()
  {
    double[] partE = new double[atom.length];
    double mean = getPartialEnergies(partE);
    return new Model3DSphereGouraud(this, getDelaunay(), partE, mean);
  }

  synchronized public Model3DSphere getModel3DLatGouraud()
  {
    double[] latE = new double[atom.length];
    double mean = getLatticeEnergies(latE);
    return new Model3DSphereGouraud(this, getDelaunay(), latE, mean);
  }

  final public double getShellRadius()
  {
    return 1.0;
  }

  final public double surfaceArea()
  {
    double r = getShellRadius();
    return 4*Const.PI*r*r;
  }

  final public int numFaces()
  {
    return 2*atom.length - 4;
  }
}
