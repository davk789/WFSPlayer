WFSEngine {
	/**
		Container class for the synth channels. Inside this class, "channel" refers to 
		sound source, rather than speaker channel. Not to be confused with the top-level
		use of the term.

		This class should perform these duties:
		 - container for the synth channels
		 - pass the parameters for all channels to the presets class -- unless the presets class 
		   is to be maintained by the top-level class
		   (the presets class should be a single class utilized by interface and engine)
		 - manage the mixer SyntDef used by the WFSSynthChannels
	  */

	var parent; // reference to the enclosing class
	var numChannels=16; // number of output channels
	var channels;       // container array for the WFSSynthChannel classes

	*new { |par|
		^super.new.init_wfsengine(par);
	}

	init_wfsengine { |par|
		// init member data
		parent = par;
		
		// startup functions
		this.loadSynthChannels(numChannels);
		
		postln(this.class.asString ++ " initialized");
	}

	loadActiveChannel { |chan|
		postln("WFSEngine:loadActiveChannel is not implemented. This is its argument:");
		postln(chan);
	}

	loadSynthChannels {
		channels = numChannels.collect{ |ind|
			WFSSynthChannel(ind);
		}
	}

	setParam { |chan,param,val|
		// all values have been converted from the container class
		channels[chan].params[param] = val;
	}
	
	*loadSynthDef {
		// load the SynthDef here, check the date of the SynthDef and build if 
		// the sources are newer.
		SynthDef.new("WFSPlayer", { |inBus=20, outBus=1, delayTime=0.01, lev=1, maxDelay=1, gate=0|
			var aSig, aIn, aEnv;
			
			aEnv = EnvGen.ar(Env.asr(0.5, 1, 0.5, 'exponential'), gate, lev, doneAction:2);

			aIn = AudioIn.ar(inBus) * lev;
			aSig = DelayC.ar(aIn, delayTime);

			Out.ar(outBus, aSig);
			
		}).load(Server.default);
	}
}

WFSSynthChannel {
	/**
		Controller class for the Synth that will pass audio from sound source to output.
		
		This class should perform these duties:
		 - contain the parameters from the running synth
		 - convert the synth parameter units to values usable by the interface.
	  */
	var channelIndex;
	
	var <>params; // standard parameter dictionary
	
	*new { |ind|
		^super.new.init_wfsengine(ind);
	}

	init_wfsengine { |ind|
		// member data
		channelIndex = ind ? 0;
		// since there are so few dynamic parameters, i need to decide if a dict
		// is necessary -- this I should decide when it's time to support the presets
		params = Dictionary[             // internal storage of synth parameters
			'inBus'     -> 20,           // !! this needs to be set on startup
			'outBus'    -> channelIndex, // each channel goes to the outputs in order. simple.
			'delayTime' -> 0.001,        // set from the converted x @ y point from interface
			'lev'       -> 1,            // same for this -- delay time and level should be accounted for
			'maxDelay'  -> 1,            // set from initialization -- converted from "roomSize"
			// gate param can safely be ignored -- it is only called to prevent clicks when killing a synth
		];

		// startup functions
		this.startSynth;
		
		postln(this.class.asString ++ " initialized");
	}

	startSynth {
		// start the synth, free the node if it exists already
	}
}