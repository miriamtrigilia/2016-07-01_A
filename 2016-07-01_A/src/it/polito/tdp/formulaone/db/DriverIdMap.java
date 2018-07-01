package it.polito.tdp.formulaone.db;

import java.util.HashMap;

import it.polito.tdp.formulaone.model.Driver;

public class DriverIdMap extends HashMap<Integer, Driver>{ // ad un id -> driver // estendiamo l'hashmap ed ereditiamo il metodo get
	
	public Driver get(Driver driver) {
		Driver old = super.get(driver.getDriverId()); // con super richiamo il metodo get di hash map, senno loop infinito
		if(old != null)
			return old;
		super.put(driver.getDriverId(), driver);
		return driver;
	}
	
	
	

}
