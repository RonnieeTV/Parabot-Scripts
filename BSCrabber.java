import java.util.ArrayList;

import org.parabot.environment.api.utils.Filter;
import org.parabot.environment.api.utils.Time;
import org.parabot.environment.scripts.Category;
import org.parabot.environment.scripts.Script;
import org.parabot.environment.scripts.ScriptManifest;
import org.parabot.environment.scripts.framework.SleepCondition;
import org.parabot.environment.scripts.framework.Strategy;
import org.rev377.min.api.methods.*;
import org.rev377.min.api.wrappers.GroundItem;
import org.rev377.min.api.wrappers.Item;
import org.rev377.min.api.wrappers.Npc;
import org.rev377.min.api.wrappers.Player;
import org.rev377.min.api.wrappers.SceneObject;
import org.rev377.min.api.wrappers.Tile;
import org.rev377.min.api.wrappers.TilePath;

@ScriptManifest(author = "Tommyb603", category = Category.COMBAT, description = "Kills Crabs on Battlescape", name = "BSCrabber", servers = { "377 only" }, version = 0.85)
public class BSCrabber extends Script {

	private final int COINS = 995, SCROLL1 = 2773, SCROLL2 = 2774,
			SCROLL3 = 2775, SHARK = 386, ROCKCRAB = 1265;
	private final int Crabs = ROCKCRAB;
	private final int[] Food = { SHARK };
	private final int[] Loot = { COINS, SCROLL1, SCROLL2, SCROLL3 };
	private final int[] Scrolls = { SCROLL1, SCROLL2, SCROLL3 };

	final Tile bank = new Tile(3097, 3496, 0);
	final Tile tele = new Tile(3103, 3494, 0);
	final Tile cexit = new Tile(2759, 10064, 0);

	public final Tile[] ToBank = new Tile[] { new Tile(2783, 10071, 0),
			new Tile(2777, 10070, 0), new Tile(2768, 10069, 0),
			new Tile(2759, 10064, 0) };
	public final Tile[] ToBooth = new Tile[] { new Tile(3099, 3497, 0),
			new Tile(3097, 3496, 0) };

	private final ArrayList<Strategy> strategy = new ArrayList<Strategy>();

	@Override
	public boolean onExecute() {
		strategy.add(new Attack());
		strategy.add(new Eat());
		strategy.add(new Loot());
		strategy.add(new clickScroll());
		strategy.add(new toBank());
		provide(strategy);

		return (true);
	}

	public class openBank implements Strategy {
		SceneObject i = SceneObjects.getClosest(26972);

		public boolean activate() {
			return i != null && bank.isOnMinimap() && !Bank.isOpen();
		}

		public void execute() {
			final SceneObject i = SceneObjects.getClosest(26972);
			if (i != null) {
				Bank.open();
				Time.sleep(new SleepCondition() {
					@Override
					public boolean isValid() {
						return Bank.isOpen();
					}

				}, 1500);
				Bank.depositAll();
				Bank.withdraw(SHARK, 27, 2000);
			}
		}
	}

	//this whole class is going to be re-written thoroughly
	//it was written like shit and is just suppose to work because
	//API was acting glitchy 5/10/2014
	public class toBank implements Strategy {
		Tile loc = Players.getMyPlayer().getLocation();
		Player me = Players.getMyPlayer();
		SceneObject[] i = SceneObjects.getNearest(34963);
		Npc[] w = Npcs.getNearest(318);

		public boolean activate() {
			return Inventory.getCount(Food) == 0;
		}

