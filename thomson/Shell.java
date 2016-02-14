/*
 * A general class which implements methods common to any 2D surface embedded
 * in 3D.
 *
 * Cris Cecka
 */


abstract class Shell
{
  Point[] atom;

  Graph d;

  Potential potential = new Pot1();
  // Default Constructor
  Shell() {}

  // Copy Constructor
  Shell(Shell shell)
  {
    atom = new Point[shell.numPoints()];
    for( int i = 0; i < atom.length; ++i )
      atom[i] = new Point(shell.atom[i]);

    potential = shell.potential;
  }

  public final int numPoints()
  {
    return (atom != null ? atom.length : 0);
  }

  /** Make a copy of the Point array and normalize to shell
   */
  synchronized public void setPoints(Point[] newP)
  {
    atom = new Point[newP.length];

    for( int i = 0; i < newP.length; ++i ) {
      atom[i] = new Point(newP[i]);
      normalizeToShell(atom[i]);
    }

    invalidateDelaunay();
  }

  /** Replace a specific Point with another
   */
  synchronized public void setPoint(int index, Point newPoint)
  {
    atom[index] = new Point(newPoint);
    normalizeToShell(atom[index]);
    invalidateDelaunay();
  }

  /** Returns a copy of the current Point set
   * Points should only be manipulated through the set and get methods.
   */
  synchronized public Point[] getCopyPoints()
  {
    Point[] result = new Point[atom.length];
    for( int i = 0; i < atom.length; ++i ) {
      result[i] = new Point(atom[i]);
    }
    return result;
  }

  public void setPotential(Potential p)
  {
    potential = p;
  }

  public void setPotential(Shell shell)
  {
    potential = shell.potential;
  }

  /** TODO: This should return a potential instead of an int
   */
  public int getPotential()
  {
    return potential.id();
  }

  /** Initialize this shell with random points
   */
  synchronized public void randomPoints(int N)
  {
    atom = new Point[N];
    for( int i = 0; i < atom.length; ++i )
      atom[i] = randomPointOnShell();

    invalidateDelaunay();
  }

  /** Generate a random point on the shell
   */
  abstract public Point randomPointOnShell();

  /** Initialize this shell with parameterized "spiral" points
   */
  synchronized public void spiralPoints()      { spiralPoints(1.0); }
  abstract public void spiralPoints(double c);

  synchronized public void setAdjArray(IntList[] adjArray)
  {
    d = new DelaunaySphere(atom, adjArray);
  }

  abstract public Point getPointNormal(Point a);
  abstract public Graph getDelaunay();
  synchronized final public void invalidateDelaunay()
  {
    //if( d != null && !d.isValid() ) // For use when isValid() is available...
    d = null;
  }
  abstract public void normalizeToShell(Point a);
  abstract public double getShellRadius();
  abstract public int numFaces();
  abstract public double surfaceArea();
  abstract public double updateWithForce(Point[] force, double scale);

  synchronized public LatticeDisk getSurroundingDefect(int start)
  {
    return new LatticeDisk(start, atom, getDelaunay());
  }

  synchronized public Model3DSphere getModel3DWithMesh()
  {
    return new Model3DSphereWithMesh(this, getDelaunay());
  }

  synchronized public Model3DSphere getModel3DDual()
  {
    return new Model3DSphereDual(this, getDelaunay());
  }

  synchronized public Model3DSphere getModel3D()
  {
    return new Model3DSphere(this);
  }

  synchronized public Model3DSphere getModel3DEGouraud()
  {
    double[] partE = new double[atom.length];
    double mean = getPartialEnergies(partE);
    return new Model3DGouraud(this, getDelaunay(), partE, mean);
  }

  synchronized public Model3DSphere getModel3DLatGouraud()
  {
    double[] latE = new double[atom.length];
    double mean = getLatticeEnergies(latE);
    return new Model3DGouraud(this, getDelaunay(), latE, mean);
  }

  synchronized public Model3DSphere getModel3DEColors()
  {
    double[] partE = new double[atom.length];
    double mean = getPartialEnergies(partE);
    return new Model3DSphereEColors(this, partE, mean);
  }

  synchronized public Model3DSphere getModel3DLatColors()
  {
    double[] latE = new double[atom.length];
    double mean = getLatticeEnergies(latE);
    return new Model3DSphereEColors(this, latE, mean);
  }

  synchronized public double totalEnergy()
  {
    double total = 0;
    for( int i = 0; i < atom.length; ++i ) {
      Point iAtom = atom[i];
      for( int j = i+1; j < atom.length; ++j ) {
        //total += vFunction(iAtom, atom[j]);
	total += potential.energy( iAtom, atom[j] );
      }
    }

    return total;
  }

  synchronized public double totalEnergyBH(double theta)
  {
    Octree o = new Octree(atom, theta, potential, getShellRadius());
    return o.getPotential();
  }


