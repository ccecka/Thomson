<?
include_once "../config/authenticate.php";

// Shell listing main page
// allows basic and advanced searching
include_once "../config/config.php";
?>
<html>
<head>
<title>Cap Builder</title>
</head>
<body background="../media/background.gif">
<h1 align="center">Cap Builder</h1>
<form action="./generate.php" method="GET">
<table align="center" cellspacing=10>
<tr align="center" bgcolor="#eeeeee">
	<td colspan=2><B><big>Choose how many of each cap to use to create a sphere</big></B><br>(note: a valid sphere requires a total charge of 12)</td>
</tr>
<tr bgcolor="#dddddd">
<td valign="top">
<table align="center" bgcolor="#dddddd" cellspacing=6 cellpadding=5>
<tr bgcolor="#eeeeee">
	<td><B>Cap</B></td>
	<td><B>#</B></td>
</tr>
<tr>
	<td align="center" bgcolor="#ffffff"><img src="./images/1.gif"></td>
	<td><input type="text" name="cap[1]" size=2 maxlength=2 value=0></td>
</tr>
<tr>
	<td align="center" bgcolor="#ffffff"><img src="./images/2.gif"></td>
	<td><input type="text" name="cap[2]" size=2 maxlength=2 value=0></td>
</tr>
<tr>
	<td align="center" bgcolor="#ffffff"><img src="./images/3.gif"></td>
	<td><input type="text" name="cap[3]" size=2 maxlength=2 value=0></td>
</tr>
<tr>
	<td align="center" bgcolor="#ffffff"><img src="./images/15.gif"></td>
	<td><input type="text" name="cap[15]" size=2 maxlength=2 value=0></td>
</tr>
<tr>
	<td align="center" bgcolor="#ffffff"><img src="./images/4.gif"></td>
	<td><input type="text" name="cap[4]" size=2 maxlength=2 value=0></td>
</tr>
<tr>
	<td align="center" bgcolor="#ffffff"><img src="./images/5.gif"></td>
	<td><input type="text" name="cap[5]" size=2 maxlength=2 value=0></td>
</tr>
</table>
</td>
<td valign="top">
<table align="center" bgcolor="#dddddd" cellspacing=6 cellpadding=5>
<tr bgcolor="#eeeeee">
	<td><B>Cap</B></td>
	<td><B>#</B></td>
</tr>
<tr>
	<td align="center" bgcolor="#ffffff"><img src="./images/7.gif"></td>
	<td><input type="text" name="cap[7]" size=2 maxlength=2 value=0></td>
</tr>
<tr>
	<td align="center" bgcolor="#ffffff"><img src="./images/8.gif"></td>
	<td><input type="text" name="cap[8]" size=2 maxlength=2 value=0></td>
</tr>
<tr>
	<td align="center" bgcolor="#ffffff"><img src="./images/12.gif"></td>
	<td><input type="text" name="cap[12]" size=2 maxlength=2 value=0></td>
</tr>
<tr>
	<td align="center" bgcolor="#ffffff"><img src="./images/13.gif"></td>
	<td><input type="text" name="cap[13]" size=2 maxlength=2 value=0></td>
</tr>
<tr>
	<td align="center" bgcolor="#ffffff"><img src="./images/11.gif"></td>
	<td><input type="text" name="cap[11]" size=2 maxlength=2 value=0></td>
</tr>
<tr>
	<td align="center" bgcolor="#ffffff"><img src="./images/14.gif"></td>
	<td><input type="text" name="cap[14]" size=2 maxlength=2 value=0></td>
</tr>
</table>
</td>
</tr>
<tr>
	<td colspan=2 align="center">
	Minimum Side Length: <input type="text" name="minLength" size=2 maxlength=2 value=1>
	</td>
</tr>
<tr>
	<td colspan=2 align="center">
	Join By: <select name="type">
		<option value="edge">Edge
		<option value="corner">Corner
		<option value="random">Random
		<option value="genetic">Genetic Algorithm
	</select>
	</td>
</tr>
<tr>
	<td colspan=2 align="center">
	<input type="submit" name="builder" value="Create Sphere"><br><br>
	</td>
</tr>
</table>
</form>
<center>
<a href="capxml.php">Create a sphere from XML</a><br>
<a href="../thomsonapplet.php">Back to Thomson applet</a>
</center>
</body>
</html>
