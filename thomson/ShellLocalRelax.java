import javax.swing.*;          // Java Swing windowing and layout managers
import javax.swing.event.*;    // Java Swing Events and Handlers
import java.text.DecimalFormat;


class ShellLocalRelax
    implements AutoAlgorithm
{
  double tStep;
  int degree;

  // Name the algorithm
  public String toString()
  {
    return "Local Relax";
  }

  // This algorithm is selected
  // Configure the applet to reflect this
  // Also perform any algorithm intialization
  public void select( ShellApplet sa )
  {
    sa.potSpinner.setValueWithInit(12);

    sa.optionsLabel[0].setText("Time Step = ");
    sa.optionsSpinner[0].setModel(new SpinnerNumberModel(1.0,0.0,100.0,.01));
    JSpinner.NumberEditor editor = (JSpinner.NumberEditor)sa.optionsSpinner[0].getEditor();
    DecimalFormat format = editor.getFormat();
    format.setMinimumFractionDigits(4);
    sa.optionsLabel[1].setText("Degree = ");
    sa.optionsSpinner[1].setModel(new SpinnerNumberModel(2, 1, 20, 1));

    sa.optionsLabel[1].setVisible(true);
    sa.optionsSpinner[1].setVisible(true);

    sa.autoButton.setVisible(true);

    tStep = ((Number)sa.optionsSpinner[0].getValue()).doubleValue();
    degree = ((Number)sa.optionsSpinner[1].getValue()).intValue();
  }

  // Apply the algorithm
  // Gather and check parameters
  // Break from this algorithm
  // Configure the applet to reflect this
  public void apply( ShellApplet sa )
  {
    tStep = ((Number)sa.optionsSpinner[0].getValue()).doubleValue();
    degree = ((Number)sa.optionsSpinner[1].getValue()).intValue();

    this.apply(sa.getShell(), tStep, degree);
  }

  // Algorithm core called by apply( ShellApplet )
  // Apply the algorithm
  // Also able to modify the AlgorithmParameters
  public static double apply(Shell shell, double tStep, int degree)
  {
    Point[] atom = shell.atom;
    Potential potential = shell.potential;

    Point[] force = new Point[atom.length];
    for( int i = 0; i < force.length; ++i )
      force[i] = new Point();

    Point iAtom;

    IntList[] adjArray = shell.getDelaunay().getAdjArray();
    boolean[][] visited = new boolean[atom.length][atom.length];

    int[] pointList = new int[atom.length];

    for( int i = 0; i < atom.length; ++i ) {

      int pIndex = 0;
      pointList[pIndex++] = i;
      int lastLevelIndex = 0;

      iAtom = atom[i];
      Point iForce = force[i];
      boolean[] iVisited = visited[i];
      iVisited[i] = true;

      for( int d = 0; d < degree; ++d ) {

        int levelIndex = pIndex;

        // For all points collected from the last degree
        for( int k = lastLevelIndex; k < levelIndex; ++k ) {

          IntList.Iterator J = adjArray[pointList[k]].getIterator();

          // For all the neighbors
          while( J.hasNext() ) {
            int j = J.next();

            if( iVisited[j] )     // i has visited j
              continue;

            pointList[pIndex++] = j;
            iVisited[j] = true;

            if( visited[j][i] )   // j visited i
              continue;

            potential.force(iAtom, atom[j], iForce, force[j]);
          }
        }

        lastLevelIndex = levelIndex;
      }
    }

    double maxCrossSq = shell.updateWithForce(force, tStep);
    shell.invalidateDelaunay();

    return maxCrossSq;
  }

  // Returns a ShellAlgorithm that implements an automated version
  public ShellAlgorithm getAutoAlgorithm()
  {
    return new ShellAutoAlgMaxCross(this);
  }

  // Apply the method to a sphere with a specific tStep
  public double apply(Shell shell, double tStep) {
    return this.apply(shell, tStep, degree);
  }
}
