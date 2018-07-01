package it.polito.tdp.formulaone.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jgrapht.Graphs;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleDirectedWeightedGraph;

import it.polito.tdp.formulaone.db.DriverIdMap;
import it.polito.tdp.formulaone.db.FormulaOneDAO;

public class Model {
	
	private FormulaOneDAO fonedao;
	private SimpleDirectedWeightedGraph<Driver, DefaultWeightedEdge> grafo;
	private DriverIdMap driverIdMap;
	
	private List<Driver> bestDreamTeam;
	private int bestDreamTeamValue;
	
	public Model() {
		fonedao = new FormulaOneDAO();
		this.driverIdMap = new DriverIdMap();
	}

	public List<Season> getAllSeasons() {
		return fonedao.getAllSeasons();
	}

	public void creaGrafo(Season s) {
		grafo = new SimpleDirectedWeightedGraph<Driver, DefaultWeightedEdge>(DefaultWeightedEdge.class);
		List<Driver> drivers = fonedao.getAllDriversBySeason(s, driverIdMap);
		Graphs.addAllVertices(grafo, drivers);
		
		for(DriverSeasonResult dsr : fonedao.getDriverSeasonResults(s, driverIdMap) ) {
			Graphs.addEdgeWithVertices(this.grafo, dsr.getD1(), dsr.getD2(),dsr.getCounter());
		}
		
		System.out.print(grafo.toString());
	}

	public Driver getBestDriver() {
		if(this.grafo == null)
			new RuntimeException("Creare il grafo!");
	
		// Inizializzazione
		Driver bestDriver = null;
		int best = Integer.MIN_VALUE;
		
		for(Driver d : this.grafo.vertexSet()) {
			int sum = 0; // sommo i pesi di tutti gli archi uscenti e entranti
			
			// Itero sugli archi uscenti
			for(DefaultWeightedEdge e : this.grafo.outgoingEdgesOf(d) ) {
				sum += this.grafo.getEdgeWeight(e);
			}
			// e su quelli entranti
			for(DefaultWeightedEdge e : this.grafo.incomingEdgesOf(d) ) {
				sum -= this.grafo.getEdgeWeight(e);
			}
			
			if(sum > best || bestDriver == null) { // controlla
				bestDriver = d;
				best = sum;
			}
		}
		
		if(bestDriver == null)
			new RuntimeException ("BestDriver not found");
		
		return bestDriver;
	}
	
	public List<Driver> getDreamTeam(int k) { // k valore massimo della ricorsione
		bestDreamTeam = new ArrayList<>();
		bestDreamTeamValue = Integer.MAX_VALUE;
		recursive(0, new ArrayList<Driver>(), k);
		return bestDreamTeam;
	}

	private void recursive(int step, ArrayList<Driver> tempDreamTeam, int k) {
		
		// condizione di terminazione
		if(step >= k) {
			if(evaluate(tempDreamTeam) < bestDreamTeamValue) {
				bestDreamTeamValue = evaluate(tempDreamTeam);
				bestDreamTeam = new ArrayList<>(tempDreamTeam); // deep copy
				return;
			}
		}
		
		// costruzione soluzione parziale
		for(Driver d : this.grafo.vertexSet()) {
			if(!tempDreamTeam.contains(d)) {
				tempDreamTeam.add(d);
				recursive(step+1, tempDreamTeam, k);
				tempDreamTeam.remove(d); // controllare che ci siano hashcode e equals perche vengono usati da remove.
			}
		}
	}

	private int evaluate(ArrayList<Driver> tempDreamTeam) {
		int sum = 0;
		Set<Driver> tempDreamTeamSet = new HashSet<>(tempDreamTeam); // nel caso in cui fosse grande questa list dato che uso molti contains così miglioro i tempi di efficenza
		for(DefaultWeightedEdge e : this.grafo.edgeSet()) {
			if(tempDreamTeamSet.contains(this.grafo.getEdgeTarget(e))) { // contiene il vertice di destinazione?
				sum += this.grafo.getEdgeWeight(e);
			}
		}
		return sum;
	}
	
}
