import javax.swing.*;          // Java Swing windowing and layout managers
import javax.swing.event.*;    // Java Swing Events and Handlers
import java.text.DecimalFormat;


class ShellRelax
    implements ShellAlgorithm, AutoAlgorithm
{
  double tStep;

  // Name the algorithm
  public String toString()
  {
    return "Relaxation";
  }

  // This algorithm is selected
  // Configure the applet to reflect this
  // Also perform any algorithm intialization
  public void select(ShellApplet sa)
  {
    sa.optionsLabel[0].setText("Time Step = ");
    sa.optionsSpinner[0].setModel(new SpinnerNumberModel(1.0,0.0,100.0,.01));
    JSpinner.NumberEditor editor = (JSpinner.NumberEditor)sa.optionsSpinner[0].getEditor();
    DecimalFormat format = editor.getFormat();
    format.setMinimumFractionDigits(4);

    sa.autoButton.setVisible(true);

    tStep = ((Number)sa.optionsSpinner[0].getValue()).doubleValue();
  }

  // Apply the algorithm
  // Gather and check parameters
  // Break from this algorithm
  // Configure the applet to reflect this
  public void apply(ShellApplet sa)
  {
    tStep = ((Number)sa.optionsSpinner[0].getValue()).doubleValue();

    this.apply(sa.getShell(), tStep);
  }

  // Algorithm core called by apply(ShellApplet)
  // Apply the algorithm
  public double apply(Shell shell, double tStep)
  {
    Point[] force = shell.getGradient();
    Point[] atom = shell.atom;

    double maxCrossSq = shell.updateWithForce(force, tStep);
    shell.invalidateDelaunay();

    // return maxCrossSq for use in autorelaxation as a proxy for energy
    return maxCrossSq;
  }

  public ShellAlgorithm getAutoAlgorithm()
  {
    return new ShellAutoAlgMaxCross(this);
  }
}
