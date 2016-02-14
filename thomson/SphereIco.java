import javax.swing.*;          // Java Swing windowing and layout managers
import javax.swing.event.*;    // Java Swing Events and Handlers
import java.text.DecimalFormat;


class SphereIco
  implements ShellAlgorithm
{
  int n;
  int m;

  // Name the algorithm
  public String toString()
  {
    return "Ico (m,n)";
  }

  // This algorithm is selected
  // Configure the applet to reflect this
  // Also perform any algorithm intialization
  public void select(ShellApplet sa)
  {
    sa.optionsLabel[0].setText("m = ");
    sa.optionsSpinner[0].setModel(new SpinnerNumberModel(3, 0, 35, 1));
    sa.optionsLabel[1].setText("n = ");
    sa.optionsSpinner[1].setModel(new SpinnerNumberModel(3, 0, 35, 1));

    sa.optionsLabel[1].setVisible(true);
    sa.optionsSpinner[1].setVisible(true);

    m = ((Number)sa.optionsSpinner[0].getValue()).intValue();
    n = ((Number)sa.optionsSpinner[1].getValue()).intValue();
  }

  // Apply the algorithm
  // Gather and check parameters
  // Break from this algorithm
  // Configure the applet to reflect this
  public void apply(ShellApplet sa)
  {
    m = ((Number)sa.optionsSpinner[0].getValue()).intValue();
    n = ((Number)sa.optionsSpinner[1].getValue()).intValue();

    int NIco = 10*(m*m+m*n+n*n)+2;
    if( NIco > sa.NMAX ) {
      Const.out.println("N = " + NIco + " > " + sa.NMAX + " is too large");
      sa.pauseAnim();
    } else if( NIco < sa.NMIN ) {
      Const.out.println("N = " + NIco + " < " + sa.NMIN + " is too small");
      sa.pauseAnim();
    } else {
      this.apply(sa.getShell(), m, n);

      sa.updateShellSize();
      sa.lastDraw = 0;
      sa.algChoice.setSelectedItem( sa.ALG_RELAX );
      sa.itemStateChanged(null);
      sa.startAnim( sa.ALG_RELAX );
    }
  }

  // Algorithm core called by apply( ShellApplet )
  // Apply the algorithm
  // Also able to modify the AlgorithmParameters
  public static void apply(Shell shell, int m, int n)
  {
    if( m < 0 || n < 0 )
      return;

    int mn = m+n;
    int m2mnn2 = mn*mn - m*n;
    Point[] atom = new Point[10*m2mnn2+2];

    if( m2mnn2 == 0 ) {
      atom[0] = new Point(0,0,1);
      atom[1] = new Point(0,0,-1);
      shell.setPoints(atom);
      return;
    }

    int numFront = -1, numEnd = atom.length;
    Point[] ico = Icosahedron.getPoints();
    TriangleByIndex[] faces = Icosahedron.getFaces();

    for( int i = 0; i < 12; ++i ) // init with Ico vertices (on edges)
      atom[++numFront] = ico[i];

    double px = ((double)mn)/((double)m2mnn2);
    double qx = ((double)n)/((double)m2mnn2);
    double pt = ((double)n)/((double)m2mnn2);
    double qt = -((double)m)/((double)m2mnn2);

 outerLoop:
    for( int fi = 0; fi < faces.length; ++fi ) {
      TriangleByIndex f = faces[fi];
      Point O = ico[f.getA()]; // The reference Origin
      Point b = ico[f.getB()];
      Point c = ico[f.getC()];
      Point P = b.minus(O);
      Point Q = O.minus(c);
      Point R = c.minus(b);
      Point normal = Q.cross(P);
      Point x = P.scaled(px).plus(Q.scaled(qx)); // Basis vectors x and t
      Point t = P.scaled(pt).plus(Q.scaled(qt));
      for( int i = -mn; i <= mn; ++i ) {  // enumerate combos of (ix, jt)
     innerLoop:
        for( int j = -mn; j <= mn; ++j ) {
          Point ixjt = x.scaled(i);  // a potential point
          ixjt.add(t.scaled(j));
          /*
           * Find the Baryocentric coordinates of this potential point
           */
          double PxY = P.cross(ixjt).dot(normal);
          double QxY = Q.cross(ixjt).dot(normal);
          double RxY = P.minus(ixjt).cross(R).dot(normal);
          ixjt.add(O);
          if( (PxY <= 1E-6 && PxY >= -1E-6) // if one of these is close to 0,
              || (QxY <= 1E-6 && QxY >= -1E-6) // the point is close to an edge of the face
              || (RxY <= 1E-6 && RxY >= -1E-6) ) {

            if( ixjt.magSq() >= 1.0 ) // any edged point should still be inside sphere
              continue innerLoop;

            ixjt.normalize(); // normalize and check previous edged points
            for( int k = 0; k < 12; ++k ) {
              if( ixjt.distanceFromSq(atom[k]) < 1E-12 )
                continue innerLoop; // reject this point if it seems another already exists
            }
            for( int k = numEnd; k < atom.length; ++k ) {
              if( ixjt.distanceFromSq(atom[k]) < 1E-12 )
                continue innerLoop; // reject this point if it seems another already exists
            }
            atom[--numEnd] = ixjt;
          } else if( PxY > 0 && QxY > 0 && RxY > 0 ) { // if point is inside face
            ixjt.normalize();
            atom[++numFront] = ixjt;
          }
          if( numFront == numEnd ) // reached capacity, should have all we need.
            break outerLoop;
        }
      }
    }

    shell.setPoints(atom);

    shell.invalidateDelaunay();
  }
}