<?
// if a login attempt was made, try to authenticate the user
if ($_REQUEST['userName'] && $_REQUEST['password']) {
	include_once "./config/mysqlconnect.php";
	$userinfo = mysql_fetch_assoc(mysql_query("SELECT id, name, password FROM users WHERE name='".$_REQUEST['userName']."'"));
	// if the password is correct, forward them to the applet
	if ($userinfo['password'] == md5($_REQUEST['password'])) {
		session_start();
		$_SESSION['userName'] = $userinfo['name'];
		$_SESSION['userID'] = $userinfo['id'];
		header("Location: ./thomsonapplet.php");
		die();
	// otherwise, set an error flag
	} else {
		$error ='<B><FONT COLOR="#FF000000"><CENTER>Sorry, the username/password combination you entered does not exist in our database</CENTER></FONT></B>';
	}
}
?>
<html>
<head>
<title>Thomson Applet</title>
</head>
<body background="./media/background.gif">
<BR>
<?
// if there is an error message from incorrect login, display it here
if ($error)
	echo $error;

include "./partials/_login_form.php"; 
?>
</body>
</html>
