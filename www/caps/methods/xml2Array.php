<?php
class xml2Array {
  
	var $arrOutput = array();
	var $resParser;
	var $strXmlData;

	function parse($strInputXML) {

		   $this->resParser = xml_parser_create ();
		   xml_set_object($this->resParser,$this);
		   xml_set_element_handler($this->resParser, "tagOpen", "tagClosed");
		  
		   xml_set_character_data_handler($this->resParser, "tagData");
	  
		   $this->strXmlData = xml_parse($this->resParser,$strInputXML );
		   if(!$this->strXmlData) {
			   die(sprintf("XML error: %s at line %d",
		   xml_error_string(xml_get_error_code($this->resParser)),
		   xml_get_current_line_number($this->resParser)));
		   }
						  
		   xml_parser_free($this->resParser);
		  
		   return $this->arrOutput;
	}
	function tagOpen($parser, $name, $attrs) {
	   $tag=array("name"=>$name,"attrs"=>$attrs);
	   array_push($this->arrOutput,$tag);
	}

	function tagData($parser, $tagData) {
	   if(trim($tagData) != "") {
		   if(isset($this->arrOutput[count($this->arrOutput)-1]['tagData'])) {
			   $this->arrOutput[count($this->arrOutput)-1]['tagData'] .= $tagData;
		   }
		   else {
			   $this->arrOutput[count($this->arrOutput)-1]['tagData'] = $tagData;
		   }
	   }
	}

	function tagClosed($parser, $name) {
	   $this->arrOutput[count($this->arrOutput)-2]['children'][] = $this->arrOutput[count($this->arrOutput)-1];
	   array_pop($this->arrOutput);
	}

	/*
	 * parse an xml array into a caplist string
	 */
	function arrToCapList($capArray) {
		$capArray = $capArray[0];

		// first we have to build an array of all the known labels
		// so we can translate them into the 'standard' integer notation
		$labels = array();
		foreach($capArray['children'] as $cap) {	
			if(isset($cap['attrs']['LABEL']))
				$labels[$cap['attrs']['LABEL']] = sizeof($labels);
			else
				$labels[sizeof($labels)] = $labels[sizeof($labels)];
		}

		// now we are ready to build the capList
		if(isset($capArray['attrs']['MINLENGTH']))
			$capList = $capArray['attrs']['MINLENGTH'].':';
		else
			$capList = '1:';

		foreach($capArray['children'] as $cap) {
			$capList .= $cap['attrs']['TYPE'];
			$link = $cap['children'][0];

			$type = "r";
			$destination = "r";
			$my_idx = "r";
			$dest_idx = "r";
			foreach($link['children'] as $link_element) {
				if($link_element['name'] == "TYPE") {
					if($link_element['tagData'] == "edge")
						$type = 'e';
					elseif($link_element['tagData'] == "corner")
						$type = 'c';
					elseif($link_element['tagData'] == "genetic")
						$type = 'g';
					else
						$type = 'r';
				}
				
				elseif($link_element['name'] == "DESTINATION") {
					if(isset($labels[$link_element['tagData']]))
						$destination = $labels[$link_element['tagData']];
					else
						$destination = 'r';
				}

				elseif($link_element['name'] == "MY_IDX") {
					if(is_numeric($link_element['tagData']))
						$my_idx = $link_element['tagData'];
					else
						$my_idx = 'r';
				}

				elseif($link_element['name'] == "DEST_IDX") {
					if(is_numeric($link_element['tagData']))
						$dest_idx = $link_element['tagData'];
					else
						$dest_idx = 'r';
				}	
			}

			$capList .= '-'.$type.'-'.$destination.'-'.$my_idx.'-'.$dest_idx.':';
		}
		return $capList;
	}

	function capListToXML($capList)
	{
		$capArray = explode(':', $capList);
		unset($capArray[sizeof($capArray)-1]);
		
		$capXML = "<sphere minlength='".$capArray[0]."'>\n";
		unset($capArray[0]);

		$count = 0;
		foreach($capArray as $cap) {
			$elements = explode('-', $cap);
			$type = $elements[0];
			
			if($elements[1] == 'e')
				$join = "edge";
			elseif($elements[1] == 'c')
				$join = "corner";
			elseif($elements[1] == 'g')
				$join = 'genetic';
			else
				$join = "random";
			
			if(is_numeric($elements[2]))
				$destination = $elements[2];
			else
				$destination = "random";
			
			if(is_numeric($elements[3]))
				$my_idx = $elements[3];
			else
				$my_idx = "random";
			
			if(is_numeric($elements[4]))
				$dest_idx = $elements[4];
			else
				$dest_idx = "random";
			
			$capXML .= "<cap type='$type' label='$count'>\n";
			$capXML .= "\t<link>\n";
			$capXML .= "\t\t<type>$join</type>\n";
			$capXML .= "\t\t<destination>$destination</destination>\n";
			$capXML .= "\t\t<my_idx>$my_idx</my_idx>\n";
			$capXML .= "\t\t<dest_idx>$dest_idx</dest_idx>\n";
			$capXML .= "\t</link>\n";
			$capXML .= "</cap>\n";
			++$count;
		}

		$capXML .= "</sphere>";
		
		return $capXML;
	}
}
?>
