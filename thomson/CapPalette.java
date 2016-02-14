/*
 * A CapPalette serves as a bridge between ReadWriteFile and CapSet
 * hides the messy work associated with creating a sphere from caps
 * (but leaves all the heavy lifting to CapSet)
 *
 * Kevin Zielnicki
 *
 */


import java.util.Vector;
import java.util.Random;
import java.util.Arrays;

class CapPalette
{
  final static Random rand = new Random();
  final static ReadWriteFile database = new ReadWriteFile();
  final static char EDGE_JOIN = 'e';
  final static char CORNER_JOIN = 'c';
  final static char GENETIC = 'g';
  final static char RANDOM = 'r';
  final static int RANDOM_IDX = -1;

  Vector<Cap> caps;
  Vector<Integer> loaded;
  Sequence sequence;
  int N, minLength;

  // for genetic algorithm
  CapSet bestSeen;
  double bestFitness;

  /*
   * construct a cap palette for a sphere with N points
   */
  CapPalette(int N_)
  {
    N = N_;
    caps = database.readCapsByLength( (int)Math.round(Math.sqrt(N) / Math.sqrt(32)) );

    // scale the caps to fit the sphere
    for( int i = 0; i < caps.size(); ++i ) {
      Cap nextCap = (Cap)caps.get(i);
      nextCap.autoRelax();
      nextCap.scaleToSphere(N);
      nextCap.scaleToSphere(N);
    }

    sequence = new Sequence(N);
  }

  /*
   * construct a cap palette from a string
   * the string contains one number indicating the minimum length, followed by several caps
   * each cap consists of 5 alphanumerics, delimited by '-' and seperated by ':'
   *   the first number is the database id of the cap in question
   *   the second letter indicates if the join is an edge or corner join (r = random)
   *   the third number is the target cap to which this cap should be joined (r = random)
   *   the fourth number is the edge or corner to join with the target cap (r = random)
   *   the fifth number is the edge or corner to join to on the target cap (r = random)
   * example1:  5:1-r-r-r-r:1-c-r-r-r:1-e-r-r-r:2-r-r-r-r:2-r-r-r-r:2-r-r-r-r:
   * example2:  4:1-r-r-r-r:1-e-0-0-0:1-e-0-0-1:1-e-0-0-2:2-e-0-0-3:
   */
  CapPalette(String capList)
  {
    caps = new Vector<Cap>();
    loaded = new Vector<Integer>();

    sequence = new Sequence(capList);

    // make sure all the caps have the same side length
    for( int i = 0; i < caps.size(); ++i ) {
      Cap nextCap = (Cap)caps.get(i);
      nextCap.grow( minLength - nextCap.sideLength );
    }

    // find out how many points are in the sphere
    double pts = 0;
    for( int i = 0; i < sequence.size(); ++i ) {
      Cap nextCap = sequence.getCap(i);
      pts += nextCap.ownedPoints();
    }
    N = (int)Math.round(pts);

    // relax the caps and scale them to the proper size
    for( int i = 0; i < caps.size(); ++i ) {
      Cap nextCap = (Cap)caps.get(i);
      nextCap.autoRelax();
      nextCap.scaleToSphere(N);
      nextCap.scaleToSphere(N);
    }
  }

