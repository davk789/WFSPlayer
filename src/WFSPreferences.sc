WFSPreferences : WFSObject {
	/**
		Store and retrieve preferences files. 

		there is a helpful explanation of DOMDocument at:
		http://swiki.hfbk-hamburg.de:8888/MusicTechnology/747
	  */
	// this class will probably be called from the top level and get data
	// from the interface and the sequencer, so it will need the parent, I think
	var presetRoot;
	*new {
		^super.new.init_wfspreferences;
	}

	init_wfspreferences {
		// maybe move this to a different location later
		presetRoot = Platform.userAppSupportDir ++  "/Extensions/WFSPlayer/prefs/";
		

	}
	
	readPrefFile { |filename|
		var doc;
		doc = DOMDocument(presetRoot ++ filename ++ ".xml");

		doc.getDocumentElement.getElementsByTagName("param").do{ |tag, index|
			// do something here
			postln([tag.getAttribute("id"), tag.getText]);
		};
		
	}

	save { |filename|
		/**
			Format DOM data and write to an xml file. This might be good to refactor later.
		*/
		var doc;
		var root;
		var channelRoot;
		var globalRoot;
		var sequenceRoot;
		var outFile, outFileName;

		doc = DOMDocument();

		root = doc.createElement("wfsplayer-data");
		doc.appendChild(root);

		// channel params
		
		channelRoot = doc.createElement("channelParams");
		root.appendChild(channelRoot);

		interface.channelWidgetValues.do{ |params, ind|
			var chanTag;

			chanTag = doc.createElement("channel");
			//chanTag.setAttribute("number", ind.asString);

			channelRoot.appendChild(chanTag);

			params.keysValuesDo{ |key, val|
				var textVal, paramTag;
				paramTag = doc.createElement("param");
				paramTag.setAttribute("id", key.asString);
				textVal = doc.createTextNode(val.asString);

				paramTag.appendChild(textVal);
				chanTag.appendChild(paramTag);
			};
			
		};

		// global params

		globalRoot = doc.createElement("globalParams");
		root.appendChild(globalRoot);

		interface.globalWidgetValues.keysValuesDo{ |param, val|
			var paramTag, textVal;

			paramTag = doc.createElement("param");
			paramTag.setAttribute("id", param.asString);
			textVal = doc.createTextNode(val.asString);

			paramTag.appendChild(textVal);
			globalRoot.appendChild(paramTag);
		};
		
		// sequencer data
		sequenceRoot = doc.createElement("sequences");
		root.appendChild(sequenceRoot);
		
		sequencer.sequences.do{ |channel, ind|
			var chanTag;
			
			chanTag = doc.createElement("channel");
			chanTag.setAttribute("number", ind.asString);

			sequenceRoot.appendChild(chanTag);

			channel.do{ |seq,i|
				var seqTag, startTag, dataTag, startVal, dataVal;
				
				seqTag = doc.createElement("sequence");
				seqTag.setAttribute("number", i.asString);
				chanTag.appendChild(seqTag);

				startTag = doc.createElement("startTime");
				startVal = doc.createTextNode(seq[0].asString);
				startTag.appendChild(startVal);
				seqTag.appendChild(startTag);
				
				dataTag = doc.createElement("data");
				dataVal = doc.createTextNode(seq[1].asInfString);
				dataTag.appendChild(dataVal);
				seqTag.appendChild(dataTag);

			};
		};
		
		// write the file
		if(filename.isNil || (filename == "")){
			outFileName = Date.localtime.stamp;
		}{
			outFileName = filename;
		};
		outFile = File(presetRoot ++ outFileName ++ ".xml", "w+");
		doc.write(outFile);
		outFile.close;
	}
	
	getPresetList {
		var paths;
		
		paths = pathMatch(presetRoot ++ "*.xml");

		^paths.collect{ |obj, ind|
			obj.split($/).last; // might want to strip off the file extension later
		};
	}

	loadPreset { |filename|
		var doc;
		var paramData, currentParam;
		var outParams = Array();
		var globalParamData;
		var outGlobalParams = Dictionary();
		var sequenceData;
		var outSequences = Array();
		
		doc = DOMDocument(presetRoot ++ filename);

		// get channelWidgetValues;
		
		paramData = doc.getDocumentElement.getElement("channelParams").getFirstChild;

		while{ paramData.notNil }{
			var outDict = Dictionary();
			
			paramData.getChildNodes.do{ |node|
				var val = node.getText.asString;

				// nasty workaround for a nasty bug -- single digit matches fail
				// with my build of SC, so, append a 0 to all tests
				// *** watch for bugs with different builds/platforms ***
				if("^-?[0-9]*\\.?[0-9]+\$".matchRegexp(val ++ "0")){
					val = val.asFloat;
				};

				outDict = outDict.add(node.getAttribute("id").asSymbol -> val);
			};

			outParams = outParams.add(outDict);

			paramData = paramData.getNextSibling;
		};

		// get global param data

		globalParamData = doc.getDocumentElement.getElement("globalParams").getFirstChild;

		while{ globalParamData.notNil }{
			var key = globalParamData.getAttribute("id").asSymbol;
			var val = globalParamData.getText.asInt;

			outGlobalParams = outGlobalParams.add(key -> val);

			globalParamData = globalParamData.getNextSibling;
		};

		// get sequence data

		sequenceData = doc.getDocumentElement.getElement("sequences").getFirstChild;

		while{sequenceData.notNil}{
			var sequenceArr = Array();
			
			sequenceData.getChildNodes.do{ |sequenceNode|
				var dataArr = Array();

				sequenceNode.getChildNodes.do{ |dataNode|
					var data = dataNode.getText.asString.interpret;
					dataArr = dataArr.add(data);
				};

				sequenceArr = sequenceArr.add(dataArr);
			};

			outSequences = outSequences.add(sequenceArr);
			
			sequenceData = sequenceData.getNextSibling;
		};
		
		^[outParams, outGlobalParams, outSequences];
		
	}
}