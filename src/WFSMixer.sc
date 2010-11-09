WFSMixer {
	var s;
	var gui, channels;
	var <numChannels, mixerGroupNum;
	*new { |numChan|
		^super.new.init_wfsmixer(numChan);
	}

	init_wfsmixer { |numChan|
		s = Server.default;
		mixerGroupNum = s.nextNodeID;
		numChannels = numChan ? 8;

		WFSSynthChannel.loadSynthDef;
		this.launchMixer;
	}

	launchMixer {
		gui = WFSGUI();
		this.fillChannels;
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

	fillChannels {
		channels = Array.fill(numChannels, { |ind|
			WFSSynthChannel(gui);
		});
	}
}