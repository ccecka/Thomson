/** A fairly conventional 3D matrix object that can transform sets of
 * 3D points and perform a variety of manipulations on the transform
 */

class Matrix3D {
  float xx, xy, xz;
  float yx, yy, yz;
  float zx, zy, zz;
  // float xo, yo, zo;      // Translation. I don't use them in this version


  /** Create a new unit matrix */
  Matrix3D () {
    unit();
  }

  /** Copy a Matrix */
  Matrix3D(Matrix3D mat)
  {
    xx = mat.xx; xy = mat.xy; xz = mat.xz; // xo = mat.xo;
    yx = mat.yx; yy = mat.yy; yz = mat.yz; // yo = mat.yo;
    zx = mat.zx; zy = mat.zy; zz = mat.zz; // zo = mat.zo;
  }

  /** Scale by f in all dimensions */
  void scale(float f)
  {
    xx *= f; xy *= f; xz *= f; // xo *= f;
    yx *= f; yy *= f; yz *= f; // yo *= f;
    zx *= f; zy *= f; zz *= f; // zo *= f;
  }

  /** Scale along each axis independently */
  void scale(float xf, float yf, float zf)
  {
    xx *= xf; xy *= xf; xz *= xf; // xo *= xf;
    yx *= yf; yy *= yf; yz *= yf; // yo *= yf;
    zx *= zf; zy *= zf; zz *= zf; // zo *= zf;
  }

  /** Translate the origin */
  //void translate(float x, float y, float z)
  //{
  //    xo += x; yo += y; zo += z;
  //}

  /** rotate theta radians about the y axis */
  void yrot(float rad)
  {
    float ct = (float) Math.cos(rad);
    float st = (float) Math.sin(rad);

    float Nxx = (xx * ct + zx * st);
    float Nxy = (xy * ct + zy * st);
    float Nxz = (xz * ct + zz * st);
    //float Nxo = (xo * ct + zo * st);

    float Nzx = (zx * ct - xx * st);
    float Nzy = (zy * ct - xy * st);
    float Nzz = (zz * ct - xz * st);
    //float Nzo = (zo * ct - xo * st);

    xx = Nxx; xy = Nxy; xz = Nxz; // xo = Nxo;
    zx = Nzx; zy = Nzy; zz = Nzz; // zo = Nzo;
  }

  /** rotate theta radians about the x axis */
  void xrot(float rad)
  {
    float ct = (float) Math.cos(rad);
    float st = (float) Math.sin(rad);

    float Nyx = (yx * ct + zx * st);
    float Nyy = (yy * ct + zy * st);
    float Nyz = (yz * ct + zz * st);
    //float Nyo = (yo * ct + zo * st);

    float Nzx = (zx * ct - yx * st);
    float Nzy = (zy * ct - yy * st);
    float Nzz = (zz * ct - yz * st);
    //float Nzo = (zo * ct - yo * st);

    yx = Nyx; yy = Nyy; yz = Nyz; // yo = Nyo;
    zx = Nzx; zy = Nzy; zz = Nzz; // zo = Nzo;
  }

  /** rotate theta radians about the z axis */
  void zrot(float rad)
  {
    float ct = (float) Math.cos(rad);
    float st = (float) Math.sin(rad);

    float Nyx = (yx * ct + xx * st);
    float Nyy = (yy * ct + xy * st);
    float Nyz = (yz * ct + xz * st);
    //float Nyo = (float) (yo * ct + xo * st);

    float Nxx = (xx * ct - yx * st);
    float Nxy = (xy * ct - yy * st);
    float Nxz = (xz * ct - yz * st);
    //float Nxo = (float) (xo * ct - yo * st);

    xx = Nxx; xy = Nxy; xz = Nxz; // xo = Nxo;
    yx = Nyx; yy = Nyy; yz = Nyz; // yo = Nyo;
  }

  /** rotate theta radians about the axis defined by the origin and Point p **/
  /** NOTE: does not rotate translation of the matrix **/
  void prot(Point p, double rad)
  {
    p.scale(1.0/p.mag());

    double st = Math.sin(rad);
    double oneMct = 1.0 - Math.cos(rad);
    double pxy = p.x*p.y;
    double pyz = p.y*p.z;
    double pxz = p.x*p.z;

    double xst = p.x*st;
    double yst = p.y*st;
    double zst = p.z*st;

    double xyct = pxy*oneMct;
    double xzct = pxz*oneMct;
    double yzct = pyz*oneMct;

    xx = (float) (1.0 + oneMct * (p.x * p.x - 1.0));
    yx = (float) (zst + xyct);
    zx = (float) (-yst + xzct);
    xy = (float) (-zst + xyct);
    yy = (float) (1.0 + oneMct * (p.y * p.y - 1.0));
    zy = (float) (xst + yzct);
    xz = (float) (yst + xzct);
    yz = (float) (-xst + yzct);
    zz = (float) (1.0 + oneMct * (p.z * p.z - 1.0));
  }

