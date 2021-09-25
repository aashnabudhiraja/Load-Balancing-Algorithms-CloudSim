package cloudsim.ext.datacenter;

import java.util.Random;
import cloudsim.VirtualMachine;

public class AntColonyVmLoadBalancer extends VmLoadBalancer {
	
	private double[][] pheromones;
	static final double alpha = 1;
	static final double beta = 1;
	static final double ONE_UNIT_PHEROMONE = 1;
	static final double EVAPORATION_FACTOR = 2;
	private final int NUM_ANTS = 10;

	Ant[] ants;
	DatacenterController dcbLocal;
	int counter = 0;

	public AntColonyVmLoadBalancer(DatacenterController dcb) {
		super();
		dcbLocal = dcb;
	}

	@Override
	public int getNextAvailableVm() {
		if(counter == 0)
			{
				pheromones = new double[dcbLocal.vmlist.size() + 1][dcbLocal.vmlist.size() + 1];
				counter++;
				ants = new Ant[NUM_ANTS];
				for (int i = 0; i < ants.length; i++) {
					ants[i] = new Ant(pheromones);
				}
			}
		
		for (int ant = 0; ant < ants.length; ant++) {
			ants[ant].SendAnt(); 
		}
		
		Evaporation();

		Ant queryAnt = new Ant(pheromones);
		int vmId = queryAnt.FetchFinalVm();
		allocatedVm(vmId);
		System.out.println("allocated "+vmId);
		return vmId;
	}


	public void Evaporation() {
		for (int i = 0; i < pheromones.length; i++) {
			for (int j = 0; j < pheromones.length; j++) {
				pheromones[i][j] /= EVAPORATION_FACTOR;
			}
		}
	}

	public class Ant {
		private int fakeVmId;

		public Ant(double[][] ph) {
			fakeVmId = ph.length - 1;
		}

		public int SendAnt() {
			return ProcessAnt(true);
		}

		public int FetchFinalVm() {
			return ProcessAnt(false);
		}

		public int ProcessAnt(boolean updatePheromones) {
			int CurrentVmId = fakeVmId;
			int nextVmId = getNextVmNode(CurrentVmId);

			if (updatePheromones) {
				UpdatePheromone(CurrentVmId, nextVmId);
			}
			while (nextVmId != CurrentVmId) {
				CurrentVmId = nextVmId;
				nextVmId = getNextVmNode(CurrentVmId);
				if (updatePheromones) {
					UpdatePheromone(CurrentVmId, nextVmId);
				}
			}

			return CurrentVmId;
		}

		// Assuming vmIds start from 0 and are consecutive.
		// Assumed there is one node that is not visited
		public int getNextVmNode(int vmId) {
			double[] probability = computeProbability(vmId);
			Random rand = new Random();
			double randomization = rand.doubles(1, 0.0, 0.5).sum();
			for (int i = 0; i < probability.length; i++) {
				randomization = randomization - probability[i];
				if (randomization <= 0) {
					return i;
				}
			}
			for (int i = 0; i < probability.length; i++) {
				System.out.println("Debug " + probability[i]);
			}
			return -1;
		}

		
		// Assumes there is at least one node that has not been visited
		public double[] computeProbability(int vmId) {
			double[] probability = new double[pheromones.length - 1];
			double sum = 0.0;
			for (int i = 0; i < probability.length; i++) {
				probability[i] = scoreFunction(vmId, i);
				sum += probability[i];
			}

			// Normalize
			for (int i = 0; i < probability.length; i++) {
				probability[i] = probability[i] / sum;
			}
			return probability;
		}


		public void UpdatePheromone(int prevId, int newId) {
			pheromones[prevId][newId] += ONE_UNIT_PHEROMONE;
		}

		
		public double scoreFunction(int prevVmId, int newVmId) {
			double maxBw = ((VirtualMachine) dcbLocal.vmlist.get(newVmId)).getCharacteristics().getBw();
			double currentBw = ((VirtualMachine) dcbLocal.vmlist.get(newVmId)).getBw();
			// double requestedBw = cloudlet.getUtilizationOfBw(0);
			return Math.pow(pheromones[prevVmId][newVmId], alpha) + 1.0 + (maxBw - currentBw / maxBw);

		}
	}
}
