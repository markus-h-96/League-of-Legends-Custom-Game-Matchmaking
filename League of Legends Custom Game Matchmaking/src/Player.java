import java.io.*;
public class Player implements Serializable, Comparable<Player>{

	String name;
	String mainRole;
	String secondaryRole;
	String rank;
	int division;
	int lp;
	int offrolePenalty;
	int elo;
	String assignedRole;
	
	
	@Override
	public int compareTo(Player p) {
		return this.elo - p.elo;
	}

	Player(String name, String mainRole, String secondaryRole, String rank, String division, String lp, String offrolePenalty) {
		this.name = name;
		this.mainRole = mainRole;
		this.secondaryRole = secondaryRole;
		this.rank = rank;
		this.division = Integer.parseInt(division);
		this.lp = Integer.parseInt(lp);
		this.offrolePenalty = Integer.parseInt(offrolePenalty);
				
		rank.toLowerCase();
		boolean ignoreDivision = false;
		
		try {
			if (rank.contains("iron")) elo = 0;
			else if (rank.contains("bronze")) elo = 400;
			else if (rank.contains("silver")) elo = 800;
			else if (rank.contains("gold")) elo = 1200;
			else if (rank.contains("plat")) elo = 1600;
			else if (rank.contains("dia")) elo = 2000;
			else if (rank.contains("grandmaster")) {
				elo = 2400;
				ignoreDivision = true;
			}
			else if (rank.contains("master")) {
				elo = 2400;
				ignoreDivision = true;
			}				
			else if (rank.contains("challenger")) {
				elo = 2400;
				ignoreDivision = true;
			}
			
			if (!ignoreDivision) {
				elo += 100 * (4-this.division);
			}
			elo += this.lp;
			
		} catch (Exception e) {
		    
		}
	}
}