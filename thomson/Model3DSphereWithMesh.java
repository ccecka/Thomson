/*
 * Another graphics class which takes a point set and an edge set and draws the
 * mesh appropriately.
 *
 * Cris Cecka
 */

import java.awt.*;

class Model3DSphereWithMesh extends Model3DSphere
{
  // A list of gray values for psuedo-depth edge colors
  protected final static int nGray = 32;
  protected final static int nGrayHalf = nGray / 2;
  protected final static Color gray[] = new Color[nGray];
  static {
    for( int i = 0; i < nGray; ++i ) {
      int gr = (int) (200*(1-Math.pow(((double)i)/(nGray-1), 2.3)));
      gray[i] = new Color(gr, gr, gr);
    }
  }

  Graph d;

  Model3DSphereWithMesh(Shell shell, Graph d_)
  {
    super(shell);
    d = d_;
  }

  // This is a very popular class so we optimize everything!!
  public void paint(Graphics g)
  {
    if( showBackground ) {
      paintEdges(g, d.getEdges());
      paintPoints(g, d.getCoorNumArray());
    } else {
      paintEdgesFront(g, d.getEdges());
      paintPointsFront(g, d.getCoorNumArray());
    }
  }

  protected void paintEdgesFront(Graphics g, EdgeByIndex[] edges)
  {
    float graphFactor = (nGray >> 2) / graphRadius;
    	
    // Draw Edges
    for( int i = edges.length; i != 0; ) {
      EdgeByIndex e = edges[--i];
      LightPoint v1 = tvert[e.getStartIndex()];
      LightPoint v2 = tvert[e.getEndIndex()];

      if(v1.z < 0 && v2.z < 0)
	continue;

      g.setColor(gray[(int)((v1.z + v2.z)*graphFactor) + nGrayHalf]);

      g.drawLine((int)v1.x, (int)v1.y,
		 (int)v2.x, (int)v2.y);
    }
  }

  protected void paintPointsFront(Graphics g, int[] coorNum)
  {
    if( showIndex ) {
      for( int i = tvert.length; i != 0; ) {
	LightPoint v = tvert[--i];
	if( v.z < 0 )
	  continue;
	
	int width = getPointDrawWidth(v);
	int offset = width >> 1;
	
	switch( coorNum[i] ) {
	case 4:	 g.setColor(Color.green);		break;
	case 5:  g.setColor(Color.red);			break;
	case 6:  g.setColor(Color.blue);
	  g.drawString(Integer.toString(i), (int)v.x-offset, (int)v.y-offset);
	  continue;
	case 7:  g.setColor(Color.yellow);		break;
	case 8:  g.setColor(Color.magenta);		break;
	default: g.setColor(Color.black);
	}
	
	g.fillOval( (int)v.x-offset, (int)v.y-offset, width, width);
	g.drawString("" + i, (int)v.x-offset, (int)v.y-offset);
      }
    } else {
      for( int i = tvert.length; i != 0; ) {
	LightPoint v = tvert[--i];
	if( v.z < 0 )
	  continue;
				
	switch( coorNum[i] ) {
	case 4:	 g.setColor(Color.green);		break;
	case 5:  g.setColor(Color.red);			break;
	case 6:  								continue;
	case 7:  g.setColor(Color.yellow);		break;
	case 8:  g.setColor(Color.magenta);		break;
	default: g.setColor(Color.black);
	}

	int width = getPointDrawWidth(v);
	int offset = width >> 1;
	
	g.fillOval( (int)v.x-offset, (int)v.y-offset, width, width);
      }
    }
  }
    
  protected void paintEdges(Graphics g, EdgeByIndex[] edges)
  {
    float graphFactor = (nGray >> 2)/graphRadius;
		
    // Draw Edges
    for( int i = edges.length; i != 0; ) {
      EdgeByIndex e = edges[--i];
      LightPoint v1 = tvert[e.getStartIndex()];
      LightPoint v2 = tvert[e.getEndIndex()];

      g.setColor(gray[(int)((v1.z + v2.z)*graphFactor) + nGrayHalf]);

      g.drawLine((int)v1.x, (int)v1.y,
		 (int)v2.x, (int)v2.y);
    }
  }

  protected void paintPoints(Graphics g, int[] coorNum)
  {	
    if( showIndex ) {
      for( int i = tvert.length; i != 0; ) {
	LightPoint v = tvert[--i];
	
	//int width = (int)(bubbleRadius + bubbleRadiusGraphRatio*.3f*v.z);
	int width = getPointDrawWidth(v);
	int offset = width >> 1;
	
	switch( coorNum[i] ) {
	case 4:	 g.setColor(Color.green);		break;
	case 5:  g.setColor(Color.red);			break;
	case 6:  g.setColor(Color.blue);
	  g.drawString(Integer.toString(i), (int)v.x-offset, (int)v.y-offset);
	  continue;
	case 7:  g.setColor(Color.yellow);		break;
	case 8:  g.setColor(Color.magenta);		break;
	default: g.setColor(Color.black);
	}
	
	g.fillOval( (int)v.x-offset, (int)v.y-offset, width, width);
	g.drawString("" + i, (int)v.x-offset, (int)v.y-offset);
      }
    } else {
      for( int i = tvert.length; i != 0; ) {
				
	switch( coorNum[--i] ) {
	case 4:	 g.setColor(Color.green);		break;
	case 5:  g.setColor(Color.red);			break;
	case 6:  								continue;
	case 7:  g.setColor(Color.yellow);		break;
	case 8:  g.setColor(Color.magenta);		break;
	default: g.setColor(Color.black);
	}
				
	LightPoint v = tvert[i];
	
	int width = getPointDrawWidth(v);
	int offset = width >> 1;
	
	g.fillOval( ((int)v.x)-offset, ((int)v.y)-offset, width, width);
      }
    }
  }


}
