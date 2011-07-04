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
				// ********** draw the rect
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
						Pen.addArc(this.coordToLocation(coord), markerSize, 0, 2pi);
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
		var coord = this.locationToCoord(loc);
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
		var coord = this.locationToCoord(loc);
		coords = coords.add(coord);
		currentIndex = coords.lastIndex;
		prThis.refresh;
	}

	moveMarker { |loc|
		var coord = this.locationToCoord(loc);
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
		var diff, oLoc;

		coords.do{ |obj,ind|
			oLoc = this.coordToLocation(obj);
			if(obj.notNil){
				diff = abs(oLoc - loc);
				if((diff.x < markerSize) && (diff.y < markerSize)){
					^ind;	
				};
			};
		};

		^nil;
	}

	coords_ { |newCoords|
		coords = newCoords;
		prThis.refresh;
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
		var scaledVal;
		// set a point scale 0..1 for an index
		scaledVal = val * (prThis.bounds.width @ prThis.bounds.height);

		coords[ind] = scaledVal;
		prThis.refresh;
	}

	getValueForIndex { |ind|
		^coords[ind] / (prThis.bounds.width @ prThis.bounds.height);
	}

	value {
		^coords;
	}

	value_ { |val|
		coords = val;
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

	coordToLocation { |coord|
		// location == the location of the marker on the view
		// coords = the list of 0..1 points aka the data value
		^coord * (prThis.bounds.width @ prThis.bounds.height);
	}

	locationToCoord { |loc|
		^loc / (prThis.bounds.width @ prThis.bounds.height)
	}

	activeSize_ { |size|
		activeSize = size;
		this.refresh;
	}

	activeStart {
		^(prThis.bounds.width - this.activeRange) / 2;
	}

	activeRange {
		^maxWidth * activeSize;
	}

}
