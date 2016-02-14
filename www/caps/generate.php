<?
include_once "../config/authenticate.php";
include_once "../config/config.php";
include_once "./methods/xml2Array.php";
$xmlParser = new xml2Array();

// if the source of the data is the cap builder
// we must parse the raw data to xml
if($_REQUEST['builder']) {
	$capArray = $_REQUEST['cap'];

	// randomly reorder the cap array
	$newCapArray = array();
	foreach($capArray as $cap => $n)
		for($i = 0; $i < $n; ++$i)
			array_push($newCapArray, $cap);
	shuffle($newCapArray);
	$capArray = $newCapArray;

	$capXML = "<sphere minlength='".$_REQUEST['minLength']."'>\n";

	foreach($capArray as $n => $cap) {
		$capXML .= "<cap type='$cap' label='$n'>\n";
		$capXML .= "\t<link>\n";
		$capXML .= "\t\t<type>".$_REQUEST['type']."</type>\n";
		$capXML .= "\t\t<destination>random</destination>\n";
		$capXML .= "\t\t<my_idx>random</my_idx>\n";
		$capXML .= "\t\t<dest_idx>random</dest_idx>\n";
		$capXML .= "\t</link>\n";
		$capXML .= "</cap>\n";
	}

	$capXML .= "</sphere>";
} elseif($_REQUEST['capXML']) {
	$capXML = stripslashes($_REQUEST['capXML']);
} elseif($_REQUEST['capList']) {
	$capXML = $xmlParser->capListToXML($_REQUEST['capList']);
} else {
	header("Location: index.php");
}


?>

<html>
<head>
<script language="JavaScript" src="./methods/collapse_expand_single_item.js"></script>
<script language="JavaScript" src="./methods/catch_tabs.js"></script>
<title>Cap Builder</title>
</head>
<body>
<body background="../media/background.gif">
<h1 align=center>Cap Builder</h1>
<hr>
<table align="center">
<tr><td>
<table border=4>
<tr><td>
<applet code=ShellApplet.class name=ShellApplet archive="<?=$ROOT?>/ShellApplet.jar" width=750 height=550>
<param name="userName" value="<?= $_SESSION['userName'] ?>">
<param name="userID" value="<?= $_SESSION['userID'] ?>">
<param name="caps" value="<?
// condense the cap XML into a short string for easy processing by the applet
// the string contains one number indicating the minimum length, followed by several caps
// each cap consists of 5 alphanumerics, delimited by '-' and seperated by ':'
//   the first number is the database id of the cap in question
//   the second letter indicates if the join is an edge or corner join (r = random)
//   the third number is the destination cap to which this cap should be joined (r = random)
//   the fourth number is the edge or corner to join with the destination cap (r = random)
//   the fifth number is the edge or corner to join to on the destination cap (r = random)
// example1:  5:1-r-r-r-r:1-c-r-r-r:1-e-r-r-r:2-r-r-r-r:2-r-r-r-r:2-r-r-r-r:
// example2:  4:1-r-r-r-r:1-e-0-0-0:1-e-0-0-1:1-e-0-0-2:2-e-0-0-3:
$capArray = $xmlParser->parse($capXML);
$capList = $xmlParser->arrToCapList($capArray);
echo $capList;
?>">
</applet>
</td></tr>
</table>
</td></tr>
<tr><td>
<br>
<a href="#xml" onClick="shoh('xml');"><img src="./images/u.gif" name="imgxml" border="0"></a>
<B><a onClick="shoh('xml');">XML data for this sphere</a></B>
<div style="display: none;" id="xml" >
	<? include "./partials/_xml_input.php"; ?>
</div
</td></tr>
</table>
</body>
</html>
