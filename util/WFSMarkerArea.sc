WFSMarkerArea {
	/* wrap a view redirected View object, rather than inherit, for cross-platform
	   compatibility. */
	var prThis; // the wrapped object
	var <dimensions, maxWidth, <activeSize=0.9; //  point representation of dimensions in feet
	var coords, <currentIndex=0, indexCounter=0;
	var <gridColor, <gridHighlightColor, <gridActiveAreaColor;
	var <markerColor, <selectionColor, <>markerSize=5;
	var <>maxNumPoints=128; // mouse down may lag with too many points
	var canMove=false;
	var <>editable=true, <>canAddMarker=true; // watch canAddMarker for bugs

	*new { |view, dim|
		^super.new.init_wfsmarkerarea(view, dim);
	}

	*test {
		var win;
		win = Window().front;
		^super.new.init_wfsmarkerarea(win, win.view.bounds);
	}

	init_wfsmarkerarea { |par,bnd|
		prThis = UserView(par,bnd);
		// when removing elements, the value at the index is set to nil,
		// rather than removing the value from the array outright
		// so be sure to check for nil
		coords = Array();
		markerColor = Color.yellow;
		selectionColor = Color.green;
		gridColor = Color.new255(55, 62, 64);
		gridHighlightColor = Color.new255(80, 95, 99);
		gridActiveAreaColor = Color.new255(30, 33, 33);
		prThis.background = Color.black.alpha_(0.8);
		dimensions = 16 @ 22;
		maxWidth = prThis.bounds.width;

		prThis.mouseDownAction = {};
		prThis.mouseUpAction = {};
		prThis.mouseMoveAction = {};

		this.setDrawFunc;
	}

	setDrawFunc {
		prThis.drawFunc = {
			Pen.use{
				// draw active area
				Pen.color = Color.new255(65, 70, 70);
				Pen.addRect(
					Rect(this.activeStart, 0, this.activeRange, prThis.bounds.height)
				);

				Pen.fill;

				// draw grid
				Pen.color = gridHighlightColor;

				dimensions.x.do{ |ind|
					var loc = ((ind / (dimensions.x - 1)) * this.activeRange) + this.activeStart;
					Pen.line(
						loc @ 0,
						loc @ prThis.bounds.height
					);
				};
				Pen.stroke;

				
				dimensions.y.do{ |ind|
					var loc = (ind / dimensions.y) * prThis.bounds.height;
					if((ind % 3) == 0){
						Pen.color = gridHighlightColor;
					}{
						Pen.color = gridColor;
					};
					Pen.line(
						0 @ loc,
						prThis.bounds.width @ loc
					);
					Pen.stroke;
				};

				// draw location points
				coords.do{ |coord, ind|
					if(ind == currentIndex){ 
						Pen.color = selectionColor;
					}{
						Pen.color = markerColor;
					};
					
					if(coord.notNil){
						Pen.addArc(this.valueToLocation(coord), markerSize, 0, 2pi);
						//Pen.addArc(coord, markerSize, 0, 2pi);
						Pen.fill;
					};
				};
			};
		};
	}

	mouseDownAction_ { |func|
		prThis.mouseDownAction = { |obj,x,y,mod|
			if(editable){
				this.handleMouseDown(x @ y, mod);
			};
			func.value(this,x,y,mod);
		};
	}

	mouseUpAction_ { |func|
		prThis.mouseUpAction = { |obj,x,y,mod|
			if(editable){
				this.handleMouseUp(x @ y, mod);
			};
			func.value(this,x,y,mod);
		};
	}

	mouseMoveAction_ { |func|
		prThis.mouseMoveAction = { |obj,x,y,mod|
			if(editable){
				this.handleMouseMove(x @ y, mod);
			};
			func.value(this,x,y,mod);
		};
	}

	handleMouseDown { |loc, mod|
		// check the add conditions
		var pointsNotFull, collisionPoint, numPoints;
		var coord = this.locationToValue(loc);
		if(mod != 131072){
			pointsNotFull = true;
			// check the collisions point using unscaled values
			collisionPoint = this.getCollisionPoint(loc);

			if(collisionPoint.isNil){
				numPoints = this.countPoints;
				pointsNotFull = numPoints < maxNumPoints;
				
				if(pointsNotFull && canAddMarker){
					this.addMarker(coord);
				};
			}{
				currentIndex = collisionPoint;
			};
	
			canMove = pointsNotFull;
			
			prThis.refresh;

		}{
			canMove = false;
		};
	}

	handleMouseUp { |loc, mod|
		var collisionPoint = this.getCollisionPoint(loc);
		
		if((mod == 131072) && collisionPoint.notNil && canAddMarker){
			this.removeMarker(collisionPoint);
		};
	}

	handleMouseMove { |loc, mod|
		if((mod != 131072) && canMove){
			this.moveMarker(loc);
		}
	}

	dimensions_ { |dim|
		dimensions = dim;
		prThis.refresh;
	}

	width {
		^dimensions.x;
	}

	width_ { |wid|
		dimensions.x = wid;
		prThis.refresh;
	}

	depth {
		^dimensions.y;
	}

	depth_ { |dp|
		dimensions.y = dp;
		prThis.refresh;
	}

	removeMarker { |markerIndex|
		coords[markerIndex] = nil;
		prThis.refresh;
	}

	addMarker { |loc|
		var coord = this.locationToValue(loc);
		coords = coords.add(coord);
		currentIndex = coords.lastIndex;
		prThis.refresh;
	}

	moveMarker { |loc|
		var coord = this.locationToValue(loc);
		coords[currentIndex] = coord;
		postln(coords);
		prThis.refresh;
	}
	
	countPoints {
		var ret=0;
		coords.do{ |obj,ind| 
			if(obj.notNil){
				ret = ret + 1;
			};
		};
		
		^ret;
	}

	getCollisionPoint { |loc|
		var diff;

		coords.do{ |obj,ind|
			if(obj.notNil){
				diff = abs(this.valueToLocation(obj) - loc);
				if((diff.x < markerSize) && (diff.y < markerSize)){
					^ind;	
				};
			};
		};

		^nil;
	}
	
	markerColor_ { |color|
		markerColor = color;
		prThis.refresh;
	}
	
	selectionColor_ { |color|
		selectionColor = color;
		prThis.refresh;
	}

	setValueForIndex { |ind, val|
		coords[ind] = val;
		prThis.refresh;
	}

	getValueForIndex { |ind|
		^coords[ind];
	}

	value {
		^coords;
	}

	value_ { |vals|
		coords = vals;
		prThis.refresh;
	}

	currentIndex_ { |ind|
		currentIndex = ind;
		prThis.refresh;
	}

	currentValue {
		^this.getValueForIndex(currentIndex);
	}

	currentValue_ { |val|
		this.setValueForIndex(currentIndex, val);
	}
	
	enabled_ { |choice|
		prThis.enabled = choice;
	}
	
	enabled {
		prThis.enabled;
	}

	resize {
		^prThis.resize;
	}

	resize_ { |choice|
		prThis.resize = choice;
	}

	valueToLocation { |val|
		var prange = activeSize @ 1;
		var pstart = this.activeStartZeroOne @ 0;
		var zoom = (val * prange) + pstart;
		^zoom * (prThis.bounds.width @ prThis.bounds.height);
	}

	locationToValue { |loc|
		var prange = activeSize @ 1;
		var pstart = this.activeStartZeroOne @ 0;
		var scale = loc / (prThis.bounds.width @ prThis.bounds.height);
		^(scale - pstart) / prange;
	}

	activeSize_ { |size|
		activeSize = size;
		this.refresh;
	}

	activeStart {
		^(prThis.bounds.width - this.activeRange) / 2;
	}

	activeStartZeroOne {
		^this.activeStart / maxWidth;
	}

	activeRange {
		^maxWidth * activeSize;
	}

	activeRange_ { |rng|
		activeSize = rng;
		maxWidth = prThis.bounds.width;
		prThis.refresh;
	}

}
