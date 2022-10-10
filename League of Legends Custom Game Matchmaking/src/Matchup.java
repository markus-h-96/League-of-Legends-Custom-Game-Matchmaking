import java.io.*;
import java.util.List;
import java.util.ArrayList;

public class Matchup implements Comparable<Matchup>, Serializable {

	
	// TODO - could create a "Team" class, but haven't felt like refactoring yet
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
	/*
	 * score to evaluate how "fair" or how "high quality" a matchup is. Lower score
	 * means more fair matchup. Very subjective, using 1 offrole = 200 team LP diff
	 * = 250 least-balanced-lane-LP diff for now
	 */
	int score;

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

	/*
	 * same as compareTo but self-explanatory name priority: offrole count > team
	 * elo diff > lane elo diff
	 */
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

	/*
	 * priority: offrole count > lane elo diff > team elo diff
	 */
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

	/*
	 * priority: team elo diff > offrole count > lane elo diff
	 */
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

	/*
	 * priority: lane elo diff > offrole count > team elo diff
	 */
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

	/*
	 * priority: team elo diff > lane elo diff > offrole count
	 */
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

	/*
	 * probably the best way of sorting, as it takes all 3 metrics (offrole count,
	 * team elo diff, lane elo diff) into account at once. can be tuned easily; for
	 * now, using 1 offrole = 200 team elo diff = 250 lane elo diff seems to do
	 * well.
	 */
	public int compareByScore(Matchup m) {
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
		this.score = (int) (200 * this.secondaryRoles + this.absoluteEloDifference
				+ 0.8 * this.highestLaneEloDifference);
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