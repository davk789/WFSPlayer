WFSSynth : Model {
	var s;
	var params;
	*new {
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

	loadSynthDef {
		SynthDef.new("WFSPlayer", { |inBus=20, outBus=1, delayTime=0.01, lev=1, i_maxDelay=1|
			var aSig, aIn;
			

			aIn = AudioIn.ar(inBus) * aLev;
			aSig = DelayC.ar(aIn, delayTime);

			Out.ar(outBus, aSig);
			
		}).load(s);
	}
}