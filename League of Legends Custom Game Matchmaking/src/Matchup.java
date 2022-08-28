import java.io.*;
import java.util.List;
import java.util.ArrayList;

public class Matchup implements Comparable<Matchup>, Serializable {

	Player[] players;
	int eloTeamOne = 0;
	int eloTeamTwo = 0;
	int eloDifference;
	int absoluteEloDifference;
	int secondaryRoles = 0;
	int secondaryRolesTeamOne = 0;
	int secondaryRolesTeamTwo = 0;

	// as in, which lane is the least fair?
	int highestLaneEloDifference = 0;
	// list because multiple roles could be equally fair / unfair
	List<Integer> leastBalancedLaneMatchup = new ArrayList<Integer>();
	// score to evaluate how "fair" or how "high quality" a matchup is; very subjective, using 1 offrole = 190 team LP diff = 475 least-balanced-lane-LP diff for now
	int score;
	
	// apparently a method is needed to use Comparator.comparing
	public int getSecondaryRoles() {
		return this.secondaryRoles;
	}

	// apparently a method is needed to use Comparator.comparing
	public int getAbsoluteEloDifference() {
		return this.absoluteEloDifference;
	}

	// apparently a method is needed to use Comparator.comparing
	public int getHighestLaneEloDifference() {
		return this.highestLaneEloDifference;
	}
	
	/*
	 * checking lane elo diff if team elo diff <200 probably doesn't work, sorting
	 * would be inconsistent, e.g. in this case:
	 * 
	 * A - 200, 500
	 * 
	 * B - 450, 400
	 * 
	 * C - 300, 450
	 * 
	 * B would come before C (team LP diff only 150LP, and B's worst lane diff (400)
	 * is lower than C (450)
	 * 
	 * C would come before A (team LP diff only 100LP, C's worst lane (450) lower
	 * than A (500)
	 * 
	 * A would come before B (team LP diff is 450-200 > 200, so we wouldn't look
	 * 
	 * 
	 * @Override public int compareTo(Matchup m) { int tmp = this.secondaryRoles -
	 * m.secondaryRoles; if (tmp == 0) { tmp = this.absoluteEloDifference -
	 * m.absoluteEloDifference; if (Math.abs(tmp) < 200) { tmp =
	 * this.highestLaneEloDifference - m.highestLaneEloDifference; } } return tmp; }
	 */

	
	@Override
	public int compareTo(Matchup m) {
		int tmp = this.secondaryRoles - m.secondaryRoles;
		if (tmp == 0) {
			tmp = this.absoluteEloDifference - m.absoluteEloDifference;
			if (Math.abs(tmp) == 0) {
				tmp = this.highestLaneEloDifference - m.highestLaneEloDifference;
			}
		}
		return tmp;
	}

	
	// same as compareTo but self-explanatory name
	// often the best sorting imo, it's not worth taking 5 extra autofills for a game that's 50 LP closer (and we account for unbalance anyway when assigning LP after a win / loss)
	public int compareByOffroleTeamLane(Matchup m) {
		int tmp = this.secondaryRoles - m.secondaryRoles;
		if (tmp == 0) {
			tmp = this.absoluteEloDifference - m.absoluteEloDifference;
			if (Math.abs(tmp) == 0) {
				tmp = this.highestLaneEloDifference - m.highestLaneEloDifference;
			}
		}
		return tmp;
	}
	
	public int compareByOffroleLaneTeam(Matchup m) {
		int tmp = this.secondaryRoles - m.secondaryRoles;
		if (tmp == 0) {
			tmp = this.highestLaneEloDifference - m.highestLaneEloDifference;
			if (Math.abs(tmp) == 0) {
				tmp = this.absoluteEloDifference - m.absoluteEloDifference;
			}
		}
		return tmp;
	}