  final protected double getPartialEnergies(double[] partE)
  {
    double mean = 0;

    for( int i = 0; i < atom.length; ++i ) {
      Point iAtom = atom[i];
      for( int j = i+1; j < atom.length; ++j ) {
        //double temp = vFunction(iAtom, atom[j]);
        double temp = potential.energy(iAtom, atom[j]);
	partE[i] += temp;
        partE[j] += temp;

        mean += temp+temp;
      }
    }

    return mean/atom.length;
  }

  final protected double getLatticeEnergies(double[] latE)
  {
    double mean = 0;

    EdgeByIndex[] edges = getDelaunay().getEdges();
    double a = Math.sqrt(Const.EIGHTPIOVERROOT3/(atom.length - 2));

    for( int i = 0; i < edges.length; ++i ) {
      EdgeByIndex e = edges[i];
      int j = e.getStartIndex();
      int k = e.getEndIndex();

      double temp = atom[j].distanceFrom(atom[k]) - a;
      temp *= temp;

      latE[j] += temp;
      latE[k] += temp;

      mean += temp + temp;
    }

    return mean/atom.length;
  }

  synchronized public double partialEnergyOf(int index)
  {
    Point iAtom = atom[index];
    double total = 0;
    for( int j = 0; j < atom.length; ++j ) {
      if( j != index ) {
        //total += vFunction(iAtom, atom[j]);
	total += potential.energy(iAtom, atom[j]);
      }
    }

    return total;
  }

  synchronized public Point[] getGradient()
  {
    Point[] force = new Point[atom.length];
    Point iForce, iAtom;

    force[0] = new Point();

    for( int i = 1; i < atom.length; ++i ) {
      iForce = force[i] = new Point();
      iAtom = atom[i];
      for( int j = 0; j < i; ++j ) {
        potential.force(iAtom, atom[j], iForce, force[j]);
      }
    }

    return force;
  }

  //Morton Code Barnes-Hut version of gradient
  synchronized public Point[] getGradientBH(double theta)
  {
    Octree oct = new Octree(atom, theta, potential, getShellRadius());
    atom = oct.getAtoms();
    return oct.getForce();
  }

  synchronized public void addRandomAtom()
  {
    Point[] newList = new Point[atom.length+1];

    System.arraycopy(atom,0, newList,0, atom.length);

    newList[atom.length] = randomPointOnShell();
    atom = newList;
    invalidateDelaunay();
  }

  synchronized public void removeRandomAtom()
  {
    int n = (int) Const.getRandom(0, atom.length);
    Point[] newList = new Point[atom.length-1];

    System.arraycopy(atom,0, newList,0, n);

    System.arraycopy(atom,n+1, newList,n, atom.length-n-1);

    atom = newList;
    invalidateDelaunay();
  }

  synchronized public void removePoint(int n)
  {
    Point[] newList = new Point[atom.length-1];

    System.arraycopy(atom,0, newList,0, n);

    System.arraycopy(atom,n+1, newList,n, atom.length-n-1);

    atom = newList;
    invalidateDelaunay();
  }

  synchronized public void addPoint(Point p)
  {
    Point[] newList = new Point[atom.length+1];

    System.arraycopy(atom,0, newList,0, atom.length);

    newList[atom.length] = p;
    atom = newList;
    invalidateDelaunay();
  }

  synchronized public void addAtomsAtAllCenters()
  {
    TriangleByIndex[] faces = getDelaunay().getFaces();

    if( atom.length + faces.length > ShellApplet.NMAX ) {
      Const.out.println("N = " + (atom.length + faces.length) +
                        " > " + ShellApplet.NMAX + " is too large");
      return;
    }

    Point[] newList = new Point[atom.length + faces.length];

    // Add a Point = average of each triangle face
    for( int i = 0; i < faces.length; ++i ) {
      TriangleByIndex f = faces[i];
      Point newP = atom[f.getA()].plus(atom[f.getB()]);
      newP.add(atom[f.getC()]);
      newP.scale(1.0/3.0);
      normalizeToShell(newP);
      newList[i] = newP;
    }

    // Add the rest
    System.arraycopy(atom,0, newList,faces.length, atom.length);

    atom = newList;
    invalidateDelaunay();
  }

  synchronized public void addAtomsAtAllMidpoints()
  {
    EdgeByIndex[] edges = getDelaunay().getEdges();

    if( atom.length + edges.length > ShellApplet.NMAX ) {
      Const.out.println("N = " + (atom.length + edges.length) +
                        " > " + ShellApplet.NMAX + " is too large");
      return;
    }

    Point[] newList = new Point[atom.length + edges.length];

    // Add a Point = midpoint of each edge
    for( int i = 0; i < edges.length; ++i ) {
      EdgeByIndex e = edges[i];
      int iStart = e.getStartIndex();
      int iEnd = e.getEndIndex();
      Point newP = atom[e.getStartIndex()].plus(atom[e.getEndIndex()]);
      newP.scale(1.0/2.0);
      normalizeToShell(newP);
      newList[i] = newP;
    }

    // Add the Rest
    System.arraycopy(atom,0, newList, edges.length, atom.length);

    atom = newList;
    invalidateDelaunay();
  }

  synchronized public String getFormattedAtomString()
  {
    return Point.getFormattedString(atom);
  }
}
