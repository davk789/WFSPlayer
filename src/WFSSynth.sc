WFSSynthChannel : Model {
	classvar channelNumber=0;
	var s;
	var params, nodeNum, groupNum;

	*new { |group|
		channelNumber = channelNumber + 1;
		^super.new.init_wfssynth(group);
	}
	
	init_wfssynth { |group|
		// init vars
		s = Server.default;
		nodeNum = s.nextNodeID;
		// assuming the enclosing class will maage the group number for now
		groupNum = group;
		params = Dictionary[
			'inBus'      -> 20,
			'outBus'     -> 1,
			'delayTime'  -> 0.01,
			'lev'        -> 1,
			'i_maxDelay' -> 1,
		];

		// init functions
		this.start;
		postln(this.class.asString ++ " initialized");
	}
	
	setParam { |par, val|
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

	start {
		s.listSendMsg(['s_new', 'WFSPlayer', nodeNum, 0, groupNum] ++ params.asKeyValuePairs);
		s.sendMsg('n_set', nodeNum, 'gate', 1);
	}

	free {
		s.sendMsg('n_set', nodeNum, 'gate', 0);
	}

	*loadSynthDef {
		SynthDef.new("WFSPlayer", { |inBus=20, outBus=1, delayTime=0.01, lev=1, i_maxDelay=1, gate=0|
			var aSig, aIn, aEnv;
			
			aEnv = EnvGen.ar(Env.asr(0.5, 1, 0.5, 'exponential'), gate, lev, doneAction:2);

			aIn = AudioIn.ar(inBus) * lev;
			aSig = DelayC.ar(aIn, delayTime);

			Out.ar(outBus, aSig);
			
		}).load(Server.default);
	}
}