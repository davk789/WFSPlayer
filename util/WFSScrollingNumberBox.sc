WFSScrollingNumberBox : JSCNumberBox {
	var lastCoord=0;
	// WARNING: this class implements the same function for action and mouseMoveAction
	var subclassAction, subclassMouseUpAction;
	var <>minVal, <>maxVal;

	*new { |par,bnd|
		^super.new(par,bnd).init_wfsscrollingnumberbox;
	}

	*test {
		var win;
		win = Window().front;
		^super.new(win, Rect(0, 0, 75, 25)).init_wfsscrollingnumberbox;
	}
	
	init_wfsscrollingnumberbox {
		this.value = 0;
		this.action = {};	
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
		super.action = { |obj,x,y,mod|
			this.checkRange;
			subclassAction.value(obj);
		};

		super.mouseMoveAction = { |obj,x,y,mod|
			this.checkRange;
			this.handleMouseMove(y, mod);
			subclassAction.value(obj, x, y, mod);
		};
	}

	checkRange {
		if(minVal.notNil){
			if(this.value < minVal){ this.value = minVal; };
		};

		if(maxVal.notNil){
			if(this.value > maxVal){ this.value = maxVal; };
		};
	}
	
	handleMouseMove { |loc, mod|
		var incrementAmount = Dictionary[0 -> 1, 524288 -> 0.1, 131072 -> 100];

		if(lastCoord < loc){
			this.value = this.value - incrementAmount[mod];
		};
		
		if(lastCoord > loc){
			this.value = this.value + incrementAmount[mod];
		};
		
		lastCoord = loc;
	}

}