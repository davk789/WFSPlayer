WFSEngine : WFSObject {
	/**
		Container class for the synths and params. Inside this class, "channel" refers to 
		sound source, rather than speaker channel. Not to be confused with the top-level
		use of the term.
	  */

	// store as internal units: meters and absolute amplitude
	var maxDelay=0.5; // calculation of longest possible delay
	var channelVolumes; // should this be kept in a better spot?
	var roomDepth=10, roomWidth=10, masterVolume=1, airTemperature=23; // these should be static variables
	var synthParams, defaultParams, s;
	var mixerNode, inputChannelNodes, synthNodes;
	var <numChannels=16; // number of speakers, not num in put channels
	var inBusCounter=20; // ... static variable?
	//	var speedOfSound=340.29; // m/s
	
	*new {
		this.loadSynthDef;
		^super.new.init_wfsengine;
	}
	
	init_wfsengine {
		synthNodes = Array();
		inputChannelNodes = Array();
		channelVolumes = Array();
		/*  param schema:
			params = Array[  // all channels
			     Array[      // the source channel
			         Dict[]  // to the output speaker
			    ]
			]
		*/
		synthParams = Array();
		s = Server.default;
		defaultParams = Dictionary[
			//|inBus=20, outBus=0, delayTime=0.01, i_maxDelay=1, lev=1, channelVol=1, masterVol=1, gate=0|
			// the channelVol and masterVol can be ignored, maxDelay is static
			'delayTime' -> 0.01,
			'lev'       -> 1,
			'outBus'    -> 0,
			'inBus'     -> 20,
		];
	}
	
	initDeferred {
		// create the group
		mixerNode = s.nextNodeID;
		// node ID, add action(1=add to tail), target node (the default node) 
		s.sendMsg('g_new', mixerNode, 1, 1);
	}

	getInBus { |chan|
		// all 'inBus' values for any channel are equal, not checking to make sure of this
		^synthParams[chan][0]['inBus'];
	}

	getAllInputs {
		// collect all input buses
		^synthParams.collect{ |obj| obj[0]['inBus']; };
	}
	
	addChannel { |index| // use this param when re-setting the num speakers to force
		                 // a channel number
		/*
			prepare the data storage and create synths on the server.
		*/
		var locationValue;
		var newChannelNode = s.nextNodeID;
		var newNodes = Array();
		var newParams = Array();
		var channelIndex = index ? interface.channelWidgetValues.lastIndex;

		// initialize all synth params in this function
		
		if(interface.globalWidgets['numSpeakersBox'].value != numChannels){
			numChannels = interface.globalWidgets['numSpeakersBox'].value;
		};

		s.sendMsg('g_new', newChannelNode, 0, mixerNode);

		// make the synths
		numChannels.do{ |ind|
			newNodes = newNodes.add(s.nextNodeID);
			newParams = newParams.add(defaultParams.copy);
			newParams.last['outBus'] = ind;
			newParams.last['inBus'] = inBusCounter;

			s.listSendMsg(
				['s_new', 'WFSMixerChannel', newNodes.last, 0, newChannelNode]
				++
				newParams.last.getPairs
			);
			s.sendMsg('n_set', newNodes.last, 'gate', 1);
		};
		
		// initialize the master volume attenuation
		masterVolume = interface.globalWidgets['masterVolumeBox'].value.dbamp;
		s.sendMsg('n_set', mixerNode, 'masterVol', masterVolume);

		// initialize the channel attenuation
		// ** this will work because this.addChannel comes after interface.addChannel
		channelVolumes = channelVolumes.add(interface.channelWidgets['channelVolumeBox'].value.dbamp);
		s.sendMsg('n_set', newChannelNode, 'channelVol', channelVolumes.last);

		// increment the bus number for the next input channel		
		inBusCounter = inBusCounter + 1;
		
		// store the nodes here for later setting methods
		inputChannelNodes = inputChannelNodes.add(newChannelNode);
		synthNodes = synthNodes.add(newNodes);
		synthParams = synthParams.add(newParams);

		// get room width and depth
		// accessing the gui in case the value is set on the widget but the action is not tiggered
		// i.e. "enter" was not pressed after the value was typed in to the box
		roomWidth = interface.globalWidgets['roomWidthBox'].value;
		roomDepth = interface.globalWidgets['roomDepthBox'].value;

		// get the max delay value for inverted delay calc
		this.calculateMaxDelay;

		// set delay values for the speakers
		this.updateLocation(
			synthParams.lastIndex, // this should be drawn from the provided argument
			// but I won't mess with if nothing is broken right now
			interface.globalWidgets['locationMarkerArea'].value[channelIndex]
		);

	}

	calculateMaxDelay {
		var maxDistance;
		maxDistance = this.calculateDistance(roomWidth, roomDepth);
		maxDelay = this.distanceToTime(maxDistance);
	}
	
	removeChannel { |chan=0|
		s.sendMsg('n_set', inputChannelNodes[chan], 'gate', 0);
		s.sendMsg('n_free', inputChannelNodes[chan]);

		inputChannelNodes.removeAt(chan);
		synthNodes.removeAt(chan);
		synthParams.removeAt(chan);

		if(inputChannelNodes.size == 0){
			inBusCounter = 20; // go back to the default value if removing the last channel
		}
	}

	loadPreset {
		/*
			remove the channels that are there, and add new ones.
		*/
		// remove the old channels
		while{synthNodes.size > 0}{
			this.removeChannel;
		};

		inBusCounter = 20; // reset the in bus counter
		
		// add the channels
		interface.channelWidgetValues.size.do{ |ind|
			this.addChannel(ind);
		};
	}

	updateAllLocations {
		var locations;
		locations = interface.globalWidgets['locationMarkerArea'].value;
		locations.do{ |loc, ind|
			this.updateLocation(ind, loc);
		};	
	}
	
	updateLocation { |chan, loc|
		/*
			take a 0..1 point val and apply it to the selected chan
		*/
		var xLoc = loc.x * roomWidth;
		var yLoc = loc.y * roomDepth;
		var roomAdjust = numChannels / (numChannels - 1);

		numChannels.do{ |ind|
			var xSpeaker, distance, delay, level, delayInvert;

			xSpeaker = ((ind * roomAdjust) / numChannels) * roomWidth;
			distance = this.calculateDistance(xLoc - xSpeaker, yLoc);

			delayInvert = interface.channelWidgetValues[chan]['channelInvertDelayButton'].toBool;

			if(delayInvert){
				delay = maxDelay - this.distanceToTime(distance);
			}{
				delay = this.distanceToTime(distance);
			};
			
			synthParams[chan][ind]['delayTime'] = delay;

			/*
				for volume -- attenuate a fixed dB/m amount, fiddle with the params -- 2dB seems
				to be the value I found online so far
			*/
			level = (distance * 0.5/*2*/).abs.neg.dbamp;
			synthParams[chan][ind]['lev'] = level;
			
			s.sendMsg('n_set', synthNodes[chan][ind], 'delayTime', delay, 'lev', level);
		};
	}

	calculateDistance { |x,y|
		^sqrt(x.pow(2) + y.pow(2));
	}
	
	feetToMeters { |feet|
		^(feet * 0.3048); // return meters
	}

	distanceToTime { |distance| // distance in meters
		// using a constant speed of sound: 340.29 m/s
		^(distance / this.speedOfSound);
	}

	fahrenheitToCelsius { |fahr|
		^(fahr - 32) / 1.8;
	}

	speedOfSound {
		// not sure the figures here are accurate, but seems
		// close enough for now
		^331.4 + (airTemperature * 0.6);
	}

	setChannelVolume { |chan, vol|
		var amp = vol.dbamp;
		
		synthParams[chan].do{ |obj|
			obj['channelVol'] = amp;
		};
		
		s.sendMsg('n_set', inputChannelNodes[chan], 'channelVol', amp);
	}
	
	// instance setter methods

	airTemperature_ { |fTemp|
		airTemperature = this.fahrenheitToCelsius(fTemp);
		this.updateAllLocations;
	}
	
	roomWidth_ { |fWidth|
		roomWidth = this.feetToMeters(fWidth);
		this.updateAllLocations;
	}

	roomDepth_ { |fDepth|
		roomDepth = this.feetToMeters(fDepth);
		this.updateAllLocations;
	}

	masterVolume_ { |val|
		masterVolume = val.dbamp;
		s.sendMsg('n_set', mixerNode, 'masterVol', masterVolume);
	}

	numChannels_ { |num|
		var numInputs;
		numChannels = num;
		numInputs = inputChannelNodes.size;

		if(numInputs == 0){
			// skip everything if there are no channels initialized
			^nil;
		};
		
		// free the existing synth nodes
		s.sendMsg('n_set', mixerNode, 'gate', 0);
		s.sendMsg('n_free', mixerNode);
		// clear the parameter and node data
		inputChannelNodes = Array();
		synthNodes = Array();
		synthParams = Array();
		inBusCounter = 20; // back to default value
		
		this.initDeferred;
		numInputs.do{ |ind|
			this.addChannel(ind);
		};
	}
	
	// class methods

	*loadSynthDef {
		/* One synth per input channel, per output channel. This should be limited to one
		synth per input channel, but leave as is for now. */
		SynthDef.new(
			"WFSMixerChannel",
			{ |inBus=20, outBus=0, delayTime=0.01, i_maxDelay=1, lev=1, channelVol=1, masterVol=1, gate=0|
				var aSig, aIn, aEnv;
				
				aEnv = EnvGen.ar(Env.asr(0.5, 1, 0.5, 'exponential'), gate, doneAction:2);
				// gain = per-channel attenuation, lev = per-input level
				aIn = In.ar(inBus);
				aSig = DelayN.ar(
					aIn,
					i_maxDelay,
					delayTime,
					lev * channelVol * masterVol
				);

				Out.ar(outBus, aSig * aEnv);
				
			}
		).load(Server.default);
	}
	
}