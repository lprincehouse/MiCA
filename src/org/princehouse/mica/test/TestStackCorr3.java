package org.princehouse.mica.test;

import fj.F3;

import java.net.UnknownHostException;
import java.util.List;

import org.princehouse.mica.base.net.model.Address;
import org.princehouse.mica.example.TreeCountNodes;
import org.princehouse.mica.example.TreeLabelNodes;
import org.princehouse.mica.lib.MinAddressLeaderElection;
import org.princehouse.mica.lib.SpanningTreeOverlay;
import org.princehouse.mica.lib.abstractions.MergeCorrelated;
import org.princehouse.mica.lib.abstractions.Overlay;
import org.princehouse.mica.lib.abstractions.StaticOverlay;
import org.princehouse.mica.util.TestHarness;


/**
 * Tests leader election + spanning tree + counting + labeling
 * @author lonnie
 *
 */
public class TestStackCorr3 extends TestHarness<MergeCorrelated> {

	/**
	 * @param args
	 * @throws UnknownHostException 
	 */
	public static void main(String[] args)  {

		F3<Integer, Address, List<Address>, MergeCorrelated> createNodeFunc = new F3<Integer, Address, List<Address>, MergeCorrelated>() {
			@Override
			public MergeCorrelated f(Integer i, Address address,
					List<Address> neighbors) {

				Overlay view = new StaticOverlay(neighbors);

				MinAddressLeaderElection leaderElection = new MinAddressLeaderElection(view);

				SpanningTreeOverlay tree = new SpanningTreeOverlay(leaderElection,view);

				TreeCountNodes counting = new TreeCountNodes(tree);

				TreeLabelNodes labeling = new TreeLabelNodes(tree,counting);

				return MergeCorrelated.merge(
						MergeCorrelated.merge(
								leaderElection,
								labeling
						),
						MergeCorrelated.merge(
								tree,
								counting
						));
			}
		};

		new TestStackCorr3().runMain(args, createNodeFunc);

	}

}
