WFSSequencer {
	/** 
		WFSSequencer: this is a top-level sequence manager.
		it will do these things --
		- contain an arbitrary number of sequence channel objects which will
		  manage the sequences for each source channel
		- manage recording and playback for each of these SequenceChannel objects
		- act as interface to the channel objects w/r/t presets management etc.
	*/

	var parent; // the top-level environment class
	var sequences; // the array of sequences
	var clock;
	*new { |par|
		^super.new.init_wfssequencer(par);
	}

	init_wfssequencer { |par| 
		/**
			
			the schema of the array should be as follows:
			
			Array[ // sequences
			    Dictionary[ // channel 1
			         'id number 1' -> [ // id-based sequences
			              1 /* starting time of recording */,
			              Array[/*subsequent timestamps*/2, 3, 4, etc.]
			         ]
			    ],
		      	... channel 2, etc
			]
		  */

		sequences = Array();
		clock = TempoClock(1); // is this the clock that I want to use?
		parent = par; // this would be a place for error handling under other circumstances

		postln(this.class.asString ++ " initialized");
	}

	addChannel {
		// this is called when the WFSInterface:addChannel is called
		sequences = sequences.add(Dictionary());
	}

	removeChannel { |chan|
		var chanToKill;
		// this needs to be called when the interface calls removeChannel
		
		// avoid an error if calling remove before adding any channels
		// ... there's that chort circuit trick again. am I overdoing this?
		if(sequences.size == 0){ ^nil; };

		chanToKill = chan ?? { sequences.lastIndex };
		
		sequences.removeAt(chanToKill);
	}

	startChannelRecording {
		postln("starting the recording");
	}

	stopChannelRecording {
		postln("stopping the recording");
	}

	
	
}

// i think I got lost here, I will try to re-build again
/*WFSSequencer {
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
		sequences.do{ |obj,ind|
			postln("this is the entry to sequences:");
			postln(obj);
			postln("... and now I am iterating:");
			obj.do{ |o| o.postln; }
		};
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

	
	}*/