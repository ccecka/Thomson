<?
include_once "../config/authenticate.php";

// grab the specified shell from the database for editing
include_once "../config/config.php";
include_once "../config/mysqlconnect.php";

$shell = mysql_fetch_assoc(mysql_query("SELECT * FROM ".$TABLES[$_REQUEST['topology']]." WHERE id = '".$_REQUEST['id']."'"));

// get information about the current user and check write permissions
$user = mysql_fetch_assoc(mysql_query("SELECT * FROM users WHERE id=".$_SESSION['userID']));
include_once "./methods/check_permissions.php";
if( sizeof($shell) <= 1 || !check_permissions($user, $shell['owner_id'], $ID)) {
	header("Location: ./index.php");
	die();
}

// get all the owner/id pairs
$owners = mysql_query("SELECT id, name FROM users ORDER BY name");
?>
<html>
<head>
<title>Edit <?= $_REQUEST['topology'] ?> information</title>
</head>
<body background="../media/background.gif">
<center>
<h1>Edit <?= $_REQUEST['topology'] ?> information</h1>
</center>
<form action="update.php" method="post">
<input type="hidden" name="shell[id]" value="<?= $shell['id'] ?>">
<input type="hidden" name="shell[topology]" value="<?= $_REQUEST['topology'] ?>">
<table align="center" bgcolor="#eeeeee" cellpadding=3 cellspacing=3>
<tr>
<td align="right">System:</td>
<td>N = <?= $shell['numpoints'] ?></td>
</tr>
<tr>
<td align="right">Energy:</td>
<td><?= $shell['energy'] ?>
</tr>
<?
// show aspect ratio for tori
if ($_REQUEST['topology'] == "torus") {
?>
<tr>
<td nowrap align="right">Aspect Ratio:</td>
<td><?= $shell['aspect_ratio'] ?>
</tr>
<? } ?>
<tr>
<td align="right">Owner:</td>
<td>
<select name="shell[owner_id]">
<option value="">&nbsp;
<?
// drop-down selection for changing owner
while($owner = mysql_fetch_assoc($owners)) {
	$selected = "";
	if($owner['id'] == $shell['owner_id'])
		$selected = ' selected="selected"';
	echo '<option value="'.$owner['id'].'"'.$selected.'>'.$owner['name'];
}
?>
</select></td>
</tr>
<tr>
<td align="right">Comment:</td>
<td><textarea name="shell[comment]" rows=5 cols=60><?= $shell['comment'] ?></textarea></td>
</tr>
<tr>
<td colspan=2 align="center">
<table cellspacing=5>
<tr>
<td><input type="submit" value="Save"></td>
</form>
<form action="destroy.php" method="post">
<input type="hidden" name="shell[id]" value="<?= $shell['id'] ?>">
<input type="hidden" name="shell[topology]" value="<?= $_REQUEST['topology'] ?>">
<td><input type="submit" value="Delete" onclick="if(confirm('Are you sure you want to delete this <?= $_REQUEST['topology'] ?>?')) post_page_request(); return false;"></td>
</form>
<td><input type="button" value="Cancel" onClick="history.go(-1)"></td>
</tr>
</table>
</td>
</tr>
</table>
</body>
</html>