  private CapSet runGenetic(int colonySize, int generations, int luckySurvivors)
  {
    Sequence[] colony = new Sequence[colonySize];
    Sequence[] children = new Sequence[(colonySize + 3)*colonySize/2];

    bestSeen = null;
    bestFitness = Double.POSITIVE_INFINITY;

    // establish the initial colony
    for( int i = 0; i < colonySize; ++i ) {
      colony[i] = findFitness( sequence.randomized() );
    }

    // for each generation, mate all sequences and
    // let the best ones live
    for( int g = 0; g < generations; ++g ) {
      int c = 0;
      for( int i = 0; i < colonySize; ++i ) {
        // all the current colony members are 'children'
        children[c++] = colony[i];

        // also save a mutated copy of current members
        children[c++] = findFitness( colony[i].mutated() );

        // mate all pairs of colony members
        for( int j = 0; j < i; ++j ) {
          children[c++] = findFitness( colony[i].mate(colony[j]) );
        }

      }

      // survival of the fittest, except for luckySurvivors
      Arrays.sort(children);
      int nFit = colonySize - luckySurvivors;
      for(int i = 0; i < nFit; ++i ) {
        colony[i] = children[i];
        //System.out.println("survivor: "+colony[i].fitness);
      }
      for(int i = nFit; i < colonySize; ++i) {
        colony[i] = children[ rand.nextInt( children.length - nFit ) + nFit ];
        //System.out.println("lucky survivor: "+colony[i].fitness);
      }
    }

    return bestSeen;
  }

  /*
   * Helper function for runGenetic, calculates the fitness of
   * a sequence and updates bestSeen if necessary
   */
  private Sequence findFitness(Sequence seq)
  {
    CapSet cs = toCapSet(seq);

    // if this is an invalid capset, set its fitness to infinity
    if( cs.numCaps() != sequence.size() || !cs.isValidSphere() ) {
      //System.out.println("invalid: "+cs);
      seq.fitness = Double.POSITIVE_INFINITY;
      return seq;
    }

    seq = new Sequence(cs.toString());
    seq.genetic = sequence.genetic;

    cs.autoRelax();
    Sphere s = cs.toSphere();
    int N = s.numPoints();
    seq.fitness = s.totalEnergy()/( .5*Math.pow(N,2) - .55230*Math.pow(N,1.5) );

    // if this is the best capset so far, remember it
    if( seq.fitness < bestFitness ) {
      bestSeen = cs;
      bestFitness = seq.fitness;
    }

    return seq;
  }


  public CapSet toCapSet()
  {
    if( sequence.isEmpty() )
      return null;

    return toCapSet(sequence);
  }

  private CapSet toCapSet(Sequence seq)
  {
    CapSet caps = new CapSet( seq.getCapCopy(0) );

    for(int i = 1; i < seq.size() && caps.isOpen(); ++i) {
      Cap nextCap = seq.getCapCopy(i);

      // if we have information specified on what
      // joins to use, employ it now
      if( seq.type != null ) {
        if( seq.type[i] == EDGE_JOIN ) {
          if( seq.destination[i] != RANDOM_IDX ) {
            if( seq.my_idx[i] != RANDOM_IDX && seq.dest_idx[i] != RANDOM_IDX )
              caps.addToEdge(nextCap, seq.destination[i], seq.my_idx[i], seq.dest_idx[i]);
            else if( seq.dest_idx[i] != RANDOM_IDX )
              caps.addToEdge(nextCap, seq.destination[i], seq.dest_idx[i]);
            else
              caps.addToEdge(nextCap, seq.destination[i]);
          } else {
            caps.addToEdge(nextCap);
          }
        } else if( seq.type[i] == CORNER_JOIN ) {
          if( seq.destination[i] != RANDOM_IDX ) {
            if( seq.my_idx[i] != RANDOM_IDX && seq.dest_idx[i] != RANDOM_IDX )
              caps.addToCorner(nextCap, seq.destination[i], seq.my_idx[i], seq.dest_idx[i]);
            else if( seq.dest_idx[i] != RANDOM_IDX )
              caps.addToCorner(nextCap, seq.destination[i], seq.dest_idx[i]);
            else
              caps.addToCorner(nextCap, seq.destination[i]);
          } else {
            caps.addToCorner(nextCap);
          }
        } else {
          caps.addToRandom(nextCap);
        }

      } else {
        caps.addCap(nextCap);
      }

    }
    return caps;
  }

