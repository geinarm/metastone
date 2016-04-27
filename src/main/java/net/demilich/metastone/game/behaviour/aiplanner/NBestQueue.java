package net.demilich.metastone.game.behaviour.aiplanner;

import java.util.SortedSet;
import java.util.TreeSet;

import org.slf4j.Logger;

public class NBestQueue {

	private SortedSet<Node> set;
	private int maxNodes;
	
	public NBestQueue(int n) {
		set = new TreeSet<Node>();
		maxNodes = n;
	}
	
	public void add(Node node) {
		set.add(node);
		
		if(set.size() > maxNodes) {
			popTail();
		}
	}

	public int getCount() {
		return set.size();
	}
	public int getSize() {
		return maxNodes;
	}
	public boolean isEmpty() {
		return set.isEmpty();
	}
	public boolean isFull() {
		return set.size() >= maxNodes;
	}
	
	public Node popHead() {
		if(set.size() == 0) {
			return null;
		}
		Node head = set.last();
		set.remove(head);
		return head;
	}
	
	public Node popTail() {
		if(set.size() == 0) {
			return null;
		}
		Node tail = set.first();
		set.remove(tail);
		return tail;
	}

}
