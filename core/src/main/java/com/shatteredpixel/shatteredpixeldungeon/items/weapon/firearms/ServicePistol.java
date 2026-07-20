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
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.firearms.ammunition.FirearmAmmo;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.firearms.ammunition.PistolAmmo;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;

public class ServicePistol extends Firearm {

	private static final int MIN_DAMAGE = 2;
	private static final int MAX_DAMAGE = 6;
	private static final int MAX_RANGE = 3;
	private static final int MAGAZINE_CAPACITY = 8;
	private static final int TIER = 1;

	public ServicePistol() {
		// TODO replace this temporary icon with an original Stalker Pixel Dungeon asset.
		image = ItemSpriteSheet.CROSSBOW;
		hitSound = Assets.Sounds.HIT_ARROW;
	}

	@Override
	public int min(int lvl) {
		return MIN_DAMAGE + lvl;
	}

	@Override
	public int max(int lvl) {
		return MAX_DAMAGE + 2 * lvl;
	}

	@Override
	public int STRReq(int lvl) {
		return STRReq(TIER, lvl);
	}

	@Override
	public int maxRange() {
		return MAX_RANGE;
	}

	@Override
	public int magazineCapacity() {
		return MAGAZINE_CAPACITY;
	}

	@Override
	public Class<? extends FirearmAmmo> ammoType() {
		return PistolAmmo.class;
	}
}