  public Sphere toSphere()
  {
    try{
      CapSet caps;

      if(sequence.geneticEnabled()) {
        caps = runGenetic(5,5,1);
      } else {
        caps = toCapSet();

        if( caps == null )
          return null;
        if( !caps.isOpen() )
          caps.autoRelax();
      }

      Const.out.println("Created sphere from caps:");
      Const.out.println(caps);
      return caps.toSphere();
    }catch(Exception e) {
      e.printStackTrace();
      return null;
    }
  }







  private class Sequence implements Comparable
  {
    Vector<Integer> order, genetic;
    char[] type;
    int[] destination, my_idx, dest_idx;
    double fitness;

    Sequence() {}

    /*
     * copy constructor
     */
    Sequence(Sequence seq)
    {
      order = (Vector<Integer>)(seq.order.clone());
      genetic = (Vector<Integer>)(seq.genetic.clone());
      type = (char[])seq.type.clone();
      destination = (int[])seq.destination.clone();
      my_idx = (int[])seq.my_idx.clone();
      dest_idx = (int[])seq.dest_idx.clone();
      fitness = seq.fitness;
    }

    /*
     * this constructor attempts to brute force a sequence of caps
     * that will combine to form a sphere of the correct number
     * of points
     */
    Sequence(double toGo)
    {
      int chargeLeft = 12;
      double[] cost = new double[caps.size()];
      order = new Vector<Integer>();

      /*
        for( int i = 0; i < caps.size(); ++i ) {
        Cap cap = (Cap)caps.get(i);
        cost[i] = N - (12/cap.totCharge) * cap.ownedPoints();
        }
      */

      // create a random initial sequence of caps
      while(chargeLeft > 0) {
        int idx = rand.nextInt(caps.size());
        Cap cap = (Cap)caps.get(idx);
        order.add( new Integer(idx) );
        chargeLeft -= cap.totCharge;
        toGo -= cap.ownedPoints();
      }

      // try up to 5000 random cap replacements to get
      // a valid sequence
      int counter = 0;
      while( (chargeLeft != 0 || toGo > .00001 || toGo < -.00001) && counter < 5000 ) {
        int idx = rand.nextInt(order.size());
        int replaced = ((Integer)order.get(idx)).intValue();
        Cap rCap = (Cap)caps.get(replaced);
        int inserted = rand.nextInt(caps.size());
        Cap iCap = (Cap)caps.get(inserted);
        order.set(idx, new Integer(inserted));

        chargeLeft = chargeLeft + rCap.totCharge - iCap.totCharge;
        toGo = toGo + rCap.ownedPoints() - iCap.ownedPoints();

        if(chargeLeft < 0) {
          idx = rand.nextInt(order.size());
          int removed = ((Integer)order.get(idx)).intValue();
          rCap = (Cap)caps.get(removed);
          order.remove(idx);
          chargeLeft += rCap.totCharge;
          toGo += rCap.ownedPoints();
        }

        if(chargeLeft > 0) {
          inserted = rand.nextInt(caps.size());
          iCap = (Cap)caps.get(inserted);
          order.add(new Integer(inserted));
          chargeLeft -= iCap.totCharge;
          toGo -= iCap.ownedPoints();
        }

        ++counter;
      }

      //System.out.println(counter+" tries");
      if( counter == 5000 )
        order = null;
    }


