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
	// stopFlags trigger the the routine to end, the playFlags describe
	// what is playing. 
	var flags; // stop, record, play
	var clock;
	var <>moveAction, <>stopAction, <>startAction; // the sequencer callback functions
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

		startAction = {};
		moveAction = {};
		stopAction = {};

		sequences = Array();
		flags = Array();
		clock = TempoClock(1);

	}

	addChannel {
		sequences = sequences.add(Array());
		flags = flags.add(Dictionary['stop' -> false, 'record' -> false, 'play' -> false, ])
	}

	removeChannel { |chan|
		var chanToKill;

		if(sequences.size == 0){ ^nil; };

		chanToKill = chan ?? { sequences.lastIndex };

		sequences.removeAt(chanToKill);
		flags.removeAt(chanToKill);
	}

	// moveAction alias

	action {
		^moveAction;
	}

	action_ { |func|
		moveAction = func;
	}

	// record functions

	prepareRecording { |channel|
		/*
			add a new sequence to initiate a new recording
		*/
		flags[channel]['record'] = true;
		postf("sequencer recording flag is %\n", flags[channel]['record']);
		sequences[channel] = sequences[channel].add([clock.beats, Array()]);
	}

	addEvent { |channel, data|
		sequences[channel].last[1] = sequences[channel].last[1].add([clock.beats, data]);
	}

	// playback functions

	playSequence { |chan, seq|
		var sequence, startTime;
		var index=0;

		flags[chan]['play'] = true;
		startTime = sequences[chan][seq][0];
		sequence = sequences[chan][seq][1];

		startAction.value(parent, chan); // passing the top-level mixer
		                                 // hopefully that is okay

		clock.sched(0, {
			var wait;
			
			case{index == 0}{
				wait = sequence[index][0] - startTime;
			}
			{(index == sequence.lastIndex) || flags[chan]['stop']}{
				wait = nil;
			}{
				wait = sequence[index + 1][0] - sequence[index][0];
			};

			flags[chan]['stop'] = false;
			
			// ** playback action **
			interface.updateMove(chan, sequence[index][1]);
			moveAction.value(parent, chan, sequence[index][1]);

			if(wait.isNil){
				interface.updateStop(chan); // execute cleanup code
				stopAction.value(parent, chan);
			};
			
			index = index + 1;
			wait; // stupid implicit returns
		});

	}

	stop { |channel|
		flags[channel] = Dictionary['play' -> false, 'record' -> false, 'stop' -> true, ];
	}

	loadPreset { |sequence|
		// stop any playing sequences
		flags.size.do{ |ind|
			flags[ind]['stop'] = true;
		};

		sequences = sequence;
		flags = Array.fill(sequences.size, {
			Dictionary['play' -> false, 'record' -> false, 'stop' -> false, ];
		});

	}

	isPlaying { |chan|
		^flags[chan]['play']; // very shitty. playFlags should probably just be exposed
		// but I will leave it like this for now, until I need to access the
		// isPlaying flags at some other time
	}
	
	isRecording { |chan|
		^flags[chan]['record'];
	}
	
}
