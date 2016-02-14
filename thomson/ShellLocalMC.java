import javax.swing.*;          // Java Swing windowing and layout managers
import javax.swing.event.*;    // Java Swing Events and Handlers
import java.text.DecimalFormat;

class ShellLocalMC
    implements ShellAlgorithm
{
  double kbT;
  double dR;

  // Name the algorithm
  public String toString()
  {
    return "Local MC";
  }

  // This algorithm is selected
  // Configure the applet to reflect this
  // Also perform any algorithm intialization
  public void select(ShellApplet sa)
  {
    sa.optionsLabel[0].setText("1000*kB*T = ");
    sa.optionsSpinner[0].setModel(new SpinnerNumberModel(.1,0.0,1e5,.001));
    sa.optionsLabel[1].setText("Movement = ");
    sa.optionsSpinner[1].setModel(new SpinnerNumberModel(.05, 0.0, 1.0, .01));

    sa.optionsLabel[1].setVisible(true);
    sa.optionsSpinner[1].setVisible(true);

    kbT = ((Number)sa.optionsSpinner[0].getValue()).doubleValue()/1000.0;
    dR  = ((Number)sa.optionsSpinner[1].getValue()).doubleValue();
  }

  // Apply the algorithm
  // Gather and check parameters
  // Break from this algorithm
  // Configure the applet to reflect this
  public void apply(ShellApplet sa)
  {
    kbT = ((Number)sa.optionsSpinner[0].getValue()).doubleValue()/1000.0;
    dR  = ((Number)sa.optionsSpinner[1].getValue()).doubleValue();

    this.apply(sa.getShell(), kbT, dR);
  }

  // Algorithm core called by apply( ShellApplet )
  // Apply the algorithm
  // Also able to modify the AlgorithmParameters
  public static void apply(Shell shell, double kbT, double dR)
  {
    Point[] atom = shell.atom;
    Potential potential = shell.potential;

    Point nAtom, iAtom, moved;
    Point randSphere = new Point();
    double randPhi, randCosTheta, randTheta, st, dr, dx, dy, dz, deltaV;

    for( int n = 0; n < atom.length; ++n ) {
      nAtom = atom[n];

      Const.setRandomSphericalPoint( randSphere );
      randSphere.scale( dR );

      moved = nAtom.plus( randSphere );
      shell.normalizeToShell(moved);

      deltaV = 0;
      for( int i = 0; i < atom.length; ++i ) {
        if( i == n )
          continue;
        iAtom = atom[i];
        deltaV += potential.energy(iAtom,moved) - potential.energy(iAtom,nAtom);
      }

      if( deltaV <= 0 || Const.getRandom() < Math.exp(-deltaV/kbT) )
        atom[n] = moved;
    }

    shell.invalidateDelaunay();
  }
}