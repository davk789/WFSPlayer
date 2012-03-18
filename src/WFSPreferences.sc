WFSPreferences : WFSObject {
	/**
		Manage static data (i.e. saved settings/window dimensions).
		this class handles the presets saved from the interface. It also saves the window
		dimensions and any other data that needs to be retrieved at startup.

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
		if(File.exists(presetRoot).not){
			unixCmd("mkdir -p \"" ++ presetRoot ++ "\"");
		};		
	}
	
	// window state

	storeWindowData { |cvBounds|
		/*
			Right now this only saves window size. This can also be used to 
			save the state of other data as well. For instance, a file browser
			would be a nice way to manage presets.

			WFSPreferences:storeWindowData gets references to the objects it needs to examine to
			save their state.
		*/
		var file, doc, root, dimTag, dimVal;
		
		file = File(presetRoot ++ ".wfsdata", "w+");

		doc = DOMDocument();

		root = doc.createElement("wfsplayer-data");
		doc.appendChild(root);

		// window size
		dimTag = doc.createElement("winsize");
		dimVal = doc.createTextNode(
		    "Rect(" ++ cvBounds.left ++ ", " ++cvBounds.top ++ ", " ++cvBounds.width ++ ", " ++ cvBounds.height ++ ")"
		);

		dimTag.appendChild(dimVal);
		root.appendChild(dimTag);

		// other objects go here
		// ...

		// cleanup
		
		doc.write(file);
		file.close;
		postln("successfully finished writing the window location");
	}

	retrieveWindowData {
		var filename, doc, winSize;
		filename = presetRoot ++ ".wfsdata";

		if(File.exists(filename).not){
			^nil;
		};

		doc = DOMDocument(filename);
		winSize = doc.getElementsByTagName("winsize")[0].getText.interpret;
		postln(winSize);
		^[winSize];  // return a list for other data that may be retrieved in the future

	}

	// presets

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
			outFileName = this.checkFileName(Date.localtime.stamp.asString);
		}{
			outFileName = this.checkFileName(filename);
		};
		
		outFile = File(presetRoot ++ outFileName ++ ".xml", "w+");
		doc.write(outFile);
		outFile.close;
	}

	checkFileName { |name|
		if(File.exists(presetRoot ++ name ++ ".xml")){
			^name ++ "_" ++ Date.localtime.stamp.asString;
		}{
			^name;
		};
	}
	
	getPresetList {
		var paths, sep;
		
		sep = Platform.pathSeparator;
		paths = pathMatch(presetRoot ++ "*.xml");

		^paths.collect{ |obj, ind|
			obj.split(sep).last; // might want to strip off the file extension later
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
			// why am I calling .add for a dictionary here?? dumb
			outParams = outParams.add(outDict);

			paramData = paramData.getNextSibling;
		};

		// get global param data

		globalParamData = doc.getDocumentElement.getElement("globalParams").getFirstChild;

		while{ globalParamData.notNil }{
			var key = globalParamData.getAttribute("id").asSymbol;
			var val = globalParamData.getText.asFloat;

			outGlobalParams = outGlobalParams.add(key -> val);

			globalParamData = globalParamData.getNextSibling;
		};

		// backwards compatibility fix 

		outGlobalParams = this.checkRoomDimensions(outGlobalParams);

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

	checkRoomDimensions { |dict|
		// BACKWARDS COMPATIBILITY FIX
		// make adjustments to the received data
		if(dict.includes('arrayWidthBox').not){
			postln("Old save file detected!!");
			postf("Updating arrayWidthBox and RoomWidthBox from value: %\n", dict['roomWidthBox'];);
			dict['arrayWidthBox'] = dict['roomWidthBox'];
			dict['roomWidthBox'] = dict['roomWidthBox'] + 2;
		};
		// the data should possibly be re-saved, but problably not in this function
		^dict;
	}
}
