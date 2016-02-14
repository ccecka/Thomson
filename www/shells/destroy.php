<?
include_once "../config/authenticate.php";

// delete the given shell from mysql and redirect to the main page
include_once "../config/config.php";
include_once "../config/mysqlconnect.php";

$shell = $_POST['shell'];

// delete the shell
mysql_query("DELETE FROM ".$TABLES[$shell['topology']]." WHERE id=".$shell['id']) or die(mysql_error());

// if the item is a cap, also delete the associated points and adjacency array
if( $shell['topology'] == "cap" ) {
	// delete the associated points	
	mysql_query("DELETE FROM ".$POINTS[$shell['topology']]." WHERE id=".$shell['id']) or die(mysql_error());
	// delete adj array	
	mysql_query("DELETE FROM cap_adjacency WHERE id=".$shell['id']) or die(mysql_error());
}

header("Location: ./index.php");
?>
