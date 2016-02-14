<?
// check_permissions returns true if user has privledges to modify owner's files
function check_permissions($user, $ownerID, $ID) {
	// if the users are identical, or the owner is guest, allow editing
	if ($user['id'] == $ownerID || $ownerID == $ID['guest'])
		return true;

	// otherwise, if the user is an admin, allow editing
	if ($user['admin'])
		return true;

	return false;
}
?>
