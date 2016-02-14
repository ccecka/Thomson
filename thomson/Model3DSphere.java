/*
 * A general class for a graphic model of a set of points. Provides general
 * query methods and a primitive paint method.
 *
 * Cris Cecka
 */

import java.awt.*;
import java.awt.image.*;

class Model3DSphere
{
  final Shell shell;

  Transformer3D transformer;

  LightPoint[] tvert;


  public boolean showBackground = true;
  public boolean showIndex = false;
	
  public float graphRadius;
  protected final static float bubbleRadiusGraphRatio = 0.035f;
  protected final static float bubbleRadiusZScale = 0.3f;

  Model3DSphere(Shell shell_)
  {
    shell = shell_;
  }

  public void setTransformer(Transformer3D t)
  {
    // If the model's size has changed, update the transformer's size
    if( t.size() != shell.numPoints() )
      t.newSize(shell.numPoints());

    // The transformer hasn't seen the model yet
    t.transformed = false;
    transformer = t.readPoints(shell.atom);
  }

  /** Transform all the points in this model */
  public boolean transform()
  {
    if( transformer != null ) {
      tvert = transformer.transform();
      return true;
    }
    return false;
  }

  public void setShowNegZ(boolean show)
  {
    showBackground = show;
  }

  public void setShowIndex(boolean show)
  {
    showIndex = show;
  }

  protected final int getPointDrawWidth(LightPoint v)
  {
    return (int)(bubbleRadiusGraphRatio*(graphRadius + bubbleRadiusZScale*v.z));
  }

  public void highlight(IntList highlighted, Graphics g)
  {
    IntList.Iterator I = highlighted.getIterator();
    g.setColor(Color.gray);

    while( I.hasNext() ) {
      LightPoint v = tvert[I.next()];

      // Twice the radius of a normal circle
      int width = 2 * getPointDrawWidth(v);
      int offset = width >> 1;

      g.fillOval((int)v.x-offset, (int)v.y-offset, width, width);
    }
  }

  public void paint(Graphics g)
  {
    g.setColor(Color.blue);

    for ( int i = tvert.length; i != 0; ) {
      LightPoint v = tvert[--i];

      if( !showBackground && v.z < 0 )
	continue;

      int width = getPointDrawWidth(v);
      int offset = width >> 1;

      g.fillOval((int)v.x-offset, (int)v.y-offset, width, width);

      if( showIndex )
	g.drawString(Integer.toString(i), (int)v.x-offset, (int)v.y-offset);
    }
  }

  public int getPointIndexAt(int x, int y)
  {
    int lowX = x - 4;
    int highX = x + 4;
    int lowY = y - 4;
    int highY = y + 4;
    float highestZ = Float.NEGATIVE_INFINITY;
    int best = -1;

    for ( int i = tvert.length; i != 0; ) {
      LightPoint v = tvert[--i];

      if( v.z > highestZ && v.x < highX && v.y < highY
	  && v.x > lowX && v.y > lowY ) {

	best = i;
	highestZ = v.z;
      }
    }

    return best;
  }

  public Point getNewPointCorrespondingTo(float x, float y)
  {
    float z = (float) Math.sqrt(graphRadius*graphRadius - x*x - y*y);
    if( Float.isNaN(z) ) { //|| isPointAt(x, y) )
      z = 0;
      // Future versions may want to scale x and y to the view sphere (graphRadius)
    }

    // Apply the inverse rotation transformation to project from viewspace to modelspace
    return transformer.amat.inverseTransform(new Point(x,y,z));
  }

  /*private boolean isPointAt(float x, float y)
    {
    transform();
    float width = .01f*graphRadius;

    float lowX = x - width;
    float highX = x + width;
    float lowY = y - width;
    float highY = y + width;

    for( int i = 0; i < tvert.length; ++i ) {
    if( tvert[i].z > 0
    && tvert[i].x < highX && tvert[i].y < highY
    && tvert[i].x > lowX && tvert[i].y > lowY ) {
    return true;
    }
    }

    return false;
    }*/
}
