/*
 * A class to handle all input/output traffic for the ShellApplet.
 * Handles the SQL databasing and provides some parsing methods.
 *
 * Cris Cecka
 */

import java.net.*;
import java.io.*;
import java.sql.*;
import java.util.*;


class ReadWriteFile
{
  final private static String MYSQL_URL = "jdbc:mysql://thomson.phy.syr.edu:3306/thomson?autoReconnect=true";
  final private static String MYSQL_USER = "thomson";
  final private static String MYSQL_PASS = "thmsn00";

  final private static int    GUEST_OWNER = 0;
  final private static int    GLOBALMIN_OWNER = -1;
  final private static String GLOBALMIN_COMMENT = "Lowest Energy Seen";
  final private static int    LOCALMIN_OWNER = -2;
  final private static String LOCALMIN_COMMENT = "Local Minimum";
  final private static int    DELTA_OWNER = -4;
  final public  static int    CAP_OWNER = -3;

  final public static String SPHERE_DB = "sphere";
  final public static String TORUS_DB  = "torus";

  Connection con;

  ReadWriteFile()
  {
    try {
      // Connect to the mysql server
      Class.forName("com.mysql.jdbc.Driver").newInstance();
      //Class.forName("org.gjt.mm.mysql.Driver").newInstance();
    } catch( Exception e ) {
      Const.out.println("SQL Driver Not Found" + e.getMessage());
    }
		
    try {
      con = DriverManager.getConnection(MYSQL_URL, MYSQL_USER, MYSQL_PASS);
    } catch( Exception e ) {
      Const.out.println("Could Not Connect To Database...\n" + e.getMessage());
    }
  }

  // Load the lowest energy stored version of the Shell
  public void loadLowest(Shell shell)
  {
    String DB;
    String ADDITIONAL_CONSTRAINTS = "";
		
    // find out if we're dealing with a sphere or torus
    if( shell instanceof Sphere ) {
      DB = SPHERE_DB;
    } else if( shell instanceof Torus ) {
      DB = TORUS_DB;
      double aspectRatio = ((Torus)shell).getRatio();
      // aspect ratio is a double so we can't compare directly for equality
      ADDITIONAL_CONSTRAINTS = " AND aspect_ratio > " + (aspectRatio-0.00001) +
	" AND aspect_ratio < " + (aspectRatio+0.00001);
    } else {
      Const.out.println("Unknown shell type!");
      return;
    }

    String query = "SELECT pointbytearray FROM " + DB + 
      " WHERE owner_id = " + GLOBALMIN_OWNER +
      " AND numpoints = "+ shell.numPoints() + 
      " AND potential = " + shell.getPotential() +
      ADDITIONAL_CONSTRAINTS;
		
    try {
      // find the ID of the best sphere with numPoints points
      ResultSet result;
      Statement stmt = con.createStatement();
      result = stmt.executeQuery(query);
      result.next();
      byte[] pointbytearray = result.getBytes("pointbytearray");
      shell.setPoints( Point.bytes2Points( pointbytearray ) );
    } catch(SQLException e) {
      Const.out.println("Shell Not Found In Database");
    } catch(NullPointerException npe) {
      Const.out.println("Could Not Read From Database");
    }
  }
	
  double ENERGY_TOLERANCE = 1.0 - 1.0E-13;

  public boolean isEnergyLower(double energy, double lowestEnergy)
  {
    return Math.abs(energy) < ENERGY_TOLERANCE * Math.abs(lowestEnergy);
  }
  
  public boolean isEnergyNewLowest(double energy, Shell shell)
  {
    return isEnergyLower(energy, lowestEnergySeen(shell));
  }