    /*
     * construct a sequence using a capList
     * see CapPalette constructor for string format
     */
    Sequence(String capList)
    {
      order = new Vector<Integer>();
      genetic = new Vector<Integer>();

      // these arrays should have just enough room to store one int for
      // each cap -- this calculation can potentially overestimate the size
      // of the array, but this is not a problem
      type = new char[ (capList.length() - 2) / 10 ];
      destination = new int[ type.length ];
      my_idx = new int[ type.length ];
      dest_idx = new int[ type.length ];

      // load the caps and create the cap sequence
      try {
        int strIdx = capList.indexOf(':');
        minLength = Integer.parseInt( capList.substring(0, strIdx) );

        int i = 0;
        while( strIdx < capList.length() - 1 ) {
          int nextStrIdx = capList.indexOf('-', strIdx+1);
          Integer capID = new Integer( capList.substring(strIdx+1, nextStrIdx) );

          int capIdx  = loaded.indexOf(capID);
          if( capIdx == -1 ) {
            capIdx = loaded.size();
            loaded.add(capID);

            Cap nextCap = database.readCap( capID.intValue() );
            caps.add( nextCap );

            if( nextCap.sideLength > minLength )
              minLength = nextCap.sideLength;
          }
          order.add(new Integer(capIdx));

          strIdx = nextStrIdx;
          nextStrIdx = capList.indexOf('-', strIdx+1);
          type[i] = capList.substring(strIdx+1, nextStrIdx).charAt(0);
          if(type[i] == GENETIC)
            genetic.add(new Integer(i));

          strIdx = nextStrIdx;
          nextStrIdx = capList.indexOf('-', strIdx+1);
          String nextSub = capList.substring(strIdx+1, nextStrIdx);
          if( nextSub.charAt(0) == RANDOM )
            destination[i] = RANDOM_IDX;
          else
            destination[i] = Integer.parseInt( nextSub );

          strIdx = nextStrIdx;
          nextStrIdx = capList.indexOf('-', strIdx+1);
          nextSub = capList.substring(strIdx+1, nextStrIdx);
          if( nextSub.charAt(0) == RANDOM )
            my_idx[i] = RANDOM_IDX;
          else
            my_idx[i] = Integer.parseInt( nextSub );

          strIdx = nextStrIdx;
          nextStrIdx = capList.indexOf(':', strIdx+1);
          nextSub = capList.substring(strIdx+1, nextStrIdx);
          if( nextSub.charAt(0) == RANDOM )
            dest_idx[i] = RANDOM_IDX;
          else
            dest_idx[i] = Integer.parseInt( nextSub );

          strIdx = nextStrIdx;
          ++i;
        }
      } catch(Exception e) {
        Const.out.println("**Malformed cap instructions**");
        e.printStackTrace();
        return;
      }
    }

    int size()
    {
      return order.size();
    }

    boolean isEmpty()
    {
      return order == null || order.size() == 0;
    }

    public int compareTo(Object seq)
    {
      if(fitness < ((Sequence)seq).fitness || Double.isNaN( ((Sequence)seq).fitness ) )
        return -1;
      else if(fitness > ((Sequence)seq).fitness || Double.isNaN(fitness) )
        return 1;
      else
        return 0;
    }

    boolean geneticEnabled()
    {
      return genetic != null && !genetic.isEmpty();
    }

    int getIndex(int c)
    {
      return ((Integer)order.get(c)).intValue();
    }

    Cap getCap(int c)
    {
      return (Cap)caps.get( getIndex(c) );
    }

    Cap getCapCopy(int c)
    {
      return new Cap(getCap(c));
    }

    void rotateRandomCaps(int n)
    {
      for( int i = 0; i < n; ++i )
        rotateCap( ((Integer)genetic.get( rand.nextInt(genetic.size()) )).intValue() );
    }

    void rotateCap(int c)
    {
      int sides = getCap(c).numSides;
      int rotations = rand.nextInt(sides-1) + 1;

      my_idx[c] = (my_idx[c] + rotations) % sides;
      for( int i = 0; i < size(); ++i ) {
        if( destination[i] == c )
          dest_idx[i] = (dest_idx[i] + rotations) % sides;
      }
    }

    Sequence randomizeGenes()
    {
      // randomly determine a new gene order
      Vector<Integer> newGeneOrder = new Vector<Integer>();
      for( int i = 0; i < genetic.size(); ++i ) {
        newGeneOrder.insertElementAt( order.get( ((Integer)genetic.get(i)).intValue() ), rand.nextInt(newGeneOrder.size() + 1) );
      }

      // apply the new gene order
      for( int i = 0; i < genetic.size(); ++i ) {
        order.setElementAt( newGeneOrder.get(i), ((Integer)genetic.get(i)).intValue() );
      }

      // set all caps to be randomly joined
      for( int i = 0; i < size(); ++i )
        type[i] = RANDOM;

      return this;
    }

