/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2026 Evan Debenham
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

package com.shatteredpixel.shatteredpixeldungeon.items.weapon.firearms;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Invisibility;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.effects.MagicMissile;
import com.shatteredpixel.shatteredpixeldungeon.items.KindOfWeapon;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.CellSelector;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.ui.QuickSlotButton;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Callback;

import java.util.ArrayList;

public abstract class Firearm extends Weapon {

	public static final String AC_FIRE = "FIRE";

	{
		stackable = false;
		defaultAction = AC_FIRE;
		usesTargeting = true;
	}

	@Override
	public ArrayList<String> actions(Hero hero) {
		ArrayList<String> actions = super.actions(hero);
		if (isEquipped(hero)) {
			actions.add(AC_FIRE);
		}
		return actions;
	}

	@Override
	public void execute(Hero hero, String action) {
		super.execute(hero, action);

		if (action.equals(AC_FIRE) && isEquipped(hero)) {
			GameScene.selectCell(shooter);
		}
	}

	@Override
	public int targetingPos(Hero user, int dst) {
		return new Ballistica(user.pos, dst, Ballistica.PROJECTILE).collisionPos;
	}

	public abstract int maxRange();

	@Override
	public boolean canReach(Char owner, int target) {
		if (owner == null
				|| Dungeon.level == null
				|| target < 0
				|| target >= Dungeon.level.length()
				|| Dungeon.level.distance(owner.pos, target) > maxRange()) {
			return false;
		}

		Ballistica shot = new Ballistica(owner.pos, target, Ballistica.PROJECTILE);
		return shot.collisionPos == target;
	}

	protected boolean canFire(Hero user) {
		return true;
	}

	protected void onShotFired(Hero user, Char target, boolean hit) {
		// Extension point for M2.2.
	}

	public boolean fire(final Hero user, final int target) {
		if (user == null
				|| Dungeon.level == null
				|| !isEquipped(user)
				|| target < 0
				|| target >= Dungeon.level.length()
				|| target == user.pos) {
			return false;
		}

		if (Dungeon.level.distance(user.pos, target) > maxRange()) {
			GLog.w(Messages.get(Firearm.class, "out_of_range"));
			return false;
		}

		if (!canFire(user)) {
			return false;
		}

		final Ballistica shot = new Ballistica(user.pos, target, Ballistica.PROJECTILE);
		final Char collisionTarget = Actor.findChar(shot.collisionPos);
		QuickSlotButton.target(collisionTarget);
		user.busy();
		Sample.INSTANCE.play(Assets.Sounds.ATK_CROSSBOW, 1f, 1.15f);

		final Callback shotComplete = new Callback() {
			@Override
			public void call() {
				boolean hit = false;
				if (collisionTarget != null
						&& collisionTarget != user
						&& collisionTarget.isAlive()
						&& collisionTarget.alignment == Char.Alignment.ENEMY
						&& !user.isCharmedBy(collisionTarget)) {
					KindOfWeapon previousThrownWeapon = user.belongings.thrownWeapon;
					user.belongings.thrownWeapon = Firearm.this;
					try {
						hit = user.attack(collisionTarget);
					} finally {
						user.belongings.thrownWeapon = previousThrownWeapon;
					}
				}

				Invisibility.dispel();
				onShotFired(user, collisionTarget, hit);
				updateQuickslot();
				user.spendAndNext(Actor.TICK);
			}
		};

		if (user.sprite != null && user.sprite.parent != null) {
			user.sprite.zap(shot.collisionPos);
			// TODO replace this temporary code-rendered tracer with an original bullet effect.
			MagicMissile tracer = (MagicMissile) user.sprite.parent.recycle(MagicMissile.class);
			tracer.reset(MagicMissile.MAGIC_MISSILE, user.pos, shot.collisionPos, shotComplete);
		} else {
			shotComplete.call();
		}

		return true;
	}

	private static final CellSelector.Listener shooter = new CellSelector.Listener() {
		@Override
		public void onSelect(Integer target) {
			if (target == null || !(curItem instanceof Firearm) || curUser == null) {
				return;
			}

			((Firearm) curItem).fire(curUser, target);
		}

		@Override
		public String prompt() {
			return Messages.get(Firearm.class, "prompt");
		}
	};
}