  // Find the minimum energy for a given shell
  public double lowestEnergySeen(Shell shell)
  {
    String DB;
    String ADDITIONAL_CONSTRAINTS = "";
    if( shell instanceof Sphere ) {
      DB = SPHERE_DB;
    } else if( shell instanceof Torus ) {
      DB = TORUS_DB;
      ADDITIONAL_CONSTRAINTS = " AND aspect_ratio = "+((Torus)shell).getRatio();
    } else {
      Const.out.println("Unknown shell type!");
      return 0;
    }

    String query = "SELECT energy FROM " + DB +
      " WHERE owner_id = " + GLOBALMIN_OWNER + 
      " AND numpoints = " + shell.numPoints() +
      " AND potential = " + shell.getPotential() +
      ADDITIONAL_CONSTRAINTS;

    try {
      ResultSet result;
      Statement stmt = con.createStatement();
      result = stmt.executeQuery(query);
      result.next();
      return result.getDouble("energy");
    } catch( SQLException e ) {
      return Double.POSITIVE_INFINITY;   // The shell isn't in the table
    } catch( NullPointerException npe ) {
      return 0;                          // The connection wasn't created
    }
  }


  // Wrapper method for write points called when a user saves a shell
  public boolean writeShell(Shell shell, int ownerID, String comment)
  {
    double energy = shell.totalEnergy();
    boolean lowest = isEnergyNewLowest(energy, shell);

    // If this is a global min, save it as a global min
    if( ownerID == GLOBALMIN_OWNER && lowest )
      return writeGlobalMin(shell, energy);

    // If this is a new low, steal it as a global min
    if( lowest )
      writeGlobalMin(shell, energy);
    
    // Else, this is some other owner, save it
    return writeShell(shell, ownerID, comment, energy);
  }

  // Wrapper method for write points called when a new lowest energy is found
  public boolean writeGlobalMin(Shell shell, double totalEnergy)
  {
    return writeShell(shell, GLOBALMIN_OWNER, GLOBALMIN_COMMENT, totalEnergy);
  }
  
  private boolean writeShell(Shell shell, int ownerID, String comment, double totalEnergy)
  {
    // Find out if we're dealing with a sphere or torus
    String DB;
    String ADDITIONAL_CONSTRAINTS = "";
    String ADDITIONAL_FIELDS = "";
    if( shell instanceof Sphere ) {
      DB = SPHERE_DB;
    } else if( shell instanceof Torus ) {
      DB = TORUS_DB;
      double aspectRatio = ((Torus)shell).getRatio();
      ADDITIONAL_CONSTRAINTS = " AND aspect_ratio > " + (aspectRatio-0.00001) +
	" AND aspect_ratio < " + (aspectRatio+0.00001);
      ADDITIONAL_FIELDS = ", aspect_ratio = " + aspectRatio;
    } else {
      Const.out.println("Unknown shell type!");
      return false;
    }
		
    Point[] points = shell.getCopyPoints();
    if( points.length > ShellApplet.NMAX ) {
      Const.out.println("N = " + (points.length) + 
			" > " + ShellApplet.NMAX + " is too large");
      return false;
    }
		
    try {
      if( ownerID == GLOBALMIN_OWNER ) {
	// Determine if this global min already exists
				
	ResultSet result;
	Statement stmt = con.createStatement();
	result = stmt.executeQuery("SELECT id,energy FROM " + DB + 
				   " WHERE numpoints = " + points.length + 
				   " AND potential = " + shell.getPotential() + 
				   " AND owner_id = " + GLOBALMIN_OWNER 
				   + ADDITIONAL_CONSTRAINTS);
				
	if( result.next() ) {
	  // Another global min for this system exists... Double check energy
	  if( isEnergyLower(totalEnergy, result.getDouble("energy")) ) {
	    // The global min already exists and this is better
	    // Set the unique id to replace
	    ADDITIONAL_FIELDS += ", id = " + result.getInt("id");
	  } else {
	    // We're trying to add a global min that isn't min!!
	    return false;
	  }
	}
      }
    } catch(SQLException sqle) {
      Const.out.println("Could Not Write To Database...");
      System.out.println("mySQL error: " + sqle.getMessage());
      return false;
    } catch( NullPointerException e ) {
      Const.out.println("Could Not Write To Database...");
      return false;
    }

    if( comment.equals("") )
      comment = "No Comment";

    int[] coorNumArray = shell.getDelaunay().getCoorNumArray();
    int potential = shell.getPotential();
    // Compute the coordinate number statistics
    int[] coorBin = {0,0,0,0,0,0,0,0,0};
    for( int k = 0; k < points.length; ++k ) 
      if( coorNumArray[k] >= 4 && coorNumArray[k] <= 8 )
	++coorBin[ coorNumArray[k] ];

    String query = "REPLACE INTO " + DB + 
      " SET numpoints = " + points.length + 
      ", potential = " + potential + 
      ", energy = " + totalEnergy + 
      ", coor4 = " + coorBin[4] +
      ", coor5 = " + coorBin[5] +
      ", coor6 = " + coorBin[6] +
      ", coor7 = " + coorBin[7] +
      ", coor8 = " + coorBin[8] +
      ", pointbytearray = ?" + 
      ", owner_id = " + ownerID + 
      ", comment = '" + comment + "'" +
      ADDITIONAL_FIELDS;

    try {

      PreparedStatement stmt = con.prepareStatement(query);
      stmt.setBytes(1, Point.points2Bytes(points));

      stmt.executeUpdate();
      return true;

    } catch(SQLException sqle) {
      Const.out.println("Could Not Write To Database...");
      System.out.println("mySQL error: "+sqle.getMessage());
      return false;
    } catch( NullPointerException e ) {
      Const.out.println("Could Not Write To Database...");
      return false;
    }
  }

