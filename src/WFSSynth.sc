WFSEngine : WFSObject {
	/**
		Container class for the synths and params. Inside this class, "channel" refers to 
		sound source, rather than speaker channel. Not to be confused with the top-level
		use of the term.
	  */

	// going to avoid using a container class for each of the output channels, for now
	var params, defaultParams, s;
	var groupNode, synthNodes;
	var >numChannels; // number of speakers, not num in put channels
	var inBusCounter=20; // ... static variable?
	
	*new {
		this.loadSynthDef;
		^super.new.init_wfsengine;
	}
	
	init_wfsengine {
		synthNodes = Array();
		/*  param schema:
			params = Array[  // all channels
			     Array[      // the source channel
			         Dict[]  // to the output speaker
			    ]
			]
		*/
		params = Array();
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

		this.initSynths;
	}

	initSynths {
		// create the group
		groupNode = s.nextNodeID;
		// for reference: node ID, add action(1=add to tail), target node (the default node) 
		s.sendMsg('g_new', groupNode, 1, 1);
	}

	getInBus { |chan|
		// all 'inBus' values for any channel are equal, not checking to make sure of this
		^params[chan][0]['inBus'];
	}
	
	addChannel {
		/*
			prepare the data storage and create synths on the server.
		*/
		var newNodes = Array();
		var newParams = Array();
		
		numChannels.do{ |ind|
			newNodes = newNodes.add(s.nextNodeID);
			newParams = newParams.add(defaultParams.copy);
			newParams.last['outBus'] = ind;
			newParams.last['inBus'] = inBusCounter;

			s.listSendMsg(
				['s_new', 'WFSMixerChannel', newNodes.last, 0, groupNode] ++ newParams.last;
			);
			s.sendMsg('n_set', newNodes.last, 'gate', 1);
		};

		inBusCounter = inBusCounter + 1;
		
		// store the nodes here for later setting methods
		synthNodes = synthNodes.add(newNodes);
		params = params.add(newParams);

		s.queryAllNodes;
	}

	*loadSynthDef {
		/* One synth per input channel, per output channel. This should be limited to one
		synth per input channel, but leave as is for now. */
		SynthDef.new("WFSMixerChannel",
			{ |inBus=20, outBus=0, delayTime=0.01, maxDelay=1, lev=1, gain=1, gate=0|
				var aSig, aIn, aEnv;
				
				aEnv = EnvGen.ar(Env.asr(0.5, 1, 0.5, 'exponential'), gate, doneAction:2);
				// gain = per-channel attenuation, lev = per-input level
				aIn = In.ar(inBus) * lev * gain;
				aSig = DelayC.ar(aIn, delayTime) * aEnv;

				Out.ar(outBus, aSig);
				
			}
		).load(Server.default);
	}
	
}