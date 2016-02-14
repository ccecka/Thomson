<?
include_once "../config/authenticate.php";

// write the given data to mysql and redirect to the main page
include_once "../config/config.php";
include_once "../config/mysqlconnect.php";

$shell = $_POST['shell'];

mysql_query("UPDATE ".$TABLES[$shell['topology']]." SET owner_id='".$shell['owner_id']."', comment='".$shell['comment']."' WHERE id=".$shell['id']) or die(mysql_error());

header("Location: ./index.php");
?>