  /** Multiply this matrix by a second: M = M*R */
  void mult(Matrix3D rhs)
  {
    float lxx = xx * rhs.xx + yx * rhs.xy + zx * rhs.xz;
    float lxy = xy * rhs.xx + yy * rhs.xy + zy * rhs.xz;
    float lxz = xz * rhs.xx + yz * rhs.xy + zz * rhs.xz;
    //float lxo = xo * rhs.xx + yo * rhs.xy + zo * rhs.xz + rhs.xo;

    float lyx = xx * rhs.yx + yx * rhs.yy + zx * rhs.yz;
    float lyy = xy * rhs.yx + yy * rhs.yy + zy * rhs.yz;
    float lyz = xz * rhs.yx + yz * rhs.yy + zz * rhs.yz;
    //float lyo = xo * rhs.yx + yo * rhs.yy + zo * rhs.yz + rhs.yo;

    float lzx = xx * rhs.zx + yx * rhs.zy + zx * rhs.zz;
    float lzy = xy * rhs.zx + yy * rhs.zy + zy * rhs.zz;
    float lzz = xz * rhs.zx + yz * rhs.zy + zz * rhs.zz;
    //float lzo = xo * rhs.zx + yo * rhs.zy + zo * rhs.zz + rhs.zo;

    xx = lxx; xy = lxy; xz = lxz; // xo = lxo;
    yx = lyx; yy = lyy; yz = lyz; // yo = lyo;
    zx = lzx; zy = lzy; zz = lzz; // zo = lzo;
  }

  /** Reinitialize to the unit matrix */
  void unit()
  {
    xx = 1; xy = 0; xz = 0; // xo = 0;
    yx = 0; yy = 1; yz = 0; // yo = 0;
    zx = 0; zy = 0; zz = 1; // zo = 0;
  }

  /** Transform nvert points from v into tv.  v contains the input
      coordinates in floating point.  Three successive entries in
      the array constitute a point.  tv ends up holding the transformed
      points as integers; three successive entries per point */
  void transform(float v[], LightPoint tv[])
  {
    int index = tv.length;
    int i = v.length;

    while( index != 0 ) {
      float z = v[--i];
      float y = v[--i];
      float x = v[--i];
      tv[--index].set(x*xx + y*xy + z*xz,  // + xo,
                      x*yx + y*yy + z*yz,  // + yo,
                      x*zx + y*zy + z*zz); // + zo);
    }
  }

  /** Should not be used in the general transformation of a point set since
   * it allocates all new objects.
   */
  Point transformed(Point a)
  {
    return new Point(a.x * xx + a.y * xy + a.z * xz,  // + xo,
                     a.x * yx + a.y * yy + a.z * yz,  // + yo,
                     a.x * zx + a.y * zy + a.z * zz); // + zo);
  }

  Point transform(Point a)
  {
    a.set(a.x * xx + a.y * xy + a.z * xz,  // + xo,
          a.x * yx + a.y * yy + a.z * yz,  // + yo,
          a.x * zx + a.y * zy + a.z * zz); // + zo);
    return a;
  }

  public Matrix3D inverse()
  {
    Matrix3D M = new Matrix3D();
    M.xx = yy*zz - yz*zy;
    M.yx = yz*zx - yx*zz;
    M.zx = yx*zy - yy*zx;
    float det = xx*M.xx + xy*M.yx + xz*M.zx;
    //xx*(yy*zz - yz*zy) + xy*(yz*zx - yx*zz) + xz*(yx*zy - yy*zx)

    if( det == 0 )
      return null;

    M.xy = xz*zy - xy*zz;
    M.xz = xy*yz - xz*yy;
    M.yy = xx*zz - xz*zx;
    M.yz = xz*yx - xx*yz;
    M.zy = xy*zx - xx*zy;
    M.zz = xx*yy - xy*yx;
    M.scale(1/det);

    //M.xo = -( M.xx*xo + M.xy*yo + M.xz*zo );
    //M.yo = -( M.yx*xo + M.yy*yo + M.yz*zo );
    //M.zo = -( M.zx*xo + M.zy*yo + M.zz*zo );

    return M;
  }

  public Point inverseTransform(Point a)
  {
    //a.sub(xo,yo,zo);

    float ixx = yy*zz - yz*zy;
    float iyx = yz*zx - yx*zz;
    float izx = yx*zy - yy*zx;
    float det = xx*ixx + xy*iyx + xz*izx;

    if( det == 0 )
      return null;

    float ixy = xz*zy - xy*zz;
    float ixz = xy*yz - xz*yy;
    float iyy = xx*zz - xz*zx;
    float iyz = xz*yx - xx*yz;
    float izy = xy*zx - xx*zy;
    float izz = xx*yy - xy*yx;

    a.set( (a.x * ixx + a.y * ixy + a.z * ixz)/det,
           (a.x * iyx + a.y * iyy + a.z * iyz)/det,
           (a.x * izx + a.y * izy + a.z * izz)/det );
	
    return a;
  }

  public float det()
  {
    return xx*(yy*zz - yz*zy) + xy*(yz*zx - yx*zz) + xz*(yx*zy - yy*zx);
  }

  public String toString()
  {
    return ("[" + xx + "," + xy + "," + xz /*+ "," + xo*/ + ";"
            + yx + "," + yy + "," + yz /*+ "," + yo*/ + ";"
            + zx + "," + zy + "," + zz /*+ "," + zo*/ + "]");
  }
}
