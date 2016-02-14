<?
include_once "../config/authenticate.php";

// Shell listing main page
// allows basic and advanced searching
include_once "../config/config.php";
?>
<html>
<head>
<title>Data for N-body problem</title>
</head>
<body background="../media/background.gif">
<h1 align="center"><a href="http://thomson.phy.syr.edu/thomsonapplet.php">Thomson Problem Data</a></h1>
<table align="center" bgcolor="#dddddd" cellspacing=5 cellpadding=5>
<tr><td><B><big>Quick Search</big></B></td></tr>
<tr><td><? include "./partials/_quick_search.php"; ?></td></tr>
<tr><td><hr></td></tr>
<tr><td><B><big>Advanced Search</big></B></td></tr>
<tr><td><? include "./partials/_advanced_search.php"; ?></td></tr>
<tr><td>You are currently logged in to this<BR>applet as "<?= $_SESSION['userName'] ?>".<BR>Click to <a href="../login.php">log in</a> as a different user.</td></tr>
</body>
</html>
