WFSScrollingNumberBox : JSCNumberBox {
	var lastCoord=0;
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
		this.mouseMoveAction = {};
		
		postln(this.class.asString ++ " initialized");
	}
	
/*	mouseDownAction_ { |func|
		
	}

	mouseUpAction_ { |func|
		
	}*/

	mouseMoveAction_ { |func|
		super.mouseMoveAction = { |obj,x,y,mod|
			this.handleMouseMove(y, mod);
			func.value(obj, x, y, mod);
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