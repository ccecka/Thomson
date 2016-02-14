<?php
// renders an owner selection box
// parameters:
//     $name     the name of the select input box
//     
// if $currentOwner is set, that owner will be selected
function owner_select($name, $owner_query, $currentOwner, $ID) {
	$owners = mysql_query("SELECT id, name FROM users WHERE id > 0 ORDER BY name");
	$output = "<select name=\"$name\">\n";
	while ($owner = mysql_fetch_assoc($owners)) {
		$selected = "";
		if ($owner['id'] == $currentOwner)
			$selected = ' selected="selected"';
		$output .= '<output value="'.$owner['id'].'"'.$selected.'>'.$owner['name'];
	}

	return $output;
}

