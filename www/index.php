<?
// "log in" the user as guest
include_once "./config/config.php";

session_start();
if(!isset($_SESSION['userID'])) {
	$_SESSION['userID'] = $ID['guest'];
	$_SESSION['userName'] = 'guest';
}
if(isset($_SESSION['desired'])) {
	$desired = $_SESSION['desired'];
	unset($_SESSION['desired']);
} else
	$desired = "./thomsonapplet.htm";

header("Location: $desired");
?>
