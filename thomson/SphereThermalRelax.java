import javax.swing.*;          // Java Swing windowing and layout managers
import javax.swing.event.*;    // Java Swing Events and Handlers
import java.text.DecimalFormat;


// TODO: Generalize to Shell

class SphereThermalRelax
    implements ShellAlgorithm
{
  double tStep;
  double kbT;

  // Name the algorithm
  public String toString()
  {
    return "Thermal Relax";
  }

  // This algorithm is selected
  // Configure the applet to reflect this
  // Also perform any algorithm intialization
  public void select( ShellApplet sa )
  {
    sa.optionsLabel[0].setText("Time Step = ");
    sa.optionsSpinner[0].setModel(new SpinnerNumberModel(1.0, 0.0,100.0,.01));
    sa.optionsLabel[1].setText("1000*kB*T = ");
    sa.optionsSpinner[1].setModel(new SpinnerNumberModel(10,0.0,1e5,.01));

    sa.optionsLabel[1].setVisible(true);
    sa.optionsSpinner[1].setVisible(true);

    tStep = ((Number)sa.optionsSpinner[0].getValue()).doubleValue();
    kbT = ((Number)sa.optionsSpinner[1].getValue()).doubleValue()/1000.0;
  }

  // Apply the algorithm
  // Gather and check parameters
  // Break from this algorithm
  // Configure the applet to reflect this
  public void apply( ShellApplet sa )
  {
    tStep = ((Number)sa.optionsSpinner[0].getValue()).doubleValue();
    kbT = ((Number)sa.optionsSpinner[1].getValue()).doubleValue()/1000.0;

    //sphere.runThermalForcesOnce(dtTR,kbTTR);
    this.apply(sa.getShell(), tStep, kbT);
  }

  // Algorithm core called by apply( ShellApplet )
  // Apply the algorithm
  // Also able to modify the AlgorithmParameters
  public static double apply(Shell shell, double tStep, double kbT)
  {
    Point[] atom = shell.atom;
    Potential potential = shell.potential;
    Point[] force = shell.getGradient();

    double maxCrossSq = 0, tempCross;

    for( int i = 0; i < atom.length; ++i ) {
      tempCross = atom[i].crossProdMagSq(force[i]);
      if( tempCross > maxCrossSq )
        maxCrossSq = tempCross;
    }

    Point iAtom, iForce, temp, nAtom;
    Point randSphere = new Point();

    double scalor;
    if( maxCrossSq != 0.0 )
      scalor = tStep/(Math.pow(maxCrossSq,.5f-.5f/(2*potential.id()+3))*atom.length);
    else
      scalor = tStep/atom.length;      // *shellR^2

    double dr, deltaV;

    for( int i = 0; i < atom.length; ++i ) {
      iForce = force[i]; iAtom = atom[i];
      iForce.scale(scalor);
      iAtom.add(iForce);
      iAtom.normalize();

      Const.setRandomSphericalPoint( randSphere );
      dr = 0.5 * Const.getRandomNormal() * iForce.scale(1-iAtom.dot(iForce)).mag();
      randSphere.scale( dr );

      temp = iAtom.plus( randSphere );
      temp.normalize();

      deltaV = 0;
      for( int n = 0; n < atom.length; ++n ) {
        if( n == i )
          continue;
        nAtom = atom[n];
	deltaV += potential.energy(nAtom,temp) - potential.energy(nAtom,iAtom);
      }

      if( deltaV <= 0 || Const.getRandom() < Math.exp(-deltaV/kbT) )
        atom[i] = temp;
    }

    shell.invalidateDelaunay();

    return maxCrossSq;
  }
}
