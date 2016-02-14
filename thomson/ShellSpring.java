import javax.swing.*;          // Java Swing windowing and layout managers
import javax.swing.event.*;    // Java Swing Events and Handlers
import java.text.DecimalFormat;


// TODO: Fix for Torus

class ShellSpring
    implements AutoAlgorithm
{
  double tStep;

  // Name the algorithm
  public String toString()
  {
    return "Edge Springs";
  }

  // This algorithm is selected
  // Configure the applet to reflect this
  // Also perform any algorithm intialization
  public void select( ShellApplet sa )
  {
    sa.optionsLabel[0].setText("Time Step = ");
    sa.optionsSpinner[0].setModel(new SpinnerNumberModel(1.0,0.0,100.0,.01));

    sa.autoButton.setVisible(true);

    tStep = ((Number)sa.optionsSpinner[0].getValue()).doubleValue();
  }

  // Apply the algorithm
  // Gather and check parameters
  // Break from this algorithm
  // Configure the applet to reflect this
  public void apply( ShellApplet sa )
  {
    tStep = ((Number)sa.optionsSpinner[0].getValue()).doubleValue();
    this.apply(sa.getShell(), tStep);
  }

  // Algorithm core called by apply( ShellApplet )
  // Apply the algorithm
  // Also able to modify the AlgorithmParameters
  public double apply(Shell shell, double tStep)
  {
    Point[] atom = shell.atom;

    // Lattice constant defined by surfaceA/faces = a^2 sqrt(3)/4  (area of eq tri)
    double a = Math.sqrt(shell.surfaceArea() * (4/Math.sqrt(3)) / shell.numFaces());

    Point[] force = new Point[atom.length];
    for( int i = 0; i < force.length; ++i )
      force[i] = new Point();

    Point iAtom, iForce, temp;
    double maxCrossSq = 0;
    double tempCross, scalor, tempMag, tempMagMa;

    Graph d = shell.getDelaunay();

    IntList[] adjArray = d.getAdjArray();
    IntList.Iterator J;
    int j;

    for( int i = 0; i < atom.length; ++i ) {

      iAtom = atom[i]; iForce = force[i];
      J = adjArray[i].getIterator();

      while( J.hasNext() ) {
        j = J.next();
        if( j < i )
          continue;

        temp = iAtom.minus(atom[j]);    // Could be faster
        tempMag = temp.mag();
        temp.scale((tempMag - a)/tempMag);

        iForce.sub(temp);
        force[j].add(temp);
      }
      tempCross = iAtom.crossProdMagSq(iForce); // A measure of the motion over the Sphere
      if( tempCross > maxCrossSq )
        maxCrossSq = tempCross;
    }

    if( maxCrossSq != 0.0 )
      scalor = tStep/(Math.sqrt(maxCrossSq)*atom.length);
    else
      scalor = tStep/atom.length;      // *shellR^2

    for( int i = 0; i < atom.length; ++i ) {
      iAtom = atom[i]; iForce = force[i];   // Since the force can be inwards,
      iForce.scale(scalor);                 // it can penetrate through the shell
      iAtom.scale(1-iAtom.dot(iForce)).add(iForce); // Subtract off radial component of force.
      iAtom.scale(1.0/iAtom.mag());
    }

    // Delaunay should not be invalidated

    return maxCrossSq;
  }

  // Returns a ShellAlgorithm that implements an automated version
  public ShellAlgorithm getAutoAlgorithm() {
    return new ShellAutoAlgMaxCross(this);
  }
}