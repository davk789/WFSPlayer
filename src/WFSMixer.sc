WFSMixer {
	/**
		all distances measured in inches
	*/
	var s;
	var <gui, <>channels, paramManager;
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
		// the param manager is the controller
		paramManager = WFSParamManager();
		// load the model and the view
		gui = WFSGUI(paramManager);
		this.fillChannels;
		// now give the param manager the environment
		paramManager.loadEnvironment(this);
	}

	fillChannels {
		var speakerLocation;
		channels = Array.fill(numChannels, { |ind|
			speakerLocation = ind * speakerSpacing;
			WFSSynthChannel(paramManager, speakerLocation);
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