<?
include_once "config.php";

// make sure the user is logged in, otherwise redirect to the login page
session_start();

if(!session_is_registered('userID')) {
	$_SESSION['desired'] = $_SERVER['REQUEST_URI'];
	header("Location: $ROOT/index.php");
}
?>
