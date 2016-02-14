<?
include_once "../config/authenticate.php";

// shell listing table view
// show shells based on information passed from index.php

include_once "../config/config.php";
include_once "../config/mysqlconnect.php";

// parse the query information
// both basic and advanced query return a mysql query
include_once "./methods/search_query.php";
if ($_REQUEST['submit'] == "Search")
    $query = advanced_query($_REQUEST);
elseif (isset($_REQUEST['topology']) && isset($_REQUEST['potential']))
    $query = basic_query($_REQUEST);
else
    header("Location: ./index.php");

// get information about the current user from the database so we can check
// write permissions later
$user = mysql_fetch_assoc(mysql_query("SELECT * FROM users WHERE id=".$_SESSION['userID']));
?>
<html>
<head>
<title>Data for N-body problem</title>
</head>
<body background="../media/background.gif">
<h1 align="center"><a href="http://thomson.phy.syr.edu/thomsonapplet.php">Thomson Problem Data</a></h1>
<center>
<? include "./partials/_quick_search.php"; ?>
<a href="./index.php">Advanced Search</a>
</center>
<BR><BR>
<table align="center" bgcolor="#ffffff" cellpadding=3 cellspacing=3>
<tr>
<? include './partials/_shell_table_head.php'; ?>
</tr>
<?
$bgcolor="#dddddd";
while($shell = mysql_fetch_assoc($query)) {
    // get the name of the shell's owner
    $result = mysql_fetch_assoc(mysql_query("SELECT name FROM users WHERE id=".$shell['owner_id']));
    $shell['owner'] = $result['name'];

    if ($bgcolor=="#dddddd")
	$bgcolor="#eeeeee";
    else
	$bgcolor="#dddddd";
?>
<tr bgcolor="<?= $bgcolor ?>">
<? include './partials/_shell_row.php'; ?>
</tr>
<? } ?>
</table>
</body>
</html>
