import java.util.ArrayList;

import org.parabot.environment.api.utils.Time;
import org.parabot.environment.scripts.Category;
import org.parabot.environment.scripts.Script;
import org.parabot.environment.scripts.ScriptManifest;
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
	public int scrollsFound = 0;

	private final ArrayList<Strategy> strategy = new ArrayList<Strategy>();

	@Override
	public boolean onExecute() {
		strategy.add(new attack());
		strategy.add(new eat());
		strategy.add(new loot());
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
			for (final Item s : Inventory.getItems(scrolls)) {
				Menu.sendAction(961, 2773, s.getSlot(), 4521985);
				Time.sleep(500);
				Menu.sendAction(961, 2774, s.getSlot(), 4521985);
				Time.sleep(500);
				Menu.sendAction(961, 2775, s.getSlot(), 4521985);
				Time.sleep(500);
				scrollsFound++;
			}
		}
	}

	public class loot implements Strategy {
		public boolean activate() {
			for (GroundItem L : GroundItems.getNearest(loot)) {
				GroundItem l = L;
				return l != null;
			}
			return false;
		}

		public void execute() {
			for (GroundItem L : GroundItems.getNearest(loot)) {
				GroundItem l = L;
				if (l != null && !Inventory.isFull()) {
					l.interact(0);
					Time.sleep(1500);
				}
			}
		}
	}

	public class eat implements Strategy {
		public boolean activate() {
			return food != null;
		}

		public void execute() {
			for (final Item f : Inventory.getItems(food)) {
				while (f != null && Players.getMyPlayer().getHealth() < 50)
					Menu.sendAction(961, 385, f.getSlot(), 4521985);
				Time.sleep(2000);
			}
		}
	}

	public class attack implements Strategy {
		public boolean activate() {
			final Npc[] C = Npcs.getNearest(Crabs);
			Npc C1 = null;
			C1 = C[1];
			return C1 != null && !Players.getMyPlayer().isInCombat()
					&& Players.getMyPlayer().getAnimation() == -1;
		}

		public void execute() {
			final Npc[] C = Npcs.getNearest(Crabs);
			Npc C1 = C[1];
			if (C1 != null && !C1.isInCombat()) {
				C1.interact(1);
				Time.sleep(2250);
			}
		}
	}
}