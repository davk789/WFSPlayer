WFSTestingVisualizationOld {
	var win, controls, channelDisplay;
	var numChannels;

	*new { |numChans|
		^super.new.init_wfstestingvisualization(numChans);
	}

	init_wfstestingvisualization { |numChans|
		controls = Dictionary();

		numChannels = numChans ? 0;
		this.makeGUI;
	}

	value_ { |value|
		channelDisplay.value = value;
	}

	makeGUI {
		win = Window(
			"because I can't possibly debug this thing for real.", 
			Rect.new(500.rand, 500.rand, 700, 400)
		).front;

		channelDisplay = MultiSliderView.new(win, win.view.bounds)
		    .background_(Color.grey(0.9))
		    .strokeColor_(Color.blue(0.3))
		    .value_(Array.fill(64, { 0.5 });)
		    .drawLines_(true)
		    .drawRects_(false)
		    .elasticMode_(1);
		//		        .editable_(false)
		//.showIndex_(true)
		//        .selectionSize_(2)
		//        .startIndex_(0)
	}
}