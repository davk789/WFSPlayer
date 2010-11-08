WFSSynthChannel : Model {
	classvar channelNumber=0;
	var s;
	var params;
	*new {
		channelNumber = channelNumber + 1;
		^super.new.init_wfssynth;
	}
	
	init_wfssynth {
		params = Dictionary[
			'inBus'      -> 20,
			'outBus'     -> 1,
			'delayTime'  -> 0.01,
			'lev'        -> 1,
			'i_maxDelay' -> 1,
		];
		postln(this.class.asString ++ " initialized");
	}
	
	setParam { |par, val|
		// error handling
		if(params.includesKey(val)){
			params[par] = val;
		}{
			postln("Key not found. Ignoring the input.");
		};
	}

	calculateDelay {
		var channel;
		channel = this.class.channelNumber;
		
	}

	loadSynthDef {
		SynthDef.new("WFSPlayer", { |inBus=20, outBus=1, delayTime=0.01, lev=1, i_maxDelay=1|
			var aSig, aIn;
			
			aIn = AudioIn.ar(inBus) * lev;
			aSig = DelayC.ar(aIn, delayTime);

			Out.ar(outBus, aSig);
			
		}).load(s);
	}
}