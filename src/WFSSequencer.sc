WFSSequencer : WFSObject {
	/** 
		WFSSequencer: this is a top-level sequence manager.
		it will do these things --
		- contain an arbitrary number of sequence channel objects which will
		  manage the sequences for each source channel
		- add events -- no filter. the interface class should determine when to record
		- manage playback for each of these SequenceChannel objects
		- interface with the presets management

		** time to support multiple simultaneous channels
	*/

	var <sequences; // the array of sequences
	var stopFlags;
	var clock;
	var <>action, <>stopAction; // the sequencer callback functions
	var playbackRoutine;
	
	*new {
		^super.new.init_wfssequencer;
	}

	init_wfssequencer {
		/**
			
			the schema of the array should be as follows:
			
			Array[ // sequences
			    [ // channel 1
			         [ // array of sequences
			              1 /* starting time of recording */,
			              Array[/*subsequent timestamp/value pairs*/],
			         ], ...etc...
			    ],
		      	... channel 2, etc
			]
		  */

		action = {};
		/*
			still not sure what the best storage for the params and the stop flags would
			be, but this works well enough for now.
		*/
		sequences = Array();
		stopFlags = Array(); 
		clock = TempoClock(1); // is this the clock that I want to use?

		postln(this.class.asString ++ " initialized");
	}

	addChannel {
		// this is called when the WFSInterface:addChannel is called
		sequences = sequences.add(Array());
		stopFlags = stopFlags.add(false);
	}

	removeChannel { |chan|
		var chanToKill;
		// avoid an error if calling remove before adding any channels
		// ... there's that chort circuit trick again. am I overdoing this?

		if(sequences.size == 0){ ^nil; };

		chanToKill = chan ?? { sequences.lastIndex };
		
		sequences.removeAt(chanToKill);
		stopFlags.removeAt(chanToKill)
	}

	// record functions

	prepareRecording { |channel|
		/*
			add a new sequence to initiate a new recording
		*/
		sequences[channel] = sequences[channel].add([clock.beats, Array()]);
	}

	addEvent { |channel, data|
		sequences[channel].last[1] = sequences[channel].last[1].add([clock.beats, data]);
	}

	// playback functions

	playSequence { |chan, seq|
		var sequence, startTime;
		var index=0;

		startTime = sequences[chan][seq][0];
		sequence = sequences[chan][seq][1];
		
		clock.sched(0, {
			var wait;
			
			case{index == 0}{
				wait = sequence[index][0] - startTime;
			}
			{(index == sequence.lastIndex) || stopFlags[chan]}{
				wait = nil;
			}{
				wait = sequence[index][0] - sequence[index - 1][0];
			};

			stopFlags[chan] = false;
			
			// ** playback action **
			action.value(sequence[index][1], chan);

			if(wait.isNil){
				stopAction.value(chan); // execute cleanup code
			};
			
			index = index + 1;
			wait; // stupid implicit returns
		});

	}

	stop { |channel|
		stopFlags[channel] = true;
	}
}
