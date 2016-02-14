<? //default values for parameters
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
    $selected = "";
    if ($topo == $_REQUEST["topology"])
	$selected = ' selected="selected"';
    echo '<option value="'.$topo.'"'.$selected.'>'.$name.'</option>';
}
?>
</select></td>
<td rowspan=4><input type="submit" value="Go"></td>
</tr>
<tr>
<td align="right">Potential:</td>
<td><select name="potential">
<?
foreach ($POTENTIALS as $p) {
    $selected = "";
    if ($p == $_REQUEST["potential"])
	$selected = ' selected="selected"';
    echo '<option value="'.$p.'"'.$selected.'">'.$p.'</option>';
}
?>
</select></td>
</tr>
<tr>
<td align="right">Minima:</td>
<td>
<select name="owner_id">
<option value="globminima">Global Minima
<option value="allminima">All Minima
</select></td>
</tr>
<tr>
<td colspan=2>N =
<input type="text" name="start" value="<?= $_REQUEST["start"] ?>" size=3>
to
<input type="text" name="end" value="<?= $_REQUEST["end"] ?>" size=3></td>
</td>
</tr>
</form>
</table>

