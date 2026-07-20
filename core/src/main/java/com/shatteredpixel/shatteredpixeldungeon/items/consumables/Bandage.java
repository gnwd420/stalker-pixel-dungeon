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

package com.shatteredpixel.shatteredpixeldungeon.items.consumables;

import com.shatteredpixel.shatteredpixeldungeon.Challenges;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Bleeding;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Healing;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfHealing;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;

public class Bandage extends MedicalConsumable {

	{
		image = ItemSpriteSheet.BLAND_CHUNKS;
	}

	@Override
	protected boolean canUse( Hero hero ) {
		if (hero.buff( Bleeding.class ) == null && hero.HP >= hero.HT) {
			GLog.w( Messages.get( this, "no_effect" ) );
			return false;
		}
		return true;
	}

	@Override
	protected void applyEffect( Hero hero ) {
		Buff.detach( hero, Bleeding.class );

		if (Dungeon.isChallenged( Challenges.NO_HEALING )) {
			PotionOfHealing.pharmacophobiaProc( hero );
		} else {
			Buff.affect( hero, Healing.class ).setHeal( 12, 0f, 3 );
		}
	}

	@Override
	public int value() {
		return 12 * quantity();
	}

}
