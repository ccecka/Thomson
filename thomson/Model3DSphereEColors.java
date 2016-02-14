/*
 * Alot like the Model3DGouraud class, but overrides the paint method so that
 * Gouraud shading is not used.
 *
 * Cris Cecka
 */

import java.awt.*;

class Model3DSphereEColors extends Model3DGouraud
{
  Model3DSphereEColors(Shell shell, double[] partE, double mean)
  {
    super(shell,null,partE,mean);
  }

  public void paint(Graphics g)
  {
    if( showBackground )
      paintAll(g);
    else
      paintFront(g);
  }

  private void paintAll(Graphics g)
  {
    for ( int i = tvert.length; i != 0; ) {
      LightPoint v = tvert[--i];

      int width = getPointDrawWidth(v);
      int offset = width >> 1;

      g.setColor(new Color(H11toRGBTable[(int)eHueColor[i]]));

      g.fillOval((int)v.x-offset, (int)v.y-offset, width, width);
      if( showIndex )
	g.drawString(Integer.toString(i), (int)v.x-offset, (int)v.y-offset);
    }
  }

  private void paintFront(Graphics g)
  {
    for ( int i = tvert.length; i != 0; ) {
      LightPoint v = tvert[--i];
      if( v.z < 0 )
	continue;

      int width = getPointDrawWidth(v);
      int offset = width >> 1;

      g.setColor(new Color(H11toRGBTable[(int)eHueColor[i]]));

      g.fillOval((int)v.x-offset, (int)v.y-offset, width, width);
      if( showIndex )
	g.drawString("" + i, (int)v.x-offset, (int)v.y-offset);
    }
  }
}
