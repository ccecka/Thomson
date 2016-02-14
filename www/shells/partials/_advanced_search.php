<?
include_once "../config/mysqlconnect.php";
// get all the owner/id pairs
$owners = mysql_query("SELECT id, name FROM users WHERE id > 0 ORDER BY name");


// get all aspect ratios
$aspectRatios = mysql_query("SELECT DISTINCT aspect_ratio FROM torus");

//default values for parameters
if (!isset($_REQUEST["start"]))
    $_REQUEST["start"] = 0;
if (!isset($_REQUEST["end"]))
    $_REQUEST["end"] = 5000;
if (!isset($_REQUEST["potential"]))
    $_REQUEST["potential"] = 1
?>
<table bgcolor="#eeeeee">
<form action = "/shells/shelltable.php" method = "GET")>
<tr>
<td align="right">Topology:</td>
<td><select name="topology">
<?
foreach ($TOPOLOGIES as $topo => $name) {
    echo '<option value="'.$topo.'">'.$name.'</option>';
}
?>
<option value="cap">Cap</option>
</select></td>
</tr>
<tr>
<td align="right" nowrap>Aspect Ratio:</td>
<td>
<select name="aspect_ratio">
<option value="">**Torus Only**
<?
// drop-down selection for aspect ratios
while($ar = mysql_fetch_assoc($aspectRatios)) {
	echo '<option value="'.$ar['aspect_ratio'].'">'.$ar['aspect_ratio'];
}
?>
</select></td>
</tr>
<tr>
<td align="right">Potential:</td>
<td><select name="potential">
<option value="">
<?
foreach ($POTENTIALS as $p) {
    $selected = "";
    if ($p == $_REQUEST["potential"])
		$selected = ' selected="selected"';
    echo '<option value="'.$p.'"'.$selected.'">'.$p;
}
?>
</select></td>
</tr>
<tr>
<td align="right">N =</td>
<td>
<input type="text" name="start" value="<?= $_REQUEST["start"] ?>" size=3>
to
<input type="text" name="end" value="<?= $_REQUEST["end"] ?>" size=3></td>
</td>
</tr>
<tr>
<td align="right">Owner:</td>
<td>
<select name="owner_id">
<option value="">Any Owner
<?
// drop-down selection for changing owner
// first, show the automatically generated owners
echo '<option value="allminima">All Minima';
echo '<option value="'.$ID['globalMin'].'">Global Minima';
echo '<option value="'.$ID['ico'].'">Icosadeltahedral Minima';
echo '<option value="'.$ID['cap'].'">Cap';
echo '<option vlaue="'.$ID['guest'].'">Guest';
echo '<option value="">---------------';

// next, show the registered users
while($owner = mysql_fetch_assoc($owners)) {
	$selected = "";
	// ---uncomment to make the current user's name the default selection---
	//if($owner['id'] == $_SESSION['userID'])
	//	$selected = ' selected="selected"';
	echo '<option value="'.$owner['id'].'"'.$selected.'>'.$owner['name'];
}
?>
</select></td>
</tr>
<tr>
<td align="right">Comment:</td>
<td><input type="text" name="comment"></td>
</tr>
<tr>
<td colspan=2><B>Coordination Number:</B></td>
</tr>
<tr>
<td colspan=2 align="center">
<table cellspacing=8>
<tr>
<td>4:
<select name="4fold_op">
<?
foreach ($OPERATORS as $opname => $op)
	echo "<option value=\"$opname\">$op";
?>
</select>
<input type="text" name="4fold" size=3>
</td>
<td>5:
<select name="5fold_op">
<?
foreach ($OPERATORS as $opname => $op)
	echo "<option value=\"$opname\">$op";
?>
</select>
<input type="text" name="5fold" size=3>
</td>
</tr>
<tr>
<td>7:
<select name="7fold_op">
<?
foreach ($OPERATORS as $opname => $op)
	echo "<option value=\"$opname\">$op";
?>
</select>
<input type="text" name="7fold" size=3>
</td>
<td>8:
<select name="8fold_op">
<?
foreach ($OPERATORS as $opname => $op)
	echo "<option value=\"$opname\">$op";
?>
</select>
<input type="text" name="8fold" size=3>
</td>
</tr>
</table>
</td>
</tr>
<tr>
<td align="center" colspan=2><input type="submit" name="submit" value="Search"></td>
</tr>
</form>
</table>

