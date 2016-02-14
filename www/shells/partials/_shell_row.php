<?
include_once "./partials/_javascript_popup.php";
?>

<td align="center" nowrap>
<a href="javascript:popup('./loaddata.php?topology=<?= $_REQUEST['topology'] ?>&id=<?= $shell['id'] ?>', '<?= $shell['id'] ?>');"><?= $shell['numpoints'] ?></a>
</td>
<?
// display an aspect ratio for tori
if ($_REQUEST['topology'] == "torus") {
?>
<td><?= $shell['aspect_ratio'] ?></td>
<?
}
// display numsides and sidelength for caps
if ($_REQUEST['topology'] == "cap") {
?>
<td align="center"><?= $shell['numsides'] ?></td>
<td align="center"><?= $shell['sidelength'] ?></td>
<?
}
// display an energy for spheres and tori
if ($_REQUEST['topology'] == "torus" || $_REQUEST['topology'] == "sphere") {
?>
<td><a href="./shellpoints.php?topology=<?= $_REQUEST['topology'] ?>&id=<?= $shell['id'] ?>"><?= number_format($shell['energy'], ceil(-log10(abs($shell['energy']))) + 12, '.', '') ?></a></td>
<?
}
// display energy difference for spheres with potential = 1
if ($_REQUEST['topology'] == "sphere" && $_REQUEST['potential'] == 1) {
$difference = round($shell['energy'] - (0.5*pow($shell['numpoints'], 2) - 0.55295*pow($shell['numpoints'], 3/2) + 0.016 * $shell['numpoints']),8);
?>
<td align="center"><?= number_format($difference, ceil(-log10(abs($difference))) + 7, '.', '') ?></td>
<? } ?>
<td align="center" nowrap><?= $shell['owner'] ?></td>
<?
// Make the comment editable if the user has write permissions
include_once "./methods/check_permissions.php";
if (check_permissions($user, $shell['owner_id'], $ID)) {
?>
<td align="center"><a href="./edit.php?topology=<?= $_REQUEST['topology'] ?>&id=<?= $shell['id'] ?>"><?= str_replace("\n",'<BR>',$shell['comment']) ?></a></td>
<? } else { ?>
<td align="center"><?= str_replace("\n",'<BR>',$shell['comment']) ?></td>
<? } ?>
<td><?= $shell['timestamp'] ?></td>
