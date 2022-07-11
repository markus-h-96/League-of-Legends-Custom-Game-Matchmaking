import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Matchmaker {
	public static void main(String args[]) {

		long startTime = System.nanoTime();

		List<Player> listOfPlayers = readActivePlayers("src/players.csv");
		Player[] players = listOfPlayers.toArray(new Player[0]);

		if (checkIfTwoPlayersPerRole(players)) {
			// only have to try 2^5 = 32 combinations

			List<Matchup> combinations = new ArrayList<Matchup>();

			findAllMatchupsAutofillNotRequired(players, combinations);
			Collections.sort(combinations, (o1, o2) -> o1.compareEloDifferenceFirst(o2));
			removeDuplicates(combinations);

			long endTime = System.nanoTime();
			long duration = endTime - startTime;
			System.out.println("Execution took " + duration / 1000000 + "ms");

			System.out.println("Everyone on main role. \n");
			printMatchupsAutofillNotRequired(combinations);

		} else {

			// figure out which role has too few people queuing for it
			List<Player> topPlayers = new ArrayList<Player>();
			List<Player> junglePlayers = new ArrayList<Player>();
			List<Player> midPlayers = new ArrayList<Player>();
			List<Player> botPlayers = new ArrayList<Player>();
			List<Player> supportPlayers = new ArrayList<Player>();

			setAvailablePlayersForEachRole(players, topPlayers, junglePlayers, midPlayers, botPlayers, supportPlayers);

			System.out.println("available top players: " + topPlayers.size());
			System.out.println("available jungle players: " + junglePlayers.size());
			System.out.println("available mid players: " + midPlayers.size());
			System.out.println("available bot players: " + botPlayers.size());
			System.out.println("available support players: " + supportPlayers.size());

			
			List<Matchup> combinations = new ArrayList<Matchup>();

			findAllMatchupsAutofillRequired(players, combinations);
			Collections.sort(combinations, (o1, o2) -> o1.compareTo(o2));
			removeDuplicates(combinations);

			long endTime = System.nanoTime();
			long duration = endTime - startTime;
			System.out.println("Execution took " + duration / 1000000 + "ms\n");

			System.out.println(
					"Can't put everyone on main role, at least one person has to be on [off role]. \nPossible matchups (exluding games with identical teams and blue/red side swapped): "
							+ combinations.size() + "\n");
			// if too many people choose "fill" as main role, console won't be able to display all matchups
			// printInfoAutofillRequired(combinations);
		}
	}

	static List<Player> readActivePlayers(String fileName) {
		List<Player> players = new ArrayList<Player>();

		try (BufferedReader br = Files.newBufferedReader(Paths.get(fileName))) {
			String line = br.readLine();
			// skip first line (header)
			line = br.readLine();
			while (line != null) {
				String[] attributes = line.split(",");
				Player player = new Player(attributes[0], attributes[1], attributes[2], attributes[3], attributes[4],
						attributes[5], attributes[6]);
				players.add(player);
				line = br.readLine();
			}
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
		return players;
	}

	// checks if there are exactly 2 people who main top, 2 jungle, 2 mid, 2 bot, 2
	// support
	static boolean checkIfTwoPlayersPerRole(Player[] players) {
		int[] roleCounter = new int[5];
		for (int i = 0; i < 10; i++) {
			if (players[i].mainRole.equals("top"))
				roleCounter[0]++;
			else if (players[i].mainRole.equals("jungle"))
				roleCounter[1]++;
			else if (players[i].mainRole.equals("mid"))
				roleCounter[2]++;
			else if (players[i].mainRole.equals("bot"))
				roleCounter[3]++;
			else if (players[i].mainRole.equals("support"))
				roleCounter[4]++;
		}

		boolean result = true;
		for (int i = 0; i < 5; i++) {
			result &= roleCounter[i] == 2;
		}
		return result;
	}

	// finds all possible matchups with every player getting their main role (used
	// when there are exactly 2 people who main top, jg, mid, bot, supp)
	static void findAllMatchupsAutofillNotRequired(Player[] players, List<Matchup> combinations) {

		List<Player> topPlayers = new ArrayList<Player>();
		List<Player> junglePlayers = new ArrayList<Player>();
		List<Player> midPlayers = new ArrayList<Player>();
		List<Player> botPlayers = new ArrayList<Player>();
		List<Player> supportPlayers = new ArrayList<Player>();

		for (int i = 0; i < 10; i++) {
			if (players[i].mainRole.equals("top"))
				topPlayers.add(players[i]);
			else if (players[i].mainRole.equals("jungle"))
				junglePlayers.add(players[i]);
			else if (players[i].mainRole.equals("mid"))
				midPlayers.add(players[i]);
			else if (players[i].mainRole.equals("bot"))
				botPlayers.add(players[i]);
			else if (players[i].mainRole.equals("support"))
				supportPlayers.add(players[i]);
		}

		for (int i = 0; i < 2; i++) {
			for (int j = 0; j < 2; j++) {
				for (int k = 0; k < 2; k++) {
					for (int l = 0; l < 2; l++) {
						for (int m = 0; m < 2; m++) {
							Player[] playersInMatch = new Player[10];

							playersInMatch[0] = topPlayers.get(i);
							playersInMatch[1] = junglePlayers.get(j);
							playersInMatch[2] = midPlayers.get(k);
							playersInMatch[3] = botPlayers.get(l);
							playersInMatch[4] = supportPlayers.get(m);

							playersInMatch[5] = topPlayers.get(1 - i);
							playersInMatch[6] = junglePlayers.get(1 - j);
							playersInMatch[7] = midPlayers.get(1 - k);
							playersInMatch[8] = botPlayers.get(1 - l);
							playersInMatch[9] = supportPlayers.get(1 - m);

							for (Player p : playersInMatch) {
								p.assignedRole = p.mainRole;
							}

							Matchup matchup = new Matchup(playersInMatch);
							combinations.add(matchup);
						}
					}
				}
			}
		}
	}

	// want to remove duplicate games that consist of identical teams with blue/red
	// sides swapped
	// can't just remove every 2nd game because those identical matchups are not
	// necessarily next to each other (if two players on the same team swap roles
	// and the overall elo difference is the same in both cases)
	// ->
	// remove games with negative elo difference first
	// then deal with the ones that are perfectly balanced (elo difference 0) by
	// specifically looking for matchups with identical teams and red/blue sides
	// swapped, and remove one of those
	static void removeDuplicates(List<Matchup> combinations) {

		int totalCombinations = combinations.size();

		for (int i = totalCombinations - 1; i >= 0; i--) {
			if (combinations.get(i).eloDifference < 0)
				combinations.remove(i);
		}

		totalCombinations = combinations.size();

		for (int i = totalCombinations - 1; i >= 0; i--) {
			if (combinations.get(i).eloDifference != 0) {
				continue;
			} else {
				Matchup currentMatchup = (Matchup) DeepCopy.copy(combinations.get(i));
				for (int j = i - 1; j >= 0; j--) {
					Matchup previousMatchup = (Matchup) DeepCopy.copy(combinations.get(j));
					if (currentMatchup.players[0].name.equals(previousMatchup.players[5].name)
							&& currentMatchup.players[1].name.equals(previousMatchup.players[6].name)
							&& currentMatchup.players[2].name.equals(previousMatchup.players[7].name)
							&& currentMatchup.players[3].name.equals(previousMatchup.players[8].name)
							&& currentMatchup.players[4].name.equals(previousMatchup.players[9].name)
							&& currentMatchup.players[5].name.equals(previousMatchup.players[0].name)
							&& currentMatchup.players[6].name.equals(previousMatchup.players[1].name)
							&& currentMatchup.players[7].name.equals(previousMatchup.players[2].name)
							&& currentMatchup.players[8].name.equals(previousMatchup.players[3].name)
							&& currentMatchup.players[9].name.equals(previousMatchup.players[4].name)) {
						combinations.remove(j);
						i--;
						break;
					}
				}
			}
		}
	}

	static void printMatchupsAutofillNotRequired(List<Matchup> combinations) {
		for (int i = 0; i < combinations.size(); i++) {
			System.out.println("Matchup " + i + " - LP Difference: " + combinations.get(i).eloDifference + " Teams: "
					+ combinations.get(i).teamsToString());
		}
	}

	/*
	 * finds all possible matchups with each player being assigned either their main
	 * role or secondary role (or anything BUT secondary, if main role == fill) used
	 * when there aren't exactly 2 players who main top, jg, mid, bot, supp, which
	 * means that someone has to be on offrole and we have to check all possible
	 * combinations
	 * 
	 * could probably make this a) look nicer and b) more efficient if I used
	 * separate lists for top/jg/mid/bot/supp players, rather than checking every
	 * player for every role
	 */

	static void findAllMatchupsAutofillRequired(Player[] players, List<Matchup> combinations) {

		for (int i = 0; i < 10; i++) {
			if (!(("top".equals(players[i].mainRole)
					|| (!"fill".equals(players[i].mainRole) && "top".equals(players[i].secondaryRole))
					|| ("fill".equals(players[i].mainRole) && !"top".equals(players[i].secondaryRole)))))
				continue;

			for (int j = 0; j < 10; j++) {
				if (j == i)
					continue;
				if (!(("jungle".equals(players[j].mainRole)
						|| (!"fill".equals(players[j].mainRole) && "jungle".equals(players[j].secondaryRole))
						|| ("fill".equals(players[j].mainRole) && !"jungle".equals(players[j].secondaryRole)))))
					continue;

				for (int k = 0; k < 10; k++) {
					if (k == i || k == j)
						continue;
					if (!(("mid".equals(players[k].mainRole)
							|| (!"fill".equals(players[k].mainRole) && "mid".equals(players[k].secondaryRole))
							|| ("fill".equals(players[k].mainRole) && !"mid".equals(players[k].secondaryRole)))))
						continue;

					for (int l = 0; l < 10; l++) {
						if (l == i || l == j || l == k)
							continue;
						if (!(("bot".equals(players[l].mainRole)
								|| (!"fill".equals(players[l].mainRole) && "bot".equals(players[l].secondaryRole))
								|| ("fill".equals(players[l].mainRole) && !"bot".equals(players[l].secondaryRole)))))
							continue;

						for (int m = 0; m < 10; m++) {
							if (m == i || m == j || m == k || m == l)
								continue;
							if (!(("support".equals(players[m].mainRole)
									|| (!"fill".equals(players[m].mainRole)
											&& "support".equals(players[m].secondaryRole))
									|| ("fill".equals(players[m].mainRole)
											&& !"support".equals(players[m].secondaryRole)))))
								continue;

							for (int n = 0; n < 10; n++) {
								if (n == i || n == j || n == k || n == l || n == m)
									continue;
								if (!(("top".equals(players[n].mainRole)
										|| (!"fill".equals(players[n].mainRole)
												&& "top".equals(players[n].secondaryRole))
										|| ("fill".equals(players[n].mainRole)
												&& !"top".equals(players[n].secondaryRole)))))
									continue;

								for (int o = 0; o < 10; o++) {
									if (o == i || o == j || o == k || o == l || o == m || o == n)
										continue;
									if (!(("jungle".equals(players[o].mainRole)
											|| (!"fill".equals(players[o].mainRole)
													&& "jungle".equals(players[o].secondaryRole))
											|| ("fill".equals(players[o].mainRole)
													&& !"jungle".equals(players[o].secondaryRole)))))
										continue;

									for (int p = 0; p < 10; p++) {
										if (p == i || p == j || p == k || p == l || p == m || p == n || p == o)
											continue;
										if (!(("mid".equals(players[p].mainRole)
												|| (!"fill".equals(players[p].mainRole)
														&& "mid".equals(players[p].secondaryRole))
												|| ("fill".equals(players[p].mainRole)
														&& !"mid".equals(players[p].secondaryRole)))))
											continue;

										for (int q = 0; q < 10; q++) {
											if (q == i || q == j || q == k || q == l || q == m || q == n || q == o
													|| q == p)
												continue;
											if (!(("bot".equals(players[q].mainRole)
													|| (!"fill".equals(players[q].mainRole)
															&& "bot".equals(players[q].secondaryRole))
													|| ("fill".equals(players[q].mainRole)
															&& !"bot".equals(players[q].secondaryRole)))))
												continue;

											for (int r = 0; r < 10; r++) {
												if (r == i || r == j || r == k || r == l || r == m || r == n || r == o
														|| r == p || r == q)
													continue;
												if (!(("support".equals(players[r].mainRole)
														|| (!"fill".equals(players[r].mainRole)
																&& "support".equals(players[r].secondaryRole))
														|| ("fill".equals(players[r].mainRole)
																&& !"support".equals(players[r].secondaryRole)))))
													continue;

												Player[] playersInMatch = new Player[10];
												playersInMatch[0] = (Player) (DeepCopy.copy(players[i]));
												playersInMatch[1] = (Player) (DeepCopy.copy(players[j]));
												playersInMatch[2] = (Player) (DeepCopy.copy(players[k]));
												playersInMatch[3] = (Player) (DeepCopy.copy(players[l]));
												playersInMatch[4] = (Player) (DeepCopy.copy(players[m]));
												playersInMatch[5] = (Player) (DeepCopy.copy(players[n]));
												playersInMatch[6] = (Player) (DeepCopy.copy(players[o]));
												playersInMatch[7] = (Player) (DeepCopy.copy(players[p]));
												playersInMatch[8] = (Player) (DeepCopy.copy(players[q]));
												playersInMatch[9] = (Player) (DeepCopy.copy(players[r]));

												for (int index = 0; index < 10; index++) {
													if (index % 5 == 0) {
														playersInMatch[index].assignedRole = "top";
													} else if (index % 5 == 1) {
														playersInMatch[index].assignedRole = "jungle";
													} else if (index % 5 == 2) {
														playersInMatch[index].assignedRole = "mid";
													} else if (index % 5 == 3) {
														playersInMatch[index].assignedRole = "bot";
													} else if (index % 5 == 4) {
														playersInMatch[index].assignedRole = "support";
													}
												}

												for (Player player : playersInMatch) {
													if (!(player.mainRole.equals("fill")
															|| player.assignedRole.equals(player.mainRole)))
														player.elo -= player.offrolePenalty;
												}

												Matchup matchup = new Matchup(playersInMatch);
												matchup.countSecondaryRoles();
												combinations.add(matchup);
											}
										}
									}
								}
							}
						}
					}
				}
			}
		}
	}

	static void printMatchupsAutofillRequired(List<Matchup> combinations) {

		for (int i = 0; i < combinations.size(); i++) {
			System.out.println("MU " + i + " - Offrole: " + combinations.get(i).secondaryRoles + " ("
					+ combinations.get(i).secondaryRolesTeamOne + " blue, " + combinations.get(i).secondaryRolesTeamTwo
					+ " red)" + " | LP difference: " + combinations.get(i).eloDifference + " | "
					+ combinations.get(i).teamsToString());
		}
	}

	// could be useful to figure out which role is underrepresented
	static void setAvailablePlayersForEachRole(Player[] players, List<Player> topPlayers, List<Player> junglePlayers,
			List<Player> midPlayers, List<Player> botPlayers, List<Player> supportPlayers) {

		for (int i = 0; i < 10; i++) {
			if (players[i].mainRole.equals("top")
					|| (!players[i].mainRole.equals("fill") && players[i].secondaryRole.equals("top"))
					|| (players[i].mainRole.equals("fill") && !players[i].secondaryRole.equals("top")))
				topPlayers.add(players[i]);
			if (players[i].mainRole.equals("jungle")
					|| (!players[i].mainRole.equals("fill") && players[i].secondaryRole.equals("jungle"))
					|| (players[i].mainRole.equals("fill") && !players[i].secondaryRole.equals("jungle")))
				junglePlayers.add(players[i]);
			if (players[i].mainRole.equals("mid")
					|| (!players[i].mainRole.equals("fill") && players[i].secondaryRole.equals("mid"))
					|| (players[i].mainRole.equals("fill") && !players[i].secondaryRole.equals("mid")))
				midPlayers.add(players[i]);
			if (players[i].mainRole.equals("bot")
					|| (!players[i].mainRole.equals("fill") && players[i].secondaryRole.equals("bot"))
					|| (players[i].mainRole.equals("fill") && !players[i].secondaryRole.equals("bot")))
				botPlayers.add(players[i]);
			if (players[i].mainRole.equals("support")
					|| (!players[i].mainRole.equals("fill") && players[i].secondaryRole.equals("support"))
					|| (players[i].mainRole.equals("fill") && !players[i].secondaryRole.equals("support")))
				supportPlayers.add(players[i]);
		}
	}
}
