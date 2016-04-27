package net.demilich.metastone.game.behaviour.aiplanner;

import org.testng.Assert;
import org.testng.annotations.Test;

public class TestNBestQueue {

	@Test
	public void test() {
		NBestQueue queue = new NBestQueue(3);
	
		Node n1 = new Node(null, null, null, 0, 10);
		Node n2 = new Node(null, null, null, 0, 20);
		Node n3 = new Node(null, null, null, 0, 30);
		Node n4 = new Node(null, null, null, 0, 40);
		Node n5 = new Node(null, null, null, 0, 50);
		
		queue.add(n1);
		queue.add(n2);
		
		Assert.assertEquals(queue.popHead(), n2);
		Assert.assertEquals(queue.popTail(), n1);
		
		queue.add(n1);
		queue.add(n2);
		queue.add(n3);
		queue.add(n4);
		queue.add(n5);
		
		Assert.assertEquals(queue.getSize(), 3);
		Assert.assertEquals(queue.isFull(), true);
		
		Assert.assertEquals(queue.popTail(), n3);
		Assert.assertEquals(queue.popHead(), n5);
	}
	
}
