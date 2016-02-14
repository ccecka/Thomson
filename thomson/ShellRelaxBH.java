import javax.swing.*;          // Java Swing windowing and layout managers
import javax.swing.event.*;    // Java Swing Events and Handlers
import java.text.DecimalFormat;
import java.util.*;

class ShellRelaxBH
    implements ShellAlgorithm, AutoAlgorithm
{
  double tStep, theta;

  // Name the algorithm
  public String toString()
  {
    return "Barnes-Hut Relax";
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

    sa.optionsLabel[1].setText("MAC = ");
    sa.optionsSpinner[1].setModel(new SpinnerNumberModel(0.5,0.05,0.5,.01));
    JSpinner.NumberEditor editor2 = (JSpinner.NumberEditor)sa.optionsSpinner[1].getEditor();
    DecimalFormat format2 = editor2.getFormat();
    format2.setMinimumFractionDigits(2);

    sa.optionsLabel[1].setVisible(true);
    sa.optionsSpinner[1].setVisible(true);

    tStep = ((Number)sa.optionsSpinner[0].getValue()).doubleValue();
    theta = ((Number)sa.optionsSpinner[1].getValue()).doubleValue();
  }

  // Apply the algorithm
  // Gather and check parameters
  // Break from this algorithm
  // Configure the applet to reflect this
  public void apply(ShellApplet sa)
  {
    tStep = ((Number)sa.optionsSpinner[0].getValue()).doubleValue();
    theta = ((Number)sa.optionsSpinner[1].getValue()).doubleValue();
    this.apply(sa.getShell(), tStep, theta);
  }

  public double apply(Shell shell, double tStep) {
    return this.apply(shell, tStep, theta);
  }

  // Algorithm core called by apply(ShellApplet)
  // Apply the algorithm
  public static double apply(Shell shell, double tStep, double theta)
  {
    Point[] force = shell.getGradientBH(theta);
    Point[] atom = shell.atom; //why is this here???? Shell atoms are already updated

    double maxCrossSq = shell.updateWithForce(force, tStep);
    shell.invalidateDelaunay();

    // return maxCrossSq for use in autorelaxation as a proxy for energy
    return maxCrossSq;
  }

  public ShellAlgorithm getAutoAlgorithm()
  {
    return new ShellAutoRelaxBH();
  }
}


class ShellAutoRelaxBH
    implements ShellAlgorithm
{
  double tStep, tStepLimit, theta;

  //create the mapping between MAC's and crossover points
  double[] MAC = {0.5, 0.4, 0.3, 0.2};
  double[]  cross = {1000, 1650, 2150, 3200};
  int N, theta_index;

  double lastMeasure, lastEnergy;
  long lastUpdate;
  boolean lastFell, oscillating, energyTest;

  /** Constructor */
  ShellAutoRelaxBH() {
    lastMeasure = Double.POSITIVE_INFINITY;
    lastEnergy = Double.NEGATIVE_INFINITY;
    lastUpdate = System.currentTimeMillis();

    lastFell = true;
    oscillating = false;
    energyTest = false;

    tStep = 8.0;
    tStepLimit = 0.1;

    theta = 0.5;
    theta_index = 0; N = 0;
  }

  /** Name the algorithm */
  public String toString()
  {
    return "Barnes-Hut Auto Relax";
  }

  /** Select and initialize the auto relaxation
   */
  public void select(ShellApplet sa)
  {
    lastMeasure = Double.POSITIVE_INFINITY;
    lastEnergy = Double.NEGATIVE_INFINITY;
    lastUpdate = System.currentTimeMillis();

    lastFell = true;
    oscillating = false;
    energyTest = false;

    tStep = ((Number)sa.optionsSpinner[0].getValue()).doubleValue();
    theta = ((Number)sa.optionsSpinner[1].getValue()).doubleValue();
    N = sa.getShell().numPoints();
  }

  /** Apply one step of the auto relax and respond to the ShellApplet
   */
  public void apply(ShellApplet sa)
  {
    // One step of the auto relax
    doAuto(sa.getShell());

    // Update the visualization periodically
    long currentTime = System.currentTimeMillis();
    if( currentTime - lastUpdate > 500 ) {
      sa.optionsSpinner[0].setValue(tStep);
      lastUpdate = currentTime;
    }

    // Once tStep gets low enough, reduce theta and start again
    if( tStep < tStepLimit  && tStepLimit >= 0.001) {
      //if we're below the crossover point switch to direct calculation
      if(cross[++theta_index] > N) {
        sa.pauseAnim();
        sa.algChoice.setSelectedItem(sa.ALG_RELAX);
        sa.autoButton.doClick();
      }
      else //otherwise we update theta
        theta = MAC[theta_index];

      tStepLimit /= 10;
      sa.optionsSpinner[0].setValue(tStep);
      sa.optionsSpinner[1].setValue(theta);
    }
    else if(tStepLimit < 0.001)
    {
      // Press the auto button which should be in the "Halt" state
      if( sa.autoButton.getText().equals("Halt") )
        sa.autoButton.doClick();
    }
  }

  /** Apply one step of the auto relax with time step tStep */
  private void doAuto(Shell shell)
  {
    double currentMeasure = ShellRelaxBH.apply(shell, tStep, theta);

    // Decide to increase or decrease timeStep (very heuristic)
    // if maxCrossSq is oscillating, decrease tStep
    if( ((currentMeasure < lastMeasure) != lastFell) && oscillating ) {
      tStep *= .90;
      oscillating = false;
    } else if( (currentMeasure < lastMeasure) != lastFell ) {
      oscillating = true;
      // if maxCrossSq is falling, increase tStep
    } else if( lastFell ) {
      tStep *= 1.1;
      oscillating = false;
      // if maxCrossSq is rising, check the energy
    } else if( !energyTest ) {
      lastEnergy = shell.totalEnergyBH(theta);
      energyTest = true;
      // if energy is rising, drop tStep quickly
    } else if( shell.totalEnergyBH(theta) > lastEnergy ) {
      tStep *= .5;
      oscillating = false;
      energyTest = false;
      // if energy is falling, increase tStep
    } else {
      tStep *= 1.1;
      oscillating = false;
      energyTest = false;
    }

    lastFell = currentMeasure < lastMeasure;
    lastMeasure = currentMeasure;
  }
}
