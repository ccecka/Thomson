import javax.swing.*;          // Java Swing windowing and layout managers
import javax.swing.event.*;    // Java Swing Events and Handlers
import java.text.DecimalFormat;


class SphereCaps
  implements ShellAlgorithm
{
  // Name the algorithm
  public String toString()
  {
    return "Cap Construct";
  }

  // This algorithm is selected
  // Configure the applet to reflect this
  // Also perform any algorithm intialization
  public void select( ShellApplet sa )
  {
    sa.optionsLabel[0].setVisible(false);
    sa.optionsSpinner[0].setVisible(false);
  }

  // Apply the algorithm
  // Gather and check parameters
  // Break from this algorithm
  // Configure the applet to reflect this
  public void apply( ShellApplet sa )
  {
    boolean success = this.apply(sa.getShell());

    if( !success )
      Const.out.println("Could not build N = " + sa.getShell().numPoints());
    sa.pauseAnim();
  }

  // Algorithm core called by apply( ShellApplet )
  // Apply the algorithm
  // Also able to modify the AlgorithmParameters
  public static boolean apply(Shell shell)
  {
    if( shell.numPoints() < 32 )
      return false;

    CapPalette palette = new CapPalette( shell.numPoints() );

    Sphere newSphere = palette.toSphere();

    if( newSphere == null )
      return false;

    shell.setPoints( newSphere.atom );
    shell.invalidateDelaunay();

    return true;
  }
}