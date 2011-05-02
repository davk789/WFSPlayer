WFSMarkerArea {
	/* wrap a view redirected View object, rather than inherit, for cross-platform
	   compatibility. */
	var pr_this; // the wrapped object
	var coords, <currentIndex=0, indexCounter=0;
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
		pr_this = UserView(par,bnd);
		// when removing elements, the value at the index is set to nil,
		// rather than removing the value from the array outright
		// so be sure to check for nil
		coords = Array();
		markerColor = Color.yellow;
		selectionColor = Color.green;
		pr_this.background = Color.black.alpha_(0.8);

		pr_this.mouseDownAction = {};
		pr_this.mouseUpAction = {};
		pr_this.mouseMoveAction = {};

		this.setDrawFunc;
	}

	setDrawFunc {
		pr_this.drawFunc = {
			Pen.use{
				coords.do{ |coord, ind|
					
					if(ind == currentIndex){ 
						Pen.color = selectionColor;
					}{
						Pen.color = markerColor;
					};
					
					if(coord.notNil){
						Pen.addArc(coord, markerSize, 0, 2pi);
						Pen.fill;
					};
				};
			};
		};
	}

	mouseDownAction_ { |func|
		pr_this.mouseDownAction = { |obj,x,y,mod|
			if(editable){
				this.handleMouseDown(x @ y, mod);
			};
			func.value(this,x,y,mod);
		};
	}

	mouseUpAction_ { |func|
		pr_this.mouseUpAction = { |obj,x,y,mod|
			if(editable){
				this.handleMouseUp(x @ y, mod);
			};
			func.value(this,x,y,mod);
		};
	}

	mouseMoveAction_ { |func|
		pr_this.mouseMoveAction = { |obj,x,y,mod|
			if(editable){
				this.handleMouseMove(x @ y, mod);
			};
			func.value(this,x,y,mod);
		};
	}

	handleMouseDown { |coord, mod|
		// check the add conditions, more to come
		var pointsNotFull, collisionPoint, numPoints;
						
		if(mod != 131072){
			pointsNotFull = true;
			collisionPoint = this.getCollisionPoint(coord);

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
			
			pr_this.refresh;

		}{
			canMove = false;
		};
	}

	handleMouseUp { |coord, mod|
		var collisionPoint = this.getCollisionPoint(coord);
		
		if((mod == 131072) && collisionPoint.notNil && canAddMarker){
			this.removeMarker(collisionPoint);
		};
	}

	handleMouseMove { |coord, mod|
		var coordPoint;
		// we can not drag the marker out of the visible area!
		coordPoint = coord.x.max(0).min(pr_this.bounds.width) @ coord.y.max(0).min(pr_this.bounds.height);
		
		if((mod != 131072) && canMove){
			this.moveMarker(coordPoint);
		}
	}

	removeMarker { |markerIndex|
		coords[markerIndex] = nil;
		pr_this.refresh;
	}

	addMarker { |coord|
		coords = coords.add(coord);
		currentIndex = coords.lastIndex;
		pr_this.refresh;
	}

	moveMarker { |coord|
		coords[currentIndex] = coord;
		pr_this.refresh;
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

	getCollisionPoint { |coord|
		var diff;
		coords.do{ |obj,ind|
			if(obj.notNil){
				diff = abs(obj - coord);
				if((diff.x < markerSize) && (diff.y < markerSize)){
					^ind;	
				};
			};
		};

		^nil;
	}

	coords_ { |newCoords|
		coords = newCoords;
		pr_this.refresh;
	}
	
	markerColor_ { |color|
		markerColor = color;
		pr_this.refresh;
	}
	
	selectionColor_ { |color|
		selectionColor = color;
		pr_this.refresh;
	}

	setValueForIndex { |ind, val|
		var scaledVal;
		// set a point scale 0..1 for an index
		scaledVal = val * (pr_this.bounds.width @ pr_this.bounds.height);

		coords[ind] = scaledVal;
		pr_this.refresh;
	}

	getValueForIndex { |ind|
		^coords[ind] / (pr_this.bounds.width @ pr_this.bounds.height);
	}

	value {
		^coords.collect{ |obj|
			obj / (pr_this.bounds.width @ pr_this.bounds.height)
		};
	}

	value_ { |val|
		coords = val.collect{ |obj|
 			obj * (pr_this.bounds.width @ pr_this.bounds.height);
		};
		pr_this.refresh;
	}

	currentIndex_ { |ind|
		currentIndex = ind;
		pr_this.refresh;
	}

	currentValue {
		^this.getValueForIndex(currentIndex);
	}

	currentValue_ { |val|
		this.setValueForIndex(currentIndex, val);
	}
	
	enabled_ { |choice|
		pr_this.enabled = choice;
	}
	
	enabled {
		pr_this.enabled;
	}
}
