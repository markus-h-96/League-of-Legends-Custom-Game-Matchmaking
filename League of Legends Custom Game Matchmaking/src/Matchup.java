import java.io.*;
public class Matchup implements Comparable<Matchup>, Serializable{

	Player[] players;
	int eloTeamOne = 0;
	int eloTeamTwo = 0;
	int eloDifference;
	int absoluteEloDifference;
	int secondaryRoles = 0;
	int secondaryRolesTeamOne = 0;
	int secondaryRolesTeamTwo = 0;
	
	
	@Override
	public int compareTo(Matchup m) {
	    int tmp = this.secondaryRoles - m.secondaryRoles;
	    if (tmp == 0) tmp = this.absoluteEloDifference - m.absoluteEloDifference;		

        return tmp;
	}
	// don't actually need this I think?
	public int compareEloDifferenceFirst(Matchup m) {
		int tmp = this.absoluteEloDifference - m.absoluteEloDifference;
		if (tmp == 0) tmp = this.secondaryRoles - m.secondaryRoles;
		
		return tmp;
	}
	// don't actually need this I think?
	public int compareOffroleCountFirst(Matchup m) {

	    int tmp = this.secondaryRoles - m.secondaryRoles;
	    if (tmp == 0) tmp = this.absoluteEloDifference - m.absoluteEloDifference;		

        return tmp;
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
	
	String teamsToString() {
		String tmp = "";
		for (int i = 0; i < 5; i++) {
			tmp += players[i].assignedRole.substring(0,1).toUpperCase() + players[i].assignedRole.substring(1,3) + ": ";
			if(!(players[i].assignedRole.equals(players[i].mainRole))) {
			    tmp += "[" + players[i].name + "]";
			} else {
			    tmp += players[i].name;
			}
			if (i < 4) tmp += ", ";
		}
		
		tmp += " ---VS--- ";
		
		for (int i = 5; i < 10; i++) {
			tmp += players[i].assignedRole.substring(0,1).toUpperCase() + players[i].assignedRole.substring(1,3) + ": ";
			if(!(players[i].assignedRole.equals(players[i].mainRole))) {
			    tmp += "[" + players[i].name + "]";
			} else {
			    tmp += players[i].name;
			}
			if (i < 9) tmp += ", ";
		}
		return tmp;
	}
}