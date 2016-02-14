<?
include_once "../config/authenticate.php";

// shell listing table view
// show shells based on information passed from index.php

include_once "../config/config.php";
include_once "../config/mysqlconnect.php";
?>

<pre>
<?
$query = mysql_query("SELECT * FROM ".$_REQUEST['topology']." WHERE id = ".$_REQUEST['id']);
$shell = mysql_fetch_assoc($query);

// Separate by bytes
$xyz = str_split($shell['pointbytearray'], 8);
// Reverse the bytes and make into a single string again
$xyz = implode('', array_map('strrev', $xyz));
// Interpret as doubles
$array = unpack('d*', $xyz);

// Print data

// N
printf("%d\n", $shell['numpoints']);
// Potential
printf("%d", $shell['potential']);
// Aspect Ratio
if ($_REQUEST['topology'] == "torus")
  printf(", %f", $shell['aspect_ratio']);
// Energy
printf("\n%s\n", number_format($shell['energy'], ceil(-log10($shell['energy'])) + 12, '.', ''));

for($i = 1; $i <= count($array); $i += 3) {
  printf("%19.16f %19.16f %19.16f\n", $array[$i], $array[$i+1], $array[$i+2]);
}
?>
</pre>