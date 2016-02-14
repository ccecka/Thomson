import javax.swing.*;          // Java Swing windowing and layout managers

class ShellRandom
    implements ShellAlgorithm
{
  int sleepTime;

  // Name the algorithm
  public String toString()
  {
    return "Random";
  }

  // This algorithm is selected
  // Configure the applet to reflect this
  // Also perform any algorithm intialization
  public void select( ShellApplet sa )
  {
    sa.optionsLabel[0].setText("Delay (ms) = ");
    sa.optionsSpinner[0].setModel(new SpinnerNumberModel(100,0,5000,10));
    sleepTime = ((Number)sa.optionsSpinner[0].getValue()).intValue();
  }

  // Apply the algorithm
  // Gather and check parameters
  // Break from this algorithm
  // Configure the applet to reflect this
  public void apply( ShellApplet sa )
  {
    sleepTime = ((Number)sa.optionsSpinner[0].getValue()).intValue();
    this.apply(sa.getShell(), sa.getShell().numPoints());
    try{Thread.sleep(sleepTime);} catch(InterruptedException e) {return;}
  }

  // Algorithm core called by apply( ShellApplet )
  // Apply the algorithm
  // Also able to modify the AlgorithmParameters
  public static void apply(Shell shell, int N)
  {
    shell.randomPoints(N);
  }
}