	// sometimes, games may become too unbalanced if I try to minimize the amount of offrole players at all cost, so this can be better in some cases
	public int compareByTeamOffroleLane(Matchup m) {
		int tmp = this.absoluteEloDifference - m.absoluteEloDifference;
		if (tmp == 0) {
			tmp = this.secondaryRoles - m.secondaryRoles;
			if (tmp == 0) {
				tmp = this.highestLaneEloDifference - m.highestLaneEloDifference;
			}
		}
		
		return tmp;
	}
	
	
	public int compareByLaneOffroleTeam(Matchup m) {
		int tmp = this.highestLaneEloDifference - m.highestLaneEloDifference;
		if (tmp == 0) {
			tmp = this.secondaryRoles - m.secondaryRoles;
			if (tmp == 0) {
				tmp = this.absoluteEloDifference - m.absoluteEloDifference;
			}
		}
		
		return tmp;
	}
	
	public int compareByTeamLaneOffrole(Matchup m) {
		int tmp = this.absoluteEloDifference - m.absoluteEloDifference;
		if (tmp == 0) {
			tmp = this.highestLaneEloDifference - m.highestLaneEloDifference;
			if (tmp == 0) {
				tmp = this.secondaryRoles - m.secondaryRoles;
			}
		}

		return tmp;
	}

	public int compareByScore(Matchup m) {
		// lower score means better matchup
		return this.score - m.score;
	}
	
	Matchup(Player[] players) {
		this.players = players;

		for (int i = 0; i < 5; i++) {
			this.eloTeamOne += this.players[i].elo;
		}

		for (int i = 5; i < 10; i++) {
			this.eloTeamTwo += this.players[i].elo;
		}

		this.eloDifference = this.eloTeamOne - this.eloTeamTwo;
		this.absoluteEloDifference = Math.abs(this.eloDifference);

		for (int i = 0; i < 5; i++) {
			int tmp = Math.abs(this.players[i].elo - this.players[i + 5].elo);
			if (tmp > highestLaneEloDifference) {
				highestLaneEloDifference = tmp;
				leastBalancedLaneMatchup.clear();
				leastBalancedLaneMatchup.add(i);
			} else if (tmp == highestLaneEloDifference) {
				leastBalancedLaneMatchup.add(i);
			}
		}
		countSecondaryRoles();
		this.score = (int) (200 * this.secondaryRoles + this.absoluteEloDifference + 0.5 * this.highestLaneEloDifference);
	}

	void countSecondaryRoles() {
		for (int i = 0; i < 5; i++) {
			if (!(players[i].assignedRole.equals(players[i].mainRole))) {
				secondaryRoles++;
				secondaryRolesTeamOne++;
			}
		}
		for (int i = 5; i < 10; i++) {
			if (!(players[i].assignedRole.equals(players[i].mainRole))) {
				secondaryRoles++;
				secondaryRolesTeamTwo++;
			}
		}
	}

	String leastBalancedLaneMatchupToString() {
		String tmp = "";
		String[] roles = { "Top", "Jun", "Mid", "Bot", "Sup" };

		for (int i = 0; i < leastBalancedLaneMatchup.size(); i++) {
			tmp += roles[leastBalancedLaneMatchup.get(i)];
			if (i < leastBalancedLaneMatchup.size() - 1)
				tmp += "/";
		}
		tmp += " - " + highestLaneEloDifference + " LP | ";

		return tmp;
	}

	String teamsToString() {
		String tmp = "";
		for (int i = 0; i < 5; i++) {
			tmp += players[i].assignedRole.substring(0, 1).toUpperCase() + players[i].assignedRole.substring(1, 3)
					+ ": ";
			if (!(players[i].assignedRole.equals(players[i].mainRole))) {
				tmp += "[" + players[i].name + "]";
			} else {
				tmp += players[i].name;
			}
			if (i < 4)
				tmp += ", ";
		}

		tmp += " ---VS--- ";

		for (int i = 5; i < 10; i++) {
			tmp += players[i].assignedRole.substring(0, 1).toUpperCase() + players[i].assignedRole.substring(1, 3)
					+ ": ";
			if (!(players[i].assignedRole.equals(players[i].mainRole))) {
				tmp += "[" + players[i].name + "]";
			} else {
				tmp += players[i].name;
			}
			if (i < 9)
				tmp += ", ";
		}
		return tmp;
	}
}