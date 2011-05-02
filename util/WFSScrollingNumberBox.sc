WFSScrollingNumberBox {
	/* wrap a view redirected number box, rather than subclass. Support Cocoa this way.
	   */
	var pr_this; // the wrapped number box
	var lastCoord=0;
	// WARNING: this class implements the same function for action and mouseMoveAction
	var subclassAction, subclassMouseUpAction;
	var <>minVal, <>maxVal;

	*new { |par,bnd|
		^super.new.init_wfsscrollingnumberbox(par,bnd);
	}

	*test {
		var win;
		win = Window().front;
		^super.new.init_wfsscrollingnumberbox(win, Rect(0, 0, 75, 25));
	}
	
	init_wfsscrollingnumberbox { |par,bnd|
		pr_this = NumberBox(par,bnd);
		pr_this.value = 0;
		pr_this.action = {};	
	}
	
/*	mouseDownAction_ { |func|
		
		}*/

	mouseUpAction {
		^subclassMouseUpAction;
	}

	mouseUpAction_ { |func|
		super.action = { |obj,x,y,mod|
			this.checkRange;
			subclassMouseUpAction.value(obj,x,y,mod);
		};
	}

	mouseMoveAction_ {
		error("mouseMoveAction_ is disabled in this class! Please set action_ instead.");
	}

	mouseMoveAction {
		error("mouseMoveAction is disabled in this class! please use action instead.");
	}

	action_ { |func|
		subclassAction = func;
		pr_this.action = { |obj,x,y,mod|
			this.checkRange;
			subclassAction.value(obj);
		};

		pr_this.mouseMoveAction = { |obj,x,y,mod|
			this.checkRange;
			this.handleMouseMove(y, mod);
			subclassAction.value(obj, x, y, mod);
		};
	}

	checkRange {
		if(minVal.notNil){
			if(pr_this.value < minVal){ pr_this.value = minVal; };
		};

		if(maxVal.notNil){
			if(pr_this.value > maxVal){ pr_this.value = maxVal; };
		};
	}
	
	handleMouseMove { |loc, mod|
		// the disct now contains modifiers for both GUI schemes
		var incrementAmount	= Dictionary[
			0 -> 1,        // default SwingOSC
			524288 -> 0.1, // option  SwingOSC
			131072 -> 100, // shift   SwingOSC
			256 -> 1,      // default CocoaGUI 
			524576 -> 0.1, // option  CocoaGUI
			131330 -> 100, // shift   CocoaGUI
		];
		
		if(lastCoord < loc){
			pr_this.value = pr_this.value - incrementAmount[mod];
		};
		
		if(lastCoord > loc){
			pr_this.value = pr_this.value + incrementAmount[mod];
		};
		
		lastCoord = loc;
	}
	
	value_ { |val|
		pr_this.value = val;
	}
	
	value {
		^pr_this.value;
	}
	
	background_ { |color|
		pr_this.background = color;
	}
	
	background {
		^pr_this.background;
	}
	
	enabled_ { |choice|
		pr_this.enabled = choice;
	}
	
	enabled {
		pr_this.enabled;
	}

}
