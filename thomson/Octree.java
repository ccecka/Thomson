/*
 * An implementation of the OcTree using Morton Code
 * Focused on speed
 * Uses the Barnes-Hut algorithm to calculate force and potential
 *
 * Jimmy Zhu
 */

//TODO: Extend usage to shapes other than sphere
//TODO: Get rid of TUPLE?
import java.util.*;


public class Octree
{
  /** A class to store a Point and its Morton code for sorting
   */
  private class Tuple implements Comparable<Tuple>
  {
    private Point atom;
    private int morton;

    public Tuple(){}
    public Tuple(Point a, int b) {
      atom = a;
      morton = b;
    }

    public Point fst() { return atom; }
    public int snd() { return morton; }

    public int compareTo(Tuple a) {
      return morton - a.snd();
    }
  }

  private class MortonCoder {
    private Point pmin;
    private Point cell_size;

    public MortonCoder(Point bl_, Point tr_, int levels_) {
      int cells_per_side = 1 << levels_;

      pmin = new Point(bl_);
      cell_size = (tr_.minus(bl_)).scale(1.0/cells_per_side);
    }

    private int spread_bits(int x) {
      x = (x | (x << 16)) & 0X30000FF;
      x = (x | (x <<  8)) & 0X300F00F;
      x = (x | (x <<  4)) & 0X30C30C3;
      x = (x | (x <<  2)) & 0X9249249;
      return x;
    }

    private int  interleave(int x, int y, int z) {
      return spread_bits(x) | (spread_bits(y) << 1) | (spread_bits(z) << 2);
    }

    private int compact_bits(int x) {
      x &= 0X9249249;
      x = (x | (x >>  2)) & 0X30C30C3;
      x = (x | (x >>  4)) & 0X300F00F;
      x = (x | (x >>  8)) & 0X30000FF;
      x = (x | (x >> 16)) & 0X3FF;
      return x;
    }

    public Point deinterleave(int c) {
      return new Point(compact_bits(c),
                       compact_bits(c >> 1),
                       compact_bits(c >> 2));
    }

    /** Return the Morton code corresponding to Point p w.r.t. the bounding box
     * @pre Point p is contained in the bounding box of this MortonCoder
     */
    public int code(Point p) {
      int sx = (int) ((p.x - pmin.x) / cell_size.x);
      int sy = (int) ((p.y - pmin.y) / cell_size.y);
      int sz = (int) ((p.z - pmin.z) / cell_size.z);
      return interleave(sx, sy, sz);
    }
  }

  private static int MAXDEPTH = 10;
  private static int N;
  private int[] boxes;
  private int[][] p2box; //p2box[d][p] gives the index of Point p at depth 'd+1' in array 'boxes'
  private Point[] centers;

  private Point[] atom; //store just the atoms
  private int[] morton; //store the morton codes corresponding to atoms

  private static final int[] lookup = {0, 3, 0, 3, 4, 7, 0, 9,
                                       3, 4, 5, 6, 7, 8, 1, 10,
                                       2, 4, 6, 9, 5, 5, 8, 2,
                                       6, 9, 7, 2, 8, 1, 1, 10};

  private final Potential potential;
  private final double thresh; //aperture size
  private double box_size; //the base box size

  //create a constructor when given radius of the shell
  public Octree(Point[] a, double theta, Potential p, double shellRad)
  {
    thresh = theta;
    potential = p;
    N = a.length;
    MortonCoder mc = new MortonCoder(
        new Point(-shellRad,-shellRad,-shellRad),
        new Point( shellRad, shellRad, shellRad),
        MAXDEPTH);

    box_size = shellRad;
    Tuple[] map = new Tuple[N];
    for(int j = 0; j < N; ++j){
      map[j] = new Tuple(a[j], mc.code(a[j]));
    }
    Arrays.sort(map);

    atom = new Point[N]; morton = new int[N];
    for(int j = 0; j < N; ++j)
    {
      atom[j] = map[j].fst();
      morton[j] = map[j].snd();
    }

    int mask;
    int size = 2*MAXDEPTH; //the first box at each level and the "flag" box at each level
    int prev;
    for(int d = 0; d < MAXDEPTH; ++d) //counts number of boxes necessary
    {
      mask = -1 << (27 - 3*d);
      prev = morton[0];
      for(int at = 1; at < N; ++at)
      {
        if( ((morton[at] ^ prev) & mask) != 0)
        {
          ++size;
          prev = morton[at];
        }
      }
    }

    boxes = new int[size]; p2box = new int[MAXDEPTH][N+1]; //do we still need the N+1?
    centers = new Point[size];
    assemble();
  }

