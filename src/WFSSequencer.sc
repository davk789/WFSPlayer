WFSSequencer {
	/**
		sequence manager for WFSMixer.
		This class has the following responsibilities:
		- store the sequence data gathered from the interface
		- play back the sequences

		... I think the class should directly interact with the top-level class
		rather than with the interface, but this might need to change.
	  */
	var parent;
	var sequences;
	var recordingStartTime, isRecording;
	var clock;
	*new { |par|
		^super.new.init_wfsequencer(par);
	}

	init_wfsequencer { |par|
		// the sequencer will interact with each sound source channel by name, I think.
		parent = par;
		sequences = Dictionary();
		isRecording = Dictionary(); // contains id -> boolean flags for recording
		clock = TempoClock(1);
		
		postln(this.class.asString ++ " initialized");
	}

	startChannelRecording { |id|
		/*
			get the start time of the recording and prepare the sequences dictionary.
		*/

		if(sequences.includesKey(id).not){
			sequences = sequences.add(id -> Array.with(Array()));
		}{
			sequences[id] = sequences[id].add(Array());
		};

		isRecording[id] = true;
		
		recordingStartTime = clock.beats;
	}

	stopChannelRecording { |id|
		isRecording[id] = false;
		// here is my output for now
		sequences.postln;
	}

	addEvent { |id, data|
		var seq;
		/**
			for now, only allow for recording new sequences
			-- add append/overdub functionality later
		*/

		if(isRecording.includesKey(id).not){
			^nil; // the key hasn't been initialized, break out of function
		};

		if(isRecording[id]){
			seq = sequences[id].last;
			seq = seq.add([clock.beats - recordingStartTime, data]);
			// this may be too heavy to be called on a mouseMove action
			sequences[id][sequences[id].lastIndex] = seq;
		}
	}

	
}