    Sequence randomized()
    {
      return new Sequence(this).randomizeGenes();
    }

    Sequence mutate()
    {
      // randomly swap between 0 - 2 genes
      int swaps = rand.nextInt(3);

      // If 0 swaps are chosen, rotate some caps instead of moving genes around
      if( swaps == 0 ) {
        rotateRandomCaps( rand.nextInt(5) + 1 );
        return this;
      }


      // otherwise, randomly swap genes
      for( int i = 0; i < swaps; ++i ) {
        int idx1 = ((Integer)genetic.get( rand.nextInt(genetic.size()) )).intValue();
        int idx2 = ((Integer)genetic.get( rand.nextInt(genetic.size()) )).intValue();

        Integer temp = order.get(idx1);
        order.setElementAt( order.get(idx2), idx1 );
        order.setElementAt( temp, idx2 );
      }

      // set all caps to be randomly joined
      for( int i = 0; i < size(); ++i )
        type[i] = RANDOM;

      return this;
    }

    Sequence mutated()
    {
      // if the sequence is invalid, randomly generate a new sequence
      if( fitness == Double.POSITIVE_INFINITY )
        return sequence.randomized();

      return new Sequence(this).mutate();
    }

    Sequence mate(Sequence partner)
    {
      // if the sequence is invalid, randomly generate a new sequence
      if( fitness == Double.POSITIVE_INFINITY || partner.fitness == Double.POSITIVE_INFINITY )
        return sequence.randomized();

      Sequence seq = new Sequence();

      // if the two lists have different orders, combine the orders
      // and set all the caps to be randomly joined
      if( !order.equals(partner.order) ) {
        seq.order = new Vector<Integer>();

        int[] remaining = new int[caps.size()];
        for( int i = 0; i < size(); ++i )
          remaining[getIndex(i)]++;

        for( int i = 0; i < size(); ++i ) {
          if( (rand.nextInt(2)%2 == 0 || remaining[partner.getIndex(i)] == 0) && remaining[getIndex(i)] > 0 ) {
            seq.order.add( order.get(i) );
            remaining[getIndex(i)]--;
          } else if( remaining[partner.getIndex(i)] > 0 ) {
            seq.order.add( partner.order.get(i) );
            remaining[partner.getIndex(i)]--;
          } else {
            for( int j = 0; j < remaining.length; ++j ) {
              if( remaining[j] > 0 ) {
                seq.order.add(new Integer(j));
                remaining[j]--;
              }
            }
          }
        }

        seq.type = new char[size()];
        for( int i = 0; i < seq.size(); ++i )
          seq.type[i] = RANDOM;
      }


      // if the orders are the same but the join types are different
      // copy order and set all caps to be randomly joined
      else if( !Arrays.equals(type, partner.type) ) {
        seq.order = (Vector)order.clone();
        seq.type = new char[size()];
        for( int i = 0; i < seq.size(); ++i )
          seq.type[i] = RANDOM;
      }

      // if the orders and join types are the same but targets are different
      // copy order and type but set all targets to random
      else if( !Arrays.equals(destination, partner.destination) ) {
        seq.order = (Vector)order.clone();
        seq.type = (char[])type.clone();
        seq.destination = new int[size()];
        for( int i = 0; i < seq.size(); ++i )
          seq.destination[i] = RANDOM_IDX;
      }

      // if the order, type, and targets are the same, copy the lowest
      // energy configuration and rotate some of its caps
      else {
        if( compareTo(partner) < 0 )
          seq = new Sequence(this);
        else
          seq = new Sequence(partner);

        rotateRandomCaps( rand.nextInt(3) + 1 );
      }

      return seq;
    }
  }
}