  // Load the best shell from database, given a 'filename'
  // (in the form id.topology)
  public Shell readShell(int id, String DB)
  {
    try {
      // if we're reading a cap, return it as a sphere
      if( DB.equalsIgnoreCase("cap") )
	return readCap(id).toSphere();

      String query = "SELECT potential, pointbytearray";
      if( DB.equals( TORUS_DB ) )
	query += ", aspect_ratio ";
      query += " FROM " + DB + " WHERE id = " + id;

      Statement stmt = con.createStatement();
      ResultSet result = stmt.executeQuery(query);
      result.next();

      Shell shell = null;
      if( DB.equals( SPHERE_DB ) ) {
	shell = new Sphere();
      } else if( DB.equals( TORUS_DB ) ) {
	shell = new Torus();
	((Torus)shell).setRatio( result.getDouble("aspect_ratio") );
      }

      shell.setPoints( Point.bytes2Points(result.getBytes("pointbytearray")) );
      shell.setPotential( Const.potentialList[ result.getInt("potential") ] );
      return shell;

    } catch( SQLException sqle ) {
      Const.out.println("Could Not Read From Database...");
      System.out.println("mySQL error: " + sqle.getMessage());
    } catch( NullPointerException e ) {
      Const.out.println("Could Not Read " + id + "." + DB + " From Database");
    }

    return null;
  }




