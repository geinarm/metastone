package net.demilich.metastone.game.behaviour.aiplanner;

import java.util.ArrayList;
import java.util.List;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.actions.GameAction;

public class Node implements Comparable<Node> {
	public Node parent;
	public GameContext state;
	public GameAction action;
	public int player;
	public double score;
	
	public Node(Node parent, GameContext s, GameAction a, int p) {
		this.parent = parent;
		this.state = s;
		this.action = a;
		this.player = p;
	}
	public Node(Node parent, GameContext s, GameAction a, int p, double score) {
		this.parent = parent;
		this.state = s;
		this.action = a;
		this.player = p;
		this.score = score;
	}
	
	public int getDepth() {
		if(this.parent == null) {
			return 0;
		}
		return this.parent.getDepth() +1;
	}
	
	public Node getRoot() {
		if(this.parent == null) {
			return this;
		}
		return this.parent.getRoot();
	}
	
	public List<GameAction> getPlan() {
		if(this.parent == null) {
			List<GameAction> plan = new ArrayList<GameAction>();
			return plan;
		}
		
		List<GameAction> plan = parent.getPlan();
		plan.add(this.action);
		return plan;
	}

	@Override
	public int compareTo(Node o) {
    	return Double.compare(this.score, o.score);
	}
	
}
