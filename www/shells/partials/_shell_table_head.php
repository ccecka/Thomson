<td align="center"><B>System N</B></td>
<?
// display an aspect ratio for tori
if ($_REQUEST['topology'] == "torus") {
?>
<td align="center" nowrap><B>Aspect Ratio</B></td>
<?
}
// display numsides and sidelength for caps
if ($_REQUEST['topology'] == "cap") {
?>
<td align="center"><B>Sides</B></td>
<td align="center" nowrap><B>Side Length</B></td>
<?
}
// display an energy, timestamp for spheres and tori
if ($_REQUEST['topology'] == "torus" || $_REQUEST['topology'] == "sphere") {
?>
<td align="center"><B>Energy</B></td>
<?
}
// display energy difference for spheres with potential = 1
if ($_REQUEST['topology'] == "sphere" && $_REQUEST['potential'] == 1) {
?>
<td align="center" nowrap><B>Estimator &Delta;<BR><font size="1">N<sup>2</sup>/2 - 0.55295N<sup>3/2</sup> + 0.016N</font></B></td>
<? } ?>
<td align="center"><B>Owner</B></td>
<td align="center"><B>Comment</B></td>
<?
// display a timestamp for spheres and tori
if ($_REQUEST['topology'] == "torus" || $_REQUEST['topology'] == "sphere") {
?>
<td align="center"><B>Date (EST)</B></td>
<? } ?>
