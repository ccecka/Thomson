/*
 * A class responsible for copying a point set, and transforming it. This is
 * used in the graphics engine for rotation, zoom, etc.
 *
 * Cris Cecka
 */


class Transformer3D
{
  Matrix3D amat;

  float[] vert;
  LightPoint[] tvert;

  boolean transformed;

  Transformer3D(int n)
  {
    amat = new Matrix3D();

    vert = new float[3*n];

    tvert = new LightPoint[n];
    for( int i = 0; i < n; ++i )
      tvert[i] = new LightPoint();

    transformed = false;
  }

  public Transformer3D readPoints(Point[] p)
  {
    int nCount = 3*p.length;
    int i = p.length;

    while( i != 0 ) {
      Point temp = p[--i];
      vert[--nCount] = (float)temp.z;
      vert[--nCount] = (float)temp.y;
      vert[--nCount] = (float)temp.x;
    }

    transformed = false;
    return this;
  }

  public LightPoint[] transform()
  {
    if( transformed )
      return tvert;

    amat.transform(vert, tvert);
    transformed = true;
    return tvert;
  }

  public void newSize(int N)
  {
    vert = new float[3*N];
    LightPoint[] oldvert = tvert;
    tvert = new LightPoint[N];

    // Optimization: Reuse old objects
    if( N < oldvert.length ) {
      System.arraycopy(oldvert,0,tvert,0, N);
    } else {
      System.arraycopy(oldvert,0,tvert,0, oldvert.length);

      for( int i = oldvert.length; i < N; ++i )
        tvert[i] = new LightPoint();
    }
  }

  final public int size()
  {
    return tvert.length;
  }

}