  /****************
   * LEGACY CODE *
   ****************/
  public boolean writeCap(Cap cap, int ownerID, String comment)
  {
    Point[] points = cap.getPoints();
    IntList[] adjArray = cap.getAdjArray();
    int[] coorNumArray = cap.getCoorNumArray();

    String query = "INSERT INTO caps SET numpoints = "+points.length
      +", numsides = "+cap.numSides+", sidelength = "+cap.sideLength
      +", owner_id = '"+ownerID+"', comment = '"+comment+"'";

    try {
      int ID=0;
      Statement stmt = con.createStatement();
      ResultSet result;

      // Insert general information into the master table
      stmt.executeUpdate(query, Statement.RETURN_GENERATED_KEYS);

      result = stmt.getGeneratedKeys();
      if(result.next())
	ID = result.getInt(1);

      // Insert individual points into point table
      for(int i=0; i<points.length; ++i) {
	Point p = points[i];
	stmt.executeUpdate("INSERT INTO cap_points SET id="+ID+", idx="+i
			   +", xcoord="+p.x+", ycoord="+p.y+", zcoord="+p.z
			   +", coor_num="+coorNumArray[i]);
      }

      // Insert adjacency array into adjacency table
      for(int i=0; i<adjArray.length; ++i) {
	IntList.Iterator adjIter = adjArray[i].getIterator();
	while( adjIter.hasNext() )
	  stmt.executeUpdate("INSERT INTO cap_adjacency SET id="+ID
			     +", idx1="+i+", idx2="+adjIter.next());
      }

      return true;
    } catch( SQLException sqle ) {
      Const.out.println("Could Not Write To Database...");
      System.out.println("mySQL error: "+sqle.getMessage());
      return false;
    } catch( NullPointerException e ) {
      Const.out.println("Could Not Write To Database...");
      return false;
    }
  }


  public Cap readCap(int ID)
  {
    try{
      int numPoints, totCharge, numSides, sideLength;

      // look up general information from master table
      ResultSet result;
      Statement stmt = con.createStatement();
      result = stmt.executeQuery("SELECT numpoints, numsides, sidelength "
				 +"FROM caps WHERE id = "+ID);
      result.next();
      numPoints = result.getInt("numpoints");
      numSides = result.getInt("numsides");
      sideLength = result.getInt("sidelength");
      totCharge = 6 - numSides;

      // look up point list and coordination numbers
      Point[] points = new Point[numPoints];
      IntList[] adjArray = new IntList[numPoints];
      int[] coorNumArray = new int[numPoints];

      result = stmt.executeQuery("SELECT xcoord, ycoord, zcoord, coor_num "
				 + "FROM cap_points WHERE id = "+ID+" ORDER BY idx");
      int i=0;
      while(result.next()) {
	points[i] = new Point(result.getDouble("xcoord"),
			      result.getDouble("ycoord"),
			      result.getDouble("zcoord"));
	coorNumArray[i] = result.getInt("coor_num");
	++i;
      }

      // look up adjacency array
      for(i=0; i<numPoints; ++i) {
	result = stmt.executeQuery("SELECT idx2 FROM cap_adjacency "
				   +"WHERE id = "+ID+" AND idx1 = "+i);
	adjArray[i] = new IntList();
	while(result.next())
	  adjArray[i].add(result.getInt("idx2"));
      }

      Cap cap = new Cap(points, adjArray, coorNumArray, totCharge, numSides, sideLength);
      cap.id = ID;
      return cap;
    } catch( SQLException sqle ) {
      Const.out.println("Could Not Read From Database...");
      System.out.println("mySQL error: "+sqle.getMessage());
      return null;
    } catch( NullPointerException e ) {
      Const.out.println("Could Not Read From Database...");
      return null;
    }
  }

  public Vector<Cap> readCapsByLength(int sideLength)
  {
    Vector<Cap> caps = new Vector<Cap>();

    try {
      ResultSet result;
      Statement stmt = con.createStatement();
      result = stmt.executeQuery("SELECT id FROM caps WHERE sideLength <= "+sideLength);
      while(result.next()) {
	Cap nextCap = readCap(result.getInt("id"));

	// make sure the cap has the correct number of edges
	nextCap.grow( sideLength - nextCap.sideLength );

	nextCap.autoRelax();
	caps.add(nextCap);
	//System.out.println("reading cap "+result.getInt("id"));
      }
    } catch( SQLException sqle ) {
      Const.out.println("Could Not Read From Database...");
      System.out.println("mySQL error: "+sqle.getMessage());
      return null;
    } catch( NullPointerException e ) {
      Const.out.println("Could Not Read From Database...");
      return null;
    }

    return caps;
  }

}
