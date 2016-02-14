<?
include_once "./config/authenticate.php";

// Shell listing main page
// allows basic and advanced searching
include_once "./config/config.php";
?>
<html>
<head><title>Thomson Problem - Points on a Sphere</title>
<link href="/thomson.css" rel="stylesheet" type="text/css" />
</head>
<body background="./media/background.gif">


<table height="100%" width="100%" cellpadding="0" cellspacing="0" border="0">
<tr><td>

<div align="center">
  <table height="100%" width="50%" cellpadding="0" cellspacing="0" border="0">
  <tr>
    <td><img src="img/thomson_logo_small.png" alt="Thomson Problem"></td>
  </tr>

  <tr>
    <td><txt>
<center>
If this applet fails to load, try updating java at <a
href="http://www.java.com/en/download">http://www.java.com/en/download</a>
</center>
</txt>
    </td>
  </tr>

  <tr>
    <td><div align="center"><br>
      <table width="100%" border="4">
      <tr>
         <td>
         <applet code=ShellApplet.class name=ShellApplet archive=ShellApplet.jar width=750 height=550>
         <param name="userName" value="guest">
         <param name="userID" value="0">
         </applet>
         </td>
      </tr>
      </table>
    </td>
  </tr>

  <tr>
    <td><div align="center">
    <txt><br>
     Left Drag: Rotate The System<br>
     Right Drag: Zoom<br>
     Shift + Click: Add A Particle<br>
     Control + Click: Remove A Particle<br>
     Shift + Control + Drag: Drag A Particle<br>
     BETA: Single Click a Point to Highlight<br>
     Double Click to Select and Rotate a Scar
     <br><br>
    </txt></td>
  </tr>

  <tr>
    <td><div align="center">
    <txt>
	<table align="center" bgcolor="#dddddd" cellspacing=5 cellpadding=5>
	<tr><td><B><big>Data Quick Search</big></B></td></tr>
	<tr><td><? include "./shells/partials/_quick_search.php"; ?><a href="./shells/index.php">Advanced Search</a></td></tr>
	</table>

	Currently logged in to this applet as "<?= $_SESSION['userName'] ?>"; you can <a href="./login.php">save data under a different user name</a>.<br>
	Try the new <a href="./index.php">cap builder</a>.<br>
	<br>
    </txt></td>
  </tr>

  <tr>
    <td><div align="justify">
    <txt class="h1">Options -> Initialization</txt>
    <txt><br><br>
    Input the number of points you want the system to have. There must be at least 2 points and at most
    5000 points (subject to change). There are two initial configuration choices:
    <br>
    <ul>
    <li><txt class="h2">Random</txt><br>
    <txt>
    Places points randomly on the surface.
    </txt>
    <br><br>
    <li><txt class="h2">Spiral</txt><br>
    <txt>
    Uses an algorithm to place the points on a spiral about the surface. This configuration will often
    be a good starting point for larger systems that may require a lot of work.</txt>
    </ul>
    <br>

    <txt class="h1">Working with the System</txt>
    <txt><br><br>
     Once the system is initialized, an algorithm can be applied to it. The drop down menu lists possible
     algorithms. They include:</txt>

     <ul>
     <li><txt class="h2">Relaxation</txt><br>
         <txt>
         Relaxation treats the particles as electrostatically charged and uses pair-interactions to determine energy
         and force. A force is calculated for each particle of the system and the particle is moved along the shell
         in the direction of the force vector a distance propotional to the magnitude of the force acting on it and
         tStep. The force vectors are "normalized" with respect to the shell size, number of particles, and the square
         root of the maximum torque on a particle. This yields the notion of a unit step over the shell, but gives a little
         more versatility. Use the "Auto" button to anneal tStep via a tuned, heuristic algorithm. Thus, this method is Steepest Descent with a heuristic step length. With arbitrarily small tStep you can achieve
         arbitrarily accurate energies for stable states within machine limits.</txt>

    <br><br>

    <li><txt class="h2">Barnes-Hut Relaxation</txt><br>
         <txt>
         Identical in function to the regular Relaxation algorithm but force and energy calculations between particles are
	 accomplished through the Barnes-Hut algorithm. By using an Octree to group particles, far away and/or compact
	 clusters can be treated as large single bodies to reduce the number of pairwise computations. The Multipole
	 Acceptance Criteria (MAC) is the maximum ratio of the cluster size to distance in order to treat the group as a single mass.
	 Higher values for the MAC (up to 0.5) allow for greatly increased speed while lower values afford greater accuracy.
	 The "Auto" button can also be used for this algorithm. Using "Auto" will change tStep just like regular Relaxation but the MAC
	 will also decrease over time to increase accuracy. Additionally, if a specific value of the MAC is too slow for the number of particles,
	 the applet  will automatically switch to the regular AutoRelaxer.
	  </txt>

    <br><br>

    <li><txt class="h2">Local Relaxation</txt><br>
         <txt>
         This takes advantage of the triangulation of the points by not considering
         the entire system, but only local neighbors to a particle. This way, far less computing work must be done (O(N)
         relaxation step under an O(NlogN) mesh generation algorithm). Two options are available: The degree of one point
         to another is the number of edges that compose the shortest path between them. The tStep is again a measure of the perturbation of the system.
         For long range potentials (s < 2) this algorithm is unreliable for obvious reasons. When s = 12, this algorithm excels.</txt>

   <br><br>

   <li><txt class="h2">Local Monte Carlo</txt><br>
       <txt>
       This is a classic optimization algorithm that functions as follows: Consider a random particle. Move this particle over the
       surface with a localized Gaassian probability distribution. If this change has caused a drop in the energy, accept the change. If the change
       increases the total energy of the system, accept it with a Boltzmann probability distribution e^(&Delta;V/KbT), where &Delta;V
       is the change in energy (J), Kb = 1.3806503E-23 J/K is Boltzmann's Constant, and T is the temperature of the system (K). Thus,
       lower temperatures will result in driving the system down in energy and higher temperatures will increase the energy and
       randomness of the system. This algorithm can be useful in evaluating finite temperature systems (in all other algorithms, the
       system is considered to be at 0 degrees Kelvin) and the stability of some topological structures that you may find.

   <br><br>

   <li><txt class="h2">Thermal Relaxation</txt><br>
       <txt>
       A combination of the Local Monte Carlo method with the Relaxation method. This causes the system to evolve on a randomly perturbed steepest descent path and can also be used to determine the stability of systems and topological structures.</txt>

   <br><br>

   <li><txt class="h2">Monte Carlo Anneal</txt><br>
       <txt>
       A modified version of Krauth's annealing algorithm for arranging circles on a sphere. We use hard-shell potentials
       about each point instead of the typical Coulombic potentials. We can then increase the radius of influence of each particle or
       decrease the shell radius to anneal the system. Instead of using a cubic Gaussian, we use a spherical Gaussian to yeild a flat directional distribution of the perturbations. As in the Local Monte Carlo, Movement specifies the distribution's standard deviation from 0.0 respect to
       the shell's radius. Prob of Anneal is the probabilty of annealing the structure with respect to the number of accepted movements
       and Anneal by is the scaling ratio to anneal the system by. Increasing the Prob of Annealing and decreasing Anneal by will
       greatly increase the rate at which the annealing process occurs.</txt>

    <br><br>

   <li><txt class="h2">Lattice Energy</txt><br>
       <txt>
       Treats the edges as springs and conserves the Delaunay trigulation of a point set. A Lattice Constant <tt>a</tt> is defined as
       4&pi;R<sup>2</sup>/Faces = (sqrt(3)/2)a<sup>2</sup>. That is, the prefered length of an edge is that which would make all faces perfect equilateral
       triangles.</txt>

   <br><br>

   <li><txt class="h2">Genetic Algorithm</txt><br>
       <txt>
       Genetic Algorithms are typically not applied to physical systems. Mating,
       mutation, and crossover in physical systems are often difficult to define and determining an organism's fitness is often
       computationally intensive. In accord, this algorithm is slow, yet highly successful for this problem. Colony defines the
       carrying capacity of the environment, extra organisms who are not fit enough will be killed and discarded after each generation.
       Lucky is the number of lucky survivors each generation. These are organisms who may not be the most fit, but are lucky enough to
       survive the natural selection. Generations is the number of generations to run the algorithm for, which can be interrupted by
       pressing PAUSE and waiting for the algorithm to complete the current generation.
       <b>WARNING: ALGORITHM IS VERY SLOW (yet very effective). TEST SMALL SYSTEMS TO GET A FEEL FOR THE SPEED, ACCURACY, AND THE PATIENCE
       OF THE USER BEFORE DOING SOMETHING RASH! BE PREPARED TO PACK A LUNCH.</b></txt>

   <br><br>

   <li><txt class="h2">Construct (m,n)</txt><br>
       <txt>
       There are "magic number" states of N defined by N = 10(m<sup>2</sup> + mn + n<sup>2</sup>) + 2 and denoted by (m,n). A feature of these magic numbers
       is that they have a configuration with 12 5-coordinated points placed at the vertices of an icosahedron (N = 12, (m,n) = (1,0)). These are
       called icosadeltahedral structures since they have icosahedral symmetry minus a chirality (except for mn = 0 and mn = n<sup>2</sup> states).
       This algorithm constructs an (m,n) state in its icosadeltahedral configuration.</txt>

   <br><br>

   <li><txt class="h2">Cap Construction</txt><br>
       <txt>
       Allows the contruction of a sphere from one or more predefined ``caps" of scars in our database.</txt>

   <br><br>

   <li><txt class="h2">Random Points</txt><br>
       <txt>
       Places the points at random on the sphere.</txt>

   <br><br>

   <li><txt class="h2">Spiral Dance</txt><br>
       <txt>
       An animation that shows the result of changing one of the coefficients in the spiral algorithm. This can be used to find a good
       initial condition for a system, but is generally just a neat animation.</txt>

    </ul>

    <br><br>
    <txt class="h1">Buttons and Options</txt>

    <ul>

    <li><txt class="h2">Energy</txt><br>
    <txt>
    Determines the energy of the current system. E = sum(1/|x<sub>i</sub>-x<sub>j</sub>|<sup>p</sup>) for all point pairs i not equal to j. After the energy is calculated,
    the database is queried and, if the current energy is lower than the recorded energy, the point set and the current energy
    are stored.</txt>

    <br><br>

    <li><txt class="h2">Lowest Energy</txt><br>
    <txt>
    Prints the lowest energy we have stored in the database for this system.</txt>

    <br><br>

    <li><txt class="h2">Load Lowest</txt><br>
    <txt>
    Loads the point set in the database for the lowest energy system we have stored.</txt>

    <br><br>

    <li><txt class="h2">Add In Midpoints</txt><br>
    <txt>
    When displaying in 3D with the mesh, this will add a point to the surface corresponding to the projection
    of the midpoint of each edge.</txt>

    <br><br>

    <li><txt class="h2">Add In Faces</txt><br>
    <txt>
    When displaying in 3D with the mesh, this will add a point to the surface of the shell corresponding to the projection
    of the center of each face.</txt>

    </ul>

    <br><br>

    <txt class="h1">System Data</txt>

    <br><br>

    <ul>

    <li><txt class="h2">File -> Point Set</txt><br>
    <txt>
    Displays the point set defining a system. This can be copied, edited, and reloaded to change the system. Many point formats are supported and data from other programs/systems can be loaded with ease.</txt>

    <br><br>

    <li><txt class="h2">File -> Adjacency Array</txt><br>
    <txt>
    Displays the Adjacency Array defined by the Delaunay triangulation. This can be copied, edited, and reloaded (providing it is valid) to change the system. Many formats are supported and data from other programs/systems can be loaded with ease.</txt>

    </ul>

    <br><br>

    <txt class="h1"> Check Boxes and Visualization</txt>

    <ul>
    <li><txt class="h2">3D</txt><br>
    <txt>Displays the point set in 3D.</txt>

    <br><br>

    <li><txt class="h2">3D + Mesh</txt><br>
    <txt> Displays a delaunay triangulation of the point set in 3D. Colors denote coordination numbers of particles:
    Green: 4, Red: 5, Blue: 6 (Flat Space), Yellow: 7, Purple: 8, Black: Other.</txt>

    <br><br>

    <li><txt class="h2">3D + Chop</txt><br>
    <txt>Toggles Full 3D view and only Positive-Z 3D view.</txt>

    <br><br>

    <li><txt class="h2">3D + Index</txt><br>
    <txt>Displays the internal index of each point which can be used to more easily discuss systems.</txt>

    <br><br>

    <li><txt class="h2">3D + Pot E</txt><br>
    <txt>Finds the partial energy of all particles with respect to the prescribed potential and maps it onto a color scheme
    where Red: High Energy, Blue: Low Energy, When Mesh is checked as well, a Gouraud Shading scheme is used to map this over the
    entire sphere.</txt>

    <br><br>

    <li><txt class="h2">3D + Lat E</txt><br>
    <txt>Finds the partial strain energy of all particles with respect to the Delaunay triangulation and maps it onto a color scheme
    where Red: High Energy, Blue: Low Energy, This can be used as a measure of the triangulation's deviation from the 'flat' or
    'equilateral triangle' configuration. When Mesh is checked as well, a Gouraud Shading scheme is used to map this over the
    entire sphere.</txt>

    </ul>

    <br><br>

    <txt class="h1">Fun Things To Try</txt><br><br>
    <txt>Start with 12 particles. Either relax them or load the best configuration from the data base. Now press the button
    entitled "Add in Faces", which adds a particle to (approximately) the center of the face formed by three particles. From
    the 12-configuration, you should now have 32 particles (and this is also the ground state for N=32!). Press "Add in Faces"
    again. Now you should have 92 particle with the red 5-coordinated particles still maintaining their icosahedral symmetry from
    the 12-system. (This is not the ground state for N=92!) This can be continued, though going past 2,432 is not recommended.
    Constructions like this are possible from other relatively symmetrical systems such as 3, 4, 5, 6, 7, 9, 10, 12, ... , but
    icosahedral symmetries are only possible for N = 10*(m<sup>2</sup> +
    n<sup>2</sup> + mn) + 2 where m,n are positive integers.<br><br>

    Start with 100-500 particles. Use the Spiral Dance algorithm to line them up (Can be even more fun if they are twisted slightly).
    Pause this and switch to Relaxation. Run Relaxation on the system and watch the ensueing explosion!<br><br>

    Construct an (m,n) system with the "Construct (m,n)" algorithm. Some interesting effects can be made by adding/removing
    particles to the center of the icosahedral faces, removing rings of particles about the 5-coordinated points, etc. Look at
    the surface energy mapping by checking "E map" for an (m,n) state. Very beautiful and symmetric creations can be made,
    experiment!<br><br>

    Simply try to beat the lowest energies that have been recorded for any of the systems using any/all of the algorithms. I
    guarantee all the lowest energies up to 200. If some are missing, or you think you can do better, go for it!


  <br><br>

   <txt class="h1">References</txt><br><br>
   <txt>
   [0] <a href="http://www.stanford.edu/~ccecka/">Cris Cecka</a>, Primary Author. <a href="mailto:ccecka@seas.harvard.edu">ccecka@seas.harvard.edu</a><br>
   [1] Zhu, Jimmy, constructor of the Barnes-Hut Relaxation algorithm
   [2] Kevin Zielnicki, SQL databasing and personal communication<br>
   [3] Middleton, Alan, personal communication.<br>
   [4] Bowick, Mark, personal communication.<br>
   [5] W. Krauth, Markov Processes Relat. Fields 8, 215 (2002)<br>
   [6] M. Bowick, Science 299 (2003) 1716<br>
   [7] J. Liu, E. Luijten, Phys. Rev. Lett. 92, 035504 (2004).<br>
   [8] T. Erber, G.M. Hockney, J. Phys. A: Math. Gen. 24 (1991) L1369-L1377<br>
   [9] E.B. Saff, and A.B.J. Kuijlaars, Distributing many points on a sphere,
      <br>&nbsp; &nbsp; &nbsp; The Mathematical Intelligencer 19, No 1 (1997), 5-11.<br>
   [10] Kulsha, Andrey, personal communication.
    </div></td>
  </tr>

  <tr>
    <td><br>
    <div align="center">
    <img src="img/su-logo.gif">
    </div>
    </td>
  </tr>

  </table>

</div>

</table>

</body>
</html>
