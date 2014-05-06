import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;
import java.awt.*;

import org.parabot.environment.api.interfaces.Paintable;
import org.parabot.environment.api.utils.Time;
import org.parabot.environment.api.utils.Timer;
import org.parabot.environment.scripts.Category;
import org.parabot.environment.scripts.Script;
import org.parabot.environment.scripts.ScriptManifest;
import org.parabot.environment.scripts.framework.Strategy;
import org.rev317.api.events.listeners.MessageListener;
import org.rev317.api.methods.Camera;
import org.rev317.api.methods.Inventory;
import org.rev317.api.methods.Players;
import org.rev317.api.methods.SceneObjects;
import org.rev317.api.methods.Skill;
import org.rev317.api.wrappers.hud.Item;
import org.rev317.api.wrappers.hud.Tab;
import org.rev317.api.wrappers.interactive.Player;
import org.rev317.api.wrappers.scene.SceneObject;

@ScriptManifest(author = "Tommyb603", category = Category.MINING, description = "Iron Miner", name = "TMiner", servers = { "PKHonor" }, version = 0.1)
public final class TMiner extends Script implements Paintable {
	public static ArrayList<Strategy> strategies = new ArrayList<Strategy>();

	static Timer runTime;
	static int IRON_ID = 2092;
	static int[] Ores = { 436, 438, 440 };
	static int[] GemIDs = { 1617, 1619, 1621, 1623, 1625, 1627, 1629 };
	int Gems = 0;
	public int StartEXP = 0;
	public int CurrentEXP = 0;
	public int GemsGained = 0;

	@Override
	public boolean onExecute() {
		runTime = new Timer(0L);
		StartEXP = Skill.MINING.getExperience();
		strategies.add(new Mine());
		strategies.add(new Drop());
		strategies.add(new toBank());
		provide(strategies);
		return true;
	}
	
	public class Drop implements Strategy {

		public boolean activate() {
			if (!Tab.INVENTORY.isOpen()) {
				Tab.INVENTORY.open();
			}
			return Inventory.isFull();

		}

		public void execute() {

			if (Inventory.isFull()) {
				
			}
			for (final Item i : Inventory.getItems(Ores)) {
				i.interact("Drop");
				sleep(750);
			}

		}

	}
	
	public class toBank implements Strategy {
		public boolean activate() {
			final Player me = Players.getLocal();
			
			return !Inventory.isFull() && me.getAnimation() == -1;
		
		}
		
		public void execute() {
			
		}
		
		
	}

	public class Mine implements Strategy {
		public boolean activate() {

			final Player me = Players.getLocal();

			return !Inventory.isFull() && me.getAnimation() == -1
					&& !me.isWalking();
		}

		public void messageReceived(MessageListener m) {
			if (m.toString().contains("You found an")) {
				Gems++;
			if (m.toString().contains("Not Enough")) {
				onFinish();
				}
			}
		}

		public void execute() {

			final SceneObject[] nearestRocks = SceneObjects.getNearest(IRON_ID);
			final SceneObject rock = nearestRocks[0];

			if (rock == null) {
				System.out.println("Turning camera to iron.");
				Camera.turnTo(rock);
			}

			if (rock.isOnScreen()) {

				rock.interact("Mine");
				Time.sleep(100);

			}

		}
	}

	public void paint(Graphics g1) {
		Graphics2D gr = (Graphics2D) g1;

		CurrentEXP = Skill.MINING.getExperience();
		CurrentEXP -= StartEXP;
		gr.setColor(Color.WHITE);
		gr.setFont(new Font("Verdana", 0, 12));
		gr.drawString("TMiner v0.2", 558, 225);
		gr.drawString("XP Gained: " + CurrentEXP, 558, 305);
		gr.drawString("Gems Gained: " + Gems, 558, 325);
		gr.drawString(
				"Runtime: "
						+ String.format(
								"%02d:%02d:%02d",
								new Object[] {
										Long.valueOf(TimeUnit.MILLISECONDS
												.toHours(runTime
														.getElapsedTime())),
										Long.valueOf(TimeUnit.MILLISECONDS
												.toMinutes(runTime
														.getElapsedTime())
												- TimeUnit.HOURS
														.toMinutes(TimeUnit.MILLISECONDS
																.toHours(runTime
																		.getElapsedTime()))),
										Long.valueOf(TimeUnit.MILLISECONDS
												.toSeconds(runTime
														.getElapsedTime())
												- TimeUnit.MINUTES
														.toSeconds(TimeUnit.MILLISECONDS
																.toMinutes(runTime
																		.getElapsedTime()))) }),
				558, 405);

		gr.drawRect(555, 211, 183, 248);
		Graphics2D rect = (Graphics2D) g1;
		rect.setColor(new Color(0, 0, 0, 100));

		rect.fillRect(555, 211, 183, 248);
	}

	@Override
	public void onFinish() {
		System.out.println("Thank you for using TScripts!");
	}

	public static String runTime(long i) {
		DecimalFormat nf = new DecimalFormat("00");
		long millis = System.currentTimeMillis() - i;
		long hours = millis / (1000 * 60 * 60);
		millis -= hours * (1000 * 60 * 60);
		long minutes = millis / (1000 * 60);
		millis -= minutes * (1000 * 60);
		long seconds = millis / 1000;
		return nf.format(hours) + ":" + nf.format(minutes) + ":"
				+ nf.format(seconds);
	}
}