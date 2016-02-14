<table cellspacing=5>
<tr>
	<td>
	Abbreviated Format<br>
	<form action="generate.php" method="POST">
	<input type="text" name="capList" size=110 value="<?=$capList?>">
	</td>
</tr>
<tr>
	<td><input type="submit" name="xml" value="Generate Sphere"></form></td>
</tr>
<tr>
	<td align="center"><B>-or-</B></td>
</tr>
<tr>
	<td>
	XML Format - <a href="xmldoc.html" target="_blank">what is this?</a><br>
	<form action="generate.php" method="POST">
	<textarea name="capXML" rows="30" cols="80" wrap=off onkeydown="return catchTab(this,event)"><?=$capXML?></textarea>
	</td>
</tr>
<tr>
	<td><input type="submit" name="xml" value="Generate Sphere"></form></td>
</tr>
</table>
