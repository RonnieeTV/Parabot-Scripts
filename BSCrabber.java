import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import javax.swing.JFrame;

import org.parabot.environment.api.interfaces.Paintable;
import org.parabot.environment.api.utils.Filter;
import org.parabot.environment.api.utils.Time;
import org.parabot.environment.api.utils.Timer;
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

@ScriptManifest(author = "Tommyb603", category = Category.COMBAT, description = "Kills Crabs on Battlescape", name = "BSCrabber", servers = { "377 only" }, version = 1.1)
public final class BSCrabber extends Script implements Paintable {

	public int StartLVL = 0;
	public int StartEXP = 0;
	public int CurrentLVL = 0;
	public int CurrentEXP = 0;
	static Timer runTime;
	boolean waitGUI = true;
	boolean buryBones = false;
	boolean eatFood = false;

	private final int COINS = 995, SCROLL1 = 2773, SCROLL2 = 2774,
			SCROLL3 = 2775, SHARK = 386, ROCKCRAB = 1265, BIGBONES = 532,
			BONES = 526;

	private final int Crabs = ROCKCRAB;
	private final int[] Food = { SHARK };
	private final int[] Bones = { BIGBONES, BONES };
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
		BSCrabberGUI g = new BSCrabberGUI();
		g.setVisible(true);
		while (waitGUI)
			sleep(500);
		StartLVL = Skill.ATTACK.getRealLevel() + Skill.STRENGTH.getRealLevel()
				+ Skill.DEFENSE.getRealLevel()
				+ Skill.CONSTITUTION.getRealLevel()
				+ Skill.RANGE.getRealLevel() + Skill.MAGIC.getRealLevel();
		runTime = new Timer(0L);
		StartEXP = Skill.ATTACK.getExperience()
				+ Skill.STRENGTH.getExperience()
				+ Skill.DEFENSE.getExperience()
				+ Skill.CONSTITUTION.getExperience()
				+ Skill.RANGE.getExperience() + Skill.MAGIC.getExperience();
		strategy.add(new Attack());
		strategy.add(new Eat());
		strategy.add(new Loot());
		strategy.add(new clickScroll());
		strategy.add(new toBank());
		strategy.add(new lootBones());
		strategy.add(new buryBones());
		provide(strategy);
		return (true);
	}

	public class buryBones implements Strategy {

		@Override
		public boolean activate() {
			for (Item b : Inventory.getItems(Bones)) {
				return b != null && buryBones == true;
			}
			return false;
		}

		@Override
		public void execute() {
			for (int i = 0; i < Inventory.getItems(Bones).length; i++) {
				Menu.sendAction(961, 526,
						Inventory.getItems(Bones)[i].getSlot(), 4521985);
				Menu.sendAction(961, 532,
						Inventory.getItems(Bones)[i].getSlot(), 4521985);
				Time.sleep(new SleepCondition() {
					@Override
					public boolean isValid() {
						return Inventory.getCount(Bones) == 0;
					}
				}, 250);
			}
		}
	}

	public class lootBones implements Strategy {

		public boolean activate() {
			for (GroundItem b : GroundItems.getNearest(Bones)) {
				return b != null && buryBones == true;
			}
			return false;
		}

		public void execute() {
			for (GroundItem B : GroundItems.getNearest(Bones)) {
				final GroundItem b = B;
				if (!Inventory.isFull() && b.distanceTo() < 18) {
					b.interact(0);
					Time.sleep(new SleepCondition() {
						public boolean isValid() {
							return b == null;
						}
					}, 4500);
				}

			}
		}
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
				Bank.withdraw(SHARK, 26, 2000);
			}
		}
	}

	// this whole class is going to be re-written thoroughly
	// it was written like shit and is just suppose to work because
	// API was acting glitchy 5/10/2014
	public class toBank implements Strategy {
		Tile loc = Players.getMyPlayer().getLocation();
		Player me = Players.getMyPlayer();
		SceneObject[] i = SceneObjects.getNearest(34963);
		Npc[] w = Npcs.getNearest(318);

		public boolean activate() {
			return Inventory.getCount(Food) == 0 && eatFood == true;
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
				Time.sleep(500);
				Time.sleep(new SleepCondition() {
					@Override
					public boolean isValid() {
						return Inventory.getCount(Food) > 1;
					}

				}, 500);
				Menu.sendAction(639, 110, 506, 28377091);
				Time.sleep(500);
				Menu.sendAction(891, 385, 2, 4521985);
				Time.sleep(500);
				Menu.sendAction(891, 385, 2, 4521985);
				Time.sleep(500);
				Menu.sendAction(891, 385, 2, 4521985);
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

	// this whole class is going to be re-written thoroughly
	// it was written like shit and is just suppose to work because
	// API was acting glitchy 5/10/2014

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
				if (!Inventory.isFull() && l.distanceTo() < 18) {
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
			return Food != null && Players.getMyPlayer().getHealth() < 50
					&& eatFood == true;
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

		public String runTime(long i) {
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

	public class BSCrabberGUI extends javax.swing.JFrame {

		/**
		 * Creates new form BSCrabberGUI
		 */
		public BSCrabberGUI() {
			initComponents();
		}

		/**
		 * This method is called from within the constructor to initialize the
		 * form. WARNING: Do NOT modify this code. The content of this method is
		 * always regenerated by the Form Editor.
		 */
		@SuppressWarnings("unchecked")
		// <editor-fold defaultstate="collapsed" desc="Generated Code">
		private void initComponents() {

			jLabel1 = new javax.swing.JLabel();
			startButton = new javax.swing.JButton();
			jLabel2 = new javax.swing.JLabel();
			eatFood = new javax.swing.JComboBox();
			jLabel3 = new javax.swing.JLabel();
			buryBones = new javax.swing.JComboBox();
			jLabel4 = new javax.swing.JLabel();

			setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
			setTitle("BSCrabber GUI V1.0");
			setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));

			jLabel1.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
			jLabel1.setForeground(new java.awt.Color(255, 0, 0));
			jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
			jLabel1.setText("BSCrabber GUI");

			startButton.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
			startButton.setText("Start");
			startButton.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent evt) {
					startButtonActionPerformed(evt);
				}
			});

			jLabel2.setText("By: Tommyb603");

			eatFood.setModel(new javax.swing.DefaultComboBoxModel(new String[] {
					"Yes", "No" }));
			eatFood.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent evt) {
					eatFoodActionPerformed(evt);
				}
			});

			jLabel3.setText("Eat Food?");

			buryBones.setModel(new javax.swing.DefaultComboBoxModel(
					new String[] { "Yes", "No" }));
			buryBones.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent evt) {
					buryBonesActionPerformed(evt);
				}
			});

			jLabel4.setText("Bury Bones?");

			javax.swing.GroupLayout layout = new javax.swing.GroupLayout(
					getContentPane());
			getContentPane().setLayout(layout);
			layout.setHorizontalGroup(layout
					.createParallelGroup(
							javax.swing.GroupLayout.Alignment.LEADING)
					.addGroup(
							layout.createSequentialGroup()
									.addContainerGap(
											javax.swing.GroupLayout.DEFAULT_SIZE,
											Short.MAX_VALUE)
									.addGroup(
											layout.createParallelGroup(
													javax.swing.GroupLayout.Alignment.LEADING)
													.addGroup(
															javax.swing.GroupLayout.Alignment.TRAILING,
															layout.createSequentialGroup()
																	.addComponent(
																			jLabel1,
																			javax.swing.GroupLayout.PREFERRED_SIZE,
																			199,
																			javax.swing.GroupLayout.PREFERRED_SIZE)
																	.addGap(18,
																			18,
																			18))
													.addComponent(
															jLabel2,
															javax.swing.GroupLayout.Alignment.TRAILING)
													.addGroup(
															javax.swing.GroupLayout.Alignment.TRAILING,
															layout.createSequentialGroup()
																	.addComponent(
																			startButton,
																			javax.swing.GroupLayout.PREFERRED_SIZE,
																			157,
																			javax.swing.GroupLayout.PREFERRED_SIZE)
																	.addGap(33,
																			33,
																			33))))
					.addGroup(
							layout.createSequentialGroup()
									.addContainerGap(
											javax.swing.GroupLayout.DEFAULT_SIZE,
											Short.MAX_VALUE)
									.addGroup(
											layout.createParallelGroup(
													javax.swing.GroupLayout.Alignment.LEADING)
													.addComponent(jLabel3)
													.addComponent(jLabel4))
									.addPreferredGap(
											javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
									.addGroup(
											layout.createParallelGroup(
													javax.swing.GroupLayout.Alignment.LEADING)
													.addComponent(
															buryBones,
															javax.swing.GroupLayout.PREFERRED_SIZE,
															javax.swing.GroupLayout.DEFAULT_SIZE,
															javax.swing.GroupLayout.PREFERRED_SIZE)
													.addComponent(
															eatFood,
															javax.swing.GroupLayout.PREFERRED_SIZE,
															javax.swing.GroupLayout.DEFAULT_SIZE,
															javax.swing.GroupLayout.PREFERRED_SIZE))
									.addContainerGap(
											javax.swing.GroupLayout.DEFAULT_SIZE,
											Short.MAX_VALUE)));
			layout.setVerticalGroup(layout
					.createParallelGroup(
							javax.swing.GroupLayout.Alignment.LEADING)
					.addGroup(
							layout.createSequentialGroup()
									.addContainerGap()
									.addComponent(
											jLabel1,
											javax.swing.GroupLayout.PREFERRED_SIZE,
											35,
											javax.swing.GroupLayout.PREFERRED_SIZE)
									.addPreferredGap(
											javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
									.addGroup(
											layout.createParallelGroup(
													javax.swing.GroupLayout.Alignment.BASELINE)
													.addComponent(jLabel3)
													.addComponent(
															eatFood,
															javax.swing.GroupLayout.PREFERRED_SIZE,
															javax.swing.GroupLayout.DEFAULT_SIZE,
															javax.swing.GroupLayout.PREFERRED_SIZE))
									.addGap(22, 22, 22)
									.addGroup(
											layout.createParallelGroup(
													javax.swing.GroupLayout.Alignment.BASELINE)
													.addComponent(
															buryBones,
															javax.swing.GroupLayout.PREFERRED_SIZE,
															javax.swing.GroupLayout.DEFAULT_SIZE,
															javax.swing.GroupLayout.PREFERRED_SIZE)
													.addComponent(jLabel4))
									.addGap(18, 18, 18)
									.addComponent(
											startButton,
											javax.swing.GroupLayout.PREFERRED_SIZE,
											61,
											javax.swing.GroupLayout.PREFERRED_SIZE)
									.addPreferredGap(
											javax.swing.LayoutStyle.ComponentPlacement.RELATED,
											javax.swing.GroupLayout.DEFAULT_SIZE,
											Short.MAX_VALUE)
									.addComponent(jLabel2)));

			eatFood.getAccessibleContext().setAccessibleName("");
			buryBones.getAccessibleContext().setAccessibleName("");

			pack();
		}// </editor-fold>

		private void startButtonActionPerformed(java.awt.event.ActionEvent evt) {
			BSCrabberGUI g = new BSCrabberGUI();
			if (startButton.isEnabled()) {
				g.setVisible(false);
				sleep(500);
			}
			waitGUI = false;
			g.dispose();
		}

		private void eatFoodActionPerformed(java.awt.event.ActionEvent evt) {
			if (eatFood.getSelectedItem().toString().equals("Yes")) {
				BSCrabber.this.eatFood = true;
			}
			waitGUI = true;
		}

		private void buryBonesActionPerformed(java.awt.event.ActionEvent evt) {
			if (buryBones.getSelectedItem().equals("Yes")) {
				BSCrabber.this.buryBones = true;
			}
			waitGUI = true;
		}

		// Variables declaration - do not modify
		private javax.swing.JComboBox buryBones;
		private javax.swing.JComboBox eatFood;
		private javax.swing.JLabel jLabel1;
		private javax.swing.JLabel jLabel2;
		private javax.swing.JLabel jLabel3;
		private javax.swing.JLabel jLabel4;
		private javax.swing.JButton startButton;
		// End of variables declaration
	}

	@Override
	public void paint(Graphics g1) {
		Graphics2D gr = (Graphics2D) g1;

		CurrentLVL = Skill.ATTACK.getRealLevel()
				+ Skill.STRENGTH.getRealLevel() + Skill.DEFENSE.getRealLevel()
				+ Skill.CONSTITUTION.getRealLevel()
				+ Skill.RANGE.getRealLevel() + Skill.MAGIC.getRealLevel();
		CurrentLVL -= StartLVL;
		CurrentEXP = Skill.ATTACK.getExperience()
				+ Skill.STRENGTH.getExperience()
				+ Skill.DEFENSE.getExperience()
				+ Skill.CONSTITUTION.getExperience()
				+ Skill.RANGE.getExperience() + Skill.MAGIC.getExperience();
		CurrentEXP -= StartEXP;
		gr.setColor(Color.WHITE);
		gr.setFont(new Font("Verdana", 0, 12));
		gr.drawString("BSCrabber v1.0", 558, 225);
		gr.drawString("by Tommyb603", 558, 240);
		gr.drawString("XP Gained: " + CurrentEXP, 558, 265);
		gr.drawString("Levels Gained: " + CurrentLVL, 558, 285);
		gr.drawString("", 558, 305);
		gr.drawString("", 558, 325);
		gr.drawString("", 558, 345);
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
		rect.setColor(new Color(0, 0, 0, 120));

		rect.fillRect(555, 211, 183, 248);
	}
}