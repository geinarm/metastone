package net.demilich.metastone.game.behaviour.aiplanner;

import java.util.Iterator;
import java.util.LinkedList;

public class NBestQueue implements Iterable<Node> {

	private LinkedList<Node> list;
	private int maxNodes;
	
	public NBestQueue(int n) {
		list = new LinkedList<Node>();
		maxNodes = n;
	}
	
	public void add(Node node) {
		addInOrder(node);
		
		if(list.size() > maxNodes) {
			list.pollLast();
		}
	}
	
	private void addInOrder(Node node) {
		if(list.isEmpty()) {
			list.add(node);
			return;
		}
		
		int index = 0;
		while(list.get(index).compareTo(node) > 0) {
			index++;
			if(index >= list.size()) {
				list.addLast(node);
				return;
			}
		}	
		list.add(index, node);
	}

	public int getCount() {
		return list.size();
	}
	
	public int getSize() {
		return maxNodes;
	}
	
	public boolean isEmpty() {
		return list.isEmpty();
	}
	
	public boolean isFull() {
		return list.size() >= maxNodes;
	}
	
	public Node popHead() {
		return list.pollFirst();
	}
	
	public Node popTail() {
		return list.pollLast();
	}

	@Override
	public Iterator<Node> iterator() {
		return list.iterator();
	}

}
