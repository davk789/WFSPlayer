WFSSynthChannel { // not inheriting from SC's built in MVC classes
	classvar channelNumber=0, <groupNumber;
//	classvar <>airTemperature=72, <>maxDelay=1;
	var <>airTemperature=72;
	var s;
	var <params; // temp read access, do not forget to remove later
	var nodeNum, groupNum;
	var paramManager; // the "controller"
	var speakerLocation, <x, <y=0;

	*new { |man, loc|
		groupNumber = Server.default.nextNodeID;
		channelNumber = channelNumber + 1;
		^super.new.init_wfssynth(man, loc);
	}
	
	init_wfssynth { |man, offset|
		// init vars
		paramManager = man; // translates between the gui and the delay values
		s = Server.default;
		nodeNum = s.nextNodeID;
		// there has to be a more elegant way to handle this
		groupNum = this.class.groupNumber; 
		speakerLocation = offset; // this is calculated by the containing class
		x = speakerLocation; // the default location is at the location of the speaker
		params = Dictionary[
			'inBus'      -> 20,
			'outBus'     -> 1,
			'delayTime'  -> 0.01,
			'lev'        -> 1,
			'maxDelay'   -> 1,
		];

		// init functions
		this.start;
//		postln(this.class.asString ++ " initialized");
	}

	setLevel { |val|
		params['lev'] = val;
		s.sendMsg('n_set', nodeNum, params['lev'], val);
	}

	maxDelay_ { |val|
		params['maxDelay'] = val;
	}

	maxDelay {
		^params['maxDelay'];
	}

	x_ { |val|
		x = val;
		this.updateDelay;
	}
	
	y_ { |val|
		y = val;
		this.updateDelay;
	}
	
	getCelsiusTemperature {
		^(this.class.airTemperature - 32) / 1.8;
	}
	
	getSoundSpeed {
		var mpsSpeed;
		mpsSpeed = 331.4 + (0.6 * this.getCelsiusTemperature); // meters per second
		^mpsSpeed * 39.3700787; // inches per second
	} // i need to check this math

	updateDelay {
		var distance, xDistance, delay;

		xDistance = abs(x - speakerLocation);
		distance = sqrt(xDistance.pow(2) + y.pow(2));
		delay = distance / this.getSoundSpeed;

		params['delayTime'] = delay;			
	}

	start {
		s.listSendMsg(['s_new', 'WFSPlayer', nodeNum, 0, groupNum] ++ params.asKeyValuePairs);
		s.sendMsg('n_set', nodeNum, 'gate', 1);
	}

	free {
		s.sendMsg('n_set', nodeNum, 'gate', 0);
	}

	// keeping the class functions down here

	*loadSynthDef {
		SynthDef.new("WFSPlayer", { |inBus=20, outBus=1, delayTime=0.01, lev=1, maxDelay=1, gate=0|
			var aSig, aIn, aEnv;
			
			aEnv = EnvGen.ar(Env.asr(0.5, 1, 0.5, 'exponential'), gate, lev, doneAction:2);

			aIn = AudioIn.ar(inBus) * lev;
			aSig = DelayC.ar(aIn, delayTime);

			Out.ar(outBus, aSig);
			
		}).load(Server.default);
	}

}