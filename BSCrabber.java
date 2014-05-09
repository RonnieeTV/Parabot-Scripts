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

@ScriptManifest(author = "Tommyb603", category = Category.COMBAT, description = "Kills Crabs on Battlescape", name = "BSCrabber", servers = { "377 only" }, version = 0.85)
public class BSCrabber extends Script {

	private final int COINS = 995, SCROLL1 = 2773, SCROLL2 = 2774,
			SCROLL3 = 2775, SHARK = 386, ROCKCRAB = 1265;
	private final int Crabs = ROCKCRAB;
	private final int[] Food = { SHARK };
	private final int[] Loot = { COINS, SCROLL1, SCROLL2, SCROLL3 };
	private final int[] Scrolls = { SCROLL1, SCROLL2, SCROLL3 };

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
				}, 1250);
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
				Camera.moveRandomly();
				Time.sleep(new SleepCondition() {

					@Override
					public boolean isValid() {
						return npc.isInCombat() && me.isInCombat();
					}

				}, 1500);
			}
		}
	}
}