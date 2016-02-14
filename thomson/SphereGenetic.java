import javax.swing.*;          // Java Swing windowing and layout managers
import javax.swing.event.*;    // Java Swing Events and Handlers
import java.text.DecimalFormat;

import java.util.*;

class SphereGenetic
    implements ShellAlgorithm
{
  int colonySize;
  int luckySurvivors = 1;
  int generations;

  // Name the algorithm
  public String toString()
  {
    return "Genetic Alg";
  }

  /** This algorithm is selected
   * Configure the applet to reflect this
   * Also perform any algorithm intialization
   */
  public void select(ShellApplet sa)
  {
    sa.optionsLabel[0].setText("Colony = ");
    sa.optionsSpinner[0].setModel(new SpinnerNumberModel(4, 1, 10, 1));
    sa.optionsLabel[1].setText("Generations = ");
    sa.optionsSpinner[1].setModel(new SpinnerNumberModel(10, 0, 200, 1));

    sa.optionsLabel[1].setVisible(true);
    sa.optionsSpinner[1].setVisible(true);

    colonySize = ((Number)sa.optionsSpinner[0].getValue()).intValue();
    luckySurvivors = 1;
    generations = ((Number)sa.optionsSpinner[1].getValue()).intValue();
  }

  /** Apply the algorithm
   * Gather and check parameters
   */
  public void apply(ShellApplet sa)
  {
    // Gather parameters
    colonySize = ((Number)sa.optionsSpinner[0].getValue()).intValue();
    luckySurvivors = 1;
    generations = ((Number)sa.optionsSpinner[1].getValue()).intValue();

    // Apply the algorithm
    this.apply(sa.getShell(), colonySize, generations, luckySurvivors);

    // Apply the auto-relaxation to the winning shell
    sa.pauseAnim();
    sa.algChoice.setSelectedItem(sa.ALG_RELAX);
    sa.autoButton.doClick();
  }

  // Algorithm core called by apply(ShellApplet)
  public static void apply(Shell shell,
                           int colonySize, int generations, int luckySurvivors)
  {
    GeneticAlg gen = new GeneticAlg(colonySize, (Sphere) shell);
    shell.setPoints( gen.runEvolution(generations, luckySurvivors).atom );
    shell.invalidateDelaunay();
  }
}




/*
 * A Genetic algorithm applied to the Thomson problem.
 * Generates a 'colony' of 'individuals' which are each relaxed. Some mate
 * by combining halves and
 *
 *
 *
 *
 */


class GeneticAlg
{
  Organism[] colony;        // A list of organisms making up a colony
  int orgSize;
  final static boolean printInfo = true;


  GeneticAlg(int colonySize_, Sphere adam)
  {
    colony = new Organism[colonySize_];
    orgSize = adam.numPoints();

    if (printInfo) Const.out.print("Generation 0: working");
    colony[0] = new Organism(adam);
    colony[0].makeFit();

    // Make the colony
    for( int i = 1; i < colony.length; ++i ) {
      if (printInfo) Const.out.print(".");
      colony[i] = new Organism();
      colony[i].makeFit();
    }

    // Sort the colony by fitness
    for( int i = 0; i < colony.length; ++i ) {
      for( int j = i+1; j < colony.length; ++j ) {
	if( colony[j].fitness < colony[i].fitness ) {
	  Organism temp = colony[j];
	  colony[j] = colony[i];
	  colony[i] = temp;
	}
      }
    }

    if (printInfo) Const.out.println();

    for( int i = 0; i < colony.length; ++i ) {
      if (printInfo) Const.out.println("     " + colony[i].fitness);
    }
  }

  // Runs for a number of generations and keeps track of the best individual it has seen
  public Sphere runEvolution(int maxGenerations, int luckySurvivors)
  {
    Organism bestSeen = colony[0];

    for (int i = 1; i < maxGenerations && !Thread.currentThread().isInterrupted(); ++i) {
      if (printInfo) Const.out.print("Generation " + i + ": working");

      naturalSelection(colony.length, luckySurvivors);

      Organism bestInGen = colony[0];
      if( bestInGen.fitness < bestSeen.fitness )
	bestSeen = bestInGen;
    }

    return bestSeen;
  }