  private void assemble() //finishes initializing all private instance variables
  {
    int index = 0; //continuous index between p2box and the boxes array
    int mask, prev, begin;
    Point p;
    for(int d = 0; d < MAXDEPTH; ++d)
    {
      mask = -1 << (27-3*d);
      p2box[d][0] = index;
      p = new Point(atom[0]); //center of mass tracker
      prev = morton[0]; //the last common morton number
      begin = 0; //beginning index of a sequence of common morton numbers
      for(int a = 1; a < N; ++a)
      {
        if( ((morton[a] ^ prev) & mask) != 0)
        {
          centers[index] = p.scale(1.0/(a-begin));
          p = new Point(atom[a]);
          boxes[index++] = begin;
          p2box[d][a] = index; //move on from previous box
          begin = a;
          prev = morton[a];
        }
        else
        {
          p.add(atom[a]);
          p2box[d][a] = index; //same as previous
        }
      }
      centers[index] = p.scale(1.0/(N-begin));
      boxes[index] = begin; boxes[++index] = N;
      p2box[d][N] = index;
      ++index; //starting point for next level (next empty box)
    }
  }

  //returns the atoms in sorted order to function correctly with Applet
  public Point[] getAtoms()
  {
    return atom;
  }

  public Point[] getForce()
  {
    Point[] force = new Point[N];
    Point kForce;
    int depth;

    // For each atom
    for(int k = 0; k < N; ++k) {
      kForce = force[k] = new Point();
      depth = 0; // 0 is depth 1 in matrix indices

      // Traverse the tree to compute the influence
      int j = 0;
      while(true) {
        // Get the current box
        int end = boxes[p2box[depth][j] + 1];
        // If this box does not contain the atom k and it satisfies the MAC
        if((end <= k || j > k)
           && isBodyRel(centers[p2box[depth][j]], atom[k], box_size/(1<<depth)))
        {
          // Compute the influence
          potential.force(atom[k], centers[p2box[depth][j]], kForce, end - j);
          // Check if we're done
          if(end == N)
            break;
          depth = branch_depth(j, end);
          j = end;
        }
        // If this box is a leaf
        else if(depth == MAXDEPTH - 1)
        {
          for(; j < end; ++j)
            potential.force(atom[k], atom[j], kForce, 1);
          // Check if we're done
          if(end == N)
            break;
          depth = branch_depth(end-1, end);
        }
        // If this box does not satisify MAC and it's not a leaf, increase depth
        else
        {
          ++depth;
        }
      }
    }
    return force;
  }

  public double getPotential()
  {
    double p = 0;
    int depth;
    // For each atom
    for(int k = 0; k < N-1; ++k) {
      depth = 0; // 0 is depth 1 in matrix indices
      // Traverse the tree to compute the influence
      int j = k+1;
      while(true) {
        // Get the current box
        int end = boxes[p2box[depth][j] + 1];
        // If this box does not contain the atom k and it satisfies the MAC
        if((end <= k || j > k)
           && isBodyRel(centers[p2box[depth][j]], atom[k], box_size/(1<<depth)))
        {
          // Compute the influence
          p += (end-j) * potential.energy(atom[k], centers[p2box[depth][j]]);
          // Check if we're done
          if(end == N)
            break;
          depth = branch_depth(j, end);
          j = end;
        }
        // If this box is a leaf
        else if(depth == MAXDEPTH-1)
        {
          for(; j < end; ++j)
            p += potential.energy(atom[k], atom[j]);
          // Check if we're done
          if(end == N)
            break;
          depth = branch_depth(end-1, end);
        }
        // If this box does not satisify MAC and it's not a leaf, increase depth
        else
        {
          ++depth;
        }
      }
    }
    return p;
  }

  //TODO: Scale this according to the base octant size and such
  //determines whether o satisfies the threshold to be considered a body
  private boolean isBodyRel(Point current, Point p, double box_data)
  {
    //size of the octant is always 2^(-depth)
    double dx = (p.x-current.x), dy = (p.y-current.y), dz = (p.z-current.z);
    return (box_data < thresh * Math.sqrt(dx*dx + dy*dy + dz*dz));
  }

  /** The lowest common depth of two Bodies
   * @param a Index of first point
   * @oaram b Index of second point
   * @return The earlist (smallest) depth at which the points are contained
   * in different boxes
   */
  private int branch_depth(int a, int b) {
    int diff = morton[a] ^ morton[b];
    diff |= diff >> 1;
    diff |= diff >> 2;
    diff |= diff >> 4;
    diff |= diff >> 8;
    diff |= diff >> 16;
    return 9 - lookup[(diff * 0X07C4ACDD) >>> 27];
  }

  /** Human readable Morton codes for debugging
   */
  private static String OctString(int i)
  {
    String ret = "";
    while(i != 0)
    {
      int rem = i % 8;
      switch (rem) {
        case 0: ret = "000 " + ret; break;
        case 1: ret = "001 " + ret; break;
        case 2: ret = "010 " + ret; break;
        case 3: ret = "011 " + ret; break;
        case 4: ret = "100 " + ret; break;
        case 5: ret = "101 " + ret; break;
        case 6: ret = "110 " + ret; break;
        case 7: ret = "111 " + ret; break;
      }
      i /= 8;
    }
    return ret;
  }
}

