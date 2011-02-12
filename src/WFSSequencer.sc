WFSSequencer {
	/** 
		WFSSequencer: this is a top-level sequence manager.
		it will do these things --
		- contain an arbitrary number of sequence channel objects which will
		  manage the sequences for each source channel
		- add events -- no filter. the interface class should determine when to record
		- manage playback for each of these SequenceChannel objects
		- interface with the presets management
	*/

	//var parent; // the top-level environment class
	var <sequences; // the array of sequences
	var clock;
	var <>action; // the sequencer callback function
	var playbackRoutine;
	var stopFlag=true;
	
	*new { |par|
		^super.new.init_wfssequencer(par);
	}

	init_wfssequencer { |par| 
		/**
			
			the schema of the array should be as follows:
			
			Array[ // sequences
			    [ // channel 1
			         [ // array of sequences
			              1 /* starting time of recording */,
			              Array[/*subsequent timestamps*/2, 3, 4, etc.]
			         ], ...etc...
			    ],
		      	... channel 2, etc
			]
		  */

		action = {};
		sequences = Array();
		clock = TempoClock(1); // is this the clock that I want to use?
		//parent = par; // this would be a place for error handling under other circumstances

		postln(this.class.asString ++ " initialized");
	}

	addChannel {
		// this is called when the WFSInterface:addChannel is called
		sequences = sequences.add(Array());
	}

	removeChannel { |chan|
		var chanToKill;
		// avoid an error if calling remove before adding any channels
		// ... there's that chort circuit trick again. am I overdoing this?

		if(sequences.size == 0){ ^nil; };

		chanToKill = chan ?? { sequences.lastIndex };
		
		sequences.removeAt(chanToKill);
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
		// currentSeq[0] has the start time of the recording, and
		// currentSeq[1] has subsequent timestamp value pairs
		
		clock.sched(0, {
			var wait;
			
			case{index == 0}{
				wait = sequence[index][0] - startTime;
			}
			{(index == sequence.lastIndex) || stopFlag}{
				wait = nil;
			}{
				wait = sequence[index][0] - sequence[index - 1][0];
			};

			stopFlag = false;
			
			// ** playback action **
			action.value(sequence[index][1]);
			// ** ** ** ** ** ** **

			index = index + 1;
			["inside sequencer", sequence[index][1], wait].postln;
			wait; // stupid implicit returns
		});

	}

	stop {
		stopFlag = true;
	}

	
}