  // Creates the next generation of indivduals
  private void naturalSelection(int numSurvivors, int maxRandomSurvivors)
  {
    Organism[] nextGen = new Organism[2*colony.length];
    int counter = nextGen.length;

    for( int i = 0; i < colony.length; ++i ) {
      nextGen[--counter] = colony[i];
    }

    // Create the next Generation by mating
    while( counter != 0 ) {
      if( Thread.currentThread().isInterrupted() )
	return;

      int mother = (int) Const.getRandom(0, colony.length);
      int father = (int) Const.getRandom(0, colony.length);
      if( mother == father )
	continue;
      Point pivot = colony[mother].randomPointOnShell();
      colony[mother].pivotPoints(pivot);
      colony[father].pivotPoints(pivot);
      nextGen[--counter] = new Organism(colony[mother], colony[father]);
      nextGen[counter].makeFit();
      if (printInfo) Const.out.print(".");
    }

    if (printInfo) Const.out.println();

    // Select the top individuals and try to ignore 'twins'
    for( int i = 0; i < numSurvivors; ++i ) {
      for( int j = i+1; j < nextGen.length; ++j ) {
	if( nextGen[j].fitness < nextGen[i].fitness
	    && (i == 0
		|| nextGen[j].fitness > nextGen[i-1].fitness + 1E-8 ) ) {
	  Organism temp = nextGen[i];
	  nextGen[i] = nextGen[j];
	  nextGen[j] = temp;
	}
      }
    }

    // Choose some random survivors for variability
    for( int i = 0; i < maxRandomSurvivors; ++i ) {
      int lucky = (int) Const.getRandom(numSurvivors, nextGen.length);
      int unlucky = numSurvivors - 1 - i;
      Organism temp = nextGen[lucky];
      nextGen[lucky] = nextGen[unlucky];
      nextGen[unlucky] = temp;
    }

    for( int i = 0; i < colony.length; ++i ) {
      colony[i] = nextGen[i];
      if (printInfo)
	Const.out.println("     " + colony[i].fitness);
    }
  }


  class Organism extends Sphere
  {
    private double fitness = -1;

    Organism()
    {
      randomPoints(orgSize);
    }

    // Make a copy (clone)
    Organism( Sphere sphere )
    {
      super(sphere);
    }

    // Mate a produce a new organism
    Organism( Organism mother, Organism father )
    {
      // Make a clone of the father
      super(father);

      // Replace the first half with genes from the mother
      int NHalf = mother.numPoints() / 2;
      for( int i = 0; i < NHalf; ++i )
	atom[i] = new Point( mother.atom[i] );
    }

    // Uses the Sphere's autorelax method to quickly relax the
    // Sphere to some threshold
    public double makeFit()
    {
      if( fitness != -1 )
	return fitness;
      Const.out.print(".");

      // Relax the shell until the tStep gets low enough
      (new ShellAutoAlgMaxCross(new ShellRelax())).apply(this, 0.01);
      return fitness = totalEnergy();
    }

    public double getFitness()
    {
      if( fitness != -1 )
	return fitness;

      fitness = totalEnergy();
      return fitness;
    }

    // Splits the points into two halves where the splitting plane is
    // by Point pivot
    public void pivotPoints(Point pivot)
    {
      int i = 0;
      int j = atom.length - 1;

      while ( true ) {
	while (i < j && pivot.dot(atom[i]) < 0) ++i;
	while (j > i && pivot.dot(atom[j]) > 0) --j;
	if( i != j ) { // Swap
	  Point temp = atom[i];
	  atom[i] = atom[j];
	  atom[j] = temp;
	} else {
	  return;
	}
      }
    }
  }
}