WFSEngine : WFSObject {
	/**
		Container class for the synths and params. Inside this class, "channel" refers to 
		sound source, rather than speaker channel. Not to be confused with the top-level
		use of the term.
	  */

	// store as internal units: meters and absolute amplitude
	var roomDepth=30, roomWidth=30, masterVolume=1; // these should be static variables
	var synthParams, defaultParams, s;
	var mixerNode, inputChannelNodes, synthNodes;
	var >numChannels=16; // number of speakers, not num in put channels
	var inBusCounter=20; // ... static variable?
	var speedOfSound=340.29; // m/s
	
	*new {
		this.loadSynthDef;
		^super.new.init_wfsengine;
	}
	
	init_wfsengine {
		synthNodes = Array();
		inputChannelNodes = Array();
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
			'delayTime' -> 0.01,
			'maxDelay'  -> 1,    // corresponds to "room size" in the interface
			'lev'       -> 1,    // per-synth calculated attenuation
			'gain'      -> 1,    // global gain attenuation
			'outBus'    -> 0,
			'inBus'     -> 20,
		];
	}
	
	initDeferred {
		numChannels = parent.numChannels;

		// build the default values from the gui
		
		// create the group
		mixerNode = s.nextNodeID;
		// node ID, add action(1=add to tail), target node (the default node) 
		s.sendMsg('g_new', mixerNode, 1, 1);
	}

	getInBus { |chan|
		// all 'inBus' values for any channel are equal, not checking to make sure of this
		^synthParams[chan][0]['inBus'];
	}
	
	addChannel {
		/*
			prepare the data storage and create synths on the server.
		*/
		var newChannelNode = s.nextNodeID;
		var newNodes = Array();
		var newParams = Array();

		// double check the number of channels from the interface
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
			// i don't know whiy this doesn't work -- the params are not being set
			// even though the synth is being created
			s.listSendMsg(
				['s_new', 'WFSMixerChannel', newNodes.last, 0, newChannelNode]
				++
				newParams.last.getPairs
			);
			s.sendMsg('n_set', newNodes.last, 'gate', 1);
		};

		inBusCounter = inBusCounter + 1;
		
		// store the nodes here for later setting methods
		inputChannelNodes = inputChannelNodes.add(newChannelNode);
		synthNodes = synthNodes.add(newNodes);
		synthParams = synthParams.add(newParams);

	}

	updateLocation { |chan, loc|
		/*
			take a 0..1 point val and apply it to the selected chan
		*/
		var xLoc = loc.x * roomWidth;
		var yLoc = loc.y * roomDepth;
		var roomAdjust = numChannels / (numChannels - 1);
		numChannels.postln;
		numChannels.do{ |ind|
			var xSpeaker, distance, delay, level;

			xSpeaker = ((ind * roomAdjust) / numChannels) * roomWidth;
			distance = this.calculateDistance(xLoc - xSpeaker, yLoc);

			delay = this.distanceToTime(distance);
			synthParams[chan][ind]['delayTime'] = delay;

			/*
				for volume -- assume 2dB per meter attenuation, double check the math
				later.
			*/
			level = (distance * 2).abs.neg.dbamp;
			synthParams[chan][ind]['lev'] = level;
			//"speaker: % level: % delay: %".format(ind, level, delay).postln;
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
		^(distance / speedOfSound);
	}

	roomDepth_ { |val|		
		// this value will be used to set the maximum delay,
		// ... if I can figure out how to use it
		roomDepth = this.feetToMeters(val);		
	}

	roomWidth_ { |val|
		roomWidth = this.feetToMeters(val);
		postln("room width is " ++ roomWidth);
	}

	masterVolume_ { |val|
		masterVolume = val.dbamp;
		postln("setting master volume to " ++ masterVolume);
		s.sendMsg('n_set', mixerNode, 'masterVol', masterVolume);
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