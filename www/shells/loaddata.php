<?
include_once "../config/authenticate.php";
?>

<html>
<head><title>N-Body Data</title>
<link href="/thomson.css" rel="stylesheet" type="text/css" />
</head>
<body>
<body background="../media/background.gif">
<h1 align=center>Thomson Problem Data</h1>
<center><a href="javascript:window.close()">Close Window</a></center>
<hr>

<div align="center">
  <table height="100%" width="50%" cellpadding="0" cellspacing="0" border="0">
  <tr>
    <td><div align="center"><br>
      <table width="100%" border="4">
      <tr>
         <td>
         <applet code=ShellApplet.class name=ShellApplet archive=../ShellApplet.jar width=750 height=550>
<param name="userName" value="<?= $_SESSION['userName'] ?>">
<param name="userID" value="<?= $_SESSION['userID'] ?>">
<param name="LoadData" value="<?= $_REQUEST['id'].".".$_REQUEST['topology'] ?>">
         </applet>
         </td> 
      </tr>
      </table>
    </td> 
  </tr>
  
  <tr>
    <td><div align="center">
    <txt class="subscript"><br>
     Left Drag: Rotate The System<br>
     Right Drag: Zoom<br>
     Shift + Click: Add A Particle<br>
     Control + Click: Remove A Particle<br>
     Shift + Control + Drag: Drag A Particle<br>
     BETA: Single Click a Point to Highlight<br>
     Double Click to Select and Rotate a Scar
     <br><br>
    </txt><td>
  </tr>
  </table>
</div>

</body>
</html>
