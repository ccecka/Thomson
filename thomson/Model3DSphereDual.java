/*
 * Another graphics class which draws the dual of a Delaunay graph
 *
 * Cris Cecka
 */

import java.awt.*;

class Model3DSphereDual extends Model3DSphereWithMesh
{

  Model3DSphereDual(Shell shell, Graph d_)
  {
    super(shell, d_);
  }

  // Override the setTransformer Method
  public void setTransformer(Transformer3D t)
  {
    Point[] points = d.getCircumcenters();

    // If the model's size has changed, update the transformer's size
    if( t.size() != points.length )
      t.newSize(points.length);

    // The transformer hasn't seen the model yet
    t.transformed = false;
    transformer = t.readPoints(points);
  }

  // Override paint to pass Dual Edges
  public void paint(Graphics g)
  {
    if( showBackground ) {
      paintEdges(g, d.getDualEdges());
      if( showIndex )
	paintIndices(g);
    } else {
      paintEdgesFront(g, d.getDualEdges());
      if( showIndex )
	paintIndicesFront(g);
    }
  }

  protected void paintIndices(Graphics g)
  {	
    g.setColor(Color.black);
    for( int i = tvert.length; i != 0; ) {
      LightPoint v = tvert[--i];

      //int width = (int)(bubbleRadius + bubbleRadiusGraphRatio*.3f*v.z);
      int width = getPointDrawWidth(v);
      int offset = width >> 1;

      g.drawString("" + i, (int)v.x-offset, (int)v.y-offset);
    }
  }
    
  protected void paintIndicesFront(Graphics g)
  {	
    g.setColor(Color.black);
    for( int i = tvert.length; i != 0; ) {
      LightPoint v = tvert[--i];
      if( v.z < 0 )
	continue;

      //int width = (int)(bubbleRadius + bubbleRadiusGraphRatio*.3f*v.z);
      int width = getPointDrawWidth(v);
      int offset = width >> 1;

      g.drawString("" + i, (int)v.x-offset, (int)v.y-offset);
    }
  }

}