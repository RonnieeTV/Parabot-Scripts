import java.util.ArrayList;

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

@ScriptManifest(author = "Tommyb603", category = Category.COMBAT, description = "Kills Crabs on Battlescape", name = "BSCrabber", servers = { "377 only" }, version = 0.3)
public class BSCrabber extends Script {

	private final int[] Crabs = { 1265 };
	private final int[] food = { 386 };
	private final int[] loot = { 995, 2773, 2774, 2775 };
	private final int[] scrolls = { 2774, 2774, 2775 };

	private final ArrayList<Strategy> strategy = new ArrayList<Strategy>();

	@Override
	public boolean onExecute() {
		strategy.add(new Attack());
		strategy.add(new Eat());
		strategy.add(new Loot());
		strategy.add(new clickScroll());
		provide(strategy);

		return (true);
	}

	public class clickScroll implements Strategy {
		public boolean activate() {
			for (Item s : Inventory.getItems(scrolls)) {
				return s != null;
			}
			return false;
		}

		public void execute() {
			for (int s = 0; s < Inventory.getItems(scrolls).length; s++) {
				Menu.sendAction(961, 2773,
						Inventory.getItems(scrolls)[s].getSlot(), 4521985);
				Menu.sendAction(961, 2774,
						Inventory.getItems(scrolls)[s].getSlot(), 4521985);
				Menu.sendAction(961, 2775,
						Inventory.getItems(scrolls)[s].getSlot(), 4521985);
				Time.sleep(new SleepCondition() {
					@Override
					public boolean isValid() {
						return Inventory.getCount(scrolls) == 0;
					}
				}, 1250);
			}
		}
	}

	public class Loot implements Strategy {
		public boolean activate() {
			for (GroundItem l : GroundItems.getNearest(loot)) {
				return l != null;
			}
			return false;
		}

		public void execute() {
			for (GroundItem L : GroundItems.getNearest(loot)) {
				final GroundItem l = L;
				if (l != null && !Inventory.isFull() && l.distanceTo() < 10) {
					l.interact(0);
					Time.sleep(new SleepCondition() {
						@Override
						public boolean isValid() {
							return l == null;
						}
					}, 1000);
				}
			}
		}
	}

	// --BEGIN--written by Paradox
	public class Eat implements Strategy {

		public boolean activate() {
			return food != null && Players.getMyPlayer().getHealth() < 50;
		}

		public void execute() {
			for (int i = 0; i < Inventory.getItems(food).length
					&& Players.getMyPlayer().getHealth() < 50; i++) {
				final int health = Players.getMyPlayer().getHealth();
				Menu.sendAction(961, 385,
						Inventory.getItems(food)[i].getSlot(), 4521985);
				Time.sleep(new SleepCondition() {
					@Override
					public boolean isValid() {
						return Players.getMyPlayer().getHealth() > health;
					}
				}, 2000);
			}
		}
	}

	// --END--written by Paradox

	public class Attack implements Strategy {
		public boolean activate() {
			final Npc[] C = Npcs.getNearest(Crabs);
			Npc C1 = null;
			C1 = C[1];
			return C1 != null && !Players.getMyPlayer().isInCombat();
		}

		public void execute() {
			final Npc[] C = Npcs.getNearest(Crabs);
			final Npc C1 = C[1];
			if (C1 != null && !C1.isInCombat()) {
				C1.interact(1);
				Time.sleep(new SleepCondition() {
					@Override
					public boolean isValid() {
						return C1.isInCombat()
								&& Players.getMyPlayer().isInCombat();
					}
				}, 2000);
			}
		}
	}
}