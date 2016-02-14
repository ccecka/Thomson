import javax.swing.*;          // Java Swing windowing and layout managers
import javax.swing.event.*;    // Java Swing Events and Handlers
import java.text.DecimalFormat;


class SphereKrauth
    implements ShellAlgorithm
{
  double localR;
  double dR;
  double annealProb;

  // Name the algorithm
  public String toString()
  {
    return "MC Anneal";
  }

  // This algorithm is selected
  // Configure the applet to reflect this
  // Also perform any algorithm intialization
  public void select(ShellApplet sa)
  {
    sa.optionsLabel[0].setText("Movement = ");
    sa.optionsSpinner[0].setModel(new SpinnerNumberModel(.05, 0.0, 1.0, .01));
    sa.optionsLabel[1].setText("Prob anneal = ");
    sa.optionsSpinner[1].setModel(new SpinnerNumberModel(.05, 0.0, 1.0, .01));

    sa.optionsLabel[1].setVisible(true);
    sa.optionsSpinner[1].setVisible(true);

    // Initialize the Krauth algorithm
    Graph d = sa.getShell().getDelaunay();
    IntList[] adjArray = d.getAdjArray();
    Point[] atom = sa.getShell().atom;

    for( int i = 0; i < atom.length; ++i ) {
      Point iAtom = atom[i];
      IntList.Iterator J = adjArray[i].getIterator();
      while( J.hasNext() )
        localR = Math.min( localR, iAtom.distanceFromSq(atom[J.next()]) );
    }
  }

  // Apply the algorithm
  // Gather and check parameters
  // Break from this algorithm
  // Configure the applet to reflect this
  public void apply(ShellApplet sa)
  {
    dR = ((Number)sa.optionsSpinner[0].getValue()).doubleValue();
    annealProb = ((Number)sa.optionsSpinner[1].getValue()).doubleValue();

    localR = this.apply(sa.getShell(), localR, dR, annealProb);
  }

  // Algorithm core called by apply( ShellApplet )
  // Apply the algorithm
  // Also able to modify the AlgorithmParameters
  public static double apply(Shell shell,
                             double localR, double dR, double annealProb)
  {
    Point[] atom = shell.atom;
    Graph d = shell.getDelaunay();

    IntList[] adjArray = d.getAdjArray();

    if( localR >= 1.0 ) {
      // Initialize the Krauth Algorithm
      for( int i = 0; i < atom.length; ++i ) {
        Point iAtom = atom[i];
        IntList.Iterator J = adjArray[i].getIterator();
        while( J.hasNext() )
          localR = Math.min( localR, iAtom.distanceFromSq(atom[J.next()]) );
      }
    }

    Point randSphere = new Point();

 nextAtom:
    for( int k = 0; k < atom.length; ++k ) {
      int n = (int) Const.getRandom(0, atom.length);

      Const.setRandomSphericalPoint( randSphere );
      randSphere.scale( Const.getRandomNormal() * dR );

      Point moved = atom[n].plus( randSphere ); // rand point + rand sph Gaussian
      moved.normalize();

      IntList.Iterator I = adjArray[n].getIterator();
      while( I.hasNext() ) {
        if( moved.distanceFromSq(atom[I.next()]) < localR )
          continue nextAtom;     // if this moved the atom within the
      }                          // hardshell of a neighbor, cancel this motion

      atom[n] = moved;
    }

    if( Const.getRandom() < annealProb ) { // Probabalistic annealing
      double newR = Double.POSITIVE_INFINITY;

      for( int i = 0; i < atom.length; ++i ) {
        Point iAtom = atom[i];
        IntList.Iterator J = adjArray[i].getIterator();
        while( J.hasNext() )
          newR = Math.min( newR, iAtom.distanceFromSq(atom[J.next()]) );
      }

      localR = (newR+localR)/2;
    }

    shell.invalidateDelaunay();

    return localR;
  }
}

