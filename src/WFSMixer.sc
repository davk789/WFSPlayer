WFSMixer {
	/**
		all distances measured in inches
	*/
	var s;
	var <gui, channels;
	var <numChannels, mixerGroupNum, <channelGroupNum;
	var roomWidth; 				// is this necessary?
	var <>speakerSpacing=18;	// 
	var roomDepth;	 	 		// for the maximum delay

	*new { |numChan|
		^super.new.init_wfsmixer(numChan);
	}

	init_wfsmixer { |numChan|
		s = Server.default;
		mixerGroupNum = s.nextNodeID;
		channelGroupNum = s.nextNodeID;
		numChannels = numChan ? 8;

		WFSSynthChannel.loadSynthDef;
		this.launchMixer;
	}

	launchMixer {
		// create the various subordinate parts
		gui = WFSGUI();
		this.fillChannels;
	}

	fillChannels {
		var speakerLocation;
		channels = Array.fill(numChannels, { |ind|
			speakerLocation = ind * speakerSpacing;
			WFSSynthChannel(this, speakerLocation);
		});
	}

	numChannels_ { |num|
		numChannels = num;

		if(channels.notNil){
			channels.do{ |channel,ind|
				channel.free;
			};
		};
		channels = nil;
		
		this.fillChannels;
	}
}