		public void execute() {
			TilePath exit = new TilePath(ToBank);
			TilePath bb = new TilePath(ToBooth);
			Walking.walkDown(exit);
			SceneObject i = SceneObjects.getClosest(34963);
			if (i != null && i.distanceTo() < 3) {
				sleep(2000);
				Menu.sendAction(35, 1073747885, 119, 34963);
				sleep(2000);
				Walking.walkDown(bb);
				Menu.sendAction(389, 1073748913, 0, 26972);
				Time.sleep(5000);
				Menu.sendAction(518, 84, 0, 28377098);
				Time.sleep(200);
				Bank.getBankItems();
				Time.sleep(500);
				Bank.withdraw(SHARK, 26, 3000);
				Time.sleep(new SleepCondition () {
					@Override
					public boolean isValid() {
						return Inventory.getCount(Food) > 1;
					}
					
				}, 500);
				Menu.sendAction(639, 110, 506, 28377091);
				Time.sleep(500);
				Menu.sendAction(891, 385, 2, 4521985);
				Time.sleep(500);
				Menu.sendAction(318, 1816, 0, 0);
				Time.sleep(4500);
				Menu.sendAction(352, 1816, 334, 3801091);
				Time.sleep(1000);
				Menu.sendAction(352, 1816, 266, 3866626);
			}
		}
	}
	//this whole class is going to be re-written thoroughly
	//it was written like shit and is just suppose to work because
	//API was acting glitchy 5/10/2014

	public class clickScroll implements Strategy {
		public boolean activate() {
			for (Item s : Inventory.getItems(Scrolls)) {
				return s != null;
			}
			return false;
		}

		public void execute() {
			for (int i = 0; i < Inventory.getItems(Scrolls).length; i++) {
				Menu.sendAction(961, 2773,
						Inventory.getItems(Scrolls)[i].getSlot(), 4521985);
				Menu.sendAction(961, 2774,
						Inventory.getItems(Scrolls)[i].getSlot(), 4521985);
				Menu.sendAction(961, 2775,
						Inventory.getItems(Scrolls)[i].getSlot(), 4521985);
				Time.sleep(new SleepCondition() {
					@Override
					public boolean isValid() {
						return Inventory.getCount(Scrolls) == 0;
					}
				}, 250);
			}
		}
	}

	public class Loot implements Strategy {
		public boolean activate() {
			for (GroundItem l : GroundItems.getNearest(Loot)) {
				return l != null;
			}
			return false;
		}

		public void execute() {
			for (GroundItem L : GroundItems.getNearest(Loot)) {
				final GroundItem l = L;
				if (l != null && !Inventory.isFull() && l.distanceTo() < 18) {
					l.interact(0);
					Time.sleep(new SleepCondition() {
						@Override
						public boolean isValid() {
							return l == null;
						}
					}, 4500);
				}
			}
		}
	}

	// --BEGIN--written by Paradox
	public class Eat implements Strategy {

		public boolean activate() {
			return Food != null && Players.getMyPlayer().getHealth() < 50;
		}

		public void execute() {
			for (int i = 0; i < Inventory.getItems(Food).length
					&& Players.getMyPlayer().getHealth() < 50; i++) {
				Menu.sendAction(961, 385,
						Inventory.getItems(Food)[i].getSlot(), 4521985);
				Time.sleep(new SleepCondition() {
					@Override
					public boolean isValid() {
						return Players.getMyPlayer().getHealth() > 50;
					}
				}, 2000);
			}
		}
	}

	// --END--written by Paradox

	public Npc getNextNPC() {
		Npc[] npc = Npcs.getNearest(new Filter<Npc>() {

			public boolean accept(Npc npc) {
				return npc.getDef().getId() == Crabs;
			}
		});
		return npc.length > 0 ? npc[0] : null;

	}

	public class Attack implements Strategy {
		public boolean activate() {
			Player me = Players.getMyPlayer();
			return !me.isInCombat();
		}

		public void execute() {
			final Player me = Players.getMyPlayer();
			final Npc npc = getNextNPC();
			if (npc != null && !npc.isInCombat() && !me.isInCombat()) {
				npc.interact(1);
				sleep(3000);
				Camera.moveRandomly();
				Time.sleep(new SleepCondition() {

					@Override
					public boolean isValid() {
						return npc.isInCombat() && me.isInCombat()
								&& npc.distanceTo() >= 10;
					}

				}, 1500);
			}
		}
	}
}