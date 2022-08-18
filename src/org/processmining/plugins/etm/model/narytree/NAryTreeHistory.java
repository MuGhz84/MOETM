package org.processmining.plugins.etm.model.narytree;


/**
 * 
 * Class to store the history of changes of the trees through the evolutionary process.
 *
 */
public class NAryTreeHistory {
	
	//type of changes
	public enum TypesOfChange {
	    REMOVE, ADD, OTHER, USELESS
	}
	
	private final NAryTree parent;
	
	private final int locationOfChange;
	
	private final TypesOfChange typeOfChange;
	
	public NAryTreeHistory(NAryTree _parent, int _locationOfChange, TypesOfChange _typeOfChange) {
		this.parent = _parent;
		this.locationOfChange = _locationOfChange;
		this.typeOfChange = _typeOfChange;
	}

	public NAryTree getParent() {
		return this.parent;
	}

	public int getLocationOfChange() {
		return this.locationOfChange;
	}
	
	public TypesOfChange getTypeOfChange(){
		return this.typeOfChange;
	}

}
