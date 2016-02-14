<?
include_once "../config/mysqlconnect.php";

// return a "basic" query which only searches on potential and number of points
function basic_query($params) {
    global $TABLES, $ID;

    $columns = "id, numpoints, potential, energy, owner_id, comment, timestamp";

    if ($params['owner_id'] == "allminima")
	$owner = "(owner_id = ".$ID['localMin']." OR owner_id = ".$ID['globalMin']." OR owner_id = ".$ID['ico'].")";
    if ($params['owner_id'] == "globminima")
	$owner = "owner_id = ".$ID['globalMin'];

    if ($params["topology"] == "sphere")
		$query = mysql_query( "SELECT ".$columns." FROM ".$TABLES["sphere"]." WHERE ".$owner." AND potential = ".$params["potential"]." AND numpoints >= ".$params["start"]." AND numpoints <= ".$params["end"]." ORDER BY numpoints, energy") or die(mysql_error());
    elseif ($params["topology"] == "torus")
		$query = mysql_query("SELECT ".$columns." FROM ".$TABLES["torus"]." WHERE ".$owner." AND potential = ".$params["potential"]." AND numpoints >= ".$params["start"]." AND numpoints <= ".$params["end"]." ORDER BY numpoints, aspect_ratio, energy") or die(mysql_error());
    else
		header("Location: ./index.php");

    return $query;
}



// return an advanced query which also searches on owner, comment,
// and coordination number
function advanced_query($params) {
    global $TABLES, $OPERATORS, $ID;
	$shelltbl = $TABLES[$params['topology']];

    $from = $TABLES[$params['topology']];
    $where = "numpoints >= ".$params["start"]." AND numpoints <= ".$params["end"];
	$order = "";

	if ($params['potential'] != '' && $params['topology'] != "cap")
		$where .= " AND potential = ".$params["potential"];

	// because aspect_ratio is a double, we can't safely compare for equality
	// so instead we check if the aspect ratio is in some small range
    if ($params['topology'] == "torus") {
		if ($params['aspect_ratio'] != '') {
			$where .= " AND aspect_ratio > ".($params['aspect_ratio']-0.000001);
			$where .= " AND aspect_ratio < ".($params['aspect_ratio']+0.000001);
		}
		$order .= "numpoints, aspect_ratio, energy";
	} elseif ($params['topology'] == "sphere") {
		$order .= "numpoints, energy";
	} elseif ($params['topology'] == "cap") {
		$order .= "numpoints";
	} else
		header("Location: ./index.php");

    	if ($params['owner_id'] == "allminima")
		$owner = "(owner_id = ".$ID['localMin']." OR owner_id = ".$ID['globalMin']." OR owner_id = ".$ID['ico'].")";
	elseif ($params['owner_id'] != '')
		$where .= " AND owner_id = '".$params['owner_id']."'";

	if ($params['comment'] != '')
		$where .= " AND comment LIKE '%".$params['comment']."%'";


	foreach (array('4','5','7','8') as $cn) {
		if ($params[$cn.'fold'] != '') {
    $where .= " AND coor".$cn.$OPERATORS[$params[$cn.'fold_op']].$params[$cn.'fold'];
    }
  }

    $query = mysql_query("SELECT DISTINCT ".$shelltbl.".* FROM ".$from." WHERE ".$where." ORDER BY ".$order) or die(mysql_error());

    return $query;
}
?>

