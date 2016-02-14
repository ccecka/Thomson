import javax.swing.*;          // Java Swing windowing and layout managers

class ShellSpiral
    implements ShellAlgorithm
{
  double spiralcoef = 1e-10;

  // Name the algorithm
  public String toString()
  {
    return "Spiral";
  }

  // This algorithm is selected
  // Configure the applet to reflect this
  // Also perform any algorithm intialization
  public void select(ShellApplet sa)
  {
    sa.optionsLabel[0].setText("Speed = ");
    sa.optionsSpinner[0].setModel(new SpinnerNumberModel(1.0,-20.0,20.0,.1));
  }

  // Apply the algorithm
  // Gather and check parameters
  // Break from this algorithm
  // Configure the applet to reflect this
  public void apply(ShellApplet sa)
  {
    double incr = 1e-6*((Number)sa.optionsSpinner[0].getValue()).doubleValue();
    this.apply(sa.getShell(), spiralcoef);
    spiralcoef += incr;
  }

  // Algorithm core called by apply( ShellApplet )
  // Apply the algorithm
  // Also able to modify the AlgorithmParameters
  public static void apply(Shell shell, double spiralcoef)
  {
    shell.spiralPoints(spiralcoef);
  }
}