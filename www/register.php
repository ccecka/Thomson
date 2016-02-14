<?
// if registration data is submitted, store it in the database
if ($_REQUEST['userName'] && $_REQUEST['password']) {
	include_once "./config/mysqlconnect.php";
	$error = "";
	
	// first check that the user name isn't already taken
	$checkname = mysql_fetch_row(mysql_query("SELECT id FROM users WHERE name='".$_REQUEST['userName']."'"));
	if ($checkname)
		$error.="The user name you have selected has already been taken<BR>";
	
	// next, make sure the passwords match
	if ($_REQUEST['password'] != $_REQUEST['password2'])
		$error.="The passwords you entered do not match<BR>";

	// if there are no errors, add the user to the database and forward them
	// to the applet
	if ($error == "") {
		mysql_query("INSERT INTO users SET name='".$_REQUEST['userName']."', password='".md5($_REQUEST['password'])."'") or die(mysql_error());
		session_start();
		$_SESSION['userName'] = $_REQUEST['userName'];
		$_SESSION['userID'] = mysql_insert_id();
		header("Location: ./thomsonapplet.php");
		die();
	}
}

// if there was an error or the form hasn't been submitted, gather data
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
	echo '<B><FONT COLOR="#FF0000"><CENTER>'.$error.'</CENTER></FONT></B>';
?>
<table cellpadding=3 align="center" bgcolor="#EEEEEE">
<form action="register.php" method="post">
<TR align="center">
	<TD colspan=2>Please choose a user name and a password</TD>
</TR>
<TR>
	<TD>User Name:</TD>
	<TD><input type="text" name="userName" maxlength=50 value="<?= $_REQUEST['userName'] ?>"></TD>
</TR>
<TR>
	<TD>Password:</TD>
	<TD><input type="password" name="password"></TD>
</TR>
<TR>
	<TD>Retype Password:</TD>
	<TD><input type="password" name="password2"></TD>
</TR>
<TR>
	<TD colspan=2 align="center"><input type="submit" value="Register"></TD>
</TR>
</form>
</table>
</body>
